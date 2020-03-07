package org.geekbang.thinking.in.spring.dependency.lookup;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 类型安全的依赖查找 Demo
 *
 * @author ajin
 */

public class TypeSafetyDependencyLookupDemo {

    public static void main(String[] args) {
        // 创建注解驱动的Spring应用上下文
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(TypeSafetyDependencyLookupDemo.class);
        // 启动Spring应用上下文
        applicationContext.refresh();

        // 验证 BeanFactory#getBean  方法的安全性
        displayBeanFactoryGetBean(applicationContext);
        // 验证 ObjectFactory#getObject  方法的安全性
        displayObjectFactoryGetObject(applicationContext);
        // 验证ObjectProvider#getIfAvailable 方法的安全性
        displayObjectProviderIfAvailable(applicationContext);
        // 验证ListableBeanFactory#getBeansOfType 方法的安全性
        displayListableBeanFactory(applicationContext);
        // 验证ObjectProvider#stream 方法的安全性
        displayObjectProviderStreamOps(applicationContext);

        // 关闭Spring应用上下文
        applicationContext.close();
    }

    private static void displayObjectProviderStreamOps(AnnotationConfigApplicationContext applicationContext) {
        // ObjectProvider is ObjectFactory
        ObjectProvider<User> userObjectProvider = applicationContext.getBeanProvider(User.class);
        printBeansException("displayObjectProviderStreamOps", () -> userObjectProvider.forEach(System.out::println));
    }

    private static void displayListableBeanFactory(AnnotationConfigApplicationContext applicationContext) {
        printBeansException("displayListableBeanFactory", () -> applicationContext.getBeansOfType(User.class));
    }

    private static void displayObjectProviderIfAvailable(AnnotationConfigApplicationContext applicationContext) {
        // ObjectProvider is ObjectFactory
        ObjectProvider<User> userObjectProvider = applicationContext.getBeanProvider(User.class);
        printBeansException("displayObjectProviderIfAvailable", () -> userObjectProvider.getIfAvailable());
    }


    private static void displayObjectFactoryGetObject(AnnotationConfigApplicationContext applicationContext) {
        // ObjectProvider is ObjectFactory
        ObjectFactory<User> userObjectFactory = applicationContext.getBeanProvider(User.class);
        printBeansException("displayObjectFactoryGetObject", () -> userObjectFactory.getObject());
    }


    public static void displayBeanFactoryGetBean(BeanFactory beanFactory) {
        printBeansException("displayBeanFactoryGetBean", () -> beanFactory.getBean(User.class));
    }

    public static void printBeansException(String source, Runnable runnable) {
        System.err.println("=======================================");
        System.err.println("Source From : " + source);

        try {
            runnable.run();
        } catch (BeansException e) {
            e.printStackTrace();
        }
    }
}
