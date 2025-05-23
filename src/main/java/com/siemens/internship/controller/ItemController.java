package com.siemens.internship.controller;

import com.siemens.internship.model.Item;
import com.siemens.internship.model.ItemStatus;
import com.siemens.internship.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody @Valid Item item) {
        item.setId(null);
        item.setStatus(ItemStatus.UNPROCESSED);
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        var existingItem = itemService.findById(id);
        return new ResponseEntity<>(existingItem, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody @Valid Item item) {
        var existingItem = itemService.findById(id);
        var status = item.getStatus() != null ? item.getStatus() : existingItem.getStatus();

        item.setId(existingItem.getId());
        item.setStatus(status);

        var updatedItem = itemService.save(item);
        return new ResponseEntity<>(updatedItem, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        var itemIds = itemService.findAllIds();
        return new ResponseEntity<>(itemService.processItemsAsync(itemIds), HttpStatus.OK);
    }
}
