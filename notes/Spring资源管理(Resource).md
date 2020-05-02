[toc]

# Spring资源管理

## Java标准资源管理

### Java标准资源定位

| 职责         | 说明                                                         |
| ------------ | ------------------------------------------------------------ |
| 面向资源     | 文件系统，artifact(jar、war、ear等资源)以及远程资源(HTTP、FTP等) |
| API整合      | `java.lang.ClassLoader#getResource`、`java.io.File`、`java.net.URL` |
| 资源定位     | `java.net.URL`、`java.net.URI`                               |
| 面向流式存储 | `java.net.URLConnection`                                     |
| 协议扩展     | `java.net.URLStreamHandler`/`java.net.URLStreamHandlerFactory` |

#### Java URL协议扩展

+ 基于`java.net.URLStreamHandler`
+ 基于`java.net.URLStreamHandlerFactory`

## Spring资源接口

| 类型       | 接口                                                  |
| ---------- | :---------------------------------------------------- |
| 输入流资源 | `org.springframework.core.io.InputStreamSource`       |
| 只读资源   | `org.springframework.core.io.Resource`                |
| 可写资源   | `org.springframework.core.io.WritableResource`        |
| 编码资源   | `org.springframework.core.io.support.EncodedResource` |
| 上下文资源 | `org.springframework.core.io.ContextResource`         |

### 输入流资源`InputStreamSource`

```java
public interface InputStreamSource {
    // 获取输入流
    InputStream getInputStream() throws IOException;
}
```

### 只读资源`Resource`

```java
public interface Resource extends InputStreamSource {
    // 资源是否存在
    boolean exists();
    // 资源是否可读
    default boolean isReadable() {
		return exists();
	}
    // 资源所代表的句柄是否被打开
    default boolean isOpen() {
		return false;
	}
    // 是否为File
    default boolean isFile() {
		return false;
	}
    // 返回资源的URI句柄
    URI getURI() throws IOException;
    // 返回资源的File句柄
    File getFile() throws IOException;
    // 返回 ReadableByteChannel
    default ReadableByteChannel readableChannel() throws IOException {
		return Channels.newChannel(getInputStream());
	}
    // 资源内容的长度
    long contentLength() throws IOException;
    // 资源最后的修改时间
    long lastModified() throws IOException;
    // 资源的文件名
    @Nullable
	String getFilename();
    // 资源的描述
    String getDescription();
}
```

### 可写资源`WritableResource`

```java
public interface WritableResource extends Resource {
    default boolean isWritable() {
		return true;
	}
    OutputStream getOutputStream() throws IOException;
    default WritableByteChannel writableChannel() throws IOException {
		return Channels.newChannel(getOutputStream());
	}
}
```

### 编码资源`EncodedResource`

```java
public class EncodedResource implements InputStreamSource {

	private final Resource resource;

	@Nullable
	private final String encoding;

	@Nullable
	private final Charset charset;
    
    /**
	 * Create a new {@code EncodedResource} for the given {@code Resource},
	 * using the specified {@code Charset}.
	 * @param resource the {@code Resource} to hold (never {@code null})
	 * @param charset the {@code Charset} to use for reading from the resource
	 */
	public EncodedResource(Resource resource, @Nullable Charset charset) {
		this(resource, null, charset);
	}

	private EncodedResource(Resource resource, @Nullable String encoding, @Nullable Charset charset) {
		super();
		Assert.notNull(resource, "Resource must not be null");
		this.resource = resource;
		this.encoding = encoding;
		this.charset = charset;
	}
    
    	/**
	 * Open a {@code java.io.Reader} for the specified resource, using the specified
	 * {@link #getCharset() Charset} or {@linkplain #getEncoding() encoding}
	 * (if any).
	 * @throws IOException if opening the Reader failed
	 * @see #requiresReader()
	 * @see #getInputStream()
	 */
	public Reader getReader() throws IOException {
        // 如果指定了字符集
		if (this.charset != null) {
			return new InputStreamReader(this.resource.getInputStream(), this.charset);
		}
		else if (this.encoding != null) {
			return new InputStreamReader(this.resource.getInputStream(), this.encoding);
		}
		else {
			return new InputStreamReader(this.resource.getInputStream());
		}
	}

	/**
	 * Open an {@code InputStream} for the specified resource, ignoring any specified
	 * {@link #getCharset() Charset} or {@linkplain #getEncoding() encoding}.
	 * @throws IOException if opening the InputStream failed
	 * @see #requiresReader()
	 * @see #getReader()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		return this.resource.getInputStream();
	}

}
```

> `EncodedResource`既可以获取字符流也可以获取字节流

### 上下文资源`ContextResource`

> 这里指的是Servlet上下文

```java
public interface ContextResource extends Resource {

	/**
	 * Return the path within the enclosing 'context'.
	 * <p>This is typically path relative to a context-specific root directory,
	 * e.g. a ServletContext root or a PortletContext root.
	 */
	String getPathWithinContext();
}
```

## Spring内建Resource实现

| 资源来源       | 资源协议      | <span style="white-space:nowrap;">实现类&emsp;&emsp;</span>  |
| :------------- | ------------- | :----------------------------------------------------------- |
| Bean定义       | 无            | `org.springframework.beans.factory.support.BeanDefinitionResource` |
| 数组           | 无            | `org.springframework.core.io.ByteArrayResource`              |
| 类路径         | classpath:/   | `org.springframework.core.io.ClassPathResource`              |
| 文件系统       | file:/        | `org.springframework.core.io.FileSystemResource`             |
| URL            | URL支持的协议 | `org.springframework.core.io.UrlResource`                    |
| ServletContext | 无            | `org.springframework.web.context.support.ServletContextResource` |

### `ByteArrayResource`

它是基于纯内存的，不涉及底层，所以用完无需关闭

```java
public class ByteArrayResource extends AbstractResource {
	// 字节数组
	private final byte[] byteArray;

	private final String description;
    
    // 构造方法
    public ByteArrayResource(byte[] byteArray, @Nullable String description) {
		Assert.notNull(byteArray, "Byte array must not be null");
		this.byteArray = byteArray;
		this.description = (description != null ? description : "");
	}
    
    
	/**
	 * Return the underlying byte array.
	 */
	public final byte[] getByteArray() {
		return this.byteArray;
	}

	/**
	 * This implementation always returns {@code true}.
	 */
	@Override
	public boolean exists() {
		return true;
	}

	/**
	 * This implementation returns the length of the underlying byte array.
	 */
	@Override
	public long contentLength() {
		return this.byteArray.length;
	}
    /**
	 * This implementation returns a ByteArrayInputStream for the
	 * underlying byte array.
	 * @see java.io.ByteArrayInputStream
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(this.byteArray);
	}
/**
	 * This implementation compares the underlying byte array.
	 * @see java.util.Arrays#equals(byte[], byte[])
	 */
	@Override
	public boolean equals(@Nullable Object other) {
        // 如果指针指向同一对象，一定相等；如果是ByteArrayResource类型并且数组每一个元素都相等
		return (this == other || (other instanceof ByteArrayResource &&
				Arrays.equals(((ByteArrayResource) other).byteArray, this.byteArray)));
	}

	/**
	 * This implementation returns the hash code based on the
	 * underlying byte array.
	 */
	@Override
	public int hashCode() {
		return (byte[].class.hashCode() * 29 * this.byteArray.length);
	}        
 ｝   
```

> 这里为什么要重写hashCode，这样设计 是否可以减少HashMap中key的hashCode冲突

```java
// Arrays.equals()  
public static boolean equals(byte[] a, byte[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
}
```

### `ClasspathResource`

```java
public class ClassPathResource extends AbstractFileResolvingResource {
	// 路径
	private final String path;

	@Nullable
	private ClassLoader classLoader;

	@Nullable
	private Class<?> clazz;
    
    public ClassPathResource(String path, @Nullable ClassLoader classLoader) {
		Assert.notNull(path, "Path must not be null");
		String pathToUse = StringUtils.cleanPath(path);
		if (pathToUse.startsWith("/")) {
			pathToUse = pathToUse.substring(1);
		}
		this.path = pathToUse;
		this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
	}
    
    	@Nullable
	protected URL resolveURL() {
		if (this.clazz != null) {
			return this.clazz.getResource(this.path);
		}
		else if (this.classLoader != null) {
			return this.classLoader.getResource(this.path);
		}
		else {
			return ClassLoader.getSystemResource(this.path);
		}
	}
    
    	@Override
	public InputStream getInputStream() throws IOException {
		InputStream is;
		if (this.clazz != null) {
			is = this.clazz.getResourceAsStream(this.path);
		}
		else if (this.classLoader != null) {
			is = this.classLoader.getResourceAsStream(this.path);
		}
		else {
			is = ClassLoader.getSystemResourceAsStream(this.path);
		}
		if (is == null) {
			throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
		}
		return is;
	}
/**
	 * This implementation compares the underlying class path locations.
	 */
	@Override
	public boolean equals(@Nullable Object other) {
        
		if (this == other) {
			return true;
		}
        // 被比较对象非ClassPathResource类型 false
		if (!(other instanceof ClassPathResource)) {
			return false;
		}
        // 强转一下
		ClassPathResource otherRes = (ClassPathResource) other;
		return (this.path.equals(otherRes.path) &&
				ObjectUtils.nullSafeEquals(this.classLoader, otherRes.classLoader) &&
				ObjectUtils.nullSafeEquals(this.clazz, otherRes.clazz));
	}

	/**
	 * This implementation returns the hash code of the underlying
	 * class path location.
	 */
	@Override
	public int hashCode() {
        // 这里的hashCode取决于path(String)的hashCode
		return this.path.hashCode();
	}   
}    
```

### `FileSystemResource`

```java
public class FileSystemResource extends AbstractResource implements WritableResource {
    
    private final String path;

	@Nullable
	private final File file;
	// 文件的路径
	private final Path filePath;
    
}
```

### `UrlResource`

```java
public class UrlResource extends AbstractFileResolvingResource {

	/**
	 * Original URI, if available; used for URI and File access.
	 */
	@Nullable
	private final URI uri;

	/**
	 * Original URL, used for actual access.
	 */
	private final URL url;

	/**
	 * Cleaned URL (with normalized path), used for comparisons.
	 */
	private final URL cleanedUrl;
    
    ...
}    
```

## Spring Resource接口扩展

### 可写资源接口

`org.springframework.core.io.WritableResource`

+ `org.springframework.core.io.FileSystemResource`

  ```java
  public class FileSystemResource extends AbstractResource implements WritableResource {
  
  	private final String path;
  
  	@Nullable
  	private final File file;
  	// @since JDK 1.7
  	private final Path filePath;
  }   
  ```

+ `org.springframework.core.io.FileUrlResource`(@since 5.0.2)

  ```java
  public class FileUrlResource extends UrlResource implements WritableResource {
  	@Nullable
  	private volatile File file;
      
      public FileUrlResource(URL url) {
  		super(url);
  	}
  }    
  ```

+ `org.springframework.core.io.PathResource`(@since 4.0 Deprecated)

  > jdk1.7增加了`java.nio.file.Path`类，`FileSystemResource`中包含了Path，所以`PathResource`被弃用了

### 编码资源接口

`org.springframework.core.io.support.EncodedResource`

```java
/**
 * 带有字符编码的{@link FileSystemResource} Demo
 *
 * @author ajin
 * @see FileSystemResource
 * @see EncodedResource
 */

public class FileSystemResourceDemo {

    public static void main(String[] args) throws IOException {
        String currentFilePath = System.getProperty("user.dir") + "/thinking-in-spring/resource/src/main/java/org/geekbang/thinking/in/spring/resource/FileSystemResourceDemo.java";
        File currentFile = new File(currentFilePath);
        // FileSystemResource -> WritableResource -> Resource
        FileSystemResource fileSystemResource = new FileSystemResource(currentFilePath);

        EncodedResource encodedResource = new EncodedResource(fileSystemResource, "UTF-8");
        // 字符输入流
        Reader reader = encodedResource.getReader();
//        CharArrayWriter writer = new CharArrayWriter();
        System.out.println(IOUtils.toString(reader));
    }
}
```

控制台输出结果：

```java
package org.geekbang.thinking.in.spring.resource;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * 带有字符编码的{@link FileSystemResource} Demo
 *
 * @author ajin
 * @see FileSystemResource
 * @see EncodedResource
 */

public class FileSystemResourceDemo {

    public static void main(String[] args) throws IOException {
        String currentFilePath = System.getProperty("user.dir") + "/thinking-in-spring/resource/src/main/java/org/geekbang/thinking/in/spring/resource/FileSystemResourceDemo.java";
        File currentFile = new File(currentFilePath);
        // FileSystemResource -> WritableResource -> Resource
        FileSystemResource fileSystemResource = new FileSystemResource(currentFilePath);

        EncodedResource encodedResource = new EncodedResource(fileSystemResource, "UTF-8");
        // 字符输入流
        Reader reader = encodedResource.getReader();
       // CharArrayWriter writer = new CharArrayWriter();
        System.out.println(IOUtils.toString(reader));
    }
}
```

## Spring资源加载器

Resource加载器

+ `org.springframework.core.io.ResourceLoader`

  + `org.springframework.core.io.DefaultResourceLoader`

    + `org.springframework.core.io.support.ResourcePatternResolver`
    + `org.springframework.core.io.FileSystemResourceLoader`
    + `org.springframework.context.support.AbstractApplicationContext`

  + `org.springframework.core.io.support.ResourcePatternResolver`

    > **通配路径资源加载器**

    + `org.springframework.core.io.support.PathMatchingResourcePatternResolver`
    + `org.springframework.context.ApplicationContext`

### `ResourceLoader`

```java
public interface ResourceLoader {

	/** Pseudo URL prefix for loading from the class path: "classpath:". */
	String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;
    // 获取Resource
    Resource getResource(String location);
    
    @Nullable
	ClassLoader getClassLoader();
}    
```

#### `DefaultResourceLoader`

```java
public class DefaultResourceLoader implements ResourceLoader {

	@Nullable
	private ClassLoader classLoader;

	private final Set<ProtocolResolver> protocolResolvers = new LinkedHashSet<>(4);

	private final Map<Class<?>, Map<Resource, ?>> resourceCaches = new ConcurrentHashMap<>(4);
    
    @Override
	public Resource getResource(String location) {
		Assert.notNull(location, "Location must not be null");

		for (ProtocolResolver protocolResolver : getProtocolResolvers()) {
			Resource resource = protocolResolver.resolve(location, this);
			if (resource != null) {
				return resource;
			}
		}

		if (location.startsWith("/")) {
			return getResourceByPath(location);
		}
		else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
			return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
		}
		else {
			try {
				// Try to parse the location as a URL...
				URL url = new URL(location);
				return (ResourceUtils.isFileURL(url) ? new FileUrlResource(url) : new UrlResource(url));
			}
			catch (MalformedURLException ex) {
				// No URL -> resolve as resource path.
				return getResourceByPath(location);
			}
		}
	}
    
    	/**
	 * ClassPathResource that explicitly expresses a context-relative path
	 * through implementing the ContextResource interface.
	 */
	protected static class ClassPathContextResource extends ClassPathResource implements ContextResource {

		public ClassPathContextResource(String path, @Nullable ClassLoader classLoader) {
			super(path, classLoader);
		}

		@Override
		public String getPathWithinContext() {
			return getPath();
		}

		@Override
		public Resource createRelative(String relativePath) {
			String pathToUse = StringUtils.applyRelativePath(getPath(), relativePath);
			return new ClassPathContextResource(pathToUse, getClassLoader());
		}
	}
}    
```

##### `FileSystemResourceLoader`

```java
public class FileSystemResourceLoader extends DefaultResourceLoader {  
	@Override
	protected Resource getResourceByPath(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return new FileSystemContextResource(path);
	}
private static class FileSystemContextResource extends FileSystemResource implements ContextResource {

		public FileSystemContextResource(String path) {
			super(path);
		}

		@Override
		public String getPathWithinContext() {
			return getPath();
		}
	}
```

下例是通过FileSystemResourceLoader去加载Resource的一个样例

```java
String currentFilePath = System.getProperty("user.dir") + "/thinking-in-spring/resource/src/main/java/org/geekbang/thinking/in/spring/resource/EncodedFileSystemResourceDemo.java";
        File currentFile = new File(currentFilePath);
        // FileSystemResource -> WritableResource -> Resource
//        FileSystemResource fileSystemResource = new FileSystemResource(currentFilePath);
        FileSystemResourceLoader fileSystemResourceLoader = new FileSystemResourceLoader();
        // 通过FileSystemResourceLoader获取Resource
        Resource fileSystemResource = fileSystemResourceLoader.getResource(currentFilePath);
        EncodedResource encodedResource = new EncodedResource(fileSystemResource, "UTF-8");
        // 字符输入流 try-with-resource AutoClosable
        try (Reader reader = encodedResource.getReader()) {
            System.out.println(IOUtils.toString(reader));
        }
```

+ `fileSystemResourceLoader.getResource`

  > `org.springframework.core.io.DefaultResourceLoader#getResource(String location)`

  + `org.springframework.core.io.FileSystemResourceLoader#getResourceByPath`

    > `FileSystemResourceLoader`重写了父类的`getResourceByPath`方法

## Spring通配路径加载器

### 如何理解路径通配Ant模式

+ 通配路径`ResourceLoader`

  + `org.springframework.core.io.support.ResourcePatternResolver`

    + `org.springframework.core.io.support.PathMatchingResourcePatternResolver`

    > 借助`ResourcePatternResolver`，我们可以通过通配路径返回多个`Resource`

+ 路径匹配器

  + `org.springframework.util.PathMatcher`
    + Ant模式匹配实现  —`org.springframework.util.AntPathMatcher`

#### `ResourcePatternResolver`

```java
public interface ResourcePatternResolver extends ResourceLoader {
    // 匹配所有classpath下面的资源
    String CLASSPATH_ALL_URL_PREFIX = "classpath*:";
    Resource[] getResources(String locationPattern) throws IOException;
}
```

> + classpath: 匹配当前classpath下面的资源
> + classpath*: 匹配所有classpath下面的资源

##### `PathMatchingResourcePatternResolver`

```java
public class PathMatchingResourcePatternResolver implements ResourcePatternResolver {
    // resourceLoader一般不会为null
    private final ResourceLoader resourceLoader;

	private PathMatcher pathMatcher = new AntPathMatcher();
    
    // 构造方法
    public PathMatchingResourcePatternResolver() {
		this.resourceLoader = new DefaultResourceLoader();
	}
    public PathMatchingResourcePatternResolver(ResourceLoader resourceLoader) {
		Assert.notNull(resourceLoader, "ResourceLoader must not be null");
		this.resourceLoader = resourceLoader;
	} 
    public PathMatchingResourcePatternResolver(@Nullable ClassLoader classLoader) {
		this.resourceLoader = new DefaultResourceLoader(classLoader);
	}
    
    // 核心方法
    @Override
	public Resource[] getResources(String locationPattern) throws IOException {
		Assert.notNull(locationPattern, "Location pattern must not be null");
        
        //  String CLASSPATH_ALL_URL_PREFIX = "classpath*:";
		if (locationPattern.startsWith(CLASSPATH_ALL_URL_PREFIX)) {
			// a class path resource (multiple resources for same name possible)
			if (getPathMatcher().isPattern(locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length()))) {
				// a class path resource pattern
				return findPathMatchingResources(locationPattern);
			}
			else {
				// all class path resources with the given name
				return findAllClassPathResources(locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length()));
			}
		}
		else {
			// Generally only look for a pattern after a prefix here,
			// and on Tomcat only after the "*/" separator for its "war:" protocol.
			int prefixEnd = (locationPattern.startsWith("war:") ? locationPattern.indexOf("*/") + 1 :
					locationPattern.indexOf(':') + 1);
			if (getPathMatcher().isPattern(locationPattern.substring(prefixEnd))) {
				// a file pattern
				return findPathMatchingResources(locationPattern);
			}
			else {
				// a single resource with the given name
				return new Resource[] {getResourceLoader().getResource(locationPattern)};
			}
		}
	}
}
```

### Spring通配路径资源扩展

+ 实现`org.springframework.util.PathMatcher`
+ 重置`PathMatcher`
  + `org.springframework.core.io.support.PathMatchingResourcePatternResolver#setPathMatcher`

## 依赖注入Spring `Resource`

> 如何在xml和java注解场景注入`Resource`对象

+ 基于`@Value`实现

  > `@Value`除了注入外部化配置外，还可以注入`Resource`资源

  ```java
  @Value("classpath:/...")
  private Resource resource;
  ```

  + 首先在resource下面创建一个文件夹METE-INF，再创建好`default.properties`文件

    ```properties
    name=ajin
    ```

  + 编写一个资源工具类`ResourceUtils`，负责将注入的Resource解析成字符串

    ```java
    public interface ResourceUtils {
    
        static String getContent(Resource resource) {
            try {
                return getContent(resource, "UTF-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    
        static String getContent(Resource resource, String encoding) throws IOException {
            EncodedResource encodedResource = new EncodedResource(resource, encoding);
            // 字符输入流
            // 字符输入流 try-with-resource AutoClosable
            try (Reader reader = encodedResource.getReader()) {
                return IOUtils.toString(reader);
            }
        }
    }
    ```

    > 在jdk1.8中，接口是支持静态方法的，当然我们也可以直接定义为一个普通类

  + 依赖注入`Resource`

    ```java
    /**
     * 注入{@link Resource}对象Demo
     *
     * @author ajin
     * @see Resource
     * @see Value
     * @see AnnotationConfigApplicationContext
     */
    
    public class InjectingResourceDemo {
    	// 注入Resource
        @Value("classpath:/META-INF/default.properties")
        private Resource defaultPropertiesResource;
    	
        // 初始化方法，在依赖注入完成后执行
        @PostConstruct
        public void init() {
            System.out.println(ResourceUtils.getContent(defaultPropertiesResource));
        }
    
        public static void main(String[] args) {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            // 将当前类注册为@Configuration Class
            // @Configuration -> @Component  -> Spring Bean
            context.register(InjectingResourceDemo.class);
            context.refresh();
            context.close();
        }
    }
    ```

    控制台输出结果：

    ```properties
    name=ajin
    ```

    如果我们再用集合注入测试一下的话(在`META-INF`文件夹下再创建一个`properties`文件)

    ```pro
    name=时间旅行者
    ```

    ```java
        @Value("classpath*:/META-INF/*.properties")
        private Resource[] propertiesResources;
    
        @Value("classpath*:/META-INF/*.properties")
        private List<Resource> resources;
    
     Stream.of(propertiesResources).map(ResourceUtils::getContent).forEach(System.out::println);
     System.out.println("======");
            Stream.of(resources.toArray()).map(ResourceUtils::getContent).forEach(System.out::println);
    ```

    测试结果如下：

    ```properties
    name=ajin
    name=\u65F6\u95F4\u65C5\u884C\u8005
    java.io.FileNotFoundException: class path resource [classpath*:/META-INF/*.properties] cannot be opened because it does not exist
    ```

    > 这里证明`List`的方式来注入`Resource`是失败的，而`Resource[]`是可以被成功注入的。
## 依赖注入`ResourceLoader`

方式一：实现`ResourceLoaderAware`接口回调

```java
// ApplicationContextAwareProcessor#postProcessBeforeInitialization
public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (!(bean instanceof EnvironmentAware || bean instanceof EmbeddedValueResolverAware ||
				bean instanceof ResourceLoaderAware || bean instanceof ApplicationEventPublisherAware ||
				bean instanceof MessageSourceAware || bean instanceof ApplicationContextAware)){
			return bean;
		}

		AccessControlContext acc = null;

		if (System.getSecurityManager() != null) {
			acc = this.applicationContext.getBeanFactory().getAccessControlContext();
		}

		if (acc != null) {
			AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
				invokeAwareInterfaces(bean);
				return null;
			}, acc);
		}
		else {
			invokeAwareInterfaces(bean);
		}

		return bean;
	}

private void invokeAwareInterfaces(Object bean) {
		if (bean instanceof EnvironmentAware) {
			((EnvironmentAware) bean).setEnvironment(this.applicationContext.getEnvironment());
		}
		if (bean instanceof EmbeddedValueResolverAware) {
			((EmbeddedValueResolverAware) bean).setEmbeddedValueResolver(this.embeddedValueResolver);
		}
        //  ResourceLoaderAware接口回调
		if (bean instanceof ResourceLoaderAware) {
			((ResourceLoaderAware) bean).setResourceLoader(this.applicationContext);
		}
		if (bean instanceof ApplicationEventPublisherAware) {
			((ApplicationEventPublisherAware) bean).setApplicationEventPublisher(this.applicationContext);
		}
		if (bean instanceof MessageSourceAware) {
			((MessageSourceAware) bean).setMessageSource(this.applicationContext);
		}
		if (bean instanceof ApplicationContextAware) {
			((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
		}
	}
```

方式二：`@Autowired`注入`ResourceLoader`

方式三：注入`ApplicationContext`作为`ResourceLoader`

在`org.springframework.context.support.AbstractApplicationContext#prepareBeanFactory`方法中，我们可以看到，`ApplicationContext`是调用了底层`DefaultListableBeanFactory#registerResolvableDependency`方法

```java
protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        ...
		// BeanFactory interface not registered as resolvable type in a plain factory.
		// MessageSource registered (and found for autowiring) as a bean.
		beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
		beanFactory.registerResolvableDependency(ResourceLoader.class, this);
		beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
		beanFactory.registerResolvableDependency(ApplicationContext.class, this);
	     ...
}
```

> 注入的`ResourceLoader`其实就是`AbstractApplicationContext`类的实现类对象

## 面试题解析

### Spring配置资源中有哪些常见类型

+ XML资源
  + 普通 BeanDefinition XML资源  — .xml
  + Spring Schema资源  — .xsd
+ Properties资源
  + 普通Properties格式资源  —.properties
  + Spring Handler实现类映射文件    `META-INF/spring.handlers`
  + Spring Schema资源映射文件  `META-INF/spring.schemas`
+ YAML资源
  + 普通YAML配置资源    .yaml /.yml

