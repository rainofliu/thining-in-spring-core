package org.geekbang.thinking.in.spring.generic;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Java泛型 Demo
 *
 * @author ajin
 */

public class GenericDemo {

    public static void main(String[] args) {
        Collection<String> list = new ArrayList<>();

        list.add("hello");
        list.add("world");
//        list.add(1); 编译时错误

        // 泛型擦写
        Collection temp = list;
        // 欺骗编译器 去掉 编译器约束
        // 编译时正确
        temp.add(1);
        // java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.String
//        list.forEach(System.out::println);
        // [hello, world, 1]
        System.out.println(list);
    }
}
