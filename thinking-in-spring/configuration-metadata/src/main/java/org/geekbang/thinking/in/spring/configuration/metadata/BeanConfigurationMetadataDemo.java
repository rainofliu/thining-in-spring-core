package org.geekbang.thinking.in.spring.configuration.metadata;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.ObjectUtils;

/**
 * Bean 配置元信息 Demo
 *
 * @author ajin
 */

public class BeanConfigurationMetadataDemo {

    public static void main(String[] args) {

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(User.class);
        beanDefinitionBuilder.addPropertyValue("name", "liutianruo");
        // 获取AbstractBeanDefinition
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        // 附加属性(不影响Bean的实例化、属性赋值和初始化阶段）
        beanDefinition.setAttribute("name", "ajin");
        // 当前BeanDefinition来自于何方
        beanDefinition.setSource(BeanConfigurationMetadataDemo.class);

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.addBeanPostProcessor(new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                // User Bean初始化后阶段
                if (ObjectUtils.nullSafeEquals("user", beanName) && User.class.equals(bean.getClass())) {
                    BeanDefinition rmd = beanFactory.getBeanDefinition(beanName);
                    if (BeanConfigurationMetadataDemo.class.equals(rmd.getSource())) {
                        // 属性上下文
                        String name = (String) rmd.getAttribute("name"); // ajin
                        User user = (User) bean;
                        user.setName(name);
                    }

                }
                return bean;
            }
        });
        // 注册User的BeanDefinition
        beanFactory.registerBeanDefinition("user", beanDefinition);

        //
        User user = beanFactory.getBean(User.class);
        System.out.println(user);

    }
}
