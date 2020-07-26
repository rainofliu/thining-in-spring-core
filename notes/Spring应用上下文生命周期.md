[toc]

# Spring应用上下文生命周期

> `AbstractApplicationContext#refresh`是Spring应用上下文生命周期的入口方法

```java
    @Override
	public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			// Prepare this context for refreshing.
            // Spring应用上下文启动准备阶段
			prepareRefresh();

			// Tell the subclass to refresh the internal bean factory.
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

			// Prepare the bean factory for use in this context.
			prepareBeanFactory(beanFactory);

			try {
				// Allows post-processing of the bean factory in context subclasses.
				postProcessBeanFactory(beanFactory);

				// Invoke factory processors registered as beans in the context.
				invokeBeanFactoryPostProcessors(beanFactory);

				// Register bean processors that intercept bean creation.
				registerBeanPostProcessors(beanFactory);

				// Initialize message source for this context.
				initMessageSource();

				// Initialize event multicaster for this context.
				initApplicationEventMulticaster();

				// Initialize other special beans in specific context subclasses.
				onRefresh();

				// Check for listener beans and register them.
				registerListeners();

				// Instantiate all remaining (non-lazy-init) singletons.
				finishBeanFactoryInitialization(beanFactory);

				// Last step: publish corresponding event.
				finishRefresh();
			}

			catch (BeansException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Exception encountered during context initialization - " +
							"cancelling refresh attempt: " + ex);
				}

				// Destroy already created singletons to avoid dangling resources.
				destroyBeans();

				// Reset 'active' flag.
				cancelRefresh(ex);

				// Propagate exception to caller.
				throw ex;
			}

			finally {
				// Reset common introspection caches in Spring's core, since we
				// might not ever need metadata for singleton beans anymore...
				resetCommonCaches();
			}
		}
	}
```

## Spring应用上下文启动准备阶段

> `AbstractApplicationContext#prepareRefresh`
>
> ```java
> 	/**
> 	 * Prepare this context for refreshing, setting its startup date and
> 	 * active flag as well as performing any initialization of property sources.
> 	 */
> 	protected void prepareRefresh() {
> 		// Switch to active.
> 		this.startupDate = System.currentTimeMillis();
> 		this.closed.set(false);
> 		this.active.set(true);
> 
> 		if (logger.isDebugEnabled()) {
> 			if (logger.isTraceEnabled()) {
> 				logger.trace("Refreshing " + this);
> 			}
> 			else {
> 				logger.debug("Refreshing " + getDisplayName());
> 			}
> 		}
> 
> 		// Initialize any placeholder property sources in the context environment.
>         // 默认是空实现，供子类实现
> 		initPropertySources();
> 
> 		// Validate that all properties marked as required are resolvable:
> 		// see ConfigurablePropertyResolver#setRequiredProperties
> 		getEnvironment().validateRequiredProperties();
> 
> 		// Store pre-refresh ApplicationListeners...
> 		if (this.earlyApplicationListeners == null) {
> 			this.earlyApplicationListeners = new LinkedHashSet<>(this.applicationListeners);
> 		}
> 		else {
> 			// Reset local application listeners to pre-refresh state.
> 			this.applicationListeners.clear();
> 			this.applicationListeners.addAll(this.earlyApplicationListeners);
> 		}
> 
> 		// Allow for the collection of early ApplicationEvents,
> 		// to be published once the multicaster is available...
> 		this.earlyApplicationEvents = new LinkedHashSet<>();
> 	}
> ```

+ 启动时间 startupDate

+ 状态标识

  > this.closed.set(false);
  > this.active.set(true);

+ 初始化`PropertySources`

  > 空方法，供子类实现，通常会提早创建`Environment`

+ 检验`Environment`中必须属性

+ 初始化事件监听器集合

+ 初始化早期Spring事件集合

## `BeanFactory`创建阶段

> `AbstractApplicationContext#obtainFreshBeanFactory`

```java
/**
	 * Tell the subclass to refresh the internal bean factory.
	 * @return the fresh BeanFactory instance
	 * @see #refreshBeanFactory()
	 * @see #getBeanFactory()
	 */
	protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
		refreshBeanFactory();
        // 返回BeanFactory
		return getBeanFactory();
	}
```

`AbstractRefreshableApplicationContext#refreshBeanFactory`

```java
/**
	 * This implementation performs an actual refresh of this context's underlying
	 * bean factory, shutting down the previous bean factory (if any) and
	 * initializing a fresh bean factory for the next phase of the context's lifecycle.
	 */
	@Override
	protected final void refreshBeanFactory() throws BeansException {
        // 判断是否创建了BeanFactory ,是的话 销毁Bean，关闭BeanFactory
		if (hasBeanFactory()) {
			destroyBeans();
			closeBeanFactory();
		}
		try {
            // 创建BeanFactory
			DefaultListableBeanFactory beanFactory = createBeanFactory();
            // 设置id
			beanFactory.setSerializationId(getId());
            // 设置是否允许BeanDefinition 重复定义 , 设置是否允许循环依赖
			customizeBeanFactory(beanFactory);
            // 加载beanDefinition
			loadBeanDefinitions(beanFactory);
			synchronized (this.beanFactoryMonitor) {
				this.beanFactory = beanFactory;
			}
		}
		catch (IOException ex) {
			throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
		}
	}

```

+ `AbstractApplicationContext#obtainFreshBeanFactory`

  + `AbstractRefreshableApplicationContext#refreshBeanFactory`

    + 如果BeanFactory已经创建好了，就关闭或销毁BeanFactory

    + `createBeanFactory()`

    + `beanFactory.setSerializationId`

    + `customizeBeanFactory(beanFactory)`

      ```java
      	protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
              // 设置是否允许BeanDefinition 重复定义
      		if (this.allowBeanDefinitionOverriding != null) {
      			beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
      		}
              // 设置是否允许循环依赖
      		if (this.allowCircularReferences != null) {
      			beanFactory.setAllowCircularReferences(this.allowCircularReferences);
      		}
      	}
      ```

    + `loadBeanDefinitions(beanFactory)`

      > xml 、注解驱动上下文 的具体实现不同

  + `getBeanFactory();`

## `BeanFactory`准备阶段

```java
/**
	 * Configure the factory's standard context characteristics,
	 * such as the context's ClassLoader and post-processors.
	 * @param beanFactory the BeanFactory to configure
	 */
	protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// Tell the internal bean factory to use the context's class loader etc.
        // 关联ClassLoader （给予xml的上下文之前没有加载到bean class）
		beanFactory.setBeanClassLoader(getClassLoader());
        // 设置Bean表达式处理器
		beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
        // 添加PropertyEditorRegistrar（类型转换）实现： ResourceEditorRegistrar
		beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

		// Configure the bean factory with context callbacks.
        // 添加Aware回调接口 BeanPostProcessor实现 ：ApplicationContextAwareProcessor
		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
        
        // 忽略Aware接口作为依赖注入接口
		beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
		beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
		beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
		beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

		// BeanFactory interface not registered as resolvable type in a plain factory.
		// MessageSource registered (and found for autowiring) as a bean.
        
        // 注册ResolvableDependency对象
		beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
		beanFactory.registerResolvableDependency(ResourceLoader.class, this);
		beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
		beanFactory.registerResolvableDependency(ApplicationContext.class, this);

		// Register early post-processor for detecting inner beans as ApplicationListeners.
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

		// Detect a LoadTimeWeaver and prepare for weaving, if found.
		if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
			beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
			// Set a temporary ClassLoader for type matching.
			beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
		}

		// Register default environment beans.
        // 注册单例对象 （environment相关）
		if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
			beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
		}
		if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
			beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
		}
		if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
			beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
		}
	}
```

## `BeanFactory`后置处理阶段

```java
	// Allows post-processing of the bean factory in context subclasses.
// 由子类实现
				postProcessBeanFactory(beanFactory);

				// Invoke factory processors registered as beans in the context.
				invokeBeanFactoryPostProcessors(beanFactory);
```

> `AbstractApplicationContext#refresh`

```java
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
	}

	/**
	 * Instantiate and invoke all registered BeanFactoryPostProcessor beans,
	 * respecting explicit order if given.
	 * <p>Must be called before singleton instantiation.
	 */
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

		// Detect a LoadTimeWeaver and prepare for weaving, if found in the meantime
		// (e.g. through an @Bean method registered by ConfigurationClassPostProcessor)
		if (beanFactory.getTempClassLoader() == null && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
			beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
			beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
		}
	}
```

### `AnnotationConfigServletWebApplicationContext#postProcessBeanFactory`

> Servlet Web 注解驱动上下文的实现

```java
@Override
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {	
        // 调用父类
		super.postProcessBeanFactory(beanFactory);
		if (!ObjectUtils.isEmpty(this.basePackages)) {
			this.scanner.scan(this.basePackages);
		}
		if (!this.annotatedClasses.isEmpty()) {
			this.reader.register(ClassUtils.toClassArray(this.annotatedClasses));
		}
	}
```

#### `1.GenericWebApplicationContext#postProcessBeanFactory` 

```java
/**
	 * Register ServletContextAwareProcessor.
	 * @see ServletContextAwareProcessor
	 */
	@Override
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		if (this.servletContext != null) {
			beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext));
			beanFactory.ignoreDependencyInterface(ServletContextAware.class);
		}
        
		WebApplicationContextUtils.registerWebApplicationScopes(beanFactory, this.servletContext);
        
		WebApplicationContextUtils.registerEnvironmentBeans(beanFactory, this.servletContext);
	}
```

##### `WebApplicationContextUtils#registerWebApplicationScopes(org.springframework.beans.factory.config.ConfigurableListableBeanFactory, javax.servlet.ServletContext)`

```java
public static void registerWebApplicationScopes(ConfigurableListableBeanFactory beanFactory,@Nullable ServletContext sc) {
		
        // 注册Scope
    	// request
		beanFactory.registerScope(WebApplicationContext.SCOPE_REQUEST, new RequestScope());
        // session
		beanFactory.registerScope(WebApplicationContext.SCOPE_SESSION, new SessionScope());
    
		if (sc != null) {
            // application
			ServletContextScope appScope = new ServletContextScope(sc);
			beanFactory.registerScope(WebApplicationContext.SCOPE_APPLICATION, appScope);
			// Register as ServletContext attribute, for ContextCleanupListener to detect it.	
            // 设置ServletContext Attribute
			sc.setAttribute(ServletContextScope.class.getName(), appScope);
		}
		
    // 注册ResolvableDependency
		beanFactory.registerResolvableDependency(ServletRequest.class, new RequestObjectFactory());
		beanFactory.registerResolvableDependency(ServletResponse.class, new ResponseObjectFactory());
		beanFactory.registerResolvableDependency(HttpSession.class, new SessionObjectFactory());
		beanFactory.registerResolvableDependency(WebRequest.class, new WebRequestObjectFactory());
		if (jsfPresent) {
			FacesDependencyRegistrar.registerFacesDependencies(beanFactory);
		}
	}
```

##### `WebApplicationContextUtils.registerEnvironmentBeans`

> 注册Servlet相关的Bean

```java
/**
	 * Register web-specific environment beans ("contextParameters", "contextAttributes")
	 * with the given BeanFactory, as used by the WebApplicationContext.
	 * @param bf the BeanFactory to configure
	 * @param sc the ServletContext that we're running within
	 */
	public static void registerEnvironmentBeans(ConfigurableListableBeanFactory bf, @Nullable ServletContext sc) {
		registerEnvironmentBeans(bf, sc, null);
	}

    public static void registerEnvironmentBeans(ConfigurableListableBeanFactory bf,
			@Nullable ServletContext servletContext, @Nullable ServletConfig servletConfig) {

		if (servletContext != null && !bf.containsBean(WebApplicationContext.SERVLET_CONTEXT_BEAN_NAME)) {
            
			bf.registerSingleton(WebApplicationContext.SERVLET_CONTEXT_BEAN_NAME, servletContext);
            
		}
		
        // servletConfig=null
		if (servletConfig != null && !bf.containsBean(ConfigurableWebApplicationContext.SERVLET_CONFIG_BEAN_NAME)) {
			bf.registerSingleton(ConfigurableWebApplicationContext.SERVLET_CONFIG_BEAN_NAME, servletConfig);
		}

		if (!bf.containsBean(WebApplicationContext.CONTEXT_PARAMETERS_BEAN_NAME)) {
			Map<String, String> parameterMap = new HashMap<>();
			if (servletContext != null) {
				Enumeration<?> paramNameEnum = servletContext.getInitParameterNames();
				while (paramNameEnum.hasMoreElements()) {
					String paramName = (String) paramNameEnum.nextElement();
					parameterMap.put(paramName, servletContext.getInitParameter(paramName));
				}
			}
			if (servletConfig != null) {
				Enumeration<?> paramNameEnum = servletConfig.getInitParameterNames();
				while (paramNameEnum.hasMoreElements()) {
					String paramName = (String) paramNameEnum.nextElement();
					parameterMap.put(paramName, servletConfig.getInitParameter(paramName));
				}
			}
			bf.registerSingleton(WebApplicationContext.CONTEXT_PARAMETERS_BEAN_NAME,
					Collections.unmodifiableMap(parameterMap));
		}

		if (!bf.containsBean(WebApplicationContext.CONTEXT_ATTRIBUTES_BEAN_NAME)) {
			Map<String, Object> attributeMap = new HashMap<>();
			if (servletContext != null) {
				Enumeration<?> attrNameEnum = servletContext.getAttributeNames();
				while (attrNameEnum.hasMoreElements()) {
					String attrName = (String) attrNameEnum.nextElement();
					attributeMap.put(attrName, servletContext.getAttribute(attrName));
				}
			}
			bf.registerSingleton(WebApplicationContext.CONTEXT_ATTRIBUTES_BEAN_NAME,
					Collections.unmodifiableMap(attributeMap));
		}
	}
```

#### `2.ClassPathBeanDefinitionScanner#scan`

> 根据`basePackages`的内容加载、注册`BeanDefinition`

```java
public int scan(String... basePackages) {
    
		int beanCountAtScanStart = this.registry.getBeanDefinitionCount();
		doScan(basePackages);

		// Register annotation config processors, if necessary.
		if (this.includeAnnotationConfig) {
			AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
		}

		return (this.registry.getBeanDefinitionCount() - beanCountAtScanStart);
}
```

### `AbstractApplicationContext#invokeBeanFactoryPostProcessors`

```java
protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
    
		PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

		// Detect a LoadTimeWeaver and prepare for weaving, if found in the meantime
		// (e.g. through an @Bean method registered by ConfigurationClassPostProcessor)
		if (beanFactory.getTempClassLoader() == null && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
			beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
			beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
		}
	}
```

#### `PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors())`

> `BeanFactoryPostProcessor`回调

```java
public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		if (beanFactory instanceof BeanDefinitionRegistry) {
            
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
            
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
                
                // 先处理BeanDefinitionRegistryPostProcessor
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
		
 					// 回调   
                 registryProcessor.postProcessBeanDefinitionRegistry(registry);
                    
					registryProcessors.add(registryProcessor);
				}
                
				else {
					regularPostProcessors.add(postProcessor);
				}
			}
            

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
                // BeanDefinitionRegistryPostProcessor  PriorityOrdered
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		else {
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}
```

## `BeanFactory`注册`BeanPostProcessor`阶段

> `AbstractApplicationContext#registerBeanPostProcessors`
>
> 主要涉及如下逻辑：
>
> 1. 注册`PriorityOrdered`类型的`BeanPostProcessor `Beans
> 2. 注册`Ordered`类型的`BeanPostProcessor `Beans
> 3. 注册普通的`BeanPostProcessor` Beans
> 4. 注册`MergedBeanDefinitionPostProcessor` Beanss
> 5. 注册`ApplicationListenerDetector`对象

```java
protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
	}
```

```java
public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {
		
    	// 获取BeanPostProcessor Bean Name List
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
        
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
    
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                // 提前初始化BeanPostProcessor Bean
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);
		
		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
    
    
	}
```

## 初始化内建Bean: `MessageSource`

## 初始化内建Bean : Spring事件广播器

## Spring应用上下文刷新阶段

## Spring事件监听器注册阶段

## `BeanFactory`初始化完成阶段

## Spring应用上下文启动完成阶段

finish

## Spring应用上下文启动阶段

start

## Spring应用上下文停止阶段

stop

## Spring应用上下文关闭阶段

close