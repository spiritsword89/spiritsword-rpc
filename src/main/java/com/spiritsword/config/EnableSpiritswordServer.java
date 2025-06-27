package com.spiritsword.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(SpiritswordPrcConfiguration.class)
public @interface EnableSpiritswordServer {
}
