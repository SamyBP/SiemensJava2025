package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnableAsync
public class ItemServiceTests {
    @MockBean
    ItemRepository itemRepository;
    @MockBean
    AsyncItemProcessor itemProcessor;
    @Autowired
    ItemService itemService;

    @Test
    void test_whenProcessItemsAsync_oneTaskFails_returnsMinusOneItem() {
        var givenIds = List.of(1L, 2L, 3L, 4L, 5L);
        var idsFailed = List.of(2L, 4L);
        var idsCompleted = List.of(1L, 3L, 5L);

        whenProcessThenReturn(idsFailed, id -> CompletableFuture.failedFuture(new EntityNotFoundException()));
        whenProcessThenReturn(idsCompleted, id -> CompletableFuture.completedFuture(new Item(id)));

        var processedItems = itemService.processItemsAsync(givenIds);
        var expectedItems = createItemSetFromIds(idsCompleted);

        assertEquals(new HashSet<>(processedItems), expectedItems);
    }

    @Test
    void test_whenProcessItemsAsync_allTasksSucceed_returnsAllProcessedItems() {
        var givenIds = List.of(1L, 2L, 3L);

        whenProcessThenReturn(givenIds, id -> CompletableFuture.completedFuture(new Item(id)));

        var processedItems = itemService.processItemsAsync(givenIds);
        var expectedItems = createItemSetFromIds(givenIds);

        assertEquals(new HashSet<>(processedItems), expectedItems);
    }

    private void whenProcessThenReturn(List<Long> ids, Function<Long, CompletableFuture<Item>> futureFactory) {
        ids.forEach(id -> {
            when(itemProcessor.process(id)).thenReturn(futureFactory.apply(id));
        });
    }

    private Set<Item> createItemSetFromIds(List<Long> ids) {
        return ids.stream()
                .map(Item::new)
                .collect(Collectors.toSet());
    }
}
