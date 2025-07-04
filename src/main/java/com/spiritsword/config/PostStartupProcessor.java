package com.spiritsword.config;

import com.spiritsword.client.RemoteService;
import com.spiritsword.client.RpcClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;

public class PostStartupProcessor implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();

        for(String beanDefinitionName : beanDefinitionNames){
            Object bean = applicationContext.getBean(beanDefinitionName);

            Field[] declaredFields = bean.getClass().getDeclaredFields();

            for(Field field : declaredFields){
                boolean annotationPresent = field.isAnnotationPresent(AutoRemoteInjection.class);

                if(annotationPresent){
                    AutoRemoteInjection annotation = field.getAnnotation(AutoRemoteInjection.class);
                    String requestedClientId = annotation.requestedServiceClientId();
                    Class<?> classType = field.getType();

                    if(RemoteService.class.isAssignableFrom(classType) && requestedClientId != null){
                        RpcClient rpcClient = applicationContext.getBean(RpcClient.class);
                        Object proxy = rpcClient.generate(classType, requestedClientId);
                        try {
                            field.setAccessible(true);
                            field.set(bean, proxy);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}
