package org.geekbang.thinking.in.spring.application.context.lifecycle;

import org.springframework.context.support.GenericApplicationContext;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * Spring Shutdown Hook 线程Demo
 *
 * @author ajin
 */

public class SpringShutdownHookThreadDemo {

    public static void main(String[] args) {
        GenericApplicationContext context = new GenericApplicationContext();

        // context.registerBeanDefinition("myLifeCycle",rootBeanDefinition(MyLI));

        context.refresh();

        context.close();
    }
}
