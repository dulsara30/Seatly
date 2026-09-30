package com.seatly.backend.common.config;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

// StringTrimModule only sees JSON bodies; this trims query params and path variables ("?tag=").
@ControllerAdvice
public class RequestParamTrimmingAdvice {

    private static final boolean EMPTY_AS_NULL = true;

    @InitBinder
    public void trimStringParameters(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(EMPTY_AS_NULL));
    }
}
