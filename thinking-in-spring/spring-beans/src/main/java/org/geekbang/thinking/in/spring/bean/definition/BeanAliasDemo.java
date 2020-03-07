package org.geekbang.thinking.in.spring.bean.definition;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Bean 别名 Demo
 *
 * @author ajin
 */

public class BeanAliasDemo {

    public static void main(String[] args) {
        // 配置xml文件
        // 启动 Spring应用上下文
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("classpath:/META-INF/bean-definition-context.xml");
        User user = (User) beanFactory.getBean("user");
        // 通过别名进行依赖查找
        User xiaomageUser = (User) beanFactory.getBean("xiaomage-user");
        System.out.println("xiaomageUser和User是否相同:" + (user == xiaomageUser));

    }
}
