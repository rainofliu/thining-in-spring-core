package org.geekbang.thinking.in.spring.bean.definition;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Bean的垃圾回收
 *
 * @author ajin
 */

public class BeanGarbageCollectionDemo {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(BeanInitializationDemo.class);
        // 启动Spring应用上下文
        applicationContext.refresh();

        // 关闭Spring应用上下文
        applicationContext.close();
    }
}
