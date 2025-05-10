package com.siemens.internship.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTests {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mvc;

    @MockBean
    ItemService itemService;

    @Test
    void test_whenDeleteItem_returnsNoContent() throws Exception {
        var givenId = 1L;

        doNothing().when(itemService).deleteById(givenId);

        mvc.perform(delete("/api/items/{id}", givenId))
                .andExpect(status().isNoContent());
    }

    @Test
    void test_whenCreateItem_invalidEmail_returnsErrorMessageAnd400() throws Exception {
        var givenItem = new Item("test", "test", "invalid-email");

        var expectedErrorDetails = new String[]{"invalid email format"};
        performInvalidCreateRequest(givenItem, expectedErrorDetails);
    }

    @Test
    void test_whenCreateItem_nameEmpty_returnsErrorMessageAnd400() throws Exception {
        var givenItem = new Item("", "test", "email@email.com");

        var expectedErrorDetails = new String[]{"name must not be empty"};
        performInvalidCreateRequest(givenItem, expectedErrorDetails);
    }

    @Test
    void test_whenCreateItem_multipleFieldsInvalid_returnsErrorMessagesAnd400() throws Exception {
        var givenItem = new Item("", "", "invalid_email");

        var expectedErrorDetails = new String[]{"name must not be empty", "description must not be empty", "invalid email format"};
        performInvalidCreateRequest(givenItem, expectedErrorDetails);
    }

    private void performInvalidCreateRequest(Item item, String... expectedErrors) throws Exception {
        var request = post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item));

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").isArray())
                .andExpect(jsonPath("$.detail.length()").value(expectedErrors.length))
                .andExpect(jsonPath("$.detail").value(containsInAnyOrder(expectedErrors)));
    }

    @Test
    void test_whenGetItemById_itemDoesNotExist_returns404() throws Exception {
        var givenId = 1L;
        var exceptionMessage = String.format("Item with id: %d not found", givenId);

        when(itemService.findById(givenId))
                .thenThrow(new EntityNotFoundException(exceptionMessage));

        mvc.perform(get("/api/items/{id}", givenId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsInAnyOrder("Item with id: 1 not found")));
    }
}
