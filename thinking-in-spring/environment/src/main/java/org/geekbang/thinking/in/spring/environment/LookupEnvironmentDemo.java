package org.geekbang.thinking.in.spring.environment;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * 依赖查找{@link Environment} Demo
 *
 * @author ajin
 */

public class LookupEnvironmentDemo implements EnvironmentAware {

    private Environment environment;

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        context.register(LookupEnvironmentDemo.class);

        context.refresh();

        LookupEnvironmentDemo lookupEnvironmentDemo = context.getBean(LookupEnvironmentDemo.class);

        Environment environment = context.getBean(ConfigurableApplicationContext.ENVIRONMENT_BEAN_NAME,
            Environment.class);

        ConfigurableEnvironment contextEnvironment = context.getEnvironment();

        System.out.println(environment == contextEnvironment);

        System.out.println(lookupEnvironmentDemo.environment==contextEnvironment);

        context.close();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
