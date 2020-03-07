package org.geekbang.thinking.in.spring.dependency.lookup;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.naming.Context;

/**
 * {@link BeanInstantiationException} Demo
 *
 * @author ajin
 */

public class BeanInstantiationExceptionDemo {

    public static void main(String[] args) {
        // 创建注解驱动的Spring应用上下文
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        BeanDefinitionBuilder beanDefinitionBuilder=BeanDefinitionBuilder.genericBeanDefinition(Context.class);
        applicationContext.registerBeanDefinition("context",beanDefinitionBuilder.getBeanDefinition());
        // 启动Spring应用上下文
        applicationContext.refresh();

        applicationContext.close();
    }
}
