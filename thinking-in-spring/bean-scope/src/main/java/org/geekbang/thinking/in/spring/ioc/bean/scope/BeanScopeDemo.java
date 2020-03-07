package org.geekbang.thinking.in.spring.ioc.bean.scope;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

/**
 * Bean的作用域Demo
 *
 * @author ajin
 */
public class BeanScopeDemo {


    @Bean
    // 默认Scope :  singleton
    public static User singletonUser() {

        return createUser();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static User prototypeUser() {

        return createUser();
    }

    @Autowired
    @Qualifier("singletonUser")
    private User singletonUser;

    @Autowired
    @Qualifier("prototypeUser")
    private User prototypeUser;

    @Autowired
    @Qualifier("singletonUser")
    private User singletonUser1;

    @Autowired
    @Qualifier("prototypeUser")
    private User prototypeUser1;

    private static User createUser() {
        User user = new User();
        user.setId(System.nanoTime());
        return user;
    }


    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        // 注册Configuration Class (配置类) -> Bean
        applicationContext.register(BeanScopeDemo.class);

        // 启动 Spring应用上下文
        applicationContext.refresh();
        // 依赖查找 Bean
//        scopedBeanByLookUp(applicationContext);
        // 依赖查找 Bean
        scopedBeansByInjection(applicationContext);
        // 关闭Spring应用上下文
        applicationContext.close();
    }

    private static void scopedBeanByLookUp(AnnotationConfigApplicationContext applicationContext) {

        for (int i = 0; i < 3; i++) {
            // singleton 共享 Bean对象
            User singletonUser = applicationContext.getBean("singletonUser", User.class);
            System.out.println("singletonUser=" + singletonUser);
            // prototype 重复创建Bean对象
            User prototypeUser = applicationContext.getBean("prototypeUser", User.class);
            System.out.println("prototypeUser=" + prototypeUser);
        }
    }


    private static void scopedBeansByInjection(AnnotationConfigApplicationContext applicationContext) {
        BeanScopeDemo beanScopeDemo = applicationContext.getBean(BeanScopeDemo.class);

        System.out.println("singletonUser=" + beanScopeDemo.singletonUser);
        System.out.println("singletonUser1=" + beanScopeDemo.singletonUser1);
        System.out.println("prototypeUser=" + beanScopeDemo.prototypeUser);
        System.out.println("prototypeUser1=" + beanScopeDemo.prototypeUser1);

    }
}
