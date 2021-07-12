package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.ItemDetailResponse;
import com.ducks.goodsduck.commons.model.dto.ItemUploadRequest;
import com.ducks.goodsduck.commons.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping("/item")
    public ItemDetailResponse uploadItem(@RequestBody ItemUploadRequest itemUploadRequest) {
        return new ItemDetailResponse(
                itemService.upload(itemUploadRequest)
        );
    }

    @GetMapping("/item/{itemId}")
    public ItemDetailResponse getDetails(@PathVariable("itemId") Long itemId) {
        return itemService.getDetails(itemId)
                .map(item -> new ItemDetailResponse(item))
                .orElseGet(() -> new ItemDetailResponse());
    }

}
