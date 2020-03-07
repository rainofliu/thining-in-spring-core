package org.geekbang.thinking.in.spring.bean.factory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author ajin
 */

public class DefaultUserFactory implements UserFactory, InitializingBean, DisposableBean {

    // 基于@PostConstruct注解
    @PostConstruct
    public void init() {
        System.out.println("@PostConstruct:UserFactory 初始化中...");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("InitializingBean#afterPropertiesSet:UserFactory 初始化中...");
    }

    public void initUserFactory() {
        System.out.println("自定义初始化方法initUserFactory:UserFactory 初始化中...");
    }


    @PreDestroy
    public void preDestroy() {
        System.out.println("@PreDestroy:UserFactory 销毁中...");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("DisposableBean#destroy:UserFactory 销毁中...");
    }

    public void destroyUserFactory() {
        System.out.println("自定义方法:UserFactory销毁中...");
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("当前DefaultUserFactory 对象正在被回收");
    }
}