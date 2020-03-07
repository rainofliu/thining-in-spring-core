package org.geekbang.thinking.in.spring.dependency.injection;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * 基于字段依赖 注入 示例
 *
 * @author ajin
 */
public class AnnotationDependencyFiledInjectionDemo {

    @Autowired // @Autowired 会忽略掉静态(static)字段
    private UserHolder userHolder;

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        // 注册Configuration Class (配置类) -> Bean
        applicationContext.register(AnnotationDependencyFiledInjectionDemo.class);

        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(applicationContext);

        String xmlResourcePath = "classpath:/META-INF/dependency-lookup.xml";

        // 加载Xml资源，解析并生成Spring BeanDefinition
        beanDefinitionReader.loadBeanDefinitions(xmlResourcePath);

        // 启动 Spring应用上下文
        applicationContext.refresh();

        // 依赖查找 AnnotationDependencyFiledInjectionDemo Bean
        AnnotationDependencyFiledInjectionDemo demo = applicationContext.getBean(AnnotationDependencyFiledInjectionDemo.class);

        // @Autowired字段关联
        UserHolder userHolder = demo.userHolder;

        System.out.println(userHolder);

        applicationContext.close();


    }

    @Bean
    public UserHolder userHolder(User user) {

        return new UserHolder(user);
    }
}
