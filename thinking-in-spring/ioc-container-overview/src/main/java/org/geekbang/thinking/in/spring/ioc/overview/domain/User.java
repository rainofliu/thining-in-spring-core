package org.geekbang.thinking.in.spring.ioc.overview.domain;

import org.geekbang.thinking.in.spring.ioc.overview.enums.City;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.List;

/**
 * 用户类
 *
 * @author ajin
 */

public class User {
    private Long id;
    private String name;

    private City city;

    private Resource configLocation;

    private City[] workCities;

    private List<City> lifeCities;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Resource getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }

    public City[] getWorkCities() {
        return workCities;
    }

    public void setWorkCities(City[] workCities) {
        this.workCities = workCities;
    }

    public List<City> getLifeCities() {
        return lifeCities;
    }

    public void setLifeCities(List<City> lifeCities) {
        this.lifeCities = lifeCities;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", city=" + city +
                ", configLocation=" + configLocation +
                ", workCities=" + Arrays.toString(workCities) +
                ", lifeCities=" + lifeCities +
                '}';
    }

    /**
     * 静态方法来创建User Bean
     *
     * @return User
     **/
    public static User createUser() {
        User user = new User();
        user.setId(1L);
        user.setName("ajin");
        return user;
    }
}
