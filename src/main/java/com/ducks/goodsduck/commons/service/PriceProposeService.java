package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.pricepropose.PriceProposeResponse;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.PricePropose;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import com.ducks.goodsduck.commons.repository.*;
import com.ducks.goodsduck.commons.repository.item.ItemRepository;
import com.ducks.goodsduck.commons.repository.item.ItemRepositoryCustom;
import com.querydsl.core.Tuple;
import com.sun.jdi.request.DuplicateRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.enums.PriceProposeStatus.*;

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

    public Optional<PriceProposeResponse> proposePrice(Long userId, Long itemId, int price) {

        // HINT: 해당 유저ID로 아이템ID에 PricePropose한 내역이 있는지 확인
        PricePropose pricePropose = priceProposeRepositoryCustom.findByUserIdAndItemId(userId, itemId);

        if (pricePropose != null) {
            throw new DuplicateRequestException("Propose of price already exists.");
        }

        var findUser = userRepository.findById(userId)
            .orElseThrow(
                    () -> new NoResultException("User not founded."));

        var findItem = itemRepository.findById(itemId)
            .orElseThrow(
                    () -> new NoResultException("Item not founded."));

        if (findItem.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot propose yourself to yourself.");
        }

        var newPricePropose = new PricePropose(findUser, findItem, price);

        PricePropose savedPricePropose = priceProposeRepository.save(newPricePropose);
        PriceProposeResponse priceProposeResponse = new PriceProposeResponse(findUser, findItem, savedPricePropose);

        return Optional.ofNullable(priceProposeResponse);
    }

    public Optional<PriceProposeResponse> cancelPropose(Long userId, Long priceProposeId) throws IllegalAccessException {
        PricePropose findPricePropose = priceProposeRepository.findById(priceProposeId)
                .orElseThrow(
                        () -> new NoResultException("PricePropose not founded."));

        // HINT: 취소하려는 가격 제안의 주체가 요청한 사용자가 아닌 경우, SUGGESTED 상태가 아닌 경우는 처리하지 않는다.
        if (!findPricePropose.getUser().getId().equals(userId)) {
            throw new IllegalAccessException("Propose of price is not given by this user.");
        } else if (!findPricePropose.getStatus().equals(SUGGESTED)) {
            throw new IllegalArgumentException("Propose is not in SUGGESTED.");
        }

        findPricePropose.setStatus(CANCELED);

        return Optional.ofNullable(new PriceProposeResponse(findPricePropose));

    }

    public boolean updatePropose(Long userId, Long priceProposeId, int price) {
        Long count = priceProposeRepositoryCustom.updatePrice(userId, priceProposeId, price);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    public List<PriceProposeResponse> findAllReceiveProposeByUser(Long userId) {
        List<Item> myItemList = itemRepository.findByUserId(userId);
        User findUser = userRepository.findById(userId).orElseThrow(
                () -> new NoResultException("User is not founded."));

        return priceProposeRepositoryCustom.findByItems(myItemList)
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0, Item.class);
                    User proposer = tuple.get(1, User.class);
                    PricePropose pricePropose = tuple.get(2, PricePropose.class);
                    return new PriceProposeResponse(proposer, item, pricePropose);
                })
                .collect(Collectors.toList());
    }

    public List<PriceProposeResponse> findAllProposeByItem(Long userId, Long itemId) {

            List<Tuple> tupleList = priceProposeRepositoryCustom.findByItemId(itemId);

            return tupleList
                .stream()
                .map(tuple ->{
                        User user = tuple.get(0, User.class);
                        Item item = tuple.get(1, Item.class);
                        PricePropose pricePropose = tuple.get(2, PricePropose.class);
                        return new PriceProposeResponse(user, item, pricePropose);
                    })
                .collect(Collectors.toList());
    }

    public List<PriceProposeResponse> findAllGiveProposeByUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("User is not founded."));

        return priceProposeRepositoryCustom.findByUserId(userId)
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0, Item.class);
                    PricePropose pricePropose = tuple.get(1, PricePropose.class);
                    return new PriceProposeResponse(user, item, pricePropose);
                })
                .collect(Collectors.toList());
    }


    public boolean updateProposeStatus(Long userId, Long itemId, Long priceProposeId, PriceProposeStatus status) {
        Tuple userAndItem = itemRepositoryCustom.findByItemId(itemId);
        User findUser = userAndItem.get(0, User.class);
        Item findItem = userAndItem.get(1, Item.class);

        if (!findUser.getId().equals(userId)) {
            return false;
        }

        Long count = priceProposeRepositoryCustom.updateStatus(priceProposeId, status);

        if (count > 0) {
            return true;
        }

        return false;
    }

    public Boolean checkStatus(Long priceProposeId) {
        PricePropose pricePropose = priceProposeRepository.findById(priceProposeId)
                .orElseThrow(() -> {
                    throw new NoResultException("No pricePropose founded.");
                });

        if (pricePropose.getStatus().equals(CANCELED) ||
                pricePropose.getStatus().equals(REFUSED)) {
            return false;
        }

        return true;
    }
}
