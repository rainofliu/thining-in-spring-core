package org.geekbang.thinking.in.spring.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.*;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * 动态更新资源{@link MessageSource}实现<br/>
 * 1. 定位资源位置<br/>
 * 2. 初始化Properties对象<br/>
 * 3. 实现{@link AbstractMessageSource#resolveCode}方法<br/>
 * 4. 监听资源文件(Java NIO2 WatchService)
 * 5. 线程池处理文件变化
 *
 * @author ajin
 */

public class DynamicResourceMessageSource extends AbstractMessageSource implements ResourceLoaderAware {

    private static final String RESOURCE_FILE_NAME = "msg.properties";

    private static final String RESOURCE_PATH = "/META-INF/" + RESOURCE_FILE_NAME;

    private static final String ENCODING = "utf-8";

    private final Resource messagePropertiesResource;
    private final Properties messageProperties;

    private ResourceLoader resourceLoader;


    private final ExecutorService executorService;

    public DynamicResourceMessageSource() {
        this.messageProperties = loadMessageProperties();
        this.messagePropertiesResource = getMessagePropertiesResource();
        this.executorService = Executors.newSingleThreadExecutor();
        // 增加文件监听
        onMessagePropertiesChanged();
    }

    private void onMessagePropertiesChanged() {
        // 判断资源是否是文件
        if (this.messagePropertiesResource.isFile()) {
            // 获取对应文件系统中的文件
            try {
                File messagePropertiesFile = this.messagePropertiesResource.getFile();
                Path messagePropertiesFilePath = messagePropertiesFile.toPath();
                // 获取文件系统
                FileSystem fileSystem = FileSystems.getDefault();
                // 新建WatchService
                WatchService watchService = fileSystem.newWatchService();
                // 获取资源文件所在目录
                Path dirPath = messagePropertiesFilePath.getParent();
                // 注册WatchService到 messagePropertiesFilePath ，并且关心修改事件
                dirPath.register(watchService, ENTRY_MODIFY);
                processMessagePropertiesChanged(watchService);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 处理文件资源变化(异步)
     *
     * @param watchService
     */
    private void processMessagePropertiesChanged(WatchService watchService) {
        executorService.submit(() ->
        {
            while (true) {
                WatchKey watchKey = watchService.take();
                try {

                    // watchKey 是否有效
                    if (watchKey.isValid()) {
                        for (WatchEvent<?> pollEvent : watchKey.pollEvents()) {
                            Watchable watchable = watchKey.watchable();
                            // 目录路径
                            Path dirPath = (Path) watchable;
                            // 事件所关联的对象即注册目录的子文件（或子目录）
                            // 事件发生源：相对路径
                            Path fileRelativePath = (Path) pollEvent.context();
                            // 处理成绝对路径
                            Path path = dirPath.resolve(fileRelativePath);
                            System.out.println("变化文件路径：" + path);
                        }
                    }
                } finally {
                    if (watchKey != null) {
                        watchKey.reset(); // 重置watchKey
                    }
                }
            }
        });
    }

    private Properties loadMessageProperties() {
        Resource loaderResource = getMessagePropertiesResource();
        EncodedResource encodedResource = new EncodedResource(loaderResource, ENCODING);
        Properties properties = new Properties();
        try (Reader reader = encodedResource.getReader()) {
            properties.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    private Resource getMessagePropertiesResource() {
        ResourceLoader resourceLoader = getResourceLoader();
        Resource resource = resourceLoader.getResource(RESOURCE_PATH);
        return resource;
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String messageFormatPattern = messageProperties.getProperty(code);
        if (StringUtils.hasText(messageFormatPattern)) {
            return new MessageFormat(messageFormatPattern, locale);
        }
        return null;
    }

    private ResourceLoader getResourceLoader() {
        return this.resourceLoader == null ? new DefaultResourceLoader() : this.resourceLoader;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public static void main(String[] args) {
        DynamicResourceMessageSource messageSource = new DynamicResourceMessageSource();
        String message = messageSource.getMessage("name", new Object[]{}, Locale.getDefault());
        System.out.println(message);
    }
}
