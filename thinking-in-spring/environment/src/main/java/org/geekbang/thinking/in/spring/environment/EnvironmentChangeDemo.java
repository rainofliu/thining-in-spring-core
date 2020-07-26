package org.geekbang.thinking.in.spring.environment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.*;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link Environment}配置属性源变更Demo
 *
 * @author ajin
 */

public class EnvironmentChangeDemo {

    @Value("${user.name}")
    private String userName;

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        context.register(EnvironmentChangeDemo.class);

        // 在Spring应用上下文启动前 ，调整 Environment 的 PropertySource
        // 获取MutablePropertySources
        ConfigurableEnvironment environment = context.getEnvironment();

        MutablePropertySources propertySources = environment.getPropertySources();

        // 动态插入PropertySource到MutablePropertySources中
        Map<String, Object> source = new HashMap<>(16);
        source.put("user.name", "刘天若");
        MapPropertySource mapPropertySource = new MapPropertySource("first-property-source", source);
        propertySources.addFirst(mapPropertySource);

        // 启动Spring应用上下文
        context.refresh();

        source.put("user.name","007");

        // 依赖查找
        EnvironmentChangeDemo environmentChangeDemo = context.getBean(EnvironmentChangeDemo.class);

        System.out.println(environmentChangeDemo.userName);

        for (PropertySource propertySource : propertySources) {
            System.out.println(propertySource.getProperty("user.name"));
        }

        context.close();
    }
}
