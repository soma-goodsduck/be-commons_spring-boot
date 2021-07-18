package com.ducks.goodsduck.commons.service;

import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.ducks.goodsduck.commons.model.dto.PriceProposeResponse;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.PricePropose;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import com.ducks.goodsduck.commons.repository.*;
import com.querydsl.core.Tuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PriceProposeService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRepositoryCustom itemRepositoryCustom;
    private final PriceProposeRepository priceProposeRepository;
    private final PriceProposeRepositoryCustom priceProposeRepositoryCustom;

    public PriceProposeService(UserRepository userRepository, ItemRepository itemRepository, ItemRepositoryCustom itemRepositoryCustom, PriceProposeRepository priceProposeRepository, PriceProposeRepositoryCustomImpl priceProposeRepositoryCustomImpl) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.itemRepositoryCustom = itemRepositoryCustom;
        this.priceProposeRepository = priceProposeRepository;
        this.priceProposeRepositoryCustom = priceProposeRepositoryCustomImpl;
    }

    public PriceProposeResponse proposePrice(Long userId, Long itemId, int price) {

        // HINT: 해당 유저ID로 아이템ID에 PricePropose한 내역이 있는지 확인
        List<PricePropose> priceProposeList = priceProposeRepositoryCustom.findByUserIdAndItemId(userId, itemId);
        if (!priceProposeList.isEmpty()) {
            return new PriceProposeResponse(priceProposeList.get(0), false);
        }

        var findUser = userRepository.findById(userId)
            .orElseThrow(
                    () -> new RuntimeException("User not founded."));

        var findItem = itemRepository.findById(itemId)
                .orElseThrow(
                        () -> new RuntimeException("Item not founded."));

        var newPricePropose = new PricePropose(findUser, findItem, price);

        // TODO : .save() 예외 처리 필요 여부 체크
        PricePropose savedPricePropose = priceProposeRepository.save(newPricePropose);

        return new PriceProposeResponse(savedPricePropose, true);

    }

    public PriceProposeResponse cancelPropose(Long userId, Long priceProposeId) {
        PricePropose findPricePropose = priceProposeRepository.findById(priceProposeId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("PricePropose not founded.")
                );

        // HINT: 취소하려는 가격 제안의 주체가 요청한 사용자가 아닌 경우
        if (!findPricePropose.getUser().getId().equals(userId)) {
            return new PriceProposeResponse(findPricePropose, false);
        }

        priceProposeRepository.delete(findPricePropose);

        return new PriceProposeResponse(findPricePropose, true);

    }

    public boolean updatePropose(Long userId, Long priceProposeId, int price) {
        long count = priceProposeRepositoryCustom.updatePrice(userId, priceProposeId, price);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    public List<PriceProposeResponse> findAllReceiveProposeByUser(Long userId) {
        List<Item> myItemList = itemRepository.findByUserId(userId);
        for (Item item: myItemList) {
            System.out.println("item = " + item);
        }
        List<PricePropose> priceProposesByItems = priceProposeRepositoryCustom.findByItems(myItemList);
        User findUser = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User is not founded.")
        );

        return priceProposesByItems
                .stream()
                .map(pricePropose ->
                        new PriceProposeResponse(findUser, pricePropose, true))
                .collect(Collectors.toList());
    }

    public List<PriceProposeResponse> findAllProposeByItem(Long userId, Long itemId) {

            List<Tuple> tupleList = priceProposeRepositoryCustom.findByItemId(itemId);

            return tupleList
                .stream()
                .map(tuple ->{
                        User user = tuple.get(0, User.class);
                        PricePropose pricePropose = tuple.get(1, PricePropose.class);
                        return new PriceProposeResponse(user, pricePropose, true);
                    })
                .collect(Collectors.toList());
    }

    public List<PriceProposeResponse> findAllGiveProposeByUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User is not founded."));

        return priceProposeRepository.findByUserId(userId)
                .stream()
                .map(pricePropose -> new PriceProposeResponse(user, pricePropose, true))
                .collect(Collectors.toList());
    }


    public boolean updateProposeStatus(Long userId, Long itemId, Long priceProposeId, PriceProposeStatus status) {
        Tuple userAndItem = itemRepositoryCustom.findByItemId(itemId);
        User findUser = userAndItem.get(0, User.class);
        Item findItem = userAndItem.get(1, Item.class);

        if (!findUser.getId().equals(userId)) {
            return false;
        }

        long count = priceProposeRepositoryCustom.updateStatus(priceProposeId, status);

        if (count > 0) {
            return true;
        }

        return false;
    }
}
