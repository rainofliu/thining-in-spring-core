package org.geekbang.thinking.in.spring.dependency.lookup;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 通过{@link org.springframework.beans.factory.ObjectProvider} 实现依赖查找
 *
 * @author ajin
 */
//@Configuration 这个注解可以不写，默认当前类就是Configuration Class
public class ObjectProviderDemo {

    public static void main(String[] args) {
        // 创建注解驱动的Spring应用上下文
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        // 注册当前类 默认就是Configuration Class
        applicationContext.register(ObjectProviderDemo.class);
        // 启动Spring应用上下文
        applicationContext.refresh();
        // 依赖查找集合对象
        lookupByObjectProvider(applicationContext);
        lookupIfAvailable(applicationContext);
        lookupByStreamOps(applicationContext);
        // 关闭Spring应用上下文
        applicationContext.close();
    }

    /**
     * 延迟依赖查找
     *
     * @param applicationContext Spring应用上下文
     */
    private static void lookupByObjectProvider(AnnotationConfigApplicationContext applicationContext) {
        ObjectProvider<String> objectProvider = applicationContext.getBeanProvider(String.class);
        System.out.println(objectProvider.getObject());
    }


    private static void lookupIfAvailable(AnnotationConfigApplicationContext applicationContext) {
        ObjectProvider<User> objectProvider = applicationContext.getBeanProvider(User.class);
        User user = objectProvider.getIfAvailable(User::createUser);
        System.out.println("当前User对象：" + user);
    }

    /**
     * @see {@link java.util.stream.Stream} Operations
     */
    private static void lookupByStreamOps(AnnotationConfigApplicationContext applicationContext) {
        ObjectProvider<String> objectProvider = applicationContext.getBeanProvider(String.class);
//        Iterable<String> stringIterable = objectProvider;
//        for (String string : stringIterable) {
//            System.out.println(string);
//        }
        objectProvider.stream().forEach(System.out::println);
    }

    /**
     * 方法名就是Bean Name: helloWorld
     */
    @Bean
    @Primary
    public String helloWorld() {
        return "Hello,World!";
    }

    @Bean
    public String message() {
        return "message";
    }
}
