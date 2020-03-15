package org.geekbang.thinking.in.spring.bean.lifecycle;

import org.geekbang.thinking.in.spring.ioc.overview.domain.SuperUser;
import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.util.ObjectUtils;

/**
 * Bean 初始化生命周期 阶段
 *
 * @author ajin
 */

public class BeanInitializationLifecycleDemo {

    @SuppressWarnings("all")
    public static void main(String[] args) {
        // 底层IoC容器
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        // 添加 BeanPostProcessor实现
        beanFactory.addBeanPostProcessor(new MyInstantiationAwareBeanPostProcessor());
        // 基于xml的BeanDefinitionReader
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

        String[] locations = {"META-INF/bean-consctructor-dependency-injection.xml","META-INF/dependency-lookup.xml"};


        int numbers = beanDefinitionReader.loadBeanDefinitions(locations);
        System.out.printf("已加载的BeanDefinition数量:%s\n", numbers);

        SuperUser superUser = beanFactory.getBean("superUser", SuperUser.class);
        System.out.println(superUser);


        User user = beanFactory.getBean("user", User.class);
        System.out.println(user);

        UserHolder userHolder = beanFactory.getBean("userHolder", UserHolder.class);
        System.out.println(userHolder);

    }

    static class MyInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

        @Override
        public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
            if (ObjectUtils.nullSafeEquals("superUser", beanName) && SuperUser.class.equals(beanClass)) {
                // 将配置好的完整的SuperUser Bean替换掉
                return new SuperUser();
            }
            return null;// 保持Spring IoC的实例化操作
        }

        @Override
        public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
            if (ObjectUtils.nullSafeEquals("user", beanName) && User.class.equals(bean.getClass())) {
                // user对象不允许属性赋值 （配置元信息-> 属性）
                return false;
            }
            return true;
        }
        // User Bean 跳过Bean属性赋值
        // SuperUser Bean跳过实例化，非主流操作，更加不会有Bean属性赋值的操作了
        // UserHolder会走过来
        @Override
        public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
            if (ObjectUtils.nullSafeEquals("userHolder", beanName) && UserHolder.class.equals(bean.getClass())) {

            }
            return null;
        }
    }
}
