package org.geekbang.thinking.in.spring.ioc.overview.repository;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

/**
 * 用户信息仓库
 *
 * @author ajin
 */

public class UserRepository {

    private Collection<User> users; // 自定义Bean

    private BeanFactory beanFactory; // 内建非Bean 对象（依赖）

    private ObjectFactory<ApplicationContext> objectFactory;

    public Collection<User> getUsers() {
        return users;
    }

    public void setUsers(Collection<User> users) {
        this.users = users;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public ObjectFactory<ApplicationContext> getUserObjectFactory() {
        return objectFactory;
    }

    public void setUserObjectFactory(ObjectFactory<ApplicationContext> userObjectFactory) {
        this.objectFactory = userObjectFactory;
    }
}
