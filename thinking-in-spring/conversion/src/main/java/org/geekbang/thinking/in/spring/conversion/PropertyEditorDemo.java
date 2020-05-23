package org.geekbang.thinking.in.spring.conversion;

import java.beans.PropertyEditor;

/**
 * {@link java.beans.PropertyEditor}Demo
 *
 * @author ajin
 */

public class PropertyEditorDemo {

    public static void main(String[] args) {

        // 模拟Spring FrameWork 的操作
        // 有一段文本 name=ajin
        String text = "name=ajin";

        PropertyEditor propertyEditor = new StringToPropertiesPropertyEditor();
        // 传递String类型的内容
        propertyEditor.setAsText(text);
        System.out.println(propertyEditor.getAsText());
        System.out.println(propertyEditor.getValue());
    }
}
