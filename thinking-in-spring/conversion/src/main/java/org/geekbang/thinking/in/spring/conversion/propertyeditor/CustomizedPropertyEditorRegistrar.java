package org.geekbang.thinking.in.spring.conversion.propertyeditor;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.stereotype.Component;

/**
 * 自定义{@link PropertyEditorRegistrar}实现
 *
 * @author ajin
 */
@Component // 将其声明为一个Bean
public class CustomizedPropertyEditorRegistrar implements PropertyEditorRegistrar {

    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        // 1. 通用类型转换
        // 2. Java Bean属性 类型转换
        registry.registerCustomEditor(User.class, "context", new StringToPropertiesPropertyEditor());

    }
}
