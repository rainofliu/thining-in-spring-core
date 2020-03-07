package org.geekbang.thinking.in.spring.ioc.bean.scope;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

/**
 * 自定义Scope Demo
 *
 * @author ajin
 * @see ThreadLocalScope
 */
public class ThreadLocalScopeDemo {

    private static User createUser() {
        User user = new User();
        user.setId(System.nanoTime());
        return user;
    }

    @Bean
    @Scope(ThreadLocalScope.SCOPE_NAME)
    public User user(){
       return createUser();
    }

    private static void scopedBeanByLookUp(AnnotationConfigApplicationContext applicationContext) {

        for (int i = 0; i < 3; i++) {
            Thread thread=new Thread(()->{
                // singleton 共享 Bean对象
                User user = applicationContext.getBean("user", User.class);
                System.out.printf("[ThreadId]=%s ,user= %s\n" ,Thread.currentThread().getId(),user);
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


//    private static void scopedBeansByInjection(AnnotationConfigApplicationContext applicationContext) {
//        BeanScopeDemo beanScopeDemo = applicationContext.getBean(BeanScopeDemo.class);
//
//        System.out.println("singletonUser=" + beanScopeDemo.singletonUser);
//        System.out.println("singletonUser1=" + beanScopeDemo.singletonUser1);
//        System.out.println("prototypeUser=" + beanScopeDemo.prototypeUser);
//        System.out.println("prototypeUser1=" + beanScopeDemo.prototypeUser1);
//
//    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(ThreadLocalScopeDemo.class);
        context.addBeanFactoryPostProcessor(beanFactory -> {
            // 注册自定义Scope
            beanFactory.registerScope(ThreadLocalScope.SCOPE_NAME,new ThreadLocalScope());
        });

        context.refresh();

        scopedBeanByLookUp(context);

        context.close();
    }
}
