package org.geekbang.thinking.in.spring.generic;

import org.springframework.core.ResolvableType;

/**
 * @author ajin
 * @see ResolvableType
 */

public class ResolvableTypeDemo {

    public static void main(String[] args) {
        // 工厂创建
        // StringList -< ArrayList -<  AbstractList
        ResolvableType resolvableType = ResolvableType.forClass(StringList.class);

        resolvableType.getSuperType(); // ArrayList
        resolvableType.getSuperType().getSuperType(); // AbstractList

        // interface java.util.Collection
        System.out.println(resolvableType.asCollection().resolve());
        //  class java.lang.String 泛型参数类型
        System.out.println(resolvableType.asCollection().resolveGeneric(0));

    }
}
