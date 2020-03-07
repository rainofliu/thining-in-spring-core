package org.geekbang.thinking.in.spring.ioc.overview.container;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

import java.util.Map;

/**
 * IoC容器示例 {@link BeanFactory}
 *
 * @author ajin
 */

public class BeanFactoryIoCContainerDemo {

    public static void main(String[] args) {
        // 创建BeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory = new DefaultListableBeanFactory();

        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(defaultListableBeanFactory);
        // XML 配置文件路径 "classpath:META-INF/dependency-lookup.xml"
        String configLocation = "classpath:META-INF/dependency-lookup.xml";
        // 加载配置
        int beanDefinitionCount = reader.loadBeanDefinitions(configLocation);
        System.out.println("Bean 定义加载的数量：" + beanDefinitionCount);
        // 依赖查找集合对象
        lookupByCollectionType(defaultListableBeanFactory);
    }

    /**
     * 按照类型查找 结合对象
     */
    private static void lookupByCollectionType(BeanFactory beanFactory) {
        if (beanFactory instanceof ListableBeanFactory) {
            ListableBeanFactory listableBeanFactory = (ListableBeanFactory) beanFactory;
            // 获取所有的User  Bean
            Map<String, User> userMap = listableBeanFactory.getBeansOfType(User.class);
            System.out.println("查找到的所有 User 集合对象" + userMap);
        }
    }

}
