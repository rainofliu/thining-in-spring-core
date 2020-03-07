package org.geekbang.thinking.in.spring.ioc.overview.dependency.lookup;

import org.geekbang.thinking.in.spring.ioc.overview.annotation.Super;
import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;

/**
 * 依赖查找Demo
 * 1. 通过名称查找
 * 2. 通过类型查找
 *
 * @author ajin
 */

public class DependencyLookupDemo {

    public static void main(String[] args) {

        // 配置xml配置文件
        // 启动spring应用上下文
        BeanFactory beanFactory =
                new ClassPathXmlApplicationContext("classpath:/META-INF/dependency-lookup.xml");
        lookupByType(beanFactory);
        lookupByCollectionType(beanFactory);
        lookupByAnnotationType(beanFactory);
//        lookupInRealTime(beanFactory);
//        lookupInLazy(beanFactory);
    }

    private static void lookupByAnnotationType(BeanFactory beanFactory) {

        if (beanFactory instanceof ListableBeanFactory) {
            ListableBeanFactory listableBeanFactory = (ListableBeanFactory) beanFactory;
            // 获取所有@Super的User  Bean
            Map<String, User> userMap = (Map) listableBeanFactory.getBeansWithAnnotation(Super.class);
            System.out.println("查找标注@Super 的所有 User 集合对象" + userMap);
        }
    }

    /**
     * 按照类型查找 集合对象
     */
    private static void lookupByCollectionType(BeanFactory beanFactory) {
        if (beanFactory instanceof ListableBeanFactory) {
            ListableBeanFactory listableBeanFactory = (ListableBeanFactory) beanFactory;
            // 获取所有的User  Bean
            Map<String, User> userMap = listableBeanFactory.getBeansOfType(User.class);
            System.out.println("查找到的所有 User 集合对象" + userMap);
        }
    }

    /**
     * 按照类型查找 单一类型
     */
    private static void lookupByType(BeanFactory beanFactory) {
        User user = beanFactory.getBean(User.class);
        System.out.println("实时查找:" + user);
    }

    /**
     * 延迟查找
     *
     * @param beanFactory
     */
    private static void lookupInLazy(BeanFactory beanFactory) {
        ObjectFactory<User> objectFactory = (ObjectFactory<User>) beanFactory.getBean("objectFactory");
        User user = objectFactory.getObject();
        System.out.println("延迟查找:" + user);
    }

    /**
     * 实时查找
     *
     * @param beanFactory IoC容器
     */
    public static void lookupInRealTime(BeanFactory beanFactory) {

        User user = (User) beanFactory.getBean("user");
        System.out.println("实时查找：" + user);
    }
}
