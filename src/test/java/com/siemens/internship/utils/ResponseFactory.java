package com.siemens.internship.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.test.web.servlet.MvcResult;

public class ResponseFactory {
    @SneakyThrows
    public static <T> T create(MvcResult result, Class<T> type, ObjectMapper objectMapper) {
        var json = result.getResponse().getContentAsString();
        return objectMapper.readValue(json, type);
    }
}
