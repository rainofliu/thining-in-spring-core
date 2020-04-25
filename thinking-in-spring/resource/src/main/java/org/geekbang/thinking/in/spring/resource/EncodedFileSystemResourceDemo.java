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

public class EncodedFileSystemResourceDemo {

    public static void main(String[] args) throws IOException {
        String currentFilePath = System.getProperty("user.dir") + "/thinking-in-spring/resource/src/main/java/org/geekbang/thinking/in/spring/resource/EncodedFileSystemResourceDemo.java";
        File currentFile = new File(currentFilePath);
        // FileSystemResource -> WritableResource -> Resource
        FileSystemResource fileSystemResource = new FileSystemResource(currentFilePath);

        EncodedResource encodedResource = new EncodedResource(fileSystemResource, "UTF-8");
        // 字符输入流
        // 字符输入流 try-with-resource AutoClosable
        try (Reader reader = encodedResource.getReader()) {
            System.out.println(IOUtils.toString(reader));
        }

    }
}
