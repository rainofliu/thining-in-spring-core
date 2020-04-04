package org.geekbang.thinking.in.spring.configuration.metadata;

import com.sun.org.apache.xml.internal.utils.NameSpace;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * users.xsd {@link NameSpace }Handler实现
 *
 * @author ajin
 */

public class UsersNameSpaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        // 将user元素注册对应的BeanDefinitionParser
        registerBeanDefinitionParser("user", new UserBeanDefinitionParser());
    }
}
