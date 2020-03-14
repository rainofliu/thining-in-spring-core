package org.geekbang.thinking.in.spring.bean.lifecycle;

import org.geekbang.thinking.in.spring.ioc.overview.domain.SuperUser;
import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;

/**
 * {@link BeanDefinition} 合并 Demo
 *
 * @author ajin
 */

public class MergedBeanDefinitionDemo {

    public static void main(String[] args) {
        // 底层IoC容器
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        // 基于xml的BeanDefinitionReader
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

        String location = "META-INF/dependency-lookup.xml";
        // 基于Classpath 加载Properties资源
        Resource resource = new ClassPathResource(location);
        EncodedResource encodedResource = new EncodedResource(resource, "UTF-8");

        int numbers=beanDefinitionReader.loadBeanDefinitions(encodedResource);
        System.out.printf("已加载的BeanDefinition数量:%s\n",numbers);

        SuperUser superUser = beanFactory.getBean("superUser", SuperUser.class);
        System.out.println(superUser);


        User user = beanFactory.getBean("user", User.class);

        System.out.println(user);


    }
}
