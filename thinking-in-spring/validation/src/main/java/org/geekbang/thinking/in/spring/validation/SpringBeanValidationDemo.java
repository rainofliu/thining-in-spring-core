package org.geekbang.thinking.in.spring.validation;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Spring Bean Validation整合Demo
 *
 * @author ajin
 * @see org.springframework.validation.Validator
 * @see org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
 */

public class SpringBeanValidationDemo {

    public static void main(String[] args) {
        // 启动Spring应用上下文
        ConfigurableApplicationContext context =
                new ClassPathXmlApplicationContext("classpath:/META-INF/bean-validation-context.xml");
        Validator validator = context.getBean(Validator.class);
        System.out.println(validator instanceof LocalValidatorFactoryBean);
        UserProcessor processor = context.getBean(UserProcessor.class);
        processor.processUser(new User());
        // 关闭上下文
        context.close();
    }

    @Component
    @Validated
    static class UserProcessor {
        public void processUser(@Valid User user) {
            System.out.println(user);
        }
    }

    static class User {
        @NotNull
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
