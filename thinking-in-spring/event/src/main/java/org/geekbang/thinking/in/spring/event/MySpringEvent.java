package org.geekbang.thinking.in.spring.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author ajin
 */

public class MySpringEvent extends ApplicationEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public MySpringEvent(Object source) {
        super(source);
//        System.out.printf("[%s]线程\n",Thread.currentThread().getName() );
    }
}
