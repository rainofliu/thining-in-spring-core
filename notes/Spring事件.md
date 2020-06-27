[toc]

在Spring中，事件的发布有两个接口 `ApplicationEventPublisher` 和`ApplicationEventMulticaster`

# `ApplicationEventPublisher` 

该接口 只关注发布事件

```java
@FunctionalInterface
public interface ApplicationEventPublisher {
    
    	default void publishEvent(ApplicationEvent event) {
		        publishEvent((Object) event);
	    }
    
        void publishEvent(Object event);

}  
```

# `ApplicationEventMulticaster`

```java
public interface ApplicationEventMulticaster {
    void addApplicationListener(ApplicationListener<?> listener);
    void addApplicationListenerBean(String listenerBeanName);
    void removeApplicationListener(ApplicationListener<?> listener);
	void removeApplicationListenerBean(String listenerBeanName);
    void removeAllListeners();
	void multicastEvent(ApplicationEvent event);
	void multicastEvent(ApplicationEvent event, @Nullable ResolvableType eventType);
}
```

我们发现`ApplicationEventMulticaster`关联了很多个`ApplicationListener`，而且multicastEvent方法用来广播事件。有这个认知后，我们跟着代码再详细理解一下。

# 原理追踪

> 该模块顺序可能会有些乱，如果影响阅读，可以查看**梳理**模块。

```java
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {
            ...
}
```

> `ApplicationContext`继承了`ApplicationEventPublisher`接口，可以猜想一下，`ApplicationContext`的实现类可能实现了`org.springframework.context.ApplicationEventPublisher#publishEvent(org.springframework.context.ApplicationEvent)`方法。

## `AbstractApplicationEvent#publishEvent`

猜想中说的实现类就是`AbstractApplicationEvent`。

```java
	@Override
	public void publishEvent(ApplicationEvent event) {
		publishEvent(event, null);
	}
	
	/**
	 * Publish the given event to all listeners.
	 * @param event the event to publish (may be an {@link ApplicationEvent}
	 * or a payload object to be turned into a {@link PayloadApplicationEvent})
	 * @param eventType the resolved event type, if known
	 * @since 4.2
	 */
	protected void publishEvent(Object event, @Nullable ResolvableType eventType) {
		Assert.notNull(event, "Event must not be null");

		// Decorate event as an ApplicationEvent if necessary
        // 封装要发布的时间
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
        // 有可能此时ApplicationEventMulticaster尚未初始化，所以需要先提前将要发布的时间先存储起来留待后面ApplicationEventMulticaster正确初始化以后再把事件广播出去
		if (this.earlyApplicationEvents != null) {
			this.earlyApplicationEvents.add(applicationEvent);
		}
		else {
            // 通过ApplicationEventMulticaster广播事件
			getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
		}

		// Publish event via parent context as well...
        // 将时间传递给parent上下文
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

> ```java
> /** Helper class used in event publishing. */
> @Nullable
> private ApplicationEventMulticaster applicationEventMulticaster;
> 
> /** Statically specified listeners. */
> private final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();
> 
> /** Local listeners registered before refresh. */
> @Nullable
> private Set<ApplicationListener<?>> earlyApplicationListeners;
> 
> /** ApplicationEvents published before the multicaster setup. */
> @Nullable
> private Set<ApplicationEvent> earlyApplicationEvents;
> ```

### `getApplicationEventMulticaster`

```java
	ApplicationEventMulticaster getApplicationEventMulticaster() throws IllegalStateException {
		if (this.applicationEventMulticaster == null) {
			throw new IllegalStateException("ApplicationEventMulticaster not initialized - " +
					"call 'refresh' before multicasting events via the context: " + this);
		}
		return this.applicationEventMulticaster;
	}
```

> 我们这里只是看到`ApplicationEventMulticaster`的获取，那么它是怎么被写入的呢

```java
// AbstractApplicationContext
@Nullable
private ApplicationEventMulticaster applicationEventMulticaster;
```

### `initApplicationEventMulticaster`

初始化`ApplicationEventMulticaster` ，初始化时机为`AbstractApplicationContext#refresh`

```java
	protected void initApplicationEventMulticaster() {
        // 获取BeanFactory
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        // 	public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";
		if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
            // 依赖查找
			this.applicationEventMulticaster =
					beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
			}
		}
		else {
            // SimpleApplicationEventMulticaster
			this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
            // 注册singletonBean
			beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
			if (logger.isTraceEnabled()) {
				logger.trace("No '" + APPLICATION_EVENT_MULTICASTER_BEAN_NAME + "' bean, using " +
						"[" + this.applicationEventMulticaster.getClass().getSimpleName() + "]");
			}
		}
	}
```



```java
@Override
	public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			// Prepare this context for refreshing.
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
                // 初始化ApplicationEventMulticaster
				initApplicationEventMulticaster();

				// Initialize other special beans in specific context subclasses.
				onRefresh();

				// Check for listener beans and register them.
				registerListeners();

				// Instantiate all remaining (non-lazy-init) singletons.
                // 实例化并初始化bean
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

### `SimpleApplicationEventMulticaster#multicastEvent(org.springframework.context.ApplicationEvent)`

```java
	@Override
	public void multicastEvent(ApplicationEvent event) {
		multicastEvent(event, resolveDefaultEventType(event));
	}

	@Override
	public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
		ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
        // 获取线程池
		Executor executor = getTaskExecutor();
		for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
			if (executor != null) {
				executor.execute(() -> invokeListener(listener, event));
			}
			else {
				invokeListener(listener, event);
			}
		}
	}
```

#### `AbstractApplicationContext#registerListeners`

> 上下文注册`ApplicationListener`

```java
protected void registerListeners() {
		// Register statically specified listeners first.
		for (ApplicationListener<?> listener : getApplicationListeners()) {
            // 给ApplicationEventMulticaster 添加关联的ApplicationListener
			getApplicationEventMulticaster().addApplicationListener(listener);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let post-processors apply to them!
		String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
		for (String listenerBeanName : listenerBeanNames) {
			getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
		}

		// Publish early application events now that we finally have a multicaster...
		Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
       // 将预先添加的事件设置为null
		this.earlyApplicationEvents = null;
		if (earlyEventsToProcess != null) {
			for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
                // 发布早期事件
				getApplicationEventMulticaster().multicastEvent(earlyEvent);
			}
		}
	}
```

#### `SimpleApplicationEventMulticaster#invokeListener`

```java
protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
		ErrorHandler errorHandler = getErrorHandler();
		if (errorHandler != null) {
			try {
                // 触发ApplicationListener的监听
				doInvokeListener(listener, event);
			}
			catch (Throwable err) {
				errorHandler.handleError(err);
			}
		}
		else {
			doInvokeListener(listener, event);
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void doInvokeListener(ApplicationListener listener, ApplicationEvent event) {
		try {
            // 触发ApplicationListener的监听
			listener.onApplicationEvent(event);
		}
		catch (ClassCastException ex) {
			String msg = ex.getMessage();
			if (msg == null || matchesClassCastMessage(msg, event.getClass())) {
				// Possibly a lambda-defined listener which we could not resolve the generic event type for
				// -> let's suppress the exception and just log a debug message.
				Log logger = LogFactory.getLog(getClass());
				if (logger.isTraceEnabled()) {
					logger.trace("Non-matching event type for listener: " + listener, ex);
				}
			}
			else {
				throw ex;
			}
		}
	}
```

# 梳理

前面的代码看起来很乱，我们整体梳理一下。

+ `AbstractApplicationContext.refresh`

  > 启动Spring应用上下文

  + `AbstractApplicationContext#initApplicationEventMulticaster`

    > 创建`ApplicationEventMulticaster` (事件发布的打工仔，实际执行者)

  + `AbstractApplicationContext#registerListeners`

    > 将`ApplicationListener`集合关联给`ApplicationEventMulticaster` 

+ Spring应用**上下文启动过程中或者启动完成后**会调用`AbstractApplicationContext#publishEvent(org.springframework.context.ApplicationEvent)`

  + 解析发布的`ApplicationEvent`

  + `ApplicationEventMulticaster#multicastEvent(org.springframework.context.ApplicationEvent, org.springframework.core.Executor)`

    + `Executor`！=null （如果没有线程池，则串行执行：即使用当前线程执行）

      + 使用线程池触发监听器监听

        + `AbstractApplicationEventMulticaster#getApplicationListeners(org.springframework.context.ApplicationEvent, org.springframework.core.ResolvableType)`

          > 获取到的`ApplicationListener`一定是和`ApplicationEvent`相关的事件

        + `SimpleApplicationEventMulticaster#invokeListener`

          + `SimpleApplicationEventMulticaster#doInvokeListener`

> 这个原理其实就是`ApplicationEventMulticast`给`ApplicationEventPublisher`打工的过程。