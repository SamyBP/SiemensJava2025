package com.siemens.internship.service;

import com.siemens.internship.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnableAsync
public class UpdateItemProcessorTests {
    @MockBean
    ItemRepository itemRepository;

    @Autowired
    AsyncItemProcessor itemProcessor;

    @Test
    void test_process_itemIsNotFound_EntityNotFoundExceptionIsRaised() {
        var givenId = 1L;

        when(itemRepository.findById(givenId)).thenReturn(Optional.empty());

        CompletionException ex = assertThrows(CompletionException.class, () -> {
            itemProcessor.process(1L).join();
        });

        assertTrue(ex.getCause() instanceof EntityNotFoundException);
    }
}
