package org.geekbang.thinking.in.spring.generic;

import org.springframework.core.GenericCollectionTypeResolver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link GenericCollectionTypeResolver}Demo
 *
 * @author ajin
 * @see GenericCollectionTypeResolver
 */

public class GenericCollectionTypeResolverDemo {

    private StringList stringList;

    private List<String> list;

    public static void main(String[] args) throws Exception {

        // StringList extends ArrayList<String> 具体化
        // 返回具体化泛型参数类型集合 的 成员类型
        Class<?> collectionType = GenericCollectionTypeResolver.getCollectionType(StringList.class);
        // class java.lang.String
        System.out.println(collectionType);
        // null 泛型参数没有具体化 字节码无法获取
        System.out.println(GenericCollectionTypeResolver.getCollectionType(ArrayList.class));


        // 获取字段
        Field field = GenericCollectionTypeResolverDemo.class.getDeclaredField("stringList");
        // class java.lang.String
        System.out.println(GenericCollectionTypeResolver.getCollectionFieldType(field));


        field = GenericCollectionTypeResolverDemo.class.getDeclaredField("list");
        // class java.lang.String
        System.out.println(GenericCollectionTypeResolver.getCollectionFieldType(field));

    }
}
