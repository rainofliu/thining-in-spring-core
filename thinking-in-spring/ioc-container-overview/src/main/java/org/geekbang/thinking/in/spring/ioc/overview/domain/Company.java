package org.geekbang.thinking.in.spring.ioc.overview.domain;

/**
 * @author ajin
 */

public class Company {
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Company{" +
                "name='" + name + '\'' +
                '}';
    }
}
