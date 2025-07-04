package com.spiritsword.config;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface AutoRemoteInjection {
    String requestedServiceClientId() default "";
}
