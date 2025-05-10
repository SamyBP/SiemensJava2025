package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Service
public class ItemService {
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    private final ItemRepository itemRepository;
    private final AsyncItemProcessor itemProcessor;

    public ItemService(ItemRepository itemRepository, AsyncItemProcessor itemProcessor) {
        this.itemRepository = itemRepository;
        this.itemProcessor = itemProcessor;
    }


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Item with id: %d not found", id)));
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    public List<Long> findAllIds() {
        return itemRepository.findAllIds();
    }

    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     * <p>
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    public List<Item> processItemsAsync(List<Long> itemIds) {
        List<CompletableFuture<Item>> futures = new ArrayList<>();

        itemIds.forEach(id -> futures.add(submitTask(id)));

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        Function<Void, List<Item>> await = v -> futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();

        return allFutures.thenApply(await).join();
    }

    private CompletableFuture<Item> submitTask(long id) {
        logger.debug("Submitting item with id: %d for process".formatted(id));
        return this.itemProcessor.process(id)
                .exceptionally(e -> {
                    logger.warn(e.getMessage());
                    return null;
                });
    }
}