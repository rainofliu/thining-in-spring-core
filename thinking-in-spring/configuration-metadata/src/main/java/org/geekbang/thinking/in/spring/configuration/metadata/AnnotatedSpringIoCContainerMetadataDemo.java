package org.geekbang.thinking.in.spring.configuration.metadata;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

/**
 * 基于Java注解Spring IoC容器元信息配置Demo
 *
 * @author ajin
 */
@ImportResource("classpath:META-INF/dependency-lookup.xml")
@Import(User.class)
@PropertySource("classpath:META-INF/user.properties")
@PropertySource("classpath:META-INF/user.properties")
public class AnnotatedSpringIoCContainerMetadataDemo {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        // 将当前类作为Configuration Class
        applicationContext.register(AnnotatedSpringIoCContainerMetadataDemo.class);
        // 启动Spring应用上下文
        applicationContext.refresh();
        Map<String, User> userMap = applicationContext.getBeansOfType(User.class);
        for (Map.Entry<String, User> entry : userMap.entrySet()) {
            System.out.printf("User Bean Name : %s  Content : %s\n", entry.getKey(), entry.getValue());
        }
        // 关闭Spring应用上下文
        applicationContext.close();
    }

}
