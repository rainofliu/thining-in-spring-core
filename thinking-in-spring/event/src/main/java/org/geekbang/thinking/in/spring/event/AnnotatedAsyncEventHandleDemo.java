package org.geekbang.thinking.in.spring.event;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * 基于注解的事件 异步 处理 实例
 *
 * @author ajin
 */
@EnableAsync
public class AnnotatedAsyncEventHandleDemo {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        // register as Configuration Class
        applicationContext.register(AnnotatedAsyncEventHandleDemo.class);

        applicationContext.refresh();

        applicationContext.publishEvent(new MySpringEvent("测试"));
        // 关闭Spring应用上下文 ContextClosedEvent
        applicationContext.close();

    }

    @EventListener
    @Async
    public void onEvent(MySpringEvent mySpringEvent) {
        System.out.printf("[线程 %s] onEvent方法监听到事件 %s \n", Thread.currentThread().getName(), mySpringEvent);
    }

    @Bean
    public Executor taskExecutor() {
        ExecutorService taskExecutor = newSingleThreadExecutor(
                new CustomizableThreadFactory("my-spring-event-thread-pool"));
        return taskExecutor;
    }
}
