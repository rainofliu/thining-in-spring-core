package org.geekbang.thinking.in.spring.dependency.injection;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;

/**
 * {@link org.springframework.beans.factory.ObjectProvider}实现延迟依赖注入
 *
 * @author ajin
 */
public class LazyAnnotationDependencyInjectionDemo {

    @Autowired
    private User user;// 实时注入

    @Autowired
    private ObjectProvider<User> userObjectProvider;  // 间接，延迟注入

    @Autowired
    private ObjectFactory<Collection<User>>  collectionObjectFactory;

    public static void main(String[] args) {

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        // 注册Configuration Class (配置类) -> Bean
        applicationContext.register(LazyAnnotationDependencyInjectionDemo.class);

        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(applicationContext);

        String xmlResourcePath = "classpath:/META-INF/dependency-lookup.xml";

        // 加载Xml资源，解析并生成Spring BeanDefinition
        beanDefinitionReader.loadBeanDefinitions(xmlResourcePath);

        // 启动 Spring应用上下文
        applicationContext.refresh();

        LazyAnnotationDependencyInjectionDemo demo = applicationContext.getBean(LazyAnnotationDependencyInjectionDemo.class);

        System.out.println("demo.user=" + demo.user);
        System.out.println("demo.userObjectProvider=" + demo.userObjectProvider.getObject());
        System.out.println("demo.userObjectFactory=" + demo.collectionObjectFactory.getObject());
        System.out.println("------------------");
        demo.userObjectProvider.forEach(System.out::println);

        applicationContext.close();

    }
}
