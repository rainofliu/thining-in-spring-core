package org.geekbang.thinking.in.spring.bean.definition;

import org.geekbang.thinking.in.spring.bean.factory.DefaultUserFactory;
import org.geekbang.thinking.in.spring.bean.factory.UserFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Bean 初始化 Demo
 *
 * @author ajin
 */
@Configuration // Configuration Class
public class BeanInitializationDemo {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(BeanInitializationDemo.class);
        // 启动Spring应用上下文
        applicationContext.refresh();
        // 非延迟初始化，在Spring应用上下文启动完成后被初始化
        System.out.println("Spring应用上下文已启动...");
        // 延迟初始化，在Spring应用上下文启动完成后按需初始化，demo是在依赖查找的情况下触发了初始化
        // 依赖查找
        UserFactory userFactory = applicationContext.getBean(UserFactory.class);
        System.out.println(userFactory);
        System.out.println("Spring应用上下文准备关闭...");
        // 关闭Spring应用上下文
        applicationContext.close();
        System.out.println("Spring应用上下文已关闭...");
        // 强制触发GC
        System.gc();
    }

    @Bean(initMethod = "initUserFactory",destroyMethod = "destroyUserFactory")
    @Lazy(value = false)
    public UserFactory userFactory() {
        return new DefaultUserFactory();
    }
}
