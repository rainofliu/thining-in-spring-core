[toc]

# `Environment`

## 依赖注入`Environment`

1. 直接依赖注入
   1. 通过`EnvironmentAware`接口回调
   2. 通过`@Autowired`注入`Environment`
2. 间接依赖注入
   1. 通过`ApplicationContextAware`接口回调
   2. 通过`@Autowired`注入`ApplicationContext`

```java
public class InjectEnvironmentDemo implements EnvironmentAware {

    private Environment environment;

    @Autowired
    private Environment environment2;

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        context.register(InjectEnvironmentDemo.class);

        context.refresh();

        InjectEnvironmentDemo injectEnvironmentDemo = context.getBean(InjectEnvironmentDemo.class);

        System.out.println(injectEnvironmentDemo.environment==injectEnvironmentDemo.environment2);

        ConfigurableEnvironment contextEnvironment = context.getEnvironment();

        System.out.println(contextEnvironment==injectEnvironmentDemo.environment);

        context.close();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
```

> true
> true

## 依赖查找`Environment`

1. 直接依赖查找

   > `org.springframework.context.ConfigurableApplicationContext#ENVIRONMENT_BEAN_NAME`

2. 间接依赖查找

   > `ApplicationContext#getEnvironment`

```java
public class LookupEnvironmentDemo implements EnvironmentAware {

    private Environment environment;

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        context.register(LookupEnvironmentDemo.class);

        context.refresh();

        LookupEnvironmentDemo lookupEnvironmentDemo = context.getBean(LookupEnvironmentDemo.class);

        Environment environment = context.getBean(ConfigurableApplicationContext.ENVIRONMENT_BEAN_NAME,
            Environment.class);

        ConfigurableEnvironment contextEnvironment = context.getEnvironment();
		// true
        System.out.println(environment == contextEnvironment);
		// true
        System.out.println(lookupEnvironmentDemo.environment==contextEnvironment);

        context.close();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
```

> 可以看到依赖查找和依赖注入的`Environment`对象是同一个

# `@Value`

## 依赖注入`@Value`

```java
public class ValueAnnotationDemo {

    @Value("${user.name}")
    private String userName;

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        context.register(ValueAnnotationDemo.class);

        context.refresh();

        ValueAnnotationDemo valueAnnotationDemo = context.getBean(ValueAnnotationDemo.class);
		// ajin
        System.out.println(valueAnnotationDemo.userName);

        context.close();
    }
}
```

我们列举一下`@Value`的依赖注入过程:

+ `AbstractAutowireCapableBeanFactory#populateBean`
  + `AutowiredAnnotationBeanPostProcessor#postProcessProperties`
    + `InjectionMetadata#inject`
      + `AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement#inject`
        + `DefaultListableBeanFactory#resolveDependency`
          + `DefaultListableBeanFactory#doResolveDependency`

```java
@Nullable
	public Object doResolveDependency(DependencyDescriptor descriptor, @Nullable String beanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {

		InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
		try {
			Object shortcut = descriptor.resolveShortcut(this);
			if (shortcut != null) {
				return shortcut;
			}

			Class<?> type = descriptor.getDependencyType();
            // 获取@Value的value属性
			Object value = getAutowireCandidateResolver().getSuggestedValue(descriptor);
			if (value != null) {
				if (value instanceof String) {
                    // 将value属性解析为值
					String strVal = resolveEmbeddedValue((String) value);
					BeanDefinition bd = (beanName != null && containsBean(beanName) ?
							getMergedBeanDefinition(beanName) : null);
					value = evaluateBeanDefinitionString(strVal, bd);
				}
				TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
				try {
					return converter.convertIfNecessary(value, type, descriptor.getTypeDescriptor());
				}
				catch (UnsupportedOperationException ex) {
					// A custom TypeConverter which does not support TypeDescriptor resolution...
					return (descriptor.getField() != null ?
							converter.convertIfNecessary(value, type, descriptor.getField()) :
							converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
				}
			}

			... 
	}
```

# 配置属性源 –`PropertySource`

```java
public class EnvironmentChangeDemo {

    @Value("${user.name}")
    private String userName;

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        context.register(EnvironmentChangeDemo.class);

        // 在Spring应用上下文启动前 ，调整 Environment 的 PropertySource
        // 获取MutablePropertySources
        ConfigurableEnvironment environment = context.getEnvironment();

        MutablePropertySources propertySources = environment.getPropertySources();

        // 动态插入PropertySource到MutablePropertySources中
        Map<String, Object> source = new HashMap<>(16);
        source.put("user.name", "刘天若");
        MapPropertySource mapPropertySource = new MapPropertySource("first-property-source", source);
        propertySources.addFirst(mapPropertySource);

        // 启动Spring应用上下文
        context.refresh();

        source.put("user.name","007");

        // 依赖查找
        EnvironmentChangeDemo environmentChangeDemo = context.getBean(EnvironmentChangeDemo.class);

        System.out.println(environmentChangeDemo.userName);

        for (PropertySource propertySource : propertySources) {
            System.out.println(propertySource.getProperty("user.name"));
        }

        context.close();
    }
}
```

