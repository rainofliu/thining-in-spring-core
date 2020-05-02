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

## `MessageResource`开箱即用实现

## `MessageResource`内建实现

## 面试题