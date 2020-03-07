package org.geekbang.thinking.in.spring.ioc.overview.container;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 注解能力{@link org.springframework.context.ApplicationContext}作为IoC容器示例
 *
 * @author ajin
 */
@Configuration
public class AnnotationApplicationContextIoCContainerDemo {

    public static void main(String[] args) {
        // 创建BeanFactory
        //  DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        // 将当前类AnnotationApplicationContextIoCContainerDemo作为配置类 Configuration Class
        applicationContext.register(AnnotationApplicationContextIoCContainerDemo.class);
        // 启动ApplicationContext
        applicationContext.refresh();
        // 依赖查找集合对象
        lookupByCollectionType(applicationContext);
        // 关闭应用上下文
        applicationContext.close();

    }

    /**
     * 注解方式定义Spring Bean
     */
    @Bean
    public User user() {
        User user = new User();
        user.setId(1L);
        user.setName("ajin");
        return user;
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
