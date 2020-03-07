package org.geekbang.thinking.in.spring.dependency.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.annotation.PostConstruct;

/**
 * ResolvableDependency 作为依赖来源
 *
 * @author ajin
 */

public class ResolvableDependencyDemo {

    @Autowired
    private String value;

    @PostConstruct
    public void init() {
        System.out.println(value);
    }


    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        // 注册Configuration Class (配置类) -> Bean
        applicationContext.register(ResolvableDependencyDemo.class);

        applicationContext.addBeanFactoryPostProcessor(beanFactory -> {
            beanFactory.registerResolvableDependency(String.class,"hello world!");
        });
        // 启动 Spring应用上下文
        applicationContext.refresh();

        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();

//        if (beanFactory instanceof ConfigurableListableBeanFactory) {
//            ConfigurableListableBeanFactory configurableListableBeanFactory = ConfigurableListableBeanFactory.class.cast(beanFactory);
//            // 注册ResolvableDependency
//            configurableListableBeanFactory.registerResolvableDependency(String.class, "helloWorld!");
//        }

        // 依赖查找 Bean
//        ResolvableDependencyDemo dependencyDemo = applicationContext.getBean(ResolvableDependencyDemo.class);

        applicationContext.close();
    }
}
