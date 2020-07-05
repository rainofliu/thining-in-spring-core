package org.geekbang.thinking.in.spring.annotation;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * {@link ComponentScan} Demo
 *
 * @author ajin
 */
// 指定classpaths
@ComponentScan(basePackages = "org.geekbang.thinking.in.spring")
public class ComponentScanDemo {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        applicationContext.register(ComponentScanDemo.class);

        applicationContext.refresh();

        applicationContext.close();
    }
}
