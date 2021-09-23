package com.ducks.goodsduck.commons.config;

import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.repository.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class Scheduler {

    private final ItemRepository itemRepository;

    @Scheduled(cron = "*/5 * * * * *")
    public void delete() {

        System.out.println("@@@@@@@@@@");

        List<Item> deleteItems = itemRepository.findAllWithDeleted();

        for (Item deleteItem : deleteItems) {
            System.out.println(deleteItem.getId());
        }
    }
}
