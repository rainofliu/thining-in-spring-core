package org.geekbang.thinking.in.spring.bean.lifecycle;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;

/**
 * Bean元信息配置 Demo
 *
 * @author ajin
 */
public class BeanMetadataConfigurationDemo {

    public static void main(String[] args) {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        // 实例化基于Properties资源的 BeanDefinitionReader
        PropertiesBeanDefinitionReader beanDefinitionReader = new PropertiesBeanDefinitionReader(beanFactory);
        String location = "classpath:/META-INF/user.properties";
        // 基于Classpath 加载Properties资源
        Resource resource = new ClassPathResource(location);
        EncodedResource encodedResource = new EncodedResource(resource, "UTF-8");
        // 指定字符编码为UTF-8
        int beanNumbers = beanDefinitionReader.loadBeanDefinitions(encodedResource);

        System.out.println("已加载的 BeanDefinition 数量 ：" + beanNumbers);

        // 通过Bean Id 和类型 依赖查找
        User user = beanFactory.getBean("user", User.class);
        System.out.println(user.toString());
    }


}
