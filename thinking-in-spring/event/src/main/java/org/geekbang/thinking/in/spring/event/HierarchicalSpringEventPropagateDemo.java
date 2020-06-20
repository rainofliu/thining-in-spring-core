package org.geekbang.thinking.in.spring.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 层次性Spring事件传播 Demo
 *
 * @author ajin
 */

public class HierarchicalSpringEventPropagateDemo {

    public static void main(String[] args) {
        // 1. 创建parent Spring应用上下文
        AnnotationConfigApplicationContext parentContext = new AnnotationConfigApplicationContext();
        parentContext.setId("parent-context");

        // register as Configuration Class (@Configuration)
        parentContext.register(MyListener.class);

        // 2. 创建current Spring应用上下文
        AnnotationConfigApplicationContext currentContext = new AnnotationConfigApplicationContext();
        currentContext.setId("current-context");
        currentContext.register(MyListener.class);

        currentContext.setParent(parentContext);

        // 启动应用上下文
        parentContext.refresh();

        currentContext.refresh();

        // 关闭全部的应用上下文
        parentContext.close();
        currentContext.close();
    }

    // 监听Spring应用上下文[ ID : parent-context]的ContextRefreshedEvent
//监听Spring应用上下文[ ID : current-context]的ContextRefreshedEvent
//监听Spring应用上下文[ ID : current-context]的ContextRefreshedEvent
    static class MyListener implements ApplicationListener<ContextRefreshedEvent> {

        private static Set<ContextRefreshedEvent> processedEvents = new LinkedHashSet<>();

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            if (processedEvents.add(event)) {
                System.out.printf("监听Spring应用上下文[ ID : %s]的ContextRefreshedEvent \n", event.getApplicationContext().getId());
            }
        }
    }
}
