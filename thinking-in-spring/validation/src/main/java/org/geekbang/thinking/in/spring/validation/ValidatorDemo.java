package org.geekbang.thinking.in.spring.validation;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.context.MessageSource;
import org.springframework.validation.*;

import java.util.Locale;

import static org.geekbang.thinking.in.spring.validation.ErrorsMessageDemo.createMessageSource;

/**
 * 自定义{@link Validator} Demo
 *
 * @author ajin
 */

public class ValidatorDemo {

    public static void main(String[] args) {
        // 创建Validator
        Validator validator = new UserValidator();
        //
        User user = new User();
        validator.supports(user.getClass());
        // 创建Errors对象
        Errors errors = new BeanPropertyBindingResult(user, "user");
        validator.validate(user, errors);

        MessageSource messageSource = createMessageSource();
        for (ObjectError error : errors.getAllErrors()) {
            String message = messageSource.getMessage(error.getCode(), error.getArguments(), Locale.getDefault());
            System.out.println(message);
        }

    }

    static class UserValidator implements Validator {
        @Override
        public boolean supports(Class<?> clazz) {
            return User.class.isAssignableFrom(clazz);
        }

        @Override
        public void validate(Object target, Errors errors) {
            User user = (User) target;
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "id", "id.required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "name.required");
            String userName = user.getName();
        }
    }
}
