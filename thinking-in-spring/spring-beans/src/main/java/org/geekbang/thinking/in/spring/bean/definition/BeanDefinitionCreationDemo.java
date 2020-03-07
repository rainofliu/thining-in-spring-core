package org.geekbang.thinking.in.spring.bean.definition;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * {@link org.springframework.beans.factory.config.BeanDefinition} 示例
 *
 * @author ajin
 */

public class BeanDefinitionCreationDemo {

    public static void main(String[] args) {
        // 1. 通过BeanDefinitionBuilder 创建
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(User.class);
        // 通过属性设置
        beanDefinitionBuilder.addPropertyValue("name", "ajin");
        beanDefinitionBuilder.addPropertyValue("age", 1);
        // 获取BeanDefinition实例
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        // BeanDefinition并非Bean的终态，所以可以自定义修改


        // 2. 通过AbstractBeanDefinition 及派生类
        GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
        // 设置Bean类型
        genericBeanDefinition.setBeanClass(User.class);
        // 通过MutablePropertyValues批量操作属性
        MutablePropertyValues propertyValues = new MutablePropertyValues();
//        propertyValues.addPropertyValue("name", "ajin");
//        propertyValues.addPropertyValue("age", 1);
        propertyValues.add("name", "ajin").add("age", 1);
        // 通过set批量操作
        genericBeanDefinition.setPropertyValues(propertyValues);

    }
}
