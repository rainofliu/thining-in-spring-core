package org.geekbang.thinking.in.spring.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * {@link ApplicationListener} Demo
 *
 * @author ajin
 */
@EnableAsync
public class ApplicationListenerDemo implements ApplicationEventPublisherAware {

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        applicationEventPublisher.publishEvent(new ApplicationEvent("hello") {
        });
    }

    public static void main(String[] args) {

//        GenericApplicationContext applicationContext = new GenericApplicationContext();

        // 注解驱动上下文
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        // Configuration Class
        applicationContext.register(ApplicationListenerDemo.class);

        applicationContext.register(MyApplicationListener.class);

        // way 1基于接口：向Spring原应用上下文注册事件
//        applicationContext.addApplicationListener(event -> System.out.println("接受到Spring事件" + event));


        // way 2 基于注解


        // 启动应用上下文
        applicationContext.refresh();

        applicationContext.start();

        applicationContext.close();
    }

    @EventListener
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        println(applicationEvent);
    }

    @EventListener
    @Async
    public void onApplicationEventAsync(ContextRefreshedEvent applicationEvent) {
        println(applicationEvent);
//        System.out.println("EventListener接收到ContextRefreshedEvent");
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent applicationEvent) {
//        println(applicationEvent);
        System.out.println("onApplicationEvent接收到ContextRefreshedEvent");
    }


    static class MyApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            System.out.println("MyApplicationListener");
        }
    }

    /**
     * {@link Order} 可以控制顺序
     */
    @EventListener
    @Order(2)
    public void onApplicationEvent1(ContextRefreshedEvent applicationEvent) {
//        println(applicationEvent);
        System.out.println("onApplicationEvent1接收到ContextRefreshedEvent");
    }


    @EventListener
    public void onApplicationEvent(ContextStartedEvent applicationEvent) {
//        println(applicationEvent);
        System.out.println("onApplicationEvent接收到ContextStartedEvent");
    }

    @EventListener
    public void onApplicationEvent(ContextClosedEvent applicationEvent) {
        println(applicationEvent);
//        System.out.println("EventListener接收到ContextClosedEvent");
    }

    private static void println(Object printable) {
        System.out.printf("[线程%s]: %s\n", Thread.currentThread().getName(), printable);
    }
}
