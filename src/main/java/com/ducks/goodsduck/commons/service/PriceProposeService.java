package com.ducks.goodsduck.commons.service;

import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.ducks.goodsduck.commons.model.dto.PriceProposeResponse;
import com.ducks.goodsduck.commons.model.entity.PricePropose;
import com.ducks.goodsduck.commons.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PriceProposeService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PriceProposeRepository priceProposeRepository;
    private final PriceProposeRepositoryCustom priceProposeRepositoryCustom;

    public PriceProposeService(UserRepository userRepository, ItemRepository itemRepository, PriceProposeRepository priceProposeRepository, PriceProposeRepositoryCustomImpl priceProposeRepositoryCustomImpl) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.priceProposeRepository = priceProposeRepository;
        this.priceProposeRepositoryCustom = priceProposeRepositoryCustomImpl;
    }

    public PriceProposeResponse proposePrice(Long userId, Long itemId, int price) {

        // HINT: 해당 유저ID로 아이템ID에 PricePropose한 내역이 있는지 확인
        List<PricePropose> priceProposeList = priceProposeRepositoryCustom.findByUserIdAndItemId(userId, itemId);
        if (!priceProposeList.isEmpty()) {
            return new PriceProposeResponse(userId, itemId, false);
        }

        var findUser = userRepository.findById(userId)
            .orElseThrow(
                    () -> new ResourceNotFoundException("User not founded."));

        var findItem = itemRepository.findById(itemId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Item not founded."));

        var newPricePropose = new PricePropose(findUser, findItem, price);

        // TODO : .save() 예외 처리 필요 여부 체크
        priceProposeRepository.save(newPricePropose);

        return new PriceProposeResponse(userId, itemId, true);


    }
}
