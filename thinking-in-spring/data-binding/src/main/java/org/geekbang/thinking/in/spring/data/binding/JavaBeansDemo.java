package org.geekbang.thinking.in.spring.data.binding;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.stream.Stream;

/**
 * @author ajin
 */

public class JavaBeansDemo {

    public static void main(String[] args) throws IntrospectionException {
        // StopClass 排除截止类
        BeanInfo beanInfo = Introspector.getBeanInfo(User.class,Object.class);
        // 属性描述符
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        //            propertyDescriptor.getReadMethod(); // Getter方法
//            propertyDescriptor.getWriteMethod(); // Setter方法
//        Stream.of(descriptors).forEach(System.out::println);

        Stream.of(beanInfo.getMethodDescriptors()).forEach(System.out::println);
    }
}
