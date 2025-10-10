package org.balanceus.topping.infrastructure.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class TemplateExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleTemplateException(Exception e, HttpServletRequest request) {
        log.error("Template rendering error on path: {}", request.getRequestURI(), e);
        
        ModelAndView mav = new ModelAndView();
        mav.setViewName("error/500");
        mav.addObject("error", "페이지를 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        mav.addObject("path", request.getRequestURI());
        
        return mav;
    }
}