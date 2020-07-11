package org.geekbang.thinking.in.spring.annotation;

import org.springframework.context.annotation.*;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * {@link Profile} Demo
 *
 * @author ajin
 * @see Profile
 * @see Environment#getActiveProfiles()
 */
@Configuration
public class ProfileDemo {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        context.register(ProfileDemo.class);


        ConfigurableEnvironment environment = context.getEnvironment();
        // 默认 兜底
        environment.setDefaultProfiles("odd");
        environment.addActiveProfile("even");



        context.refresh();

        Integer number = context.getBean("number", Integer.class);
        System.out.println(number);
        context.close();
    }

    /**
     * 奇数
     */
    @Bean(name = "number")
    @Profile("odd")
    public Integer odd() {
        return 1;
    }

    /**
     * 偶数
     */
    @Bean(name = "number")
    // @Profile("even")
    @Conditional({EvenCondition.class})
    public Integer even() {
        return 2;
    }
}
