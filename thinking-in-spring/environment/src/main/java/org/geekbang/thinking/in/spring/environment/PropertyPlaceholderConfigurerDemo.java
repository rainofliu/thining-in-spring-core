package org.geekbang.thinking.in.spring.environment;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * {@link PropertyPlaceholderConfigurer}处理占位符 Demo  before SpringFramework 3.1
 *
 * @author ajin
 */

public class PropertyPlaceholderConfigurerDemo {

    public static void main(String[] args) {
        // 创建并启动Spring应用上下文
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/placeholder-context.xml");

        User user = context.getBean("user",User.class);
        System.out.println(user);
        // 关闭Spring应用上下文
        context.close();

    }
}
