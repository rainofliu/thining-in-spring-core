package org.geekbang.thinking.in.spring.configuration.metadata;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

import java.util.Map;

/** Spring XML 扩展 Demo
 * @author ajin
 */

public class ExtensibleXmlAuthoringDemo {
    public static void main(String[] args) {
        DefaultListableBeanFactory beanFactory=new DefaultListableBeanFactory();
        XmlBeanDefinitionReader xmlBeanDefinitionReader=new XmlBeanDefinitionReader(beanFactory);
        // 加载xml配置文件
        xmlBeanDefinitionReader.loadBeanDefinitions("META-INF/user-context.xml");
        Map<String, User> userMap = beanFactory.getBeansOfType(User.class);
        System.out.println(userMap.get(0));
    }
}
