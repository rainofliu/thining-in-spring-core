package org.geekbang.thinking.in.spring.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.support.GenericApplicationContext;

/**
 * {@link ApplicationListener} Demo
 *
 * @author ajin
 */

public class ApplicationListenerDemo {

    public static void main(String[] args) {
        GenericApplicationContext applicationContext = new GenericApplicationContext();

        // 向Spring原应用上下文注册事件
        applicationContext.addApplicationListener(event -> {
            System.out.println("接受到Spring事件" + event);
        });

        // 启动应用上下文
        applicationContext.refresh();

        applicationContext.start();

        applicationContext.close();
    }
}
