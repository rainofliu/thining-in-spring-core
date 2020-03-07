package org.geekbang.thinking.in.spring.bean.definition;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * {@link AnnotatedBeanDefinition} Demo
 *
 * @author ajin
 */
@Import(AnnotatedBeanDefinitionDemo.Config.class)
public class AnnotatedBeanDefinitionDemo {


    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        applicationContext.register(AnnotatedBeanDefinitionDemo.class);

        // 通过BeanDefinition 注册API实现
        registerUserBeanDefinition(applicationContext,"ajin-user");
        registerUserBeanDefinition(applicationContext);
        registerUserBeanDefinition(applicationContext);
        applicationContext.refresh();

        Map<String, Config> configMap = applicationContext.getBeansOfType(Config.class);
        System.out.println("Config类型的Bean :" + configMap);

        System.out.println("User类型的Bean : " + applicationContext.getBeansOfType(User.class));
        applicationContext.close();
    }

    @Component
    public static class Config {
        @Bean
        public User user() {
            User user = new User();
            user.setId(1L);
            user.setName("ajin");
            return user;
        }
    }

    /**
     * 基于Spring API 注册User Bean
     *
     * @param registry
     * @param beanName 可以为Null
     */
    public static void registerUserBeanDefinition(BeanDefinitionRegistry registry, String beanName) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(User.class);
        beanDefinitionBuilder
                .addPropertyValue("id", 1L)
                .addPropertyValue("name", "ajin");
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();

        // 如果beanName存在
        if (StringUtils.hasText(beanName)) {
            registry.registerBeanDefinition(beanName, beanDefinition);
        } else {
            // 非命名的注册方式
            BeanDefinitionReaderUtils.registerWithGeneratedName((AbstractBeanDefinition) beanDefinition, registry);
        }
    }

    public static void registerUserBeanDefinition(BeanDefinitionRegistry registry) {
        registerUserBeanDefinition(registry, null);
    }
}
