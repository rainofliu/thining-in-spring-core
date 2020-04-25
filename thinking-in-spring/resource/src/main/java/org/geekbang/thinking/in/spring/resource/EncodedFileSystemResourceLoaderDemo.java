package org.geekbang.thinking.in.spring.resource;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * 带有字符编码的{@link FileSystemResourceLoader} Demo
 *
 * @author ajin
 * @see FileSystemResourceLoader
 * @see FileSystemResource
 * @see EncodedResource
 */
@SuppressWarnings("all")
public class EncodedFileSystemResourceLoaderDemo {

    public static void main(String[] args) throws IOException {
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

    }
}
