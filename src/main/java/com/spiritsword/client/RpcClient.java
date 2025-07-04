package com.spiritsword.client;


import com.spiritsword.exceptions.RequestedClassNotDeterminedException;
import com.spiritsword.handler.ClientHeartbeatHandler;
import com.spiritsword.handler.JsonCallMessageEncoder;
import com.spiritsword.handler.JsonMessageDecodeHandler;
import com.spiritsword.handler.RpcClientMessageHandler;
import com.spiritsword.model.MarkAsRpc;
import com.spiritsword.model.MessagePayload;
import com.spiritsword.model.MessageType;
import com.spiritsword.model.MethodDescriptor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RpcClient implements SmartInitializingSingleton, ApplicationContextAware, NonLazyInitService {

    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private String[] scanPackages;

    private ApplicationContext context;

    private Channel channel;

    private String clientId;

    NioEventLoopGroup worker = new NioEventLoopGroup();

    @Value("${spiritsword.rpc.server.port}")
    private String port;
    @Value("${spiritsword.rpc.client.host}")
    private String host;


    public String[] getScanPackages() {
        return scanPackages;
    }

    public void setScanPackages(String[] scanPackages) {
        this.scanPackages = scanPackages;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    private Map<String, Map<String, MethodDescriptor>> methodHashMap = new HashMap<>();

    private Map<String, Method> classMethodsMap = new HashMap<>();

    private Map<String, CompletableFuture<MessagePayload.RpcResponse>> requestMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }).start();

        System.out.println("Client finished initialization process");
    }

    private void connect() {
        Bootstrap bootstrap = new Bootstrap();

        try {
            bootstrap
                    .group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new JsonCallMessageEncoder());
                            socketChannel.pipeline().addLast(new JsonMessageDecodeHandler());
                            socketChannel.pipeline().addLast(new IdleStateHandler(0, 5,0));
                            socketChannel.pipeline().addLast(new ClientHeartbeatHandler());
                            socketChannel.pipeline().addLast(new RpcClientMessageHandler(RpcClient.this));
                        }
                    });

            ChannelFuture channelFuture1 = bootstrap.connect(host, Integer.parseInt(port)).addListener(f -> {
                if (f.isSuccess()) {
                    ChannelFuture channelFuture = (ChannelFuture) f;
                    this.channel = channelFuture.channel();
                    this.sendRegistrationRequest();
                } else {
                    logger.warn("Failed to connect to server, trying to reconnect again");
                    reconnect();
                }
            });
            channelFuture1.channel().closeFuture().sync();
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }finally {
            worker.shutdownGracefully();
        }
    }

    public void reconnect() {
        worker.schedule(this::connect, 5, TimeUnit.SECONDS);
    }

    public void sendRegistrationRequest() {
        MessagePayload msg = new MessagePayload.RequestMessageBuilder().setClientId(clientId).setMessageType(MessageType.REGISTER).build();
        this.channel.writeAndFlush(msg);
    }

    @SuppressWarnings("all")
    public <T> T generate(Class<T> clazz, String requestedServiceClientId) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if (name.equals("toString")) return "Proxy[" + this.getClass().getSimpleName() + "]";
                if (name.equals("hashCode")) return System.identityHashCode(proxy);
                if (name.equals("equals")) return proxy == args[0];

                Class<?>[] parameterTypes = method.getParameterTypes();

                String[] allTypes = new String[parameterTypes.length];

                for(int i = 0; i < parameterTypes.length; i++){
                    allTypes[i] = (parameterTypes[i].getSimpleName());
                }

                String requstId = UUID.randomUUID().toString();
                MessagePayload payload = new MessagePayload.RequestMessageBuilder()
                        .setClientId(clientId)
                        .setMessageType(MessageType.CALL)
                        .setRequestId(requstId)
                        .setRequestClientId(requestedServiceClientId)
                        .setParamTypes(allTypes)
                        .setParams(args)
                        .setRequestMethodName(method.getName())
                        .setReturnValueType(method.getReturnType().getName())
                        .setRequestedClassName(clazz.getName()).build();

                CompletableFuture<MessagePayload.RpcResponse> future = new CompletableFuture<>();

                requestMap.put(requstId, future);

                channel.writeAndFlush(payload);

                try {
                    MessagePayload.RpcResponse rpcResponse = future.get();
                    requestMap.remove(requstId);
                    return rpcResponse.getResult();
                }catch (Exception e){
                    logger.error(e.getMessage(), e);
                }

                return null;
            }
        });
    }

    public void processRequest(MessagePayload message) throws Exception {
        MessagePayload.RpcRequest request = (MessagePayload.RpcRequest)message.getPayload();
        String requestedClassName = request.getRequestedClassName();
        String requestMethodSimpleName = request.getRequestMethodSimpleName();
        String[] paramTypes = request.getParamTypes();
        String returnValueSimpleName = request.getReturnValueSimpleName();

        String requestedMethodId = MethodDescriptor.generateMethodId(requestMethodSimpleName, paramTypes.length, paramTypes, returnValueSimpleName);

        Map<String, ?> beansOfType = null;
        try {
            beansOfType = context.getBeansOfType(Class.forName(requestedClassName));
        } catch (ClassNotFoundException e) {
            logger.error("Class Not Found");
            throw new ClassNotFoundException();
        }

        if(beansOfType.size() > 1) {
            logger.info("More than one beans of type {} found", requestedClassName);
            throw new RequestedClassNotDeterminedException();
        }

        Object requestedClassBean = beansOfType.values().iterator().next();

        String className = requestedClassBean.getClass().getName();

        MethodDescriptor methodDescriptor = methodHashMap.get(className).get(requestedMethodId);

        if(methodDescriptor == null) {
            logger.info("Method Not Found");
            throw new NoSuchMethodException();
        }

        if(methodDescriptor.getMethodName().equals(requestMethodSimpleName) && methodDescriptor.getNumOfParams() == paramTypes.length) {
            // more check shall be added

            Method method = classMethodsMap.get(requestedMethodId);

            try {
                Object result = method.invoke(requestedClassBean, request.getParams());
                MessagePayload responseMessagePayload = new MessagePayload.RequestMessageBuilder()
                        .setRequestId(request.getRequestId())
                        .setMessageType(MessageType.RESPONSE)
                        .setReturnValue(result).build();

                channel.writeAndFlush(responseMessagePayload);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void completeRequest(MessagePayload.RpcResponse response) {
        String requestId = response.getRequestId();
        CompletableFuture<MessagePayload.RpcResponse> rpcResponseCompletableFuture = requestMap.get(requestId);

        rpcResponseCompletableFuture.complete(response);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        System.out.println("afterSingletonsInstantiated started");
        List<Class<?>> classList = new ArrayList<>();
        for(String scanPackage: this.scanPackages) {
            scanPackage = scanPackage.replaceAll("\\.", "/");
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(scanPackage);

            for(BeanDefinition bd: candidateComponents) {
                try {
                    Class<?> aClass = Class.forName(bd.getBeanClassName());

                    if(RemoteService.class.isAssignableFrom(aClass)) {
                        classList.add(aClass);
                    }

                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage());
                }
            }
        }

        for(Class clazz: classList) {
            Method[] declaredMethods = clazz.getDeclaredMethods();

            for(Method m: declaredMethods) {
                if(m.isAnnotationPresent(MarkAsRpc.class)) {
                    MethodDescriptor methodDescriptor = MethodDescriptor.build(m);
                    methodHashMap.computeIfAbsent(clazz.getName(), k -> new HashMap<>()).put(methodDescriptor.getMethodId(), methodDescriptor);
                    classMethodsMap.put(methodDescriptor.getMethodId(), m);
                }
            }
        }
    }
}
