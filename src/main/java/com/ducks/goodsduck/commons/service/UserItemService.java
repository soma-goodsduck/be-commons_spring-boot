package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.item.ItemDto;
import com.ducks.goodsduck.commons.model.dto.LikeItemResponse;
import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.repository.ItemRepository;
import com.ducks.goodsduck.commons.repository.UserItemRepository;
import com.ducks.goodsduck.commons.repository.UserRepository;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class UserItemService {

    private final UserItemRepository userItemRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final JPAQueryFactory queryFactory;

    private QUser u = new QUser("u");
    private QItem i = new QItem("i");
    private QUserItem ui = new QUserItem("ui");
    private QCategoryItem ci = new QCategoryItem("ci");
    private QIdolMember im = new QIdolMember("im");
    private QIdolGroup ig = new QIdolGroup("ig");

    public UserItemService(UserItemRepository userItemRepository, UserRepository userRepository, ItemRepository itemRepository, EntityManager em) {
        this.userItemRepository = userItemRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<ItemDto> getLikeItemsOfUser(Long userId) {
        List<Tuple> itemList = queryFactory.select(ui, i, ci, im, ig)
                .from(ui)
                .join(ui.item, i)
                .join(i.categoryItem, ci)
                .join(i.idolMember, im)
                .join(im.idolGroup, ig)
                .join(ui.user, u)
                .where(ui.user.id.eq(userId))
                .fetch();

        return itemList.stream()
                .map(tuple -> {
                    var item = tuple.get(i);
                    var categoryItem = tuple.get(ci);
                    var idolMember = tuple.get(im);

                    var itemDto = new ItemDto(item);
                    itemDto.setCategoryItem(categoryItem);
                    itemDto.setUserSimpleDto(new UserSimpleDto(item.getUser()));
                    itemDto.setIdolMember(idolMember);
                    itemDto.likesOfMe();
                    return itemDto;
                })
                .collect(Collectors.toList());
    }

    public LikeItemResponse doLike(Long userId, Long itemId) {

        // HINT: UESR가 이미 좋아한 아이템인지 확인
        List<UserItem> likeItemsOfUser = queryFactory.selectFrom(ui)
                .where(ui.user.id.eq(userId).and(ui.item.id.eq(itemId)))
                .fetch();

        // HINT: 이미 좋아한 아이템이라면 좋아요 불가
        if (!likeItemsOfUser.isEmpty()) {
            return new LikeItemResponse(userId, itemId);
        }

        var findUser = userRepository.findById(userId)
                .orElseThrow(
                        () -> new RuntimeException("User not founded.")
                );

        var findItem = itemRepository.findById(itemId)
                .map(item1 -> item1.liked())
                .orElseThrow(
                        () -> new RuntimeException("Item not founded.")
                );

        var savedUserItem = userItemRepository.save(new UserItem(findUser, findItem));
        return new LikeItemResponse(savedUserItem);
    }

    public boolean cancelLikeItem(Long userId, Long itemId) {

        List<Tuple> tupleList = queryFactory.select(ui, i)
                .from(ui)
                .join(ui.item, i).fetchJoin()
                .where(ui.user.id.eq(userId).and(
                        i.id.eq(itemId)
                )).fetch();

        if (tupleList.isEmpty()) {
            return false;
        }

        var tuple = tupleList.get(0);
        var findUserItem = tuple.get(ui);
        var findItem = findUserItem.getItem();
        userItemRepository.delete(findUserItem);
        findItem.unLiked();

        return true;
    }
}
