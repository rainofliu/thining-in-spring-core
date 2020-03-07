package org.geekbang.thinking.in.spring.ioc.overview.dependency.injection;

import org.geekbang.thinking.in.spring.ioc.overview.repository.UserRepository;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.Environment;

/**
 * 依赖注入示例
 *
 * @author ajin
 */

public class DependencyInjectionDemo {

    public static void main(String[] args) {
        // 配置xml配置文件
        // 启动spring应用上下文
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("classpath:META-INF/dependency-injection.xml");
        // 依赖来源1. 业务Bean
        UserRepository userRepository = beanFactory.getBean("userRepository", UserRepository.class);
        // 依赖注入(依赖来源2. 内建依赖)
        System.out.println(userRepository.getBeanFactory());

        ObjectFactory objectFactory = userRepository.getUserObjectFactory();
        // true
        System.out.println(objectFactory.getObject() == beanFactory);
        // 依赖查找 NoSuchBeanDefinitionException
        // System.out.println(beanFactory.getBean(BeanFactory.class));
        // false
//        System.out.println(userRepository.getBeanFactory()==beanFactory);

        // 依赖来源3. 容器内部Bean
        Environment environment = beanFactory.getBean(Environment.class);
        System.out.println(environment);
    }


}
