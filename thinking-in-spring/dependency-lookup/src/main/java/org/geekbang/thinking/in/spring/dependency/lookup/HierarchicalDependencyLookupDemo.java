package org.geekbang.thinking.in.spring.dependency.lookup;


import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 层次性依赖查找 Demo
 *
 * @author ajin
 * @see ConfigurableListableBeanFactory 可配置的 , 可列举的 并且具有层次性的{@link org.springframework.beans.factory.BeanFactory}
 * @see ConfigurableBeanFactory
 * @see HierarchicalBeanFactory
 * @see ListableBeanFactory
 */

public class HierarchicalDependencyLookupDemo {

    public static void main(String[] args) {
        // 创建注解驱动的Spring应用上下文
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        // 1. 获取HierarchicalBeanFactory  <--  ConfigurableBeanFactory  <--  ConfigurableListableBeanFactory
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
//        System.out.println("当前 BeanFactory 的 Parent BeanFactory: " + beanFactory.getParentBeanFactory());
        ConfigurableListableBeanFactory parentBeanFactory = createBeanFactory();
        // 2. 设置 Parent BeanFactory
        beanFactory.setParentBeanFactory(parentBeanFactory);
//        System.out.println("当前 BeanFactory 的 Parent BeanFactory: " + beanFactory.getParentBeanFactory());

        displayContainsLocalBean(beanFactory, "user");
        displayContainsLocalBean(parentBeanFactory, "user");

        displayContainsBean(beanFactory, "user");
        displayContainsBean(parentBeanFactory, "user");

        // 启动Spring应用上下文
        applicationContext.refresh();
        // 关闭Spring应用上下文
        applicationContext.close();
    }

    private static void displayContainsBean(HierarchicalBeanFactory beanFactory, String beanName) {
        System.out.printf("当前 BeanFactory [%s]  是否包含 Local bean [name:  %s]   :%s \n", beanFactory, beanName,
                containsBean(beanFactory, beanName));
    }

    private static boolean containsBean(HierarchicalBeanFactory beanFactory, String beanName) {
        BeanFactory parentBeanFactory = beanFactory.getParentBeanFactory();
        // 判断Parent BeanFactory 是否为空 同时判断是否为HierarchicalBeanFactory类型
        if (parentBeanFactory instanceof HierarchicalBeanFactory) {
            HierarchicalBeanFactory parentHierarchicalBeanFactory = HierarchicalBeanFactory.class.cast(parentBeanFactory);
            if (containsBean(parentHierarchicalBeanFactory, beanName)) {
                return true;
            }
        }
        return beanFactory.containsLocalBean(beanName);
    }

    /**
     * 检查当前BeanFactory 本地是否包含某个Bean（仅在当前 BeanFactory中查询)
     */
    private static void displayContainsLocalBean(HierarchicalBeanFactory beanFactory, String beanName) {
        System.out.printf("当前 BeanFactory [%s]  是否包含 Local bean [name:  %s]   :%s \n", beanFactory, beanName,
                beanFactory.containsLocalBean(beanName));
    }

    /**
     * 创建 Parent BeanFactory
     */
    private static ConfigurableListableBeanFactory createBeanFactory() {
        // 创建BeanFactory容器
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        // XML 配置文件路径 "classpath:META-INF/dependency-lookup.xml"
        String configLocation = "classpath:META-INF/dependency-lookup.xml";
        reader.loadBeanDefinitions(configLocation);

        return beanFactory;
    }
}
