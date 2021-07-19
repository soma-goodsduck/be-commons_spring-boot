package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.item.ItemDto;
import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.repository.ItemRepository;
import com.ducks.goodsduck.commons.repository.UserItemRepository;
import com.ducks.goodsduck.commons.repository.UserItemRepositoryCustom;
import com.ducks.goodsduck.commons.repository.UserRepository;
import com.querydsl.core.Tuple;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class UserItemService {

    private final UserItemRepository userItemRepository;
    private final UserItemRepositoryCustom userItemRepositoryCustom;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public UserItemService(UserItemRepository userItemRepository, UserItemRepositoryCustom userItemRepositoryCustom, UserRepository userRepository, ItemRepository itemRepository) {
        this.userItemRepository = userItemRepository;
        this.userItemRepositoryCustom = userItemRepositoryCustom;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    public List<ItemDto> getLikeItemsOfUser(Long userId) {

        return userItemRepositoryCustom.findTupleByUserId(userId)
                .stream()
                .map(tuple -> {
                    var item = tuple.get(1, Item.class);
                    var categoryItem = tuple.get(2, CategoryItem.class);
                    var idolMember = tuple.get(3, IdolMember.class);

                    var itemDto = new ItemDto(item);
                    itemDto.setCategoryItem(categoryItem);
                    itemDto.setUserSimpleDto(new UserSimpleDto(item.getUser()));
                    itemDto.setIdolMember(idolMember);
                    itemDto.likesOfMe();
                    return itemDto;
                })
                .collect(Collectors.toList());
    }

    public boolean doLike(Long userId, Long itemId) {

        // HINT: UESR가 이미 좋아한 아이템인지 확인
        List<UserItem> likeItemsOfUser = userItemRepositoryCustom.findByUserIdAndItemId(userId, itemId);

        // HINT: 이미 좋아한 아이템이라면 좋아요 불가
        if (!likeItemsOfUser.isEmpty()) {
            throw new DuplicateRequestException("Like of item already exists.");
        }

        var findUser = userRepository.findById(userId)
                .orElseThrow(
                        () -> new NoResultException("User not founded.")
                );

        var findItem = itemRepository.findById(itemId)
                .map(i -> i.liked())
                .orElseThrow(
                        () -> new NoResultException("Item not founded.")
                );

        userItemRepository.save(new UserItem(findUser, findItem));

        return true;
    }

    public boolean cancelLikeItem(Long userId, Long itemId) {

        Tuple tuple = userItemRepositoryCustom.findTupleByUserIdAndItemId(userId, itemId);
        if (tuple.equals(null)) {
            throw new NoResultException("Can't find record like item by user.");
        }

        var findUserItem = tuple.get(0, UserItem.class);
        var findItem = tuple.get(1, Item.class);
        userItemRepository.delete(findUserItem);
        findItem.unLiked();

        return true;
    }
}
