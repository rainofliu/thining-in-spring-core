package org.geekbang.thinking.in.spring.application.context.lifecycle;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.LiveBeansView;

import static org.springframework.context.support.LiveBeansView.MBEAN_DOMAIN_PROPERTY_NAME;

/**
 * {@link LiveBeansView} Demo
 *
 * @author ajin
 */

public class LiveBeansViewDemo {

    public static void main(String[] args) {

        // 添加LiveBeansView 的 objectName 的domain
        System.setProperty(MBEAN_DOMAIN_PROPERTY_NAME, "org.geekbang.thinking.in.spring");

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        // 注册Configuration Class
        context.register(LiveBeansViewDemo.class);

        // 启动Spring应用上下文
        context.refresh();

        System.out.println("");

        // 关闭Spring应用上下文
        context.close();
    }
}
