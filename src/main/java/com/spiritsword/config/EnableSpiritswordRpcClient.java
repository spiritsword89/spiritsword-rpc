package com.spiritsword.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({RpcClientRegistrar.class, PostStartupProcessor.class})
public @interface EnableSpiritswordRpcClient {
    String clientId() default "";
    String[] packages() default {};
}
