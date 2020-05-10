package org.geekbang.thinking.in.spring.validation;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.Locale;

/**
 * 错误文案Demo
 *
 * @author ajin
 * @see Errors
 */

public class ErrorsMessageDemo {

    public static void main(String[] args) {
        // 0. 创建User对象
        User user = new User();
        user.setName("haha");
        // 1. 选择Errors实现 BeanPropertyBindingResult
        Errors errors = new BeanPropertyBindingResult(user, "user");
        // 2. 调用reject/rejectValue
        // reject  -> 生成ObjectError
        // rejectValue  -> 生成FieldError
        errors.reject("user.properties.not.null");
        errors.rejectValue("name","name.required");

        // 3. 获取errors中的ObjectError或者FieldError
        List<ObjectError> globalErrors = errors.getGlobalErrors();
        List<FieldError> fieldErrors = errors.getFieldErrors();
        List<ObjectError> allErrors = errors.getAllErrors();

        // 4. 通过errors中的code和arguments关联MessageSource实现
        MessageSource messageSource = createMessageSource();
        for (ObjectError allError : allErrors) {
            String message = messageSource.getMessage(allError.getCode(), allError.getArguments(), Locale.getDefault());
            System.out.println(message);
        }


    }

    private static MessageSource createMessageSource() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("user.properties.not.null", Locale.getDefault(), "the properties of user must not be null");
        messageSource.addMessage("name.required", Locale.getDefault(), "the name of user must not be null");
        return messageSource;
    }
}
