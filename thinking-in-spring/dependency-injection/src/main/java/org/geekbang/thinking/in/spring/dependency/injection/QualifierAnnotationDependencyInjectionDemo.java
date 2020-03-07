package org.geekbang.thinking.in.spring.dependency.injection;

import org.geekbang.thinking.in.spring.dependency.injection.annotation.UserGroup;
import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Collection;

/**
 * {@link Qualifier}注解依赖注入
 *
 * @author ajin
 * @see Qualifier
 */
public class QualifierAnnotationDependencyInjectionDemo {

    @Autowired
    private User user; // superUser primary=true

    @Autowired
    @Qualifier("user") // 指定Bean Name /Id
    private User namedUser;

    // 整体Spring上下文存在4个User类型的Bean:
    // superUser
    // user
    // user1 -> @Qualifier
    // user2 -> @Qualifier

    @Autowired
    private Collection<User> allUsers; // 2个Bean=superUser+user


    @Autowired
    @Qualifier
    private Collection<User> qualifiedUsers; // user1+user2+user3+user4

    @Autowired
    @UserGroup
    private Collection<User> groupedUsers; // user3+user4

    @Bean
    @Qualifier // 进行逻辑分组
    public User user1() {
        User user1 = new User();
        user1.setId(7L);
        return user1;
    }

    @Bean
    @Qualifier // 进行逻辑分组
    public User user2() {
        User user2 = new User();
        user2.setId(8L);
        return user2;
    }

    @Bean
    @UserGroup // 进行逻辑分组
    public User user3() {
        User user3 = new User();
        user3.setId(9L);
        return user3;
    }

    @Bean
    @UserGroup // 进行逻辑分组
    public User user4() {
        User user4 = new User();
        user4.setId(10L);
        return user4;
    }


    public static void main(String[] args) {

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        // 注册Configuration Class (配置类) -> Bean
        applicationContext.register(QualifierAnnotationDependencyInjectionDemo.class);

        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(applicationContext);

        String xmlResourcePath = "classpath:/META-INF/dependency-lookup.xml";

        // 加载Xml资源，解析并生成Spring BeanDefinition
        beanDefinitionReader.loadBeanDefinitions(xmlResourcePath);

        // 启动 Spring应用上下文
        applicationContext.refresh();

        QualifierAnnotationDependencyInjectionDemo demo = applicationContext.getBean(QualifierAnnotationDependencyInjectionDemo.class);
        // 期待 superUser
        System.out.println(demo.user);
        // 期待 user
        System.out.println(demo.namedUser);
        // 期待 superUser user user1 user2
        System.out.println(demo.allUsers);
        // 期待 user1  user2
        System.out.println(demo.qualifiedUsers);
        // 期待 user3  user4
        System.out.println(demo.groupedUsers);

        applicationContext.close();

    }
}
