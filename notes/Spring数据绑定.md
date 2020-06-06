[toc]

# Spring数据绑定使用场景

1. Spring BeanDefinition 到 Bean实例的创建
2. Spring数据绑定(DataBinder)
3. Spring Web 参数绑定（Web DataBinder）

# 数据绑定组件

+ 标准组件
  + `org.springframework.validation.DataBinder`
+ Web组件
  + `org.springframework.web.bind.WebDataBinder`
  + `org.springframework.web.bind.ServletRequestDataBinder`
  + `org.springframework.web.bind.support.WebRequestDataBinder`
  + `org.springframework.web.bind.support.WebExchangeDataBinder`

```java
public class DataBinder implements PropertyEditorRegistry, TypeConverter {
    // 关联目标Bean
    @Nullable
	private final Object target;
    // 目标Bean名称
	private final String objectName;
	// 属性绑定结果
	@Nullable
	private AbstractPropertyBindingResult bindingResult;
	// 类型转换器
	@Nullable
	private SimpleTypeConverter typeConverter;
    // 类型转换服务
    @Nullable
	private ConversionService conversionService;
	// 校验错误文案Code处理器
	@Nullable
	private MessageCodesResolver messageCodesResolver;
    // 关联的Bean Validator实例集合
    private final List<Validator> validators = new ArrayList<>();
	
    // 绑定 ： 将外部配置作为属性 绑定给内部Bean对象
    // 例如 xml定义bean时，会将xml中的属性绑定给bean对象
    public void bind(PropertyValues pvs) {
		MutablePropertyValues mpvs = (pvs instanceof MutablePropertyValues ?
				(MutablePropertyValues) pvs : new MutablePropertyValues(pvs));
		doBind(mpvs);
	}
}
```

## `DataBinder`绑定元数据

元数据：`org.springframework.beans.PropertyValues`

| 特征       | 说明 <span style="white-space:nowrap;"> &emsp;&emsp;</span>                                                       |
| ------------ | :----------------------------------------------------------- |
| 数据来源     | BeanDefinition ,主要来源于xml配置BeanDefinition              |
| 数据结构     | 由一个或多个`PropertyValue`组成                              |
| 成员结构     | `PropertyValue`包含属性名称，以及属性值（包括原始值和类型转换后的值） |
| 常见实现     | `org.springframework.beans.MutablePropertyValues`            |
| Web扩展实现  | `ServletRequestParameterPropertyValues`、`HttpServletBean.ServletConfigPropertyValues` |
| 相关生命周期 | `InstantiationAwareBeanPostProcessor#postProcessProperties` |

### Demo

```java
        // 1. 创建空白对象
        User user = new User();
        // 2. 创建DataBinder
        DataBinder dataBinder = new DataBinder(user);
        Map<String, Object> source = new HashMap<>(8);
        source.put("id", 1);
        source.put("name", "ajin");
        // 3. 创建 PropertyValues
        PropertyValues propertyValues = new MutablePropertyValues(source);

        dataBinder.bind(propertyValues);

        // 4. 输出user内容
        System.out.println(user);

```

> User{id=1, name='ajin', city=null, configLocation=null, workCities=null, lifeCities=null}

### `DataBinder`绑定控制参数

```java
    // 是否忽略未知字段
    private boolean ignoreUnknownFields = true;
    // 是否忽略非法字段
	private boolean ignoreInvalidFields = false;
    // 是否自动增加嵌套路径
	private boolean autoGrowNestedPaths = true;

	private int autoGrowCollectionLimit = DEFAULT_AUTO_GROW_COLLECTION_LIMIT;
	// 绑定字段白名单
	@Nullable
	private String[] allowedFields;
    // 绑定字段黑名单
	@Nullable
	private String[] disallowedFields;
	// 必须绑定字段
	@Nullable
	private String[] requiredFields;
```

