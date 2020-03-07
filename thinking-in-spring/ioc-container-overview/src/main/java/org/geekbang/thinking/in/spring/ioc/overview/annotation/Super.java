package org.geekbang.thinking.in.spring.ioc.overview.annotation;

import java.lang.annotation.*;

/**
 * 标记 超级
 * @author ajin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Super {
}
