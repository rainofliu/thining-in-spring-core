# Spring配置元信息(`Configuration Metadata`)

[toc]

## Spring配置元信息基本介绍

配置元信息主要分为：

+ Spring Bean 配置元信息  	 - `BeanDefinition`
+ Spring Bean属性元信息        - `PropertyValues`
+ Spring容器配置元信息
+ Spring外部化配置元信息      - `PropertySource`
+ Spring Profile元信息             - `@Profile`

## Bean配置元信息 `BeanDefinition`

+ `GenericBeanDefinition`

  + 通用型`BeanDefinition`

    > 一般xml配置文件中定义的Bean为`GenericBeanDefinition`

+ `RootBeanDefiniton`

  + 无Parent的`BeanDefinition` 或者合并后的`BeanDefinition`

+ `AnnotatedBeanDefinition`

  + 注解标注的`BeanDefinition`

### `GenericBeanDefinition`

`GenericBeanDefinition`继承自`AbstractBeanDefinition`

```java
public class GenericBeanDefinition extends AbstractBeanDefinition {

	@Nullable
	private String parentName;
    ...
     // 重写BeanDefinition接口中的setParentName方法
    @Override
	public void setParentName(@Nullable String parentName) {
		this.parentName = parentName;
	}

	@Override
	@Nullable
	public String getParentName() {
		return this.parentName;
	}    
} 
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {
 
	void setParentName(@Nullable String parentName);
	@Nullable
	String getParentName();
}
```

### 	`RootBeanDefinition`

> 我们可以通过API的方式直接创建`RootBeanDefinition`，也可以定义`GenericBeanDefinition`，其被merge之后会生成`RootBeanDefinition`

```java
public class RootBeanDefinition extends AbstractBeanDefinition {
    
    @Override
	public String getParentName() {
		return null;
	}
	// 因为RootBeanDefinition不存在Parent ,所以这里会抛出异常
	@Override
	public void setParentName(@Nullable String parentName) {
		if (parentName != null) {
			throw new IllegalArgumentException("Root bean cannot be changed into a child bean with parent reference");
		}
	}
}
```
### `AnnotatedBeanDefinition`

子类如下：

+ `AnnotatedGenericBeanDefinition`
+ `ConfigurationClassBeanDefinitionReader.ConfigurationClassBeanDefinition`
+ `ScannedGenericBeanDefinition`

```java
public interface AnnotatedBeanDefinition extends BeanDefinition {

	/**
	 * Obtain the annotation metadata (as well as basic class metadata)
	 * for this bean definition's bean class.
	 * @return the annotation metadata object (never {@code null})
	 */
	AnnotationMetadata getMetadata();

	/**
	 * Obtain metadata for this bean definition's factory method, if any.
	 * @return the factory method metadata, or {@code null} if none
	 * @since 4.1.1
	 */
	@Nullable
	MethodMetadata getFactoryMethodMetadata();

}
```

`AnnotationMetadata`子类

+ `StandardAnnotationMetadata`   基于Java反射
+ `AnnotationMetadataReadingVisitor`     基于asm字节码操作
+ `StandardAnnotationMetadata`

## Bean属性元信息

```java
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {
    ...
}
```

> 可见`BeanDefinition`继承了`AttributeAccessor`、`BeanMetadataElement`接口，并重写了其中的方法

### `PropertyValues`

+ 可修改实现   `MutablePropertyValues`
+ 元素成员   `PropertyValue`

### Bean属性上下文存储   –`AttributeAccesor`

> 对于Bean实例化、属性赋值以及初始化阶段没有影响，只是辅助的元信息
>
> ```java
> public interface AttributeAccessor {
> 	void setAttribute(String name, @Nullable Object value);
> 
> 	@Nullable
> 	Object getAttribute(String name);
>     
> 	@Nullable
> 	Object removeAttribute(String name);
> 
> 	boolean hasAttribute(String name);
> 
> 	String[] attributeNames();
> 
> }
> 
> ```

### Bean元信息元素     --`BeanMetadataElement`

> 对于Bean实例化、属性赋值以及初始化阶段没有影响，只是辅助的元信息
>
> ```java
> public interface BeanMetadataElement {
> 	@Nullable
> 	default Object getSource() {
> 		return null;
> 	}
> }
> ```

```java
  // 构建BeanDefinition
BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(User.class);
        beanDefinitionBuilder.addPropertyValue("name", "liutianruo");
        // 获取AbstractBeanDefinition
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        // 附加属性(其本身不影响Bean的实例化、属性赋值和初始化阶段）
        beanDefinition.setAttribute("name", "ajin");
        // 当前BeanDefinition来自于何方
        beanDefinition.setSource(BeanConfigurationMetadataDemo.class);

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.addBeanPostProcessor(new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                // User Bean初始化后阶段
                if (ObjectUtils.nullSafeEquals("user", beanName) && User.class.equals(bean.getClass())) {
                    BeanDefinition rmd = beanFactory.getBeanDefinition(beanName);
                    if (BeanConfigurationMetadataDemo.class.equals(rmd.getSource())) {
                        // 属性上下文
                        String name = (String) rmd.getAttribute("name"); // ajin
                        User user = (User) bean;
                        user.setName(name);
                    }

                }
                return bean;
            }
        });
        // 注册User的BeanDefinition
        beanFactory.registerBeanDefinition("user", beanDefinition);

        // 获取User Bean
        User user = beanFactory.getBean(User.class);
        System.out.println(user);

    }
```

控制台输出如下:

> User{id=null, name='ajin', city=null, configLocation=null, workCities=null, lifeCities=null}

可以看见我们通过`AttributeAccessor`和`BeanMetadateElement`一起将User Bean的name属性修改了

## 容器配置元信息

### Spring XML配置元信息  -beans元素相关

| beans元素属性     | 默认值       | 使用场景              |
| :---------------- | ------------ | --------------------- |
| profile           | null（留空） | Spring Profiles配置值 |
| default-lazy-init | default      |     当 outter beans “default-lazy-init” 属性存在 时，继承该值，否则为“false”                  |
|     default-merge              |   default           | 当 outter beans “default-merge” 属性存在时， 继承该值，否则为“false”                      |
|      default-autowire             |    default          |   当 outter beans “default-autowire” 属性存 在时，继承该值，否则为“no”                    |
|   default-autowire-candidates                | null（留空）             |      默认 Spring Beans 名称 pattern                 |
|   default-init-method                | null（留空）             |  默认 Spring Beans 自定义初始化方法                     |
|     default-destroy-method              |null（留空）              |默认 Spring Beans 自定义销毁方法                      |

> 这里针对的是<beans>标签下所有的bean（包含嵌套xml中的bean）而不是单个bean

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd" >
    <!-- 通过导入复用dependency-lookup.xml -->
    <import resource="classpath:/META-INF/dependency-lookup.xml"/>

    <bean class="org.geekbang.thinking.in.spring.dependency.injection.UserHolder">
        <!-- Setter 手动输入 -->
        <property name="user" ref="user"/>
    </bean>
</beans>
```

针对上面表格总结内容找到了相应的代码:

#### `BeanDefinitionParserDelegate#populateDefaults`

```java
// 一些属性场景定义
	public static final String DEFAULT_LAZY_INIT_ATTRIBUTE = "default-lazy-init";

	public static final String DEFAULT_MERGE_ATTRIBUTE = "default-merge";

	public static final String DEFAULT_AUTOWIRE_ATTRIBUTE = "default-autowire";

	public static final String DEFAULT_AUTOWIRE_CANDIDATES_ATTRIBUTE = "default-autowire-candidates";

	public static final String DEFAULT_INIT_METHOD_ATTRIBUTE = "default-init-method";

	public static final String DEFAULT_DESTROY_METHOD_ATTRIBUTE = "default-destroy-method";

protected void populateDefaults(DocumentDefaultsDefinition defaults, @Nullable DocumentDefaultsDefinition parentDefaults, Element root) {
		String lazyInit = root.getAttribute(DEFAULT_LAZY_INIT_ATTRIBUTE);
		if (isDefaultValue(lazyInit)) {
			// Potentially inherited from outer <beans> sections, otherwise falling back to false.
			lazyInit = (parentDefaults != null ? parentDefaults.getLazyInit() : FALSE_VALUE);
		}
		defaults.setLazyInit(lazyInit);

		String merge = root.getAttribute(DEFAULT_MERGE_ATTRIBUTE);
		if (isDefaultValue(merge)) {
			// Potentially inherited from outer <beans> sections, otherwise falling back to false.
			merge = (parentDefaults != null ? parentDefaults.getMerge() : FALSE_VALUE);
		}
		defaults.setMerge(merge);

		String autowire = root.getAttribute(DEFAULT_AUTOWIRE_ATTRIBUTE);
		if (isDefaultValue(autowire)) {
			// Potentially inherited from outer <beans> sections, otherwise falling back to 'no'.
			autowire = (parentDefaults != null ? parentDefaults.getAutowire() : AUTOWIRE_NO_VALUE);
		}
		defaults.setAutowire(autowire);

		if (root.hasAttribute(DEFAULT_AUTOWIRE_CANDIDATES_ATTRIBUTE)) {
			defaults.setAutowireCandidates(root.getAttribute(DEFAULT_AUTOWIRE_CANDIDATES_ATTRIBUTE));
		}
		else if (parentDefaults != null) {
			defaults.setAutowireCandidates(parentDefaults.getAutowireCandidates());
		}

		if (root.hasAttribute(DEFAULT_INIT_METHOD_ATTRIBUTE)) {
			defaults.setInitMethod(root.getAttribute(DEFAULT_INIT_METHOD_ATTRIBUTE));
		}
		else if (parentDefaults != null) {
			defaults.setInitMethod(parentDefaults.getInitMethod());
		}

		if (root.hasAttribute(DEFAULT_DESTROY_METHOD_ATTRIBUTE)) {
			defaults.setDestroyMethod(root.getAttribute(DEFAULT_DESTROY_METHOD_ATTRIBUTE));
		}
		else if (parentDefaults != null) {
			defaults.setDestroyMethod(parentDefaults.getDestroyMethod());
		}	defaults.setSource(this.readerContext.extractSource(root));
	}
```



### Spring XML配置元信息   –应用上下文相关

| XML元素 | 使用场景 |
| ------- | -------- |
| <context:annotation-config />       |   激活 Spring 注解驱动       |
|    <context:component-scan /> since2.5    |    Spring `@Component` 以及自定义注解扫描    |
|     <context:load-time-weaver />    |    激活 Spring `LoadTimeWeaver`    |
|    <context:mbean-export />     |     暴露 Spring Beans 作为 JMX Beans     |
|    <context:mbean-server />     | 将当前平台作为 MBeanServer         |
|   <context:property-placeholder />      |  加载外部化配置资源作为 Spring 属性配置        |
|    <context:property-override />     |       利用外部化配置资源覆盖 Spring 属性值   |

## 基于XML资源装载Spring Bean配置元信息

+ Spring Bean配置元信息

| XML元素          | 使用场景                                  |
| ---------------- | ----------------------------------------- |
| <beans:beans />  | 单个XML资源下的多个Spring Bean配置        |
| <beans: bean  /> | 单个Spring Bean定义(BeanDefinition)配置   |
| <beans: alias /> | 为Spring Bean定义(BeanDefinition)映射别名 |
| <beans:import /> | 加载外部Spring XML配置资源                |

### 底层实现 --`XmlBeanDefinitionReader`

1. 首先配置xml文件

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:context="http://www.springframework.org/schema/context"
   
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://www.springframework.org/schema/beans
           https://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context
           https://www.springframework.org/schema/beans/spring-context.xsd" profile="dev.test">
   
       <!--<context:annotation-config/>-->
       <!--<context:component-scan base-package="org.apache"/>-->
   
       <bean id="user" class="org.geekbang.thinking.in.spring.ioc.overview.domain.User">
           <property name="id" value="1"/>
           <property name="name" value="ajin"/>
           <property name="city" value="HANGZHOU"/>
           <property name="configLocation" value="classpath:META-INF/user.properties"/>
           <property name="workCities" value="BEIJING,HANGZHOU"/>
           <property name="lifeCities">
               <list>
                   <value>BEIJING</value>
                   <value>NANJING</value>
               </list>
           </property>
       </bean>
   
       <bean id="superUser" class="org.geekbang.thinking.in.spring.ioc.overview.domain.SuperUser" parent="user"
             primary="true">
           <property name="address" value="gy"/>
       </bean>
       <bean id="objectFactory" class="org.springframework.beans.factory.config.ObjectFactoryCreatingFactoryBean">
           <property name="targetBeanName" value="user"/>
       </bean>
   </beans>
   ```

   

2. 编写Java代码

   ```java
     // 底层IoC容器
           DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
           // 添加 BeanPostProcessor实现
           beanFactory.addBeanPostProcessor(new MyInstantiationAwareBeanPostProcessor());
           // 基于xml的BeanDefinitionReader
           XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
   
           String[] locations = {"META-INF/bean-consctructor-dependency-injection.xml","META-INF/dependency-lookup.xml"};
   
   
           int numbers = beanDefinitionReader.loadBeanDefinitions(locations);
   ```

```java
protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
			throws BeanDefinitionStoreException {

		try {
            // 解析xml文件，返回Document
			Document doc = doLoadDocument(inputSource, resource);	
            // 注册BeanDefinition
			int count = registerBeanDefinitions(doc, resource);
		
			return count;
		}

	}
public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
    // 创建BeanDefinitionDocumentReader
		BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
    // 获取注册前的beanDefinition数量
		int countBefore = getRegistry().getBeanDefinitionCount();
    // 使用BeanDefinitionDocumentReader注册BeanDefinitions
		documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
    // 返回此次注册的BeanDefinition的数量
		return getRegistry().getBeanDefinitionCount() - countBefore;
	}
```

![](https://liutianruo-2019-go-go-go.oss-cn-shanghai.aliyuncs.com/images/123_00.png)

![](https://liutianruo-2019-go-go-go.oss-cn-shanghai.aliyuncs.com/images/123_01.png)

## 基于Properties资源装载Spring Bean配置元信息

略，不是重点`PropertiesBeanDefinitionReader#loadBeanDefinitions`

本质上是对配置的key value解析成`BeanDefinition` , 再对`BeanDefinition`进行注册

## 基于Java注解装载Spring Bean配置元信息

### Spring模式注解

| Spring 注解    | 场景说明         | 起始版本 |
| -------------- | ---------------- | -------- |
| @Repository    | 数据仓储模式注解 | 2.0      |
| @Component     | 通用组件模式注解 | 2.5      |
| @Service       | 服务模式注解     | 2.5      |
| @Controller    | 控制器模式注解   | 2.5      |
| @Configuration | 配置类模式注解   | 3.0      |

### Spring Bean定义注解

![](https://liutianruo-2019-go-go-go.oss-cn-shanghai.aliyuncs.com/images/SpringBean定义相关注解.png)

### Spring Bean依赖注入注解

![](https://liutianruo-2019-go-go-go.oss-cn-shanghai.aliyuncs.com/images/SpringBean依赖注入注解.png)

+ `AutowiredAnnotationBeanPostProcessor`

  ```java
  public AutowiredAnnotationBeanPostProcessor() {
  		this.autowiredAnnotationTypes.add(Autowired.class);
  		this.autowiredAnnotationTypes.add(Value.class);
  		try {
  			this.autowiredAnnotationTypes.add((Class<? extends Annotation>)
  					ClassUtils.forName("javax.inject.Inject", AutowiredAnnotationBeanPostProcessor.class.getClassLoader()));
  		catch (ClassNotFoundException ex) {
  			// JSR-330 API not available - simply skip.
  		}
  	}
  ```

+ `CommonAnnotationBeanPostProcessor`

  ```java
  static {
      ...
  		resourceAnnotationTypes.add(Resource.class);
  		
  	}
  ```

### Spring Bean条件装配注解

![](https://liutianruo-2019-go-go-go.oss-cn-shanghai.aliyuncs.com/images/SpringBean条件装配注解.png)

### Spring Bean生命周期回调注解

![](https://liutianruo-2019-go-go-go.oss-cn-shanghai.aliyuncs.com/images/SpringBean生命周期回调注解.png)



