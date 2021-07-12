package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.ItemUploadRequest;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;

    public Optional<Item> getDetails(Long itemId) {
        return itemRepository.findById(itemId);
    }

    public Item upload(ItemUploadRequest itemUploadRequest) {
        return itemRepository.save(new Item(itemUploadRequest));
    }
}
