package org.geekbang.thinking.in.spring.bean.lifecycle;

import org.geekbang.thinking.in.spring.ioc.overview.domain.SuperUser;
import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.ObjectUtils;

/**
 * Bean 实例化生命周期 阶段
 *
 * @author ajin
 */

public class BeanInstantiationLifecycleDemo {

    @SuppressWarnings("all")
    public static void main(String[] args) {
        // 底层IoC容器
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        // 添加 BeanPostProcessor实现
        beanFactory.addBeanPostProcessor(new MyInstantiationAwareBeanPostProcessor());
        // 基于xml的BeanDefinitionReader
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

        String location = "META-INF/dependency-lookup.xml";
        // 基于Classpath 加载Properties资源
        Resource resource = new ClassPathResource(location);
        EncodedResource encodedResource = new EncodedResource(resource, "UTF-8");

        int numbers = beanDefinitionReader.loadBeanDefinitions(encodedResource);
        System.out.printf("已加载的BeanDefinition数量:%s\n", numbers);

        SuperUser superUser = beanFactory.getBean("superUser", SuperUser.class);
        System.out.println(superUser);


        User user = beanFactory.getBean("user", User.class);

        System.out.println(user);

    }

    static class MyInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

        @Override
        public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
            if (ObjectUtils.nullSafeEquals("superUser", beanName) && SuperUser.class.equals(beanClass)) {
                // 将配置好的完整的SuperUser Bean替换掉
                return new SuperUser();
            }
            return null;// 保持Spring IoC的实例化操作
        }
    }
}
