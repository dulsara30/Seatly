package com.seatly.backend.common.config;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * The other half of the global trimmer. StringTrimModule only sees JSON
 * bodies; query parameters and path variables are bound by Spring MVC, not
 * Jackson. Without this, "?tag=" arrives as "" rather than null and filters
 * on an empty tag instead of not filtering at all.
 */
@ControllerAdvice
public class RequestParamTrimmingAdvice {

    private static final boolean EMPTY_AS_NULL = true;

    @InitBinder
    public void trimStringParameters(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(EMPTY_AS_NULL));
    }
}
