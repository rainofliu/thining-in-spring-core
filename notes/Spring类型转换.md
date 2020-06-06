[toc]

```xml
 <bean id="propertiesToStringConverter"
          class="org.geekbang.thinking.in.spring.conversion.converter.PropertiesToStringConverter"/>
    <!-- 声明 ConversionServiceFactoryBean name 必须为conversionService -->
    <bean id="conversionService" class="org.springframework.context.support.ConversionServiceFactoryBean">
        <property name="converters"
                  ref="propertiesToStringConverter"/>
 </bean>
```

`org.springframework.context.support.AbstractApplicationContext#finishBeanFactoryInitialization`

```java
protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
		// Initialize conversion service for this context.
    // 将我们的ConversionServiceFactoryBean设置给底层的BeanFactory
		if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
				beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
			beanFactory.setConversionService(
					beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
		}

		// Register a default embedded value resolver if no bean post-processor
		// (such as a PropertyPlaceholderConfigurer bean) registered any before:
		// at this point, primarily for resolution in annotation attribute values.
		if (!beanFactory.hasEmbeddedValueResolver()) {
			beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
		}

		// Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
		String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
		for (String weaverAwareName : weaverAwareNames) {
			getBean(weaverAwareName);
		}

		// Stop using the temporary ClassLoader for type matching.
		beanFactory.setTempClassLoader(null);

		// Allow for caching all bean definition metadata, not expecting further changes.
		beanFactory.freezeConfiguration();

		// Instantiate all remaining (non-lazy-init) singletons.
		beanFactory.preInstantiateSingletons();
	}
```

Spring上下文启动过程中  Bean的生命周期：

`org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#instantiateBean`

```java
	protected BeanWrapper instantiateBean(final String beanName, final RootBeanDefinition mbd) {
		try {
			Object beanInstance;
			final BeanFactory parent = this;
			if (System.getSecurityManager() != null) {
				beanInstance = AccessController.doPrivileged((PrivilegedAction<Object>) () ->
						getInstantiationStrategy().instantiate(mbd, beanName, parent),
						getAccessControlContext());
			}
			else {
				beanInstance = getInstantiationStrategy().instantiate(mbd, beanName, parent);
			}
            // 创建BeanWrapperImpl
			BeanWrapper bw = new BeanWrapperImpl(beanInstance);
			initBeanWrapper(bw);
			return bw;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					mbd.getResourceDescription(), beanName, "Instantiation of bean failed", ex);
		}
	}
```

```java
protected void initBeanWrapper(BeanWrapper bw) {
		bw.setConversionService(getConversionService());
		registerCustomEditors(bw);
}
```

> 这里会将`ConversionService`设置给`BeanWrapper`

`BeanWrapper`会利用`TypeConverterDelegate`来处理类型转换

```java
org.springframework.beans.TypeConverterSupport#convertIfNecessary(java.lang.Object, java.lang.Class<T>)
  @Nullable
	@Override
	public <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType,
			@Nullable TypeDescriptor typeDescriptor) throws TypeMismatchException {

		Assert.state(this.typeConverterDelegate != null, "No TypeConverterDelegate");
		try {
			return this.typeConverterDelegate.convertIfNecessary(null, null, value, requiredType, typeDescriptor);
		}
		catch (ConverterNotFoundException | IllegalStateException ex) {
			throw new ConversionNotSupportedException(value, requiredType, ex);
		}
		catch (ConversionException | IllegalArgumentException ex) {
			throw new TypeMismatchException(value, requiredType, ex);
		}
	}
```

