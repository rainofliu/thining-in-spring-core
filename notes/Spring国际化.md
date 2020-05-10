[toc]

# Spring国际化(i18n)

## Spring国际化使用场景

+ 普通国际化文案
+ Bean Validation校验国际化文案（Spring Boot场景）
+ Web站点页面渲染
  + 根据不同的ip确定不同的国家，来渲染页面文字样式等等
+ Web MVC错误信息提示

## Spring国际化接口

+ 核心接口 

  + `org.springframework.context.MessageSource`

+ 开箱即用的实现

  + `org.springframework.context.support.ResourceBundleMessageSource`
  + `org.springframework.context.support.ReloadableResourceBundleMessageSource`

+ 主要概念

  + 文案模板编码(code)
    + **文案模板并非文案本身**
  + 文案模板参数(args)
  + 区域(`Locale`)

  > 对于文案模板的理解，可以类别到模板引擎，例如thymeleaf ，上面列出的文案模板参数（args）可以类比为占位符，可以被动态替换

  ```java
  public interface MessageSource {
      // defaultMessage：兜底信息 该方法返回处理后的文案
      @Nullable
  	String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage , Locale locale);
      
      String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException;
      
      String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException;
  }
  ```

## 层次性`MessageResource`

### Spring层次性接口

+ `org.springframework.beans.factory.HierarchicalBeanFactory`

  > 父亲容器
  >
  > ```java
  > public interface HierarchicalBeanFactory extends BeanFactory {
  >     BeanFactory getParentBeanFactory();
  >     boolean containsLocalBean(String name);
  > }
  > ```

+ `org.springframework.context.ApplicationContext`

  > 父亲上下文
  >
  > ```java
  > public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
  > 		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {
  >   ...
  >   ApplicationContext getParent();    
  >   ...    
  > }
  > ```

+ `org.springframework.beans.factory.config.BeanDefinition`

  > ```java
  > public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {
  >   ...
  >   String getParentName();  
  >   ...   
  > }
  > ```

### Spring层次性国际化接口

+ `org.springframework.context.HierarchicalMessageSource`

```java
public interface HierarchicalMessageSource extends MessageSource {
    void setParentMessageSource(@Nullable MessageSource parent);
    MessageSource getParentMessageSource();
}
```

## Java国际化标准实现

### 核心接口

+ 抽象实现   `java.util.ResourceBundle`
+ `Properties`资源实现     `java.util.PropertyResourceBundle`
+ 例举实现 `java.util.ListResourceBundle`

### `ResourceBundle`核心特性

+ `key-value`设计
+ 层次性设计
+ 缓存设计
+ 字符编码控制    `java.util.ResourceBundle.Control`
+ `Control` SPI扩展     `java.util.spi.ResourceBundleControlProvider`

## Java文本格式化

### 核心接口

+ `java.text.MessageFormat`

### 基本用法

+ 设置消息格式模式  `new MessageFormat(...)`
+ 格式化  format

```java
 public static void main(String[] args) {
        int planet = 7;
        String event = "a disturbance in the Force";

        String result = MessageFormat.format(
                "At {1,time,long} on {1,date,full}, there was {2} on planet {0,number,integer}.",
                planet, new Date(), event);
        System.out.println(result);
  }
// At 下午12时56分59秒 on 2020年5月2日 星期六, there was a disturbance in the Force on planet // 7.
```

### 高级特性

+ 重置消息格式模式
+ 重置`java.util.Locale`
+ 重置`java.text.Format`

```java
 public static void main(String[] args) {
        int planet = 7;
        String result;
        String event = "a disturbance in the Force";

        String messageFormatPattern = "At {1,time,long} on {1,date,full}, there was {2} on planet {0,number,integer}.";
        MessageFormat messageFormat = new MessageFormat(messageFormatPattern);
        messageFormat.format(new Object[]{planet, new Date(), event});

        messageFormatPattern = "this is a text:{0}";
        // 重置MessageFormatPattern
        messageFormat.applyPattern(messageFormatPattern);
        result = messageFormat.format(new Object[]{"helloWorld"});
        System.out.println(result);

        // 重置Locale
        messageFormat.setLocale(Locale.ENGLISH);
        messageFormatPattern = "At {1,time,long} on {1,date,full}, there was {2} on planet {0,number,integer}.";
        messageFormat.applyPattern(messageFormatPattern);
        result = messageFormat.format(new Object[]{planet, new Date(), event});
        System.out.println(result);

        // 重置Format
        // 根据参数索引来设置 Pattern
        messageFormat.setFormat(1,new SimpleDateFormat("YYYY--MM-dd HH:mm:ss"));
        result = messageFormat.format(new Object[]{planet, new Date(), event});
        System.out.println(result);
    }
```

> this is a text:helloWorld
> At 1:11:22 PM CST on Saturday, May 2, 2020, there was a disturbance in the Force on planet 7.
> At 1:11:22 PM CST on 2020--05-02 13:11:22, there was a disturbance in the Force on planet 7.

## `MessageSource`开箱即用实现

+ 基于`ResourceBundle`+ `MessageFormat` 组合`MessageSource`实现

  + `org.springframework.context.support.ResourceBundleMessageSource`

    > + `ResourceBundle`定位文案，`MessageFormat`格式化文案

    + `AbstractMessageSource#getMessage(java.lang.String, java.lang.Object[], java.lang.String, java.util.Locale)`  => `MessageSource`
      + `AbstractMessageSource#getMessageInternal`  String
        + `ResourceBundleMessageSource#resolveCode`   
          + `ResourceBundleMessageSource#getResourceBundle`
          + `ResourceBundleMessageSource#getMessageFormat`
        + `java.text.MessageFormat#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)`

    ![](https://liutianruo-2019-go-go-go.oss-cn-shanghai.aliyuncs.com/images/6292_oizgysgz_ResourceBundleMessageSource原理.png)

    + `MessageFormat`是非线程安全的，所以这里的`MessageFormat`是只读的，并且不能调整文案内容，所以它的功能是被限制了

+ 可重载`Properties`+ `MessageFormat`组合`MessageSource`实现

  + `org.springframework.context.support.ReloadableResourceBundleMessageSource`

## `MessageSource`内建实现

`MessageSource`内建Bean来源

+ 预注册Bean名称为`messageSource` ，类型为`MessageSource` Bean
+ 默认内建实现   `org.springframework.context.support.DelegatingMessageSource`
  + 层次性查找 `MessageSource`对象

```java
@Nullable
private MessageSource messageSource;
// AbstractApplicationContext
protected void initMessageSource() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
         //  如果MessageSource Bean存在于当前上下文
		if (beanFactory.containsLocalBean(MESSAGE_SOURCE_BEAN_NAME)) {
            // 触发创建Bean
			this.messageSource = beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME, MessageSource.class);
			// Make MessageSource aware of parent MessageSource.
            // 如果是层次性的，则设置parentMessageSource
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
        // 如果尚未注册MessageSource Bean ，则创建一个DelegatingMessageSource Bean
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
private MessageSource getMessageSource() throws IllegalStateException {
		if (this.messageSource == null) {
			throw new IllegalStateException("MessageSource not initialized - " +
					"call 'refresh' before accessing messages via the context: " + this);
		}
		return this.messageSource;
}

```

```java
// DelegatingMessageSource
 @Override
	public String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException {
		if (this.parentMessageSource != null) {
			return this.parentMessageSource.getMessage(code, args, locale);
		}
		else {
			throw new NoSuchMessageException(code, locale);
		}
	}
```

> 如果当前`ApplicationContext`的父上下文中的`MessageSource`为空，那么在没有定义`MessageSource`Bean的前提下，`AbstractApplicationContext`会创建一个空的`DelegatingMessageSource`，调用`DelegatingMessageSource#getMessage`方法一定会抛出`NoSuchMessageException`

## Spring Boot为什么要新建MessageSource Bean

+ `AbstractApplicationContext`的实现决定了`MessageSource`的内建实现

+ Spring Boot通过外部化配置简化`MessageSource` Bean构建

+ Spring Boot基于 Bean Validation校验非常普遍

  > Bean Validation 依赖国际化技术

```java
@Configuration(proxyBeanMethods = false)
// 搜索当前BeanFactory ，如果没有MessageSource Bean 才会往下执行
@ConditionalOnMissingBean(name = AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME, search = SearchStrategy.CURRENT)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Conditional(ResourceBundleCondition.class)
@EnableConfigurationProperties
public class MessageSourceAutoConfiguration {

	private static final Resource[] NO_RESOURCES = {};
    
	// 外部化配置
	@Bean
	@ConfigurationProperties(prefix = "spring.messages")
	public MessageSourceProperties messageSourceProperties() {
		return new MessageSourceProperties();
	}
	
	@Bean
	public MessageSource messageSource(MessageSourceProperties properties) {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		if (StringUtils.hasText(properties.getBasename())) {
			messageSource.setBasenames(StringUtils
					.commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(properties.getBasename())));
		}
		if (properties.getEncoding() != null) {
			messageSource.setDefaultEncoding(properties.getEncoding().name());
		}
		messageSource.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
		Duration cacheDuration = properties.getCacheDuration();
		if (cacheDuration != null) {
			messageSource.setCacheMillis(cacheDuration.toMillis());
		}
		messageSource.setAlwaysUseMessageFormat(properties.isAlwaysUseMessageFormat());
		messageSource.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());
		return messageSource;
	}
    ...
}        
```

> `AbstractApplicationContext.initMessageSource`会扫描包内Bean的定义，虽然此时MessageSource Bean还没有创建，但是会读取到它的Bean的定义，就不会创建空的`DelegatingMessageSource`

## 面试题

### Spring国际化接口包含哪些

+ 核心接口   `MessageSource`
+ 层次性接口  `org.springframework.context.HierarchicalMessageSource`

### Spring有哪些`MessageSource`内建实现

+ `org.springframework.context.support.ResourceBundleMessageSource`
+ `org.springframework.context.support.ReloadableResourceBundleMessageSource`
+ `org.springframework.context.support.StaticMessageSource`
+ `org.springframework.context.support.DelegatingMessageSource`

### 如何实现配置自动更新`MessageSource`

主要技术

+ Java NIO2 : `java.nio.file.WatchService`
+ Java多线程： `java.util.concurrent.ExecutorService`
+ Spring :   `org.springframework.context.support.AbstractMessageSource`