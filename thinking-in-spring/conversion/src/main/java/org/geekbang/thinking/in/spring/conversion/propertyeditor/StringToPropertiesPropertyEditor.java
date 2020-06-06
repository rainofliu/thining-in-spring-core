package org.geekbang.thinking.in.spring.conversion.propertyeditor;


import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

/**
 * String -> Properties {@link PropertyEditor}
 *
 * @author ajin
 */

public class StringToPropertiesPropertyEditor extends PropertyEditorSupport {

    // 1. 实现setAsText
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        Properties properties = new Properties();
        try {
            // 2. String 类型转换成Properties对象
            properties.load(new StringReader(text));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        // 3. 临时存储Properties对象
        setValue(properties);
        // 4. 获取Properties临时对象

    }

    @Override
    public String getAsText() {
        Properties properties = (Properties) getValue();
        StringBuilder textBuilder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            textBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append(System.getProperty("line.separator"));
        }
        return textBuilder.toString();
    }
}
