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

    public List<Item> processItemsAsync(List<Long> itemIds) {
        List<CompletableFuture<Item>> processingFutures = new ArrayList<>();

        itemIds.forEach(id -> processingFutures.add(submitTask(id)));

        CompletableFuture<Void> allFuturesDone = CompletableFuture.allOf(processingFutures.toArray(new CompletableFuture[0]));

        return allFuturesDone
                .thenApply(collectResults(processingFutures))
                .join();
    }

    private CompletableFuture<Item> submitTask(long id) {
        logger.debug("Submitting item with id: %d for process".formatted(id));
        return this.itemProcessor.process(id)
                .exceptionally(e -> {
                    logger.warn(e.getMessage());
                    return null;
                });
    }

    private Function<Void, List<Item>> collectResults(List<CompletableFuture<Item>> futures) {
        return v -> futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
    }
}