package org.geekbang.thinking.in.spring.environment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * 注入{@link Environment}
 *
 * @author ajin
 */

public class InjectEnvironmentDemo implements EnvironmentAware {

    private Environment environment;

    @Autowired
    private Environment environment2;

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        context.register(InjectEnvironmentDemo.class);

        context.refresh();

        InjectEnvironmentDemo injectEnvironmentDemo = context.getBean(InjectEnvironmentDemo.class);

        System.out.println(injectEnvironmentDemo.environment==injectEnvironmentDemo.environment2);

        ConfigurableEnvironment contextEnvironment = context.getEnvironment();

        System.out.println(contextEnvironment==injectEnvironmentDemo.environment);

        context.close();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
