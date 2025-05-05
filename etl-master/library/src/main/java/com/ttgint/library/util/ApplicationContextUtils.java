package com.ttgint.library.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextUtils implements ApplicationContextAware {

    public static ApplicationContext context;

    public static <T> T getSingleBeanOfType(Class<T> beanClass) {
        return context.getBeansOfType(beanClass).values().iterator().next();
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        ApplicationContextUtils.context = context;
    }

}
