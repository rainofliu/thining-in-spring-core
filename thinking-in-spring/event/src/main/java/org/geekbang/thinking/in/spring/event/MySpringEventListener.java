package org.geekbang.thinking.in.spring.event;

import org.springframework.context.ApplicationListener;

/**
 * @author ajin
 */

public class MySpringEventListener implements ApplicationListener<MySpringEvent> {
    @Override
    public void onApplicationEvent(MySpringEvent event) {
        System.out.printf("【%s】线程监听到MySpringEvent\n",Thread.currentThread().getName());
    }
}
