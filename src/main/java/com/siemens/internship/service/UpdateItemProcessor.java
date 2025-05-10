package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.model.ItemStatus;
import com.siemens.internship.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class UpdateItemProcessor implements AsyncItemProcessor {
    private static final Logger logger = LoggerFactory.getLogger(UpdateItemProcessor.class);
    private final ItemRepository itemRepository;
    private final AtomicInteger processedCount;

    public UpdateItemProcessor(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
        this.processedCount = new AtomicInteger(0);
    }

    @Async
    @Override
    @SneakyThrows
    public CompletableFuture<Item> process(long itemId) {
        logger.debug("Processing item %d".formatted(itemId));

        Thread.sleep(1000);

        var itemToProcess = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item with id: %d not found".formatted(itemId)));

        itemToProcess.setStatus(ItemStatus.PROCESSED);
        var updatedItem = itemRepository.save(itemToProcess);

        this.processedCount.incrementAndGet();

        logger.debug("Finished processing item: %d".formatted(itemId));
        return CompletableFuture.completedFuture(updatedItem);
    }

    public int getProcessedCount() {
        return this.processedCount.get();
    }
}
