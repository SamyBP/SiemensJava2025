package com.siemens.internship.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.model.Item;
import com.siemens.internship.model.ItemStatus;
import com.siemens.internship.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

        var expectedErrorResponse = new ErrorResponse(Set.of("invalid email format"));
        performInvalidCreateRequest(givenItem, expectedErrorResponse);
    }

    @Test
    void test_whenCreateItem_nameEmpty_returnsErrorMessageAnd400() throws Exception {
        var givenItem = new Item("", "test", "email@email.com");

        var expectedErrorResponse = new ErrorResponse(Set.of("name must not be empty"));
        performInvalidCreateRequest(givenItem, expectedErrorResponse);
    }

    @Test
    void test_whenCreateItem_multipleFieldsInvalid_returnsErrorMessagesAnd400() throws Exception {
        var givenItem = new Item("", "", "invalid_email");

        var expectedErrorResponse = new ErrorResponse(Set.of("name must not be empty", "description must not be empty", "invalid email format"));
        performInvalidCreateRequest(givenItem, expectedErrorResponse);
    }

    @Test
    void test_whenCreateItem_returnsExpectedItem() throws Exception {
        var givenItem = new Item("test", "test", "test@test.com");
        when(itemService.save(givenItem)).thenReturn(givenItem);

        var request = prepareRequestWithPayload(givenItem);
        var result = mvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var response = createItemFromRequestResult(result);
        assertEquals(response, givenItem);
    }

    private void performInvalidCreateRequest(Item item, ErrorResponse expectedErrorResponse) throws Exception {
        var request = prepareRequestWithPayload(item);

        var result =  mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn();

        var response = createErrorResponseFromRequestResult(result);
        assertEquals(response, expectedErrorResponse);
    }

    @Test
    void test_whenUpdateItem_invalidBody_returnsErrorResponseWith400() throws Exception {
        var givenId = 1L;

        var request = put("/api/items/{id}", givenId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Item()));

        mvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_whenUpdateItem_ItemDoesNotExist_returnsErrorResponseWith404() throws Exception {
        var givenId = 1L;
        var givenItem = new Item("name", "description", "email@email.com");

        when(itemService.findById(givenId)).thenThrow(new EntityNotFoundException("Item 1 not found"));

        var request = put("/api/items/{id}", givenId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenItem));

        var result = mvc.perform(request)
                .andExpect(status().isNotFound())
                .andReturn();

        var response = createErrorResponseFromRequestResult(result);
        assertEquals(response, new ErrorResponse(Set.of("Item 1 not found")));
    }

    @Test
    void test_whenUpdateItem_returnsUpdatedItem() throws Exception {
        var givenId = 1L;
        var givenItem = new Item(null, "name", "description",  ItemStatus.PROCESSED, "email@email.com");
        var existingItem = new Item(givenId, "name", "description", ItemStatus.UNPROCESSED, "email@email.com");
        var updatedItem = new Item(givenId, "name", "description", ItemStatus.PROCESSED, "email@email.com");

        when(itemService.findById(givenId)).thenReturn(existingItem);
        when(itemService.save(givenItem)).thenReturn(updatedItem);

        var request = put("/api/items/{id}", givenId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenItem));

        mvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    void test_whenGetItemById_itemDoesNotExist_returns404() throws Exception {
        var givenId = 1L;
        var exceptionMessage = "Item with id: %d not found".formatted(givenId);

        when(itemService.findById(givenId))
                .thenThrow(new EntityNotFoundException(exceptionMessage));

        var result = mvc.perform(get("/api/items/{id}", givenId))
                .andExpect(status().isNotFound())
                .andReturn();

        var response = createErrorResponseFromRequestResult(result);
        assertEquals(response, new ErrorResponse(Set.of(exceptionMessage)));
    }

    @Test
    void test_whenGetItemById_returns200AndItem() throws Exception {
        var givenId = 1L;
        var expectedItem = new Item(1L);

        when(itemService.findById(givenId)).thenReturn(expectedItem);

        var result = mvc.perform(get("/api/items/{id}", givenId))
                .andExpect(status().isOk())
                .andReturn();

        var response = createItemFromRequestResult(result);
        assertEquals(response, expectedItem);
    }

    @Test
    void test_whenGetAllItems_returns200() throws Exception {
        var expectedItems = Stream.generate(Item::new).limit(5).toList();
        when(itemService.findAll()).thenReturn(expectedItems);

        mvc.perform(get("/api/items"))
                .andExpect(status().isOk());
    }

    @Test
    void test_whenProcessItems_returns200() throws Exception {
        var givenIds = List.of(1L, 2L, 3L, 4L);
        var expectedItems = givenIds.stream().map(Item::new).toList();

        when(itemService.findAllIds()).thenReturn(givenIds);
        when(itemService.processItemsAsync(givenIds)).thenReturn(expectedItems);

        mvc.perform(get("/api/items"))
                .andExpect(status().isOk());
    }

    private ErrorResponse createErrorResponseFromRequestResult(MvcResult result) throws Exception {
        var json = result.getResponse().getContentAsString();
        return objectMapper.readValue(json, ErrorResponse.class);
    }

    private Item createItemFromRequestResult(MvcResult result) throws Exception {
        var json = result.getResponse().getContentAsString();
        return objectMapper.readValue(json, Item.class);
    }

    private MockHttpServletRequestBuilder prepareRequestWithPayload(Item payload) throws JsonProcessingException {
        return post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload));
    }
}
