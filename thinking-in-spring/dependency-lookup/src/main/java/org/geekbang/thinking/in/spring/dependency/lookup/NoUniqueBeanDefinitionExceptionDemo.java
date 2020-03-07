package org.geekbang.thinking.in.spring.dependency.lookup;

import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * {@link NoUniqueBeanDefinitionException} Demo
 *
 * @author ajin
 */

public class NoUniqueBeanDefinitionExceptionDemo {

    public static void main(String[] args) {
        // 创建注解驱动的Spring应用上下文
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        // 将NoUniqueBeanDefinitionExceptionDemo 作为 Configuration Class
        applicationContext.register(NoUniqueBeanDefinitionExceptionDemo.class);
        // 启动Spring应用上下文
        applicationContext.refresh();

        try {
            // 由于Spring应用上下文存在两个String 类型的Bean ,所以根据单一类型依赖查找会抛出异常
            applicationContext.getBean(String.class);
        } catch (NoUniqueBeanDefinitionException e) {
            System.out.printf("当前Spring应用上下文存在 %d 个%s类型的Bean,具体原因:%s\n", e.getNumberOfBeansFound(),
                    String.class.getSimpleName(), e.getMessage());
        }
        applicationContext.close();
    }

    @Bean
    public String bean1() {
        return "bean1";
    }

    @Bean
    public String bean2() {
        return "bean2";
    }
}
