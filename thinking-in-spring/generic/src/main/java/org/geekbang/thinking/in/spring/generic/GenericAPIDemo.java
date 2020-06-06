package org.geekbang.thinking.in.spring.generic;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Java泛型API Demo
 *
 * @author ajin
 * @see Type
 */

public class GenericAPIDemo {

    public static void main(String[] args) {

        // 原生类型 primitive types: int long
        Class intClass = int.class;

        // 数组类型 array types:int[] Object[]
        Class objectArrayClass = Object[].class;

        // 原始类型 raw types : java.lang.String
        Class rawClass = String.class;

        // 泛型参数类型 java.util.AbstractList<E>
        ParameterizedType parameterizedType = (ParameterizedType) ArrayList.class.getGenericSuperclass();

        // java.util.AbstractList<E>
        System.out.println(parameterizedType.toString());
        // class java.util.AbstractList
        System.out.println(parameterizedType.getRawType());

        // E
        Stream.of(parameterizedType.getActualTypeArguments()).forEach(System.out::println);


    }
}
