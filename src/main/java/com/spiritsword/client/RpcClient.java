package com.spiritsword.client;


import com.spiritsword.handler.JsonCallMessageEncoder;
import com.spiritsword.handler.JsonMessageDecodeHandler;
import com.spiritsword.model.MessagePayload;
import com.spiritsword.model.MessageType;
import com.spiritsword.model.MethodDescriptor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class RpcClient {
    private Channel channel;
    @Value("${spiritsword.rpc.client.id}")
    private String clientId;
    @Value("${spiritsword.rpc.client.port}")
    private String port;
    @Value("${spiritsword.rpc.client.host}")
    private String host;
    @Value("${spiritsword.rpc.client.worker}")
    private int workerGroup;

    private Map<String, Map<String, MethodDescriptor>> methodHashMap = new HashMap<>();

    Map<String, CompletableFuture<MessagePayload.RpcResponse>> requestMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        connect();
        sendRegistrationRequest();
    }

    private void connect() {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup worker = new NioEventLoopGroup(workerGroup);

        try {
            bootstrap
                    .group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new JsonCallMessageEncoder());
                            socketChannel.pipeline().addLast(new JsonMessageDecodeHandler());
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, Integer.parseInt(port)).sync();
            future.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            worker.shutdownGracefully();
        }
    }

    public void sendRegistrationRequest() {
        MessagePayload msg = new MessagePayload.RequestMessageBuilder().setClientId(clientId).setMessageType(MessageType.REGISTER).build();
        this.channel.writeAndFlush(msg);
    }

    public void registerChannel(Channel channel) {
        this.channel = channel;
    }

    @SuppressWarnings("all")
    public <T> T generate(Class<T> clazz, String requestedServiceClientId) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                int totalParams = args.length;
                String[] paramTypes = new String[totalParams];

                for(int i = 0; i < totalParams; i++) {
                    paramTypes[i] = args[i].getClass().getName();
                }

                MessagePayload payload = new MessagePayload.RequestMessageBuilder()
                        .setClientId(clientId)
                        .setMessageType(MessageType.CALL)
                        .setRequestId(UUID.randomUUID().toString())
                        .setRequestClientId(requestedServiceClientId)
                        .setParamTypes(paramTypes)
                        .setParams(args)
                        .setRequestMethodName(method.getName())
                        .setRequestedClassName(clazz.getName()).build();



                return null;
            }
        });
    }



    public void completeRequest(MessagePayload.RpcResponse response) {
        String requestId = response.getRequestId();
        CompletableFuture<MessagePayload.RpcResponse> rpcResponseCompletableFuture = requestMap.get(requestId);

        rpcResponseCompletableFuture.complete(response);
    }

}
