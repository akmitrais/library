package com.mitrais.khotim.library.errors;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.HashMap;
import java.util.Map;

public class ErrorMessages {
    public static Map<String, String> getMessages(BindingResult errors) {
        if (errors.hasErrors()) {
            Map<String, String> messages = new HashMap<>();

            for (ObjectError error : errors.getAllErrors()) {
                messages.put(((FieldError)error).getField(), error.getDefaultMessage());
            }

            return messages;
        }
        return null;
    }
}
