package org.geekbang.thinking.in.spring.conversion.propertyeditor;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.beans.PropertyEditor;

/**
 * Spring自定义{@link PropertyEditor} Demo
 *
 * @author ajin
 */
public class SpringCustomizedPropertyEditorDemo {

    public static void main(String[] args) {
        // 创建Spring应用上下文 并启动上下文
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
                "classpath:/META-INF/property-editors-context.xml");

        User user = context.getBean(User.class);
        // User{id=1, name='ajin', city=null, configLocation=null, workCities=null, lifeCities=null, company=null, context={name=ajin, id=1}}
        System.out.println(user);
        // 显式关闭Spring应用上下文
        context.close();
    }
}
