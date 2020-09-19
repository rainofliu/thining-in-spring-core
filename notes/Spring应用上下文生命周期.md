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

> `AbstractApplicationContext#initMessageSource` 

```java
	protected void initMessageSource() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(MESSAGE_SOURCE_BEAN_NAME)) {
			this.messageSource = beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME, MessageSource.class);
			// Make MessageSource aware of parent MessageSource.
			if (this.parent != null && this.messageSource instanceof HierarchicalMessageSource) {
				HierarchicalMessageSource hms = (HierarchicalMessageSource) this.messageSource;
				if (hms.getParentMessageSource() == null) {
					// Only set parent context as parent MessageSource if no parent MessageSource
					// registered already.
					hms.setParentMessageSource(getInternalParentMessageSource());
				}
			}
			if (logger.isTraceEnabled()) {
				logger.trace("Using MessageSource [" + this.messageSource + "]");
			}
		}
		else {
			// Use empty MessageSource to be able to accept getMessage calls.
			DelegatingMessageSource dms = new DelegatingMessageSource();
			dms.setParentMessageSource(getInternalParentMessageSource());
			this.messageSource = dms;
			beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
			if (logger.isTraceEnabled()) {
				logger.trace("No '" + MESSAGE_SOURCE_BEAN_NAME + "' bean, using [" + this.messageSource + "]");
			}
		}
	}
```

> 默认情况下会注册一个`DelegatingMessageSource` 类型的`MessageSource`(国际化)  singleton Bean

## 初始化内建Bean : Spring事件广播器

`ApplicationEventMulticaster`在Spring应用上下文中必须存在

```java
protected void initApplicationEventMulticaster() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
			this.applicationEventMulticaster =
					beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
			}
		}
		else {
            // 默认注册SimpleApplicationEventMulticaster Bean
			this.applicationEventMulticaster = new SimpleApplicationEventMulticaster Bean(beanFactory);
			beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
			if (logger.isTraceEnabled()) {
				logger.trace("No '" + APPLICATION_EVENT_MULTICASTER_BEAN_NAME + "' bean, using " +
						"[" + this.applicationEventMulticaster.getClass().getSimpleName() + "]");
			}
		}
	}
```

## Spring应用上下文刷新阶段

`AbstractApplicationContext#onRefresh`默认是空方法，供具体的上下文（Web应用）进行自定义扩展

```java
	protected void onRefresh() throws BeansException {
		// For subclasses: do nothing by default.
	}
```

## Spring事件监听器注册阶段

```java
protected void registerListeners() {
		// Register statically specified listeners first.
        // 1.添加当前Spring应用上下文关联的ApplicationListener对象集合
		for (ApplicationListener<?> listener : getApplicationListeners()) {
			getApplicationEventMulticaster().addApplicationListener(listener);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let post-processors apply to them!
    // 2. 添加 BeanFactory所注册的ApplicationListener Beans
    // 这里不初始化Bean，延迟初始化
		String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
		for (String listenerBeanName : listenerBeanNames) {
			getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
		}

		// Publish early application events now that we finally have a multicaster...
        // 广播早期Spring事件
		Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
		this.earlyApplicationEvents = null;
		if (earlyEventsToProcess != null) {
			for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
				getApplicationEventMulticaster().multicastEvent(earlyEvent);
			}
		}
	}
```

### `getApplicationEventMulticaster`

```java
/**
	 * Return the internal ApplicationEventMulticaster used by the context.
	 * @return the internal ApplicationEventMulticaster (never {@code null})
	 * @throws IllegalStateException if the context has not been initialized yet
	 */
	ApplicationEventMulticaster getApplicationEventMulticaster() throws IllegalStateException {
		if (this.applicationEventMulticaster == null) {
			throw new IllegalStateException("ApplicationEventMulticaster not initialized - " +
					"call 'refresh' before multicasting events via the context: " + this);
		}
		return this.applicationEventMulticaster;
	}
```

### `AbstractApplicationEventMulticaster#addApplicationListener`

```java
@Override
	public void addApplicationListener(ApplicationListener<?> listener) {
        // 加锁 保证多线程操作的原子性
		synchronized (this.retrievalMutex) {
			// Explicitly remove target for a proxy, if registered already,
			// in order to avoid double invocations of the same listener.
			Object singletonTarget = AopProxyUtils.getSingletonTarget(listener);
			if (singletonTarget instanceof ApplicationListener) {
				this.defaultRetriever.applicationListeners.remove(singletonTarget);
			}
			this.defaultRetriever.applicationListeners.add(listener);
			this.retrieverCache.clear();
		}
	}
```

## `BeanFactory`初始化完成阶段

```java
/**
	 * Finish the initialization of this context's bean factory,
	 * initializing all remaining singleton beans.
	 */
	protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
		// Initialize conversion service for this context.
        // BeanFactory 关联 ConversionService Bean（如果存在）
		if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
				beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
			beanFactory.setConversionService(
					beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
		}

		// Register a default embedded value resolver if no bean post-processor
		// (such as a PropertyPlaceholderConfigurer bean) registered any before:
		// at this point, primarily for resolution in annotation attribute values.
        // BeanFactory添加StringValueResolver对象
		if (!beanFactory.hasEmbeddedValueResolver()) {
			beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
		}

		// Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
        
		String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
		for (String weaverAwareName : weaverAwareNames) {
            // 依赖查找LoadTimeWeaverAware Bean
			getBean(weaverAwareName);
		}

		// Stop using the temporary ClassLoader for type matching.
        // 将BeanFactory的临时ClassLoader置为空
		beanFactory.setTempClassLoader(null);

		// Allow for caching all bean definition metadata, not expecting further changes.
        // BeanFactory冻结配置
		beanFactory.freezeConfiguration();

		// Instantiate all remaining (non-lazy-init) singletons.
        // 初始化非延迟单例 Bean
		beanFactory.preInstantiateSingletons();
	}
```

### 冻结配置

> `DefaultListableBeanFactory`

```java
@Override
	public void freezeConfiguration() {
		this.configurationFrozen = true;
        // 冻结 不让修改的beanDefinition 名称
		this.frozenBeanDefinitionNames = StringUtils.toStringArray(this.beanDefinitionNames);
	}
```

### 初始化非延迟单例 Bean

```java
@Override
	public void preInstantiateSingletons() throws BeansException {
        
		if (logger.isTraceEnabled()) {
			logger.trace("Pre-instantiating singletons in " + this);
		}

		// Iterate over a copy to allow for init methods which in turn register new bean definitions.
		// While this may not be part of the regular factory bootstrap, it does otherwise work fine.
		List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

		// Trigger initialization of all non-lazy singleton beans...
		for (String beanName : beanNames) {
            // merged BeanDefiniton
			RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
			if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
                
				if (isFactoryBean(beanName)) {
					Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
					if (bean instanceof FactoryBean) {
						final FactoryBean<?> factory = (FactoryBean<?>) bean;
						boolean isEagerInit;
						if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
							isEagerInit = AccessController.doPrivileged((PrivilegedAction<Boolean>)
											((SmartFactoryBean<?>) factory)::isEagerInit,
									getAccessControlContext());
						}
						else {
							isEagerInit = (factory instanceof SmartFactoryBean &&
									((SmartFactoryBean<?>) factory).isEagerInit());
						}
						if (isEagerInit) {
							getBean(beanName);
						}
					}
				}
				else {
					getBean(beanName);
				}
			}
		}

		// Trigger post-initialization callback for all applicable beans...
		for (String beanName : beanNames) {
			Object singletonInstance = getSingleton(beanName);
			if (singletonInstance instanceof SmartInitializingSingleton) {
				final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
				if (System.getSecurityManager() != null) {
					AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
						smartSingleton.afterSingletonsInstantiated();
						return null;
					}, getAccessControlContext());
				}
				else {
					smartSingleton.afterSingletonsInstantiated();
				}
			}
		}
	}

```

#### `SmartInitializingSingleton#afterSingletonsInstantiated`

> 回调

## Spring应用上下文刷新完成阶段

> finish
>
> `org.springframework.context.support.AbstractApplicationContext#finishRefresh`

```java
/**
	 * Finish the refresh of this context, invoking the LifecycleProcessor's
	 * onRefresh() method and publishing the
	 * {@link org.springframework.context.event.ContextRefreshedEvent}.
	 */
	protected void finishRefresh() {
		// Clear context-level resource caches (such as ASM metadata from scanning).
        // 清除ResourceLoader缓存
		clearResourceCaches();

		// Initialize lifecycle processor for this context.
        // 初始化LifecycleProcessor对象
		initLifecycleProcessor();

		// Propagate refresh to lifecycle processor first.
        // 调用LifecycleProcessor.onrefresh方法
		getLifecycleProcessor().onRefresh();

		// Publish the final event.
        // 发布Spring应用上下文已刷新事件
		publishEvent(new ContextRefreshedEvent(this));

		// Participate in LiveBeansView MBean, if active.
        // 向MBeanServer 托管Live Beans
		LiveBeansView.registerApplicationContext(this);
	}
```

### 清除ResourceLoader缓存

> `private final Map<Class<?>, Map<Resource, ?>> resourceCaches = new ConcurrentHashMap<>(4);`

```java
/**
	 * Clear all resource caches in this resource loader.
	 * @since 5.0
	 * @see #getResourceCache
	 */
	public void clearResourceCaches() {
		this.resourceCaches.clear();
	}
```

### 初始化LifecycleProcessor对象

```java
protected void initLifecycleProcessor() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
    
		if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
			this.lifecycleProcessor =
					beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
            
			if (logger.isTraceEnabled()) {
				logger.trace("Using LifecycleProcessor [" + this.lifecycleProcessor + "]");
			}
		}
    
		else {
            // 默认实现
			DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
			defaultProcessor.setBeanFactory(beanFactory);
			this.lifecycleProcessor = defaultProcessor;
			beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
            
			if (logger.isTraceEnabled()) {
				logger.trace("No '" + LIFECYCLE_PROCESSOR_BEAN_NAME + "' bean, using " +
						"[" + this.lifecycleProcessor.getClass().getSimpleName() + "]");
			}
            
		}
    
}
```

### 调用LifecycleProcessor#onRefresh方法

> `DefaultLifecycleProcessor#onRefresh`

```java
@Override
	public void onRefresh() {
		startBeans(true);
		this.running = true;
	}
private void startBeans(boolean autoStartupOnly) {
        // 获取Lifecycle Beans
		Map<String, Lifecycle> lifecycleBeans = getLifecycleBeans();
		Map<Integer, LifecycleGroup> phases = new HashMap<>();
        // 遍历
		lifecycleBeans.forEach((beanName, bean) -> {
            // 不满足条件
			if (!autoStartupOnly || (bean instanceof SmartLifecycle && ((SmartLifecycle) bean).isAutoStartup())) {
				int phase = getPhase(bean);
				LifecycleGroup group = phases.get(phase);
				if (group == null) {
					group = new LifecycleGroup(phase, this.timeoutPerShutdownPhase, lifecycleBeans, autoStartupOnly);
					phases.put(phase, group);
				}
				group.add(beanName, bean);
			}
		});
		if (!phases.isEmpty()) {
			List<Integer> keys = new ArrayList<>(phases.keySet());
			Collections.sort(keys);
			for (Integer key : keys) {
				phases.get(key).start();
			}
		}
	}
```

### 发布Spring应用上下文已刷新事件

> 广播`ContextRefreshedEvent`事件
>
> `AbstractApplicationContext#publishEvent`
>
> 下面的源码就是之前说的`ApplicationEventMulticaster`给`ApplicationEventPublisher`(一般为Spring应用上下文)打工的故事

```java
protected void publishEvent(Object event, @Nullable ResolvableType eventType) {
		Assert.notNull(event, "Event must not be null");

		// Decorate event as an ApplicationEvent if necessary
		ApplicationEvent applicationEvent;
		if (event instanceof ApplicationEvent) {
			applicationEvent = (ApplicationEvent) event;
		}
		else {
			applicationEvent = new PayloadApplicationEvent<>(this, event);
			if (eventType == null) {
				eventType = ((PayloadApplicationEvent<?>) applicationEvent).getResolvableType();
			}
		}

		// Multicast right now if possible - or lazily once the multicaster is initialized
		if (this.earlyApplicationEvents != null) {
			this.earlyApplicationEvents.add(applicationEvent);
		}
		else {
			getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
		}

		// Publish event via parent context as well...
		if (this.parent != null) {
			if (this.parent instanceof AbstractApplicationContext) {
				((AbstractApplicationContext) this.parent).publishEvent(event, eventType);
			}
			else {
				this.parent.publishEvent(event);
			}
		}
	}
```

### 向MBeanServer 托管Live Beans(JMX)

```java
// LiveBeansView.registerApplicationContext
static void registerApplicationContext(ConfigurableApplicationContext applicationContext) {
    	
        // 获取配置的mbeanDomain
		String mbeanDomain = applicationContext.getEnvironment().getProperty(MBEAN_DOMAIN_PROPERTY_NAME);
    
		if (mbeanDomain != null) {
			synchronized (applicationContexts) {
                
				if (applicationContexts.isEmpty()) {
					try {
                        // MBeanServer
						MBeanServer server = ManagementFactory.getPlatformMBeanServer();
						applicationName = applicationContext.getApplicationName();
                        
						server.registerMBean(new LiveBeansView(),
								new ObjectName(mbeanDomain, MBEAN_APPLICATION_KEY, applicationName));
                        
					}
					catch (Throwable ex) {
						throw new ApplicationContextException("Failed to register LiveBeansView MBean", ex);
					}
				}
				applicationContexts.add(applicationContext);
			}
		}
	}
```

## Spring应用上下文启动阶段

> start  手动触发

```java
	@Override
	public void start() {
		getLifecycleProcessor().start();
        // 发布ContextStartedEvent事件
		publishEvent(new ContextStartedEvent
ContextStartedEvent(this));
	}
```

## Spring应用上下文停止阶段

> stop  手动触发

```java
@Override
	public void stop() {
		getLifecycleProcessor().stop();
		publishEvent(new ContextStoppedEvent(this));
	}
```

当我们的Bean实现了`LifeCycle`接口时，调用Spring应用上下文的start/stop方法，Bean 的start方法和stop方法会随之调用，这是对Spring上下文生命周期的补充，定义了一种全新的生命周期。

```java
public interface Lifecycle {
    void start();
    void stop();
    boolean isRunning();
}    
```

## Spring应用上下文关闭阶段

> close

```java
    /** Synchronization monitor for the "refresh" and "destroy". */
	private final Object startupShutdownMonitor = new Object();

	@Override
	public void close() {
		synchronized (this.startupShutdownMonitor) {
			doClose();
			// If we registered a JVM shutdown hook, we don't need it anymore now:
			// We've already explicitly closed the context.
			if (this.shutdownHook != null) {
				try {
					Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
				}
				catch (IllegalStateException ex) {
					// ignore - VM is already shutting down
				}
			}
		}
	}
```

### `AbstractApplicationContext#doClose`

```java
/** Flag that indicates whether this context is currently active. */
private final AtomicBoolean active = new AtomicBoolean();

/** Flag that indicates whether this context has been closed already. */
private final AtomicBoolean closed = new AtomicBoolean();

protected void doClose() {
		// Check whether an actual close attempt is necessary...
		if (this.active.get() && this.closed.compareAndSet(false, true)) {

			// 撤销JMX对Spring Bean的托管
			LiveBeansView.unregisterApplicationContext(this);

			try {
				// Publish shutdown event.
                // 发布ContextClosedEvent事件
				publishEvent(new ContextClosedEvent(this));
			}
			catch (Throwable ex) {
				...
			}

			// Stop all Lifecycle beans, to avoid delays during individual destruction.
			if (this.lifecycleProcessor != null) {
				try {
					this.lifecycleProcessor.onClose();
				}
				catch (Throwable ex) {
					...
				}
			}

			// Destroy all cached singletons in the context's BeanFactory.
			destroyBeans();

			// Close the state of this context itself.
			closeBeanFactory();

			// Let subclasses do some final clean-up if they wish...
			onClose();

			// Reset local application listeners to pre-refresh state.
			if (this.earlyApplicationListeners != null) {
				this.applicationListeners.clear();
				this.applicationListeners.addAll(this.earlyApplicationListeners);
			}

			// Switch to inactive.
			this.active.set(false);
		}
	}
```

#### `destroyBeans`

```java
protected void destroyBeans() {
	getBeanFactory().destroySingletons();
}
```

#### `closeBeanFactory`

### `removeShutdownHook`

> 优雅宕机

## 面试题

### Spring应用上下文生命周期有哪些阶段

+ 刷新阶段 refresh
+ 启动阶段  start
+ 停止 stop
+ 关闭 close

### Environment完整的生命周期

#### Spring Framework

##### `AbstractApplicationContext#prepareRefresh`

> 属于Spring应用上下文生命周期比较早的阶段

```java
        // Initialize any placeholder property sources in the context environment.
		initPropertySources();

		// Validate that all properties marked as required are resolvable:
		// see ConfigurablePropertyResolver#setRequiredProperties
		getEnvironment().validateRequiredProperties();
```

###### `AbstractApplicationContext#initPropertySources`

```java
/**
	 * <p>Replace any stub property sources with actual instances.
	 * @see org.springframework.core.env.PropertySource.StubPropertySource
	 * @see org.springframework.web.context.support.WebApplicationContextUtils#initServletPropertySources
	 */
	protected void initPropertySources() {
		// For subclasses: do nothing by default.
	}
```

> 该方法是模板方法，给子类来实现，下面的子类都实现了该方法
>
> + `AbstractRefreshableWebApplicationContext`
> + `GenericWebApplicationContext`
> + `StaticWebApplicationContext`

###### `AbstractApplicationContext#getEnvironment`

```java
/**
	 * Return the {@code Environment} for this application context in configurable
	 * form, allowing for further customization.
	 * <p>If none specified, a default environment will be initialized via
	 * {@link #createEnvironment()}.
	 */
	@Override
	public ConfigurableEnvironment getEnvironment() {
		if (this.environment == null) {
			this.environment = createEnvironment();
		}
		return this.environment;
	}
```

> 上面的英文注释（allowing for further customization）说允许子类实现，同样地`getEnvironment`方法也会被`AbstractApplicationContext`的子类覆写。

#### Spring Boot

```java
public ConfigurableApplicationContext run(String... args) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ConfigurableApplicationContext context = null;
		Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
		configureHeadlessProperty();
		SpringApplicationRunListeners listeners = getRunListeners(args);
		listeners.starting();
		try {
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
            // 1
			ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
			configureIgnoreBeanInfo(environment);
			Banner printedBanner = printBanner(environment);
			context = createApplicationContext();
			exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
					new Class[] { ConfigurableApplicationContext.class }, context);
            // 2
			prepareContext(context, environment, listeners, applicationArguments, printedBanner);
			refreshContext(context);
			afterRefresh(context, applicationArguments);
			stopWatch.stop();
			if (this.logStartupInfo) {
				new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
			}
			listeners.started(context);
			callRunners(context, applicationArguments);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, listeners);
			throw new IllegalStateException(ex);
		}

		try {
			listeners.running(context);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, null);
			throw new IllegalStateException(ex);
		}
		return context;
	}
```

##### `SpringApplication#prepareEnvironment`

> 先

```java
private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners,
			ApplicationArguments applicationArguments) {
    
		// Create and configure the environment
		ConfigurableEnvironment environment = getOrCreateEnvironment();
		configureEnvironment(environment, applicationArguments.getSourceArgs());
		ConfigurationPropertySources.attach(environment);
		listeners.environmentPrepared(environment);
		bindToSpringApplication(environment);
		if (!this.isCustomEnvironment) {
			environment = new EnvironmentConverter(getClassLoader()).convertEnvironmentIfNecessary(environment,
					deduceEnvironmentClass());
		}
		ConfigurationPropertySources.attach(environment);
		return environment;
	}
```

##### `SpringApplication#prepareContext`

   > 后

   ```java
   private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment,
   			SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
       	// 前面已经创建好了Environment对象
   		context.setEnvironment(environment);
   		postProcessApplicationContext(context);
   		applyInitializers(context);
   		
   	}
   ```

###### `applyInitializersapplyInitializers` 

```java
protected void applyInitializers(ConfigurableApplicationContext context) {
		// 遍历ApplicationContextInitializer对象
		for (ApplicationContextInitializer
ApplicationContextInitializer initializer : getInitializers()) {
			Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(initializer.getClass(),
					ApplicationContextInitializer.class);
			Assert.isInstanceOf(requiredType, context, "Unable to call initializer.");
			initializer.initialize(context);
		}
	}
```

> 我们可以自定义`ApplicationContextInitializer`类，实现`initialize`方法，在方法里面替换掉默认的`Environment`

```java
public interface ApplicationContextInitializer<C extends ConfigurableApplicationContext> {

	/**
	 * Initialize the given application context.
	 * @param applicationContext the application to configure
	 */
	void initialize(C applicationContext);

}
```

### Spring应用上下文生命周期执行动作

> 参考本文除面试题以外的章节