package org.geekbang.thinking.in.spring.dependency.source;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;

/**
 * 外部化配置作为依赖来源 Demo
 *
 * @author ajin
 */
@Configuration
@PropertySource("META-INF/default.properties")
public class ExternalConfigurationDependencySourceDemo {

    @Value("${user.id:-1}")
    private Long id;

    @Value("${user.resource:classpath://default.properties}")
    private Resource resource;

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        // 注册Configuration Class (配置类) -> Bean
        applicationContext.register(ExternalConfigurationDependencySourceDemo.class);

        // 启动 Spring应用上下文
        applicationContext.refresh();
        ExternalConfigurationDependencySourceDemo dependencySourceDemo =
                applicationContext.getBean(ExternalConfigurationDependencySourceDemo.class);
        System.out.println(dependencySourceDemo.id);
        System.out.println(dependencySourceDemo.resource);

        applicationContext.close();
    }
}
