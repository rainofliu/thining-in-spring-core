package org.geekbang.thinking.in.spring.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

/**
 * 事件 异步 处理 实例
 *
 * @author ajin
 */

public class AsyncEventHandleDemo {

    public static void main(String[] args) {

        GenericApplicationContext applicationContext = new GenericApplicationContext();

        // 此时 ApplicationEventMulticaster尚未初始化
        applicationContext.addApplicationListener(new MySpringEventListener());

        applicationContext.refresh();

        // 通过依赖查找 获取 ApplicationEventMulticaster
        ApplicationEventMulticaster applicationEventMulticaster =
                applicationContext.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);

        if (applicationEventMulticaster instanceof SimpleApplicationEventMulticaster) {
            SimpleApplicationEventMulticaster simpleApplicationEventMulticaster =
                    (SimpleApplicationEventMulticaster) applicationEventMulticaster;
            // 异步处理监听到的事件
            ExecutorService taskExecutor = newSingleThreadExecutor(
                    new CustomizableThreadFactory("my-spring-event-thread-pool")
            );
            simpleApplicationEventMulticaster.setTaskExecutor(taskExecutor);

            // ErrorHandler
            simpleApplicationEventMulticaster.setErrorHandler(e-> System.out.println("抛出异常为"+e.getClass().getSimpleName()));

            // 添加ContextClosedEvent 事件处理
            simpleApplicationEventMulticaster.addApplicationListener(new ApplicationListener<ContextClosedEvent>() {
                @Override
                public void onApplicationEvent(ContextClosedEvent event) {
                    if (!taskExecutor.isShutdown()) {
                        taskExecutor.shutdown();
                    }
                    throw new RuntimeException();
                }
            });
        }
        for (int i = 0; i < 4000; i++) {
            applicationContext.publishEvent(new MySpringEvent("测试" + i + 1));
        }
        // 关闭Spring应用上下文 ContextClosedEvent
        applicationContext.close();


    }
}
