package org.geekbang.thinking.in.spring.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;

/**
 * 注入{@link ResourceLoader}对象Demo
 *
 * @author ajin
 * @see Resource
 * @see ResourceLoader
 * @see Value
 * @see AnnotationConfigApplicationContext
 */

public class InjectingResourceLoaderDemo implements ResourceLoaderAware {

    @Autowired
    private ResourceLoader autowiredResourceLoader;

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        System.out.println(autowiredResourceLoader==resourceLoader);
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        // @Configuration Class
        context.register(InjectingResourceLoaderDemo.class);
        context.refresh();
        context.close();
    }
}
