package org.geekbang.thinking.in.spring.generic;

import org.springframework.core.GenericTypeResolver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.core.GenericTypeResolver.resolveReturnTypeArgument;

/**
 * {@link GenericTypeResolver} Demo
 *
 * @author ajin
 */

public class GenericTypeResolverDemo {

    public static void main(String[] args) throws NoSuchMethodException {

        displayReturnTypeGenericInfo(GenericTypeResolverDemo.class, Comparable.class, "getString");

        displayReturnTypeGenericInfo(GenericTypeResolverDemo.class, List.class, "getList");

        displayReturnTypeGenericInfo(GenericTypeResolverDemo.class, List.class, "getStringList");


    }


    public static String getString() {
        return null;
    }

    public static <E> List<E> getList() {
        return null;
    }

    /**
     * 泛型参数具体化 （字节码有记录）
     */
    public static List<StringList> getStringList() {
        return null;
    }

    private static void displayReturnTypeGenericInfo(Class<?> containingClass, Class<?> genericClass,
                                                     String methodName, Class... argumentTypes) throws NoSuchMethodException {

        Method method = containingClass.getMethod(methodName);

        Class<?> resolveReturnType = GenericTypeResolver.resolveReturnType(method, GenericTypeResolverDemo.class);

        // 常规类作为方法的返回值
        System.out.printf("GenericTypeResolver.resolveReturnType(%s,%s)= %s \n", methodName,
                containingClass.getSimpleName(), resolveReturnType);

        Class<?> returnTypeArgument = resolveReturnTypeArgument(method, genericClass);
        // 常规类型不具备泛型参数类型 List<E>
        System.out.printf("GenericTypeResolver.resolveReturnTypeArgument(%s,%s) = %s  \n", methodName, containingClass.getSimpleName(),
                returnTypeArgument);


    }


}
