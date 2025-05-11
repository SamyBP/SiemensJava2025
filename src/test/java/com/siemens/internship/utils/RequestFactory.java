package com.siemens.internship.utils;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;


public class RequestFactory {
    public static MockHttpServletRequestBuilder create(MockHttpServletRequestBuilder builder, String content) {
        return builder
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
    }
}
