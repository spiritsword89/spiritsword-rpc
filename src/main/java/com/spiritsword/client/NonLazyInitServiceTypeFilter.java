package com.spiritsword.client;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.LazyInitializationExcludeFilter;
import org.springframework.stereotype.Component;

@Component
public class NonLazyInitServiceTypeFilter implements LazyInitializationExcludeFilter {
    @Override
    public boolean isExcluded(String beanName, BeanDefinition beanDefinition, Class<?> beanType) {
        return NonLazyInitService.class.isAssignableFrom(beanType);
    }
}
