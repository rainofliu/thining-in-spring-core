本文基于**Spring Framework 5.2.2.RELEASE**版本，主要参考了 [极客时间 小马哥讲Spring核心编程思想 ](https://time.geekbang.org/course/intro/265) ，谢谢小马哥如此用心且深入浅出的讲解。

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

  > 基于XML的配置，一般指定了字符编码为UTF-8，所以一般不会有乱码问题

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

  > 基于Properties的配置无法指定编码，所以有可能会出现乱码
  
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

`AnnotatedBeanDefinitionReader#register`不仅仅将类解析为`BeanDefinition`，还对`BeanDefinition`进行了注册，从下面的代码中可以看出来

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

### 疑问（TO SOLVE）

为什么会出现这个updatedDefinitions 复制beanDefinitionNames的过程

## BeanDefinition合并阶段

+ BeanDefinition合并
  + 父子BeanDefinition合并
    + 当前BeanFactory查找
    + 层次性BeanFactory查找

```xml
<!-- 1. RootBeanDefinition  不存在parent --> 
<!-- 2. 普通的BeanDefinition  GenericBeanDefinition  -->
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
<!-- 普通的BeanDefinition  GenericBeanDefinition  -->
 <bean id="superUser" class="org.geekbang.thinking.in.spring.ioc.overview.domain.SuperUser" parent="user"
          primary="true">
        <property name="address" value="gy"/>
 </bean>
```

> + User和SuperUser类从Java面向对象角度来看，就是继承与被继承的关系，也就是子类和父类的关系。
> + User BeanDefinition可以作为`RootBeanDefiniton`，同时也可以作为`GenericBeanDefinition`，而SuperUser对应的BeanDefinition只可以作为`GenericBeanDefinition`。

`SuperUser`类对应的`BeanDefinition`需要拥有`User`类对应的`BeanDefinition`中的属性，那Spring是如何满足的呢？

从表面上看，我们配置了**parent="user"**，就可以实现属性的继承，从而可以完成BeanDefinition的合并。

接下来我们基于xml的BeanDefinition配置方式来理解BeanDefinition的合并。



```java
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {
    BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;
}
```

`ConfigurableBeanFactory`有一个`getMergedBeanDefinition`方法，其唯一实现如下：

```java
    // AbstractBeanFactory
    @Override
	public BeanDefinition getMergedBeanDefinition(String name) throws BeansException {
		String beanName = transformedBeanName(name);
        // 递归的方式来获取BeanDefinition
		// Efficiently check whether bean definition exists in this factory.
		if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableBeanFactory) {
			return ((ConfigurableBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(beanName);
		}
		// Resolve merged bean definition locally.
		return getMergedLocalBeanDefinition(beanName);
	}

	/** Map from bean name to merged RootBeanDefinition. */
	private final Map<String, RootBeanDefinition> mergedBeanDefinitions = new ConcurrentHashMap<>(256);

	protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
		// Quick check on the concurrent map first, with minimal locking.
        // 只关注当前BeanFactory
		RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
		if (mbd != null && !mbd.stale) {
			return mbd;
		}
        // 缓存未命中
		return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
	}
	// 缓存未命中
	protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd)
			throws BeanDefinitionStoreException {

		return getMergedBeanDefinition(beanName, bd, null);
	}	

    protected RootBeanDefinition getMergedBeanDefinition(
			String beanName, BeanDefinition bd, @Nullable BeanDefinition containingBd)
			throws BeanDefinitionStoreException {

		synchronized (this.mergedBeanDefinitions) {
			RootBeanDefinition mbd = null;
			RootBeanDefinition previous = null;

			// Check with full lock now in order to enforce the same merged instance.
            // 如果有其他线程修改了mergedBeanDefinitions，此处就有可能获取到RootBeanDefinition
			if (containingBd == null) {
				mbd = this.mergedBeanDefinitions.get(beanName);
			}

			if (mbd == null || mbd.stale) {
				previous = mbd;
				if (bd.getParentName() == null) {
					// Use copy of given root bean definition.
                    // 如果是RootBeanDefinition，就不需要再merge
					if (bd instanceof RootBeanDefinition) {
						mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
					}
					else {
                        // 否则就新建一个RootBeanDefinition，封装原有BeanDefinition
						mbd = new RootBeanDefinition(bd);
					}
				}
                // 需要合并的情况
				else {
					// Child bean definition: needs to be merged with parent.
					BeanDefinition pbd;
					try {
                        // 规范parentBeanName名称
						String parentBeanName = transformedBeanName(bd.getParentName());
						if (!beanName.equals(parentBeanName)) {
							pbd = getMergedBeanDefinition(parentBeanName);
						}
						else {
							BeanFactory parent = getParentBeanFactory();
							if (parent instanceof ConfigurableBeanFactory) {
								pbd = ((ConfigurableBeanFactory) parent).getMergedBeanDefinition(parentBeanName);
							}
							else {
								throw new NoSuchBeanDefinitionException(parentBeanName,
										"Parent name '" + parentBeanName + "' is equal to bean name '" + beanName +
										"': cannot be resolved without an AbstractBeanFactory parent");
							}
						}
					}
					catch (NoSuchBeanDefinitionException ex) {
						throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
								"Could not resolve parent bean definition '" + bd.getParentName() + "'", ex);
					}
					// Deep copy with overridden values.
					mbd = new RootBeanDefinition(pbd);
					mbd.overrideFrom(bd);
				}

				// Set default singleton scope, if not configured before.
				if (!StringUtils.hasLength(mbd.getScope())) {
					mbd.setScope(SCOPE_SINGLETON);
				}

				// A bean contained in a non-singleton bean cannot be a singleton itself.
				// Let's correct this on the fly here, since this might be the result of
				// parent-child merging for the outer bean, in which case the original inner bean
				// definition will not have inherited the merged outer bean's singleton status.
				if (containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()) {
					mbd.setScope(containingBd.getScope());
				}

				// Cache the merged bean definition for the time being
				// (it might still get re-merged later on in order to pick up metadata changes)
				if (containingBd == null && isCacheBeanMetadata()) {
					this.mergedBeanDefinitions.put(beanName, mbd);
				}
			}
			if (previous != null) {
				copyRelevantMergedBeanDefinitionCaches(previous, mbd);
			}
			return mbd;
		}
	}
```

调用链路如下：

+ `AbstractBeanFactory.getMergedBeanDefinition(String name)`
  + `getMergedLocalBeanDefinition(String beanName)`
    + `getMergedBeanDefinition(String beanName, BeanDefinition bd)`
      + `getMergedBeanDefinition(String beanName, BeanDefinition bd, @Nullable BeanDefinition containingBd)`

对于`getMergedBeanDefinition(String beanName, BeanDefinition bd, @Nullable BeanDefinition containingBd)`，我们通过Debug的方式更直观地去理解。

首先先写好代码，注意我们是通过`DefaultListableBeanFactory.getBean`方法来被动启动底层IoC容器的。

```java
 public static void main(String[] args) {
        // 底层IoC容器
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        // 基于xml的BeanDefinitionReader
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

        String location = "META-INF/dependency-lookup.xml";
        // 基于Classpath 加载Properties资源
        Resource resource = new ClassPathResource(location);
        EncodedResource encodedResource = new EncodedResource(resource, "UTF-8");

        int numbers=beanDefinitionReader.loadBeanDefinitions(encodedResource);
        System.out.printf("已加载的BeanDefinition数量:%s\n",numbers);
        User user = beanFactory.getBean("user", User.class);

        System.out.println(user);

        SuperUser superUser = beanFactory.getBean("superUser", SuperUser.class);
        System.out.println(superUser);

   }
```

然后去DEBUG

首先是获取User类对应的mergedBeanDefinition

![](https://liutianruo-2019-go-go-go.oss-cn-shanghai.aliyuncs.com/images/BeanDefinition合并1.png)

这里mergedBeanDefinitions为空，可以断定User类的原始BeanDefinition是GenericBeanDefinition类型，new RootBeanDefinition方法其实就是对原始BeanDefinition的复制

```java
	protected AbstractBeanDefinition(BeanDefinition original) {
		setParentName(original.getParentName());
		setBeanClassName(original.getBeanClassName());
		setScope(original.getScope());
		setAbstract(original.isAbstract());
		setFactoryBeanName(original.getFactoryBeanName());
		setFactoryMethodName(original.getFactoryMethodName());
		setRole(original.getRole());
		setSource(original.getSource());
		copyAttributesFrom(original);

		if (original instanceof AbstractBeanDefinition) {
			AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
			if (originalAbd.hasBeanClass()) {
				setBeanClass(originalAbd.getBeanClass());
			}
			if (originalAbd.hasConstructorArgumentValues()) {
				setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
			}
			if (originalAbd.hasPropertyValues()) {
				setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
			}
			if (originalAbd.hasMethodOverrides()) {
				setMethodOverrides(new MethodOverrides(originalAbd.getMethodOverrides()));
			}
			Boolean lazyInit = originalAbd.getLazyInit();
			if (lazyInit != null) {
				setLazyInit(lazyInit);
			}
			setAutowireMode(originalAbd.getAutowireMode());
			setDependencyCheck(originalAbd.getDependencyCheck());
			setDependsOn(originalAbd.getDependsOn());
			setAutowireCandidate(originalAbd.isAutowireCandidate());
			setPrimary(originalAbd.isPrimary());
			copyQualifiersFrom(originalAbd);
			setInstanceSupplier(originalAbd.getInstanceSupplier());
			setNonPublicAccessAllowed(originalAbd.isNonPublicAccessAllowed());
			setLenientConstructorResolution(originalAbd.isLenientConstructorResolution());
			setInitMethodName(originalAbd.getInitMethodName());
			setEnforceInitMethod(originalAbd.isEnforceInitMethod());
			setDestroyMethodName(originalAbd.getDestroyMethodName());
			setEnforceDestroyMethod(originalAbd.isEnforceDestroyMethod());
			setSynthetic(originalAbd.isSynthetic());
			setResource(originalAbd.getResource());
		}
		else {
			setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
			setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
			setLazyInit(original.isLazyInit());
			setResourceDescription(original.getResourceDescription());
		}
	}
```



然后我们看SuperUser类对应的BeanDefinition ，此时mergedBeanDefinitions已经存在一个了，就是刚刚User类的。

由于SuperUser BeanDefinition id和User BeanDefinition id不同的，所以不会去父类的方法中获取parent BeanDefinition的相关信息。

![](https://liutianruo-2019-go-go-go.oss-cn-shanghai.aliyuncs.com/images/BeanDefinition合并2.png)

![](https://liutianruo-2019-go-go-go.oss-cn-shanghai.aliyuncs.com/images/BeanDefinition合并3.png)

![](https://liutianruo-2019-go-go-go.oss-cn-shanghai.aliyuncs.com/images/BeanDefinition合并4.png)

此时通过mergedBeanDefinitions可以获取到了User类的mergedBeanDefinition

![](https://liutianruo-2019-go-go-go.oss-cn-shanghai.aliyuncs.com/images/BeanDefinition合并5.png)

这一步就是去将父类mergedBeanDefiniton复制过来，然后将自己的属性，比如address属性设置到SuperUser的mergedBeanDefinition中。



**其实我们改变getBean的顺序，Spring处理起来也是差不多的,但它会在SuperUser getBean的时候往mergedBeanDefinitions中加入两个元素，然后在User执行时，又将key为user的BeanDefinition覆盖掉了**

    SuperUser superUser = beanFactory.getBean("superUser", SuperUser.class);`
    User user = beanFactory.getBean("user", User.class);


总的来说，对于User和SuperUser来说，getMergedBeanDefinitions都是一个从`GenericBeanDefinition`到`RootBeanDefinition`的过程。

## Bean Class加载阶段

在Bean被创建之前，Bean Class会被加载。

+ `ClassLoader`加载机制

+ Java Sercurity安全机制

  > Spring中几乎是没有使用它的

+ `ConfigurableBeanFactory` 临时 `ClassLoader`

  > 应用场景比较局限

```java
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {
    ...
    // 注意此时beanClassName是String类型的    
	@Nullable
	String getBeanClassName();
	void setBeanClassName(@Nullable String beanClassName);
}
```

`User user = beanFactory.getBean("user", User.class);`

我们通过上述代码理解一下Bean Class 加载的基本原理

+ `AbstractBeanFactory#getBean(String name, Class<T> requiredType)`
  + `doGetBean(final String name, @Nullable final Class<T> requiredType,@Nullable final Object[] args, boolean typeCheckOnly)`
    + `getMergedLocalBeanDefinition(String beanName)`

User BeanDefinition设置是singleton的

```java
// AbstractBeanFactory#doGetBean				
// Create bean instance.
if (mbd.isSingleton()) {
		sharedInstance = getSingleton(beanName, () -> {
			try {
				return createBean(beanName, mbd, args);
			}
			catch (BeansException ex) {
					// Explicitly remove instance from singleton cache: It might have been put there
					// eagerly by the creation process, to allow for circular reference resolution.
					// Also remove any beans that received a temporary reference to the bean.
					destroySingleton(beanName);
					throw ex;
			}
		});
	bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
}
   // AbstractAutowireCapableBeanFactory
   @Override
	protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
			throws BeanCreationException {

		if (logger.isTraceEnabled()) {
			logger.trace("Creating instance of bean '" + beanName + "'");
		}
		RootBeanDefinition mbdToUse = mbd;

		// Make sure bean class is actually resolved at this point, and
		// clone the bean definition in case of a dynamically resolved Class
		// which cannot be stored in the shared merged bean definition.
        // 重点关注，请看下方代码
		Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
		if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
			mbdToUse = new RootBeanDefinition(mbd);
			mbdToUse.setBeanClass(resolvedClass);
		}

		// Prepare method overrides.
		try {
			mbdToUse.prepareMethodOverrides();
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
					beanName, "Validation of method overrides failed", ex);
		}

		try {
			// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
			Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
			if (bean != null) {
				return bean;
			}
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
					"BeanPostProcessor before instantiation of bean failed", ex);
		}

		try {
			Object beanInstance = doCreateBean(beanName, mbdToUse, args);
			if (logger.isTraceEnabled()) {
				logger.trace("Finished creating instance of bean '" + beanName + "'");
			}
			return beanInstance;
		}
		catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
			// A previously detected exception with proper bean creation context already,
			// or illegal singleton state to be communicated up to DefaultSingletonBeanRegistry.
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
		}
	}
// AbstractBeanFactory
@Nullable
	protected Class<?> resolveBeanClass(final RootBeanDefinition mbd, String beanName, final Class<?>... typesToMatch)
			throws CannotLoadBeanClassException {

		try {
			if (mbd.hasBeanClass()) {
				return mbd.getBeanClass();
			}
			if (System.getSecurityManager() != null) {
				return AccessController.doPrivileged((PrivilegedExceptionAction<Class<?>>) () ->
					doResolveBeanClass(mbd, typesToMatch), getAccessControlContext());
			}
			else {
				return doResolveBeanClass(mbd, typesToMatch);
			}
		}
		catch (PrivilegedActionException pae) {
			ClassNotFoundException ex = (ClassNotFoundException) pae.getException();
			throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
		}
		catch (ClassNotFoundException ex) {
			throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
		}
		catch (LinkageError err) {
			throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), err);
		}
	}
@Nullable
	private Class<?> doResolveBeanClass(RootBeanDefinition mbd, Class<?>... typesToMatch)
			throws ClassNotFoundException {
		// 获取AppClassLoader
		ClassLoader beanClassLoader = getBeanClassLoader();
		ClassLoader dynamicLoader = beanClassLoader;
		boolean freshResolve = false;

		......
		// 获取String类型的类名
		String className = mbd.getBeanClassName();
		if (className != null) {
			Object evaluated = evaluateBeanDefinitionString(className, mbd);
			if (!className.equals(evaluated)) {
				// A dynamically resolved expression, supported as of 4.2...
				if (evaluated instanceof Class) {
					return (Class<?>) evaluated;
				}
				else if (evaluated instanceof String) {
					className = (String) evaluated;
					freshResolve = true;
				}
				else {
					throw new IllegalStateException("Invalid class name expression result: " + evaluated);
				}
			}
			if (freshResolve) {
				// When resolving against a temporary class loader, exit early in order
				// to avoid storing the resolved Class in the bean definition.
				if (dynamicLoader != null) {
					try {
						return dynamicLoader.loadClass(className);
					}
					catch (ClassNotFoundException ex) {
						if (logger.isTraceEnabled()) {
							logger.trace("Could not load class [" + className + "] from " + dynamicLoader + ": " + ex);
						}
					}
				}
				return ClassUtils.forName(className, dynamicLoader);
			}
		}

		// 重点关注
		return mbd.resolveBeanClass(beanClassLoader);
	}

```

```java
  // AbstractBeanDefiniton	
    @Nullable
	private volatile Object beanClass;
 
    @Nullable
	public Class<?> resolveBeanClass(@Nullable ClassLoader classLoader) throws ClassNotFoundException {
		String className = getBeanClassName();
		if (className == null) {
			return null;
		}
        // 通过反射区加载Bean Class
		Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
        // 将beanClass从String类型的全类名 替换成 Class类型
		this.beanClass = resolvedClass;
        // 返回 Bean Class
		return resolvedClass;
	}
	@Override
	@Nullable
	public String getBeanClassName() {
        // beanClass 是String类型
		Object beanClassObject = this.beanClass;
		if (beanClassObject instanceof Class) {
			return ((Class<?>) beanClassObject).getName();
		}
		else {
			return (String) beanClassObject;
		}
	}
```

## Bean实例化前阶段

+ Bean属性赋值(Populate)判断
  + `InstantiationAwareBeanPostProcessor#postProcessAfterInstantiation`

     ```java
    public static void main(String[] args) {
        // 底层IoC容器
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        // 添加 BeanPostProcessor实现
        beanFactory.addBeanPostProcessor(new MyInstantiationAwareBeanPostProcessor());
        // 基于xml的BeanDefinitionReader
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

        String location = "META-INF/dependency-lookup.xml";
        // 基于Classpath 加载Properties资源
        Resource resource = new ClassPathResource(location);
        EncodedResource encodedResource = new EncodedResource(resource, "UTF-8");

        int numbers = beanDefinitionReader.loadBeanDefinitions(encodedResource);
        System.out.printf("已加载的BeanDefinition数量:%s\n", numbers);

        SuperUser superUser = beanFactory.getBean("superUser", SuperUser.class);
        System.out.println(superUser);


        User user = beanFactory.getBean("user", User.class);

        System.out.println(user);

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
    }
     ```

我们Debug的方式运行main方法

+ `AbstractAutowireCapableBeanFactory#createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)`
  + `resolveBeforeInstantiation(beanName, mbdToUse)`
    + `applyBeanPostProcessorsBeforeInstantiation`
      + 调用我们自定义的`InstantiationAwareBeanPostProcessor`实现拦截

```java
@Nullable
	protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
		Object bean = null;
		if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
			// Make sure bean class is actually resolved at this point.
			if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
				Class<?> targetType = determineTargetType(beanName, mbd);
				if (targetType != null) {
                    // 会调用BeanPostProcessor
					bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
					if (bean != null) {
						bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
					}
				}
			}
			mbd.beforeInstantiationResolved = (bean != null);
		}
		return bean;
	}
@Nullable
	protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
		for (BeanPostProcessor bp : getBeanPostProcessors()) {
			if (bp instanceof InstantiationAwareBeanPostProcessor) {
				InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                // 这里会执行我们定义的InstantiationAwareBeanPostProcessor实现类
				Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
```

我们拦截了SuperUser，直接返回一个Bean，那么此时就替换了原有创建Bean的方法。

```java
      // AbstractAutowireCapableBeanFactory#createBean
      // 省略部分代码
	  Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
	 if (bean != null) {
		return bean;
     ......
```

## Bean实例化阶段

+ 实例化方式
  + 传统实例化方式
    + 实例化策略  —InstantiationStrategy
  + 构造器依赖注入