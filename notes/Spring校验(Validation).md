[toc]

# Spring校验使用场景

1. Spring常规校验(Validator)

   > 用途较少

2. Spring 数据绑定(Data Binder)

3. Spring Web参数绑定(Web Data Binder)

4. Spring WebMVC/WebFlux 处理方法参数校验

# Validator接口设计

特别不好用

```java
public class UserLoginValidator implements Validator {
  
      private static final int MINIMUM_PASSWORD_LENGTH = 6;
  
      public boolean supports(Class clazz) {
         return UserLogin.class.isAssignableFrom(clazz);
      }
  
      public void validate(Object target, Errors errors) {
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "field.required");
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "field.required");
         UserLogin login = (UserLogin) target;
         if (login.getPassword() != null
               && login.getPassword().trim().length() < MINIMUM_PASSWORD_LENGTH) {
            errors.rejectValue("password", "field.min.length",
                  new Object[]{Integer.valueOf(MINIMUM_PASSWORD_LENGTH)},
                  "The password must be at least [" + MINIMUM_PASSWORD_LENGTH + "] characters in length.");
         }
      }
   }
```

# Errors接口设计



# Errors文案来源

# 自定义Validator



# Validator的救赎

Bean Validation

# 面试题