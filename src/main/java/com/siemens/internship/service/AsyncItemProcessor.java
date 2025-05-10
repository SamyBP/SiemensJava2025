package com.siemens.internship.service;

import com.siemens.internship.model.Item;

import java.util.concurrent.CompletableFuture;

public interface AsyncItemProcessor {
    CompletableFuture<Item> process(long itemId);
}
