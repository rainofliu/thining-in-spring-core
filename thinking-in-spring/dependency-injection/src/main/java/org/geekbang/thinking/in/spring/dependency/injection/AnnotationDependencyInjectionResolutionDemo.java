package org.geekbang.thinking.in.spring.dependency.injection;

import org.geekbang.thinking.in.spring.dependency.injection.annotation.InjectedUser;
import org.geekbang.thinking.in.spring.dependency.injection.annotation.MyAutowired;
import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.springframework.context.annotation.AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME;

/**
 * 注解驱动的依赖注入处理过程
 *
 * @author ajin
 */
@SuppressWarnings("all")
public class AnnotationDependencyInjectionResolutionDemo {

    // DependencyDescriptor ->
    // 必须(required=true)
    // 实时注入 (eager=true)
    // 通过类型（User.class)（处理）
    // 字段名称
    // primary=true
    @Autowired // 依赖查找
    private User user;

//    @Autowired
//    private Collection<User> users;

//    @Autowired
//    private Map<String,User> userMap;

    @MyAutowired
    private Optional<User> userOptional;

    @Inject
    private User injectUser;

    @InjectedUser
    private User myUser;

    /**
     * 通过{@link AutowiredAnnotationBeanPostProcessor} 自定义依赖注入
     */
    @Bean(name = AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)
    public static AutowiredAnnotationBeanPostProcessor beanPostProcessor() {
        AutowiredAnnotationBeanPostProcessor beanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
        Set<Class<? extends Annotation>> autowiredAnnotationTypes =
                new LinkedHashSet<>(asList(Autowired.class, Inject.class, InjectedUser.class));
        // 替换原有注解处理，使用新注解@InjectedUser
        beanPostProcessor.setAutowiredAnnotationTypes(autowiredAnnotationTypes);
        return beanPostProcessor;
    }


    public static void main(String[] args) {

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        // 注册Configuration Class (配置类) -> Bean
        applicationContext.register(AnnotationDependencyInjectionResolutionDemo.class);

        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(applicationContext);

        String xmlResourcePath = "classpath:/META-INF/dependency-lookup.xml";

        // 加载Xml资源，解析并生成Spring BeanDefinition
        beanDefinitionReader.loadBeanDefinitions(xmlResourcePath);

        // 启动 Spring应用上下文
        applicationContext.refresh();

        AnnotationDependencyInjectionResolutionDemo demo = applicationContext.getBean(AnnotationDependencyInjectionResolutionDemo.class);

        System.out.println("demo.user=" + demo.user);
        System.out.println("demo.injectUser=" + demo.injectUser);
        System.out.println("demo.myUser=" + demo.myUser);

        applicationContext.close();

    }
}
