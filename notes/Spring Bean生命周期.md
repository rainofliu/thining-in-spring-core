本文基于**Spring Framework 5.2.2.RELEASE**版本，主要参考了 [极客时间 小马哥讲Spring核心编程思想 ](https://time.geekbang.org/course/intro/265) 

准备工作：

+ 新建一个maven工程

+ 添加pom依赖

  ```xml
  <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-context</artifactId>
         <version>5.2.2.RELEASE</version>
  </dependency>
  ```

  TODO： 写一篇Spring之前学习的总结

  > + 只有系统化学习才能产生有效的总结
  > + 学会了才叫学习过
  > + 系统学习加上输出总结文档或者学习原理的思考文档

# Spring Bean生命周期理解

## Bean元信息配置阶段

### BeanDefinition 配置

#### 面向资源配置

+ XML配置

  ```java
     // 创建BeanFactory
          DefaultListableBeanFactory defaultListableBeanFactory = new DefaultListableBeanFactory();
  
          XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(defaultListableBeanFactory);
          // XML 配置文件路径 "classpath:META-INF/dependency-lookup.xml"
          String configLocation = "classpath:META-INF/dependency-lookup.xml";
          // 加载配置
          int beanDefinitionCount = reader.loadBeanDefinitions(configLocation);
          System.out.println("Bean 定义加载的数量：" + beanDefinitionCount);
  ```

+ Properties配置

  ```properties
  user.(class)=org.geekbang.thinking.in.spring.ioc.overview.domain.User
  user.id=1001
  user.name=刘天若
  user.city=HANGZHOU
  ```

  > 基于Properties的Bean元信息配置

  ```java
          DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
          // 实例化基于Properties资源的 BeanDefinitionReader
          PropertiesBeanDefinitionReader beanDefinitionReader = new                 PropertiesBeanDefinitionReader(beanFactory);
          String location = "classpath:/META-INF/user.properties";
          // 基于Classpath 加载Properties资源
          Resource resource = new ClassPathResource(location);
          EncodedResource encodedResource = new EncodedResource(resource, "UTF-8");
          // 指定字符编码为UTF-8
          int beanNumbers = beanDefinitionReader.loadBeanDefinitions(encodedResource);
  
          System.out.println("已加载的 BeanDefinition 数量 ：" + beanNumbers);
  
          // 通过Bean Id 和类型 依赖查找
          User user = beanFactory.getBean("user", User.class);
          System.out.println(user.toString());
  ```

#### 面向注解

+ `@Component`

+ `@Configuration`  

  > 比较特殊的`@Component`派生注解

+ `@Configuration`&&`@Bean`

+ `@Service`/`@Repository`/`@Controller` 等`@Component`派生注解

#### 面向API

+ `BeanDefinitionBuilder#genericBeanDefinition`

+ `AbstractBeanDefinition`

  ```java
   public static void main(String[] args) {
          // 1. 通过BeanDefinitionBuilder 创建
          BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(User.class);
          // 通过属性设置
          beanDefinitionBuilder.addPropertyValue("name", "ajin");
          beanDefinitionBuilder.addPropertyValue("age", 1);
          // 获取BeanDefinition实例
          BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
          // BeanDefinition并非Bean的终态，所以可以自定义修改
  
  
          // 2. 通过AbstractBeanDefinition 及派生类
          GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
          // 设置Bean类型
          genericBeanDefinition.setBeanClass(User.class);
          // 通过MutablePropertyValues批量操作属性
          MutablePropertyValues propertyValues = new MutablePropertyValues();
  //        propertyValues.addPropertyValue("name", "ajin");
  //        propertyValues.addPropertyValue("age", 1);
          propertyValues.add("name", "ajin").add("age", 1);
          // 通过set批量操作
          genericBeanDefinition.setPropertyValues(propertyValues);
  
      }
  ```



## Bean元信息解析阶段

> Bean元信息解析就是生成BeanDefinition的过程，而面向API的方式已经直接创建了`BeanDefinition`，无需再解析。

### 面向资源`BeanDefinition`解析

#### `BeanDefinitionReader`

+ `org.springframework.beans.factory.xml.XmlBeanDefinitionReader`
+ `org.springframework.beans.factory.support.PropertiesBeanDefinitionReader`

#### XML解析器 —`BeanDefinitionParser`

### 面向注解`BeanDefinition`解析

#### `AnnotatedBeanDefinitionReader`

```java
DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    // 基于Java注解的 AnnotatedBeanDefinitionReader 实现
    AnnotatedBeanDefinitionReader beanDefinitionReader = new AnnotatedBeanDefinitionReader(beanFactory);
    int beanDefinitionCountBefore = beanFactory.getBeanDefinitionCount();
    // 注册当前类（非Component Class）
    beanDefinitionReader.register(AnnotatedBeanDefinitionParsingDemo.class);
    int beanDefinitionCountAfter = beanFactory.getBeanDefinitionCount();
    int beanDefinitionCount = beanDefinitionCountAfter - beanDefinitionCountBefore;
    System.out.println("已加载的 BeanDefinition 数量 : " + beanDefinitionCount);
    // 普通的Class类作为Component注册Spring IoC容器中，通常Bean名称为annotatedBeanDefinitionParsingDemo
    // Bean名称的生成来自于 BeanNameGenerator ，注解实现为AnnotationBeanNameGenerator
    AnnotatedBeanDefinitionParsingDemo demo = beanFactory.getBean("annotatedBeanDefinitionParsingDemo",
            AnnotatedBeanDefinitionParsingDemo.class);
    System.out.println(demo);
```

##### `AnnotatedBeanDefinitionReader#register`

`AnnotatedBeanDefinitionReader#register`不仅仅将类解析为`BeanDefinition`，还对BeanDefinition进行了注册，从下面的代码中可以看出来

```java
//`AnnotatedBeanDefinitionReader 	
public void register(Class<?>... componentClasses) {
	for (Class<?> componentClass : componentClasses) {
	  registerBean(componentClass);
	}
}
public void registerBean(Class<?> beanClass) {
	doRegisterBean(beanClass, null, null, null, null);
}
private <T> void doRegisterBean(Class<T> beanClass, @Nullable String name,
			@Nullable Class<? extends Annotation>[] qualifiers, @Nullable Supplier<T> supplier,
			@Nullable BeanDefinitionCustomizer[] customizers) {
		// 根据beanClass解析成AnnotatedGenericBeanDefinition
		AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
		if (this.conditionEvaluator.shouldSkip(abd.getMetadata())) {
			return;
		}

		abd.setInstanceSupplier(supplier);
		ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
		abd.setScope(scopeMetadata.getScopeName());
    	// 生成beanName ，其实我们可以通过API的方式替换掉默认的beanNameGenerator
		String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));

		AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
		if (qualifiers != null) {
			for (Class<? extends Annotation> qualifier : qualifiers) {
				if (Primary.class == qualifier) {
					abd.setPrimary(true);
				}
				else if (Lazy.class == qualifier) {
					abd.setLazyInit(true);
				}
				else {
					abd.addQualifier(new AutowireCandidateQualifier(qualifier));
				}
			}
		}
        // 注册Bean之前的回调
		if (customizers != null) {
			for (BeanDefinitionCustomizer customizer : customizers) {
				customizer.customize(abd);
			}
		}

		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
		definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
      // 注册Bean
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
}
```

##### `AnnotationBeanNameGenerator`

```java
// AnnotatedBeanDefinitionReader
private BeanNameGenerator beanNameGenerator = AnnotationBeanNameGenerator.INSTANCE;
// 通过该方法我们可以替换掉默认的beanNameGenerator： AnnotationBeanNameGenerator
public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator =
				(beanNameGenerator != null ? beanNameGenerator : AnnotationBeanNameGenerator.INSTANCE);
}

private <T> void doRegisterBean(Class<T> beanClass, @Nullable String name,
			@Nullable Class<? extends Annotation>[] qualifiers, @Nullable Supplier<T> supplier,
			@Nullable BeanDefinitionCustomizer[] customizers) {
    ......
String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));
    ......
}  
```

> 上面`setBeanNameGenerator()`中的`AnnotationBeanNameGenerator.INSTANCE`是很明显的单例模式，并且是5.2版本以后才有的。
>
> ```java
> public class AnnotationBeanNameGenerator implements BeanNameGenerator {
> 
> 	/**
> 	 * A convenient constant for a default {@code AnnotationBeanNameGenerator} instance,
> 	 * as used for component scanning purposes.
> 	 * @since 5.2
> 	 */
> 	public static final AnnotationBeanNameGenerator INSTANCE = new AnnotationBeanNameGenerator();
>     ...
> }
> ```

## Bean注册阶段

### `BeanDefinition`注册接口——`BeanDefinitionRegistry`

`BeanDefinitionRegistry`唯一实现为：`DefaultListableBeanFactory`

```java
    // DefaultListableBeanFactory 
    @Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {

		Assert.hasText(beanName, "Bean name must not be empty");
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");

		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				((AbstractBeanDefinition) beanDefinition).validate();
			}
			catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
						"Validation of bean definition failed", ex);
			}
		}

		BeanDefinition existingDefinition = this.beanDefinitionMap.get(beanName);
		if (existingDefinition != null) {
            // 看是否允许覆盖BeanDefinition注册
			if (!isAllowBeanDefinitionOverriding()) {
				throw new BeanDefinitionOverrideException(beanName, beanDefinition, existingDefinition);
			}
			else if (existingDefinition.getRole() < beanDefinition.getRole()) {
				// e.g. was ROLE_APPLICATION, now overriding with ROLE_SUPPORT or ROLE_INFRASTRUCTURE
				if (logger.isInfoEnabled()) {
					logger.info("Overriding user-defined bean definition for bean '" + beanName +
							"' with a framework-generated bean definition: replacing [" +
							existingDefinition + "] with [" + beanDefinition + "]");
				}
			}
			else if (!beanDefinition.equals(existingDefinition)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Overriding bean definition for bean '" + beanName +
							"' with a different definition: replacing [" + existingDefinition +
							"] with [" + beanDefinition + "]");
				}
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("Overriding bean definition for bean '" + beanName +
							"' with an equivalent definition: replacing [" + existingDefinition +
							"] with [" + beanDefinition + "]");
				}
			}
			this.beanDefinitionMap.put(beanName, beanDefinition);
		}
		else {
            // 判断此时是否处理Bean创建的阶段，正常情况下会走这个逻辑
			if (hasBeanCreationStarted()) {
				// Cannot modify startup-time collection elements anymore (for stable iteration)
                // 为了确保线程安全 
                // 加锁原因： ArrayList是非线程安全的
				synchronized (this.beanDefinitionMap) {
                    // beanName -> beanDefinition Map
					this.beanDefinitionMap.put(beanName, beanDefinition);
					List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames.size() + 1);
					updatedDefinitions.addAll(this.beanDefinitionNames);
					updatedDefinitions.add(beanName);
                    // 保存beanName的List ，确保BeanDefinition注册的顺序
					this.beanDefinitionNames = updatedDefinitions;
					removeManualSingletonName(beanName);
				}
			}
			else {
				// Still in startup registration phase
				this.beanDefinitionMap.put(beanName, beanDefinition);
				this.beanDefinitionNames.add(beanName);
				removeManualSingletonName(beanName);
			}
			this.frozenBeanDefinitionNames = null;
		}

		if (existingDefinition != null || containsSingleton(beanName)) {
			resetBeanDefinition(beanName);
		}
	}
```

> ```java
> 	/** Map of bean definition objects, keyed by bean name. */
> 	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
> 	/** List of bean definition names, in registration order. */
> 	private volatile List<String> beanDefinitionNames = new ArrayList<>(256);
> ```