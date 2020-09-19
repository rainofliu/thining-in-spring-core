package org.geekbang.thinking.in.spring.questions;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

/**
 * {@link ObjectFactory}延迟加载讨论 Demo
 *
 * @author ajin
 * @see ObjectFactory
 * @see ObjectProvider
 */

public class ObjectFactoryLazyLookupDemo {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(ObjectFactoryLazyLookupDemo.class);

        context.refresh();

        ObjectFactoryLazyLookupDemo lazyLookupDemo = context.getBean(ObjectFactoryLazyLookupDemo.class);

        System.out.println("userObjectFactory==userObjectProvider：" + (lazyLookupDemo.userObjectFactory
            == lazyLookupDemo.userObjectProvider));

        System.out.println(lazyLookupDemo.userObjectFactory.getObject());

        context.close();
    }

    @Autowired
    private ObjectFactory<User> userObjectFactory;

    @Autowired
    private ObjectProvider<User> userObjectProvider;

    @Bean
    @Lazy
    public static User user() {
        return User.createUser();
    }
}
