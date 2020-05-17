package org.geekbang.thinking.in.spring.data.binding;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.validation.DataBinder;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link DataBinder}Demo
 *
 * @author ajin
 * @see DataBinder
 */

public class DataBinderDemo {

    public static void main(String[] args) {
        // 1. 创建空白对象
        User user = new User();
        // 2. 创建DataBinder
        DataBinder dataBinder = new DataBinder(user);
        Map<String, Object> source = new HashMap<>(8);
        source.put("id", 1);
        source.put("name", "ajin");

        // PropertyValues存在User中不存在的属性值
        // DataBinder特性1：忽略未知的属性
        source.put("age","18");

        // PropertyValues存在一个嵌套属性 company.name
        // User{id=1, name='ajin', city=null, configLocation=null, workCities=null, lifeCities=null, company=Company{name='geekbang'}}
        // DataBinder特性2：支持嵌套属性
        // Company company=new Company();
        // company.setName("");
        // user.setCompany(company);
        source.put("company.name","geekbang");

        // 3. 创建 PropertyValues
        PropertyValues propertyValues = new MutablePropertyValues(source);
        // NotWritablePropertyException
        // dataBinder.setIgnoreUnknownFields(false);

        // 默认情况调整不变化
        // User{id=1, name='ajin', city=null, configLocation=null, workCities=null, lifeCities=null, company=null}
        dataBinder.setAutoGrowNestedPaths(false);
        dataBinder.setIgnoreInvalidFields(false);

        dataBinder.bind(propertyValues);

        // 4. 输出user内容
        System.out.println(user);

    }
}
