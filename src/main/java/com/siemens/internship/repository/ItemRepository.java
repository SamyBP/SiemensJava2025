package com.siemens.internship.repository;

import com.siemens.internship.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    // Doesn't make any sense without the filtering, process the same item multiple times??
    // Taking ids of unprocessed items. Would put a partial index on item.status where status = 'UNPROCESSED'
    @Query("SELECT id FROM  Item where status = 'UNPROCESSED'")
    List<Long> findAllIds();

    int deleteItemById(long id);
}
