package org.geekbang.thinking.in.spring.dependency.injection;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 基于API实现Constructor方法注入 Demo
 *
 * @author ajin
 */

public class ApiDependencyConstructorInjection {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        // 生成 UserHolder 的BeanDefinition
        BeanDefinition beanDefinition = createUserHolderDefinition();
        // 注册 UserHolder 的BeanDefinition
        applicationContext.registerBeanDefinition("userHolder", beanDefinition);


        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(applicationContext);
        String xmlResourcePath = "classpath:/META-INF/dependency-lookup.xml";

        // 加载Xml资源，解析并生成Spring BeanDefinition
        beanDefinitionReader.loadBeanDefinitions(xmlResourcePath);


        // 启动注解驱动的上下文
        applicationContext.refresh();
        UserHolder userHolder = applicationContext.getBean(UserHolder.class);
        System.out.println(userHolder);

        // 关闭Spring应用上下文
        applicationContext.close();
    }

    /**
     * 为{@link UserHolder} 生成{@link BeanDefinition}
     *
     * @return BeanDefinition
     */
    private static BeanDefinition createUserHolderDefinition() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(UserHolder.class);
        builder.addConstructorArgReference("superUser");
        return builder.getBeanDefinition();
    }
}
