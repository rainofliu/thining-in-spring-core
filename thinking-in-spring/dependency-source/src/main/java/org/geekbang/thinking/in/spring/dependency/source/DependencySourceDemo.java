package org.geekbang.thinking.in.spring.dependency.source;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;

/**
 * 依赖来源 Demo
 *
 * @author ajin
 */

public class DependencySourceDemo {

    /**
     * 注入在 postProcessProperties方法执行 早于setter注入，也早于@PostConstruct
     */
    @Autowired
    private BeanFactory beanFactory;
    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @PostConstruct
    public void initByLookup() {
        getBean(BeanFactory.class);
        getBean(ResourceLoader.class);
        getBean(ApplicationContext.class);
        getBean(ApplicationEventPublisher.class);

    }
    @PostConstruct
    public void initByInject() {
        System.out.println("beanFactory==applicationContext " + (beanFactory == applicationContext));
        System.out.println("beanFactory==applicationContext.getBeanFactory  " + (beanFactory == applicationContext.getAutowireCapableBeanFactory()));
        System.out.println("applicationContext==resourceLoader  " + (resourceLoader == applicationContext));
        System.out.println("applicationContext==applicationEventPublisher    " + (applicationEventPublisher == applicationContext));

    }



    private <T> T getBean(Class<T> beanType) {
        try {
            return applicationContext.getBean(beanType);
        } catch (NoSuchBeanDefinitionException e) {
            System.err.println("Spring上下文没有查找到当前Bean:"+beanType);
            return null;
        }
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        // 注册Configuration Class (配置类) -> Bean
        applicationContext.register(DependencySourceDemo.class);

        // 启动 Spring应用上下文
        applicationContext.refresh();
        // 依赖查找 Bean
        DependencySourceDemo dependencySourceDemo = applicationContext.getBean(DependencySourceDemo.class);
        System.out.println();

        applicationContext.close();
    }
}
