package org.geekbang.thinking.in.spring.i18n;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Spring Boot场景下 自定义{@link MessageSource} Bean
 *
 * @author ajin
 * @see MessageSource
 * @see MessageSourceAutoConfiguration
 * @see ReloadableResourceBundleMessageSource
 */
@EnableAutoConfiguration
public class CustomizedMessageSourceDemo {
    /**
     * 在Spring Boot场景中 ，@EnableAutoConfiguration Class (Primary Class) 优先级高于*AutoConfiguration Class
     */
    @Bean(name = AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME)
    public MessageSource messageSource() {
        return new ReloadableResourceBundleMessageSource();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(CustomizedMessageSourceDemo.class, args);
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

        if (beanFactory.containsBean(AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME)) {

            MessageSource messageSource = applicationContext.getBean(AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME, MessageSource.class);
            System.out.println(messageSource);

            // 查找messageSource BeanDefinition
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME);
            System.out.println(beanDefinition);

        }

        // 关闭Spring Boot应用上下文
        applicationContext.close();
    }
}
