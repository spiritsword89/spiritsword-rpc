package com.spiritsword.demo;

import com.spiritsword.model.MessagePayload;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MyDemo {

    @Test
    public void run() {
        Class<MessagePayload> messagePayloadClass = MessagePayload.class;
        String name = messagePayloadClass.getName();
        System.out.println(name);
    }

    @Test
    public void run2() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);

                    future.complete("hello");

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        String result = future.get();

        System.out.println(result);

        System.out.println("Finished");
    }

    @Test
    public void run3() throws IOException {
        String scanPackage = "com.spiritsword.model";
        scanPackage = scanPackage.replaceAll("\\.", "/");

        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(scanPackage);

        while(resources.hasMoreElements()) {
            URL url = resources.nextElement();
            File file = new File(url.getFile());

            if(file.isDirectory()) {
                File[] files = file.listFiles();

                for(File f : files) {
                    System.out.println(f.getName());
                }
            }
        }
    }

    @Test
    public void run4() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
//        scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));
        scanner.addIncludeFilter((meta, factory) -> true);
//        scanner.addIncludeFilter(new AssignableTypeFilter(RemoteService.class));

        Set<BeanDefinition> candidateComponents =
                scanner.findCandidateComponents("com.spiritsword.client");

        for(BeanDefinition candidateComponent : candidateComponents) {
            System.out.println(candidateComponent.getBeanClassName());
        }
    }
}
