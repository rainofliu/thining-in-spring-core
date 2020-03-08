package org.geekbang.thinking.in.spring.bean.lifecycle;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;

/**
 * 注解BeanDefinition 解析 Demo
 *
 * @author ajin
 */

public class AnnotatedBeanDefinitionParsingDemo {

    public static void main(String[] args) {
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    // 基于Java注解的 AnnotatedBeanDefinitionReader 实现
    AnnotatedBeanDefinitionReader beanDefinitionReader = new AnnotatedBeanDefinitionReader(beanFactory);
    int beanDefinitionCountBefore = beanFactory.getBeanDefinitionCount();
    // 注册当前类（非Component Class）
    beanDefinitionReader.register(AnnotatedBeanDefinitionParsingDemo.class);
    int beanDefinitionCountAfter = beanFactory.getBeanDefinitionCount();
    int beanDefinitionCount = beanDefinitionCountAfter - beanDefinitionCountBefore;
    System.out.println("已加载的 BeanDefinition 数量 : " + beanDefinitionCount);
    // 普通的Class类作为Component注册Spring IoC容器中，通常Bean名称为annotatedBeanDefinitionParsingDemo
    // Bean名称的生成来自于 BeanNameGenerator ，注解实现为AnnotationBeanNameGenerator
    AnnotatedBeanDefinitionParsingDemo demo = beanFactory.getBean("annotatedBeanDefinitionParsingDemo",
            AnnotatedBeanDefinitionParsingDemo.class);
    System.out.println(demo);

}
}
