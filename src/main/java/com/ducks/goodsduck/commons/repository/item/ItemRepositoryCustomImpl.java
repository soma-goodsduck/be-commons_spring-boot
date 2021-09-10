package com.ducks.goodsduck.commons.repository.item;

import com.ducks.goodsduck.commons.model.dto.ItemFilterDto;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.Image.QImage;
import com.ducks.goodsduck.commons.model.entity.Image.QItemImage;
import com.ducks.goodsduck.commons.model.entity.category.QItemCategory;
import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.querydsl.core.BooleanBuilder;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static com.ducks.goodsduck.commons.model.enums.TradeStatus.*;

@Repository
@Slf4j
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QUserItem userItem = QUserItem.userItem;
    private final QItem item = QItem.item;
    private final QUser user = QUser.user;
    private final QIdolMember idolMember = QIdolMember.idolMember;
    private final QIdolGroup idolGroup = QIdolGroup.idolGroup;
    private final QItemCategory itemCategory = QItemCategory.itemCategory;
    private final QImage image = QImage.image;
    private final QItemImage itemImage = QItemImage.itemImage;
    private final QItemImage subImage = new QItemImage("subImage");
    private final QPricePropose pricePropose = QPricePropose.pricePropose;

    public ItemRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Tuple findByItemId(Long itemId) {

        return queryFactory.select(user, item)
                .from(item)
                .join(item.user, user)
                .where(item.id.eq(itemId))
                .fetchOne();
    }

    @Override
    public List<Item> findAll(Pageable pageable) {
        return queryFactory
                .select(item)
                .from(item)
                .orderBy(item.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    @Override
    public List<Tuple> findAllV2(Pageable pageable, String keyword) {

        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null) {
            builder.and(item.name.contains(keyword));
        }

        builder.and(isHaveImage(subImage).in(1L, null));

        return queryFactory
                .select(item, idolGroup, idolMember, image, itemCategory)
                .from(item)
//                .leftJoin(image).on(image.item.id.eq(item.id))
                .join(item.idolMember, idolMember)
                .join(item.idolMember.idolGroup, idolGroup)
                .join(item.itemCategory, itemCategory)
                .join(item.user, user)
                .where(builder)
                .orderBy(item.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    @Override
    public List<Item> findAllV3(Long itemId) {

        BooleanBuilder builder = new BooleanBuilder();

        if(itemId != 0) {
            builder.and(item.id.lt(itemId));
        }

        return queryFactory
                .select(item)
                .from(item)
                .where(builder.and(item.deletedAt.isNull()))
                .orderBy(item.updatedAt.desc())
                .limit(PropertyUtil.PAGEABLE_SIZE + 1)
                .fetch();
    }

    @Override
    public List<Item> findAllByIdolGroup(Long idolGroupId, Pageable pageable) {
        return queryFactory
                .select(item)
                .from(item)
                .where(item.idolMember.idolGroup.id.eq(idolGroupId))
                .orderBy(item.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    @Override
    public List<Tuple> findAllByIdolGroupV2(Long idolGroupId, Pageable pageable, String keyword) {

        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null) {
            builder.and(item.name.contains(keyword));
        }

        builder.and(isHaveImage(subImage).in(1L, null));

        return queryFactory
                .select(item, idolGroup, idolMember, itemCategory)
                .from(item)
                .join(item.idolMember, idolMember)
                .join(item.idolMember.idolGroup, idolGroup)
                .join(item.itemCategory, itemCategory)
                .join(item.user, user)
                .where(item.idolMember.idolGroup.id.eq(idolGroupId).and(
                        builder
                ))
                .orderBy(item.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    @Override
    public List<Item> findAllByIdolGroupV3(Long idolGroupId, Long itemId) {

        BooleanBuilder builder = new BooleanBuilder();

        if(itemId != 0) {
            builder.and(item.id.lt(itemId));
        }

        return queryFactory
                .select(item)
                .from(item)
                .where(item.idolMember.idolGroup.id.eq(idolGroupId).and(builder).and(item.deletedAt.isNull()))
                .orderBy(item.updatedAt.desc())
                .limit(PropertyUtil.PAGEABLE_SIZE + 1)
                .fetch();
    }

    @Override
    public List<Item> findAllByFilter(ItemFilterDto itemFilterDto, Pageable pageable) {

        List<Long> idolMembersId = itemFilterDto.getIdolMembersId();
        TradeType tradeType = itemFilterDto.getTradeType();
        Long itemCategoryId = itemFilterDto.getItemCategoryId();
        GradeStatus gradeStatus = itemFilterDto.getGradeStatus();
        Long minPrice = itemFilterDto.getMinPrice();
        Long maxPrice = itemFilterDto.getMaxPrice();

        BooleanBuilder builder = new BooleanBuilder();
        if(idolMembersId != null) {
            for (Long idolMemberId : idolMembersId) {
                builder.or(item.idolMember.id.eq(idolMemberId));
            }
        }
        if(tradeType != null) { builder.and(item.tradeType.eq(tradeType)); }
        if(itemCategoryId != null) { builder.and(item.itemCategory.id.eq(itemCategoryId)); }
        if(gradeStatus != null) { builder.and(item.gradeStatus.eq(gradeStatus)); }
        if(minPrice != null) { builder.and(item.price.goe(minPrice)); }
        if(maxPrice != null) { builder.and(item.price.loe(maxPrice)); }

        return queryFactory
                .select(item)
                .from(item)
                .where(builder)
                .orderBy(item.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    @Override
    public List<Tuple> findAllByFilterV2(ItemFilterDto itemFilterDto, Pageable pageable, String keyword) {

        List<Long> idolMembersId = itemFilterDto.getIdolMembersId();
        TradeType tradeType = itemFilterDto.getTradeType();
        Long itemCategoryId = itemFilterDto.getItemCategoryId();
        GradeStatus gradeStatus = itemFilterDto.getGradeStatus();
        Long minPrice = itemFilterDto.getMinPrice();
        Long maxPrice = itemFilterDto.getMaxPrice();

        BooleanBuilder builder = new BooleanBuilder();

        if(idolMembersId != null) {
            for (Long idolMemberId : idolMembersId) {
                builder.or(item.idolMember.id.eq(idolMemberId));
            }
        }

        builder.and(isHaveImage(subImage).in(1L, null));

        if(keyword != null) { builder.and(item.name.contains(keyword)); }
        if(tradeType != null) { builder.and(item.tradeType.eq(tradeType)); }
        if(itemCategoryId != null) { builder.and(item.itemCategory.id.eq(itemCategoryId)); }
        if(gradeStatus != null) { builder.and(item.gradeStatus.eq(gradeStatus)); }
        if(minPrice != null) { builder.and(item.price.goe(minPrice)); }
        if(maxPrice != null) { builder.and(item.price.loe(maxPrice)); }

        return queryFactory
                .select(item, idolGroup, idolMember, itemCategory)
                .from(item)
                .join(item.idolMember, idolMember)
                .join(item.idolMember.idolGroup, idolGroup)
                .join(item.itemCategory, itemCategory)
                .join(item.user, user)
                .where(builder)
                .orderBy(item.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    @Override
    public List<Item> findAllByFilterV3(ItemFilterDto itemFilterDto, Long itemId) {

        Long idolGroupId = itemFilterDto.getIdolGroupId();
        List<Long> idolMembersId = itemFilterDto.getIdolMembersId();
        TradeType tradeType = itemFilterDto.getTradeType();
        Long itemCategoryId = itemFilterDto.getItemCategoryId();
        GradeStatus gradeStatus = itemFilterDto.getGradeStatus();
        Long minPrice = itemFilterDto.getMinPrice();
        Long maxPrice = itemFilterDto.getMaxPrice();

        BooleanBuilder builder = new BooleanBuilder();

        if(idolMembersId != null) {
            for (Long idolMemberId : idolMembersId) {
                builder.or(item.idolMember.id.eq(idolMemberId));
            }
        } else {
            builder.and(item.idolMember.idolGroup.id.eq(idolGroupId));
        }

        if(itemId != 0) {
            builder.and(item.id.lt(itemId));
        }

        if(tradeType != null && !tradeType.equals(TradeType.ALL)) { builder.and(item.tradeType.eq(tradeType)); }
        if(itemCategoryId != null) { builder.and(item.itemCategory.id.eq(itemCategoryId)); }
        if(gradeStatus != null) { builder.and(item.gradeStatus.eq(gradeStatus)); }
        if(minPrice != null) { builder.and(item.price.goe(minPrice)); }
        if(maxPrice != null) { builder.and(item.price.loe(maxPrice)); }

        return queryFactory
                .select(item)
                .from(item)
                .where(builder.and(item.deletedAt.isNull()))
                .orderBy(item.updatedAt.desc())
                .limit(PropertyUtil.PAGEABLE_SIZE + 1)
                .fetch();
    }

    public List<Tuple> findAllByUserIdolGroupsWithUserItem(Long userId, List<UserIdolGroup> userIdolGroups, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();

        if (userIdolGroups.size() != 0) {
            for (UserIdolGroup userIdolGroup : userIdolGroups) {
                builder.or(idolGroup.id.eq(userIdolGroup.getIdolGroup().getId()));
            }
        }

        return queryFactory
                .select(item, userItem)
                .from(item)
                .leftJoin(userItem).on(userItem.user.id.eq(userId), userItem.item.id.eq(item.id))
                .join(item.idolMember, idolMember)
                .join(item.idolMember.idolGroup, idolGroup)
                .join(item.itemCategory, itemCategory)
                .join(item.user, user)
                .where(builder)
                .orderBy(item.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    @Override
    public List<Tuple> findAllByUserIdolGroupsWithUserItemV3(Long userId, List<UserIdolGroup> userIdolGroups, Long itemId) {

        BooleanBuilder builder = new BooleanBuilder();

        if (userIdolGroups.size() != 0) {
            for (UserIdolGroup userIdolGroup : userIdolGroups) {
                builder.or(idolGroup.id.eq(userIdolGroup.getIdolGroup().getId()));
            }
        }

        if(itemId != 0) {
            builder.and(item.id.lt(itemId));
        }

        return queryFactory
                .select(item, userItem)
                .from(item)
                .leftJoin(userItem).on(userItem.user.id.eq(userId), userItem.item.id.eq(item.id))
                .join(item.idolMember, idolMember)
                .join(item.idolMember.idolGroup, idolGroup)
                .join(item.itemCategory, itemCategory)
                .join(item.user, user)
                .where(builder.and(item.deletedAt.isNull()))
                .orderBy(item.updatedAt.desc())
                .limit(PropertyUtil.PAGEABLE_SIZE + 1)
                .fetch();
    }

    @Override
    public List<Tuple> findAllByUserIdolGroupsWithUserItemV2(Long userId, List<UserIdolGroup> userIdolGroups, Pageable pageable, String keyword) {

        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null) {
            builder.and(item.name.contains(keyword));
        }

        if (userIdolGroups.size() != 0) {
            for (UserIdolGroup userIdolGroup : userIdolGroups) {
                builder.or(idolGroup.id.eq(userIdolGroup.getIdolGroup().getId()));
            }
        }

        builder.and(isHaveImage(subImage).in(1L, null));

        return queryFactory
                .select(item, userItem, idolGroup, idolMember, itemCategory)
                .from(item)
                .leftJoin(userItem).on(userItem.user.id.eq(userId), userItem.item.id.eq(item.id))
                .join(item.idolMember, idolMember)
                .join(item.idolMember.idolGroup, idolGroup)
                .join(item.itemCategory, itemCategory)
                .join(item.user, user)
                .where(builder)
                .orderBy(item.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    public List<Tuple> findAllByIdolGroupWithUserItem(Long userId, Long idolGroupId, Pageable pageable) {
        return queryFactory
                .select(item, userItem)
                .from(item)
                .leftJoin(userItem).on(userItem.user.id.eq(userId), userItem.item.id.eq(item.id))
                .where(item.idolMember.idolGroup.id.eq(idolGroupId))
                .orderBy(item.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    public List<Tuple> findAllByIdolGroupWithUserItemV2(Long userId, Long idolGroupId, Pageable pageable, String keyword) {

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(isHaveImage(subImage).in(1L, null));
        if(keyword != null) { builder.and(item.name.contains(keyword)); }

        return queryFactory
                .select(item, userItem)
                .from(item)
                .leftJoin(userItem).on(userItem.user.id.eq(userId), userItem.item.id.eq(item.id))
                .join(item.idolMember, idolMember)
                .join(item.idolMember.idolGroup, idolGroup)
                .join(item.itemCategory, itemCategory)
                .join(item.user, user)
                .where(item.idolMember.idolGroup.id.eq(idolGroupId).and(builder))
                .orderBy(item.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    @Override
    public List<Tuple> findAllByIdolGroupWithUserItemV3(Long userId, Long idolGroupId, Long itemId) {

        BooleanBuilder builder = new BooleanBuilder();

        if(itemId != 0) {
            builder.and(item.id.lt(itemId));
        }

        return queryFactory
                .select(item, userItem)
                .from(item)
                .leftJoin(userItem).on(userItem.user.id.eq(userId), userItem.item.id.eq(item.id))
                .where(item.idolMember.idolGroup.id.eq(idolGroupId).and(builder).and(item.deletedAt.isNull()))
                .orderBy(item.updatedAt.desc())
                .limit(PropertyUtil.PAGEABLE_SIZE + 1)
                .fetch();
    }

    @Override
    public List<Tuple> findAllByFilter(Long userId, ItemFilterDto itemFilterDto, Pageable pageable) {

        List<Long> idolMembersId = itemFilterDto.getIdolMembersId();
        TradeType tradeType = itemFilterDto.getTradeType();
        Long itemCategoryId = itemFilterDto.getItemCategoryId();
        GradeStatus gradeStatus = itemFilterDto.getGradeStatus();
        Long minPrice = itemFilterDto.getMinPrice();
        Long maxPrice = itemFilterDto.getMaxPrice();

        BooleanBuilder builder = new BooleanBuilder();
        if(idolMembersId != null) {
            for (Long idolMemberId : idolMembersId) {
                builder.or(item.idolMember.id.eq(idolMemberId));
            }
        }
        if(tradeType != null) { builder.and(item.tradeType.eq(tradeType)); }
        if(itemCategoryId != null) { builder.and(item.itemCategory.id.eq(itemCategoryId)); }
        if(gradeStatus != null) { builder.and(item.gradeStatus.eq(gradeStatus)); }
        if(minPrice != null) { builder.and(item.price.goe(minPrice)); }
        if(maxPrice != null) { builder.and(item.price.loe(maxPrice)); }

        return queryFactory
                .select(item, userItem)
                .from(item)
                .leftJoin(userItem).on(userItem.user.id.eq(userId), userItem.item.id.eq(item.id))
                .where(builder)
                .orderBy(item.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    @Override
    public List<Tuple> findAllByFilterV2(Long userId, ItemFilterDto itemFilterDto, Pageable pageable, String keyword) {

        List<Long> idolMembersId = itemFilterDto.getIdolMembersId();
        TradeType tradeType = itemFilterDto.getTradeType();
        Long itemCategoryId = itemFilterDto.getItemCategoryId();
        GradeStatus gradeStatus = itemFilterDto.getGradeStatus();
        Long minPrice = itemFilterDto.getMinPrice();
        Long maxPrice = itemFilterDto.getMaxPrice();

        BooleanBuilder builder = new BooleanBuilder();
        if(idolMembersId != null) {
            for (Long idolMemberId : idolMembersId) {
                builder.or(item.idolMember.id.eq(idolMemberId));
            }
        }

        builder.and(isHaveImage(subImage).in(1L, null));

        if(keyword != null) { builder.and(item.name.contains(keyword)); }
        if(tradeType != null) { builder.and(item.tradeType.eq(tradeType)); }
        if(itemCategoryId != null) { builder.and(item.itemCategory.id.eq(itemCategoryId)); }
        if(gradeStatus != null) { builder.and(item.gradeStatus.eq(gradeStatus)); }
        if(minPrice != null) { builder.and(item.price.goe(minPrice)); }
        if(maxPrice != null) { builder.and(item.price.loe(maxPrice)); }

        return queryFactory
                .select(item, userItem, idolGroup, idolMember, itemCategory)
                .from(item)
                .leftJoin(userItem).on(userItem.user.id.eq(userId), userItem.item.id.eq(item.id))
                .join(item.idolMember, idolMember)
                .join(item.idolMember.idolGroup, idolGroup)
                .join(item.itemCategory, itemCategory)
                .join(item.user, user)
                .where(builder)
                .orderBy(item.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    @Override
    public List<Tuple> findAllByFilterV3(Long userId, ItemFilterDto itemFilterDto, Long itemId) {

        Long idolGroupId = itemFilterDto.getIdolGroupId();
        List<Long> idolMembersId = itemFilterDto.getIdolMembersId();
        TradeType tradeType = itemFilterDto.getTradeType();
        Long itemCategoryId = itemFilterDto.getItemCategoryId();
        GradeStatus gradeStatus = itemFilterDto.getGradeStatus();
        Long minPrice = itemFilterDto.getMinPrice();
        Long maxPrice = itemFilterDto.getMaxPrice();

        BooleanBuilder builder = new BooleanBuilder();

        if(idolMembersId != null) {
            for (Long idolMemberId : idolMembersId) {
                builder.or(item.idolMember.id.eq(idolMemberId));
            }
        } else {
            builder.and(item.idolMember.idolGroup.id.eq(idolGroupId));
        }

        if(itemId != 0) {
            builder.and(item.id.lt(itemId));
        }

        if(tradeType != null && !tradeType.equals(TradeType.ALL)) { builder.and(item.tradeType.eq(tradeType)); }
        if(itemCategoryId != null) { builder.and(item.itemCategory.id.eq(itemCategoryId)); }
        if(gradeStatus != null) { builder.and(item.gradeStatus.eq(gradeStatus)); }
        if(minPrice != null) { builder.and(item.price.goe(minPrice)); }
        if(maxPrice != null) { builder.and(item.price.loe(maxPrice)); }

        return queryFactory
                .select(item, userItem)
                .from(item)
                .leftJoin(userItem).on(userItem.user.id.eq(userId), userItem.item.id.eq(item.id))
                .join(item.idolMember.idolGroup, idolGroup)
                .where(builder.and(item.deletedAt.isNull()))
                .orderBy(item.updatedAt.desc())
                .limit(PropertyUtil.PAGEABLE_SIZE + 1)
                .fetch();
    }

    @Override
    public Tuple findByIdWithUserItem(Long userId, Long itemId) {
        return queryFactory.select(item, new CaseBuilder()
                .when(userItem.user.id.eq(userId)).then(1L).otherwise(0L).sum())
                .from(item)
                .leftJoin(userItem).on(userItem.item.eq(item))
                .where(item.id.eq(itemId))
                .groupBy(item)
                .fetchOne();
    }

    @Override
    public List<Item> findAllByUserIdAndTradeStatus(Long userId, TradeStatus status) {

        List<TradeStatus> statusList = new ArrayList<>();
        statusList.add(status);
        BooleanExpression conditionOfTradeStatus;

        switch (status) {
            case BUYING:
                statusList.add(RESERVING);
                conditionOfTradeStatus = item.tradeStatus.in(statusList).and(item.tradeType.eq(TradeType.BUY));
                break;
            case SELLING:
                statusList.add(RESERVING);
                conditionOfTradeStatus = item.tradeStatus.in(statusList).and(item.tradeType.eq(TradeType.SELL));
                break;
            default:
                conditionOfTradeStatus = item.tradeStatus.in(statusList);
                break;

        }

        return queryFactory
                .select(item)
                .from(item)
                .where(item.user.id.eq(userId).and(conditionOfTradeStatus))
                .orderBy(getStatusCompareExpression().desc(), item.updatedAt.desc())
                .fetch();
    }

    private JPQLQuery<Long> isHaveImage(QItemImage subImage) {
        return JPAExpressions
                .select(subImage.count().add(1))
                .from(subImage)
                .where(subImage.id.lt(image.id).and(
                       subImage.item.eq(itemImage.item)
                ));
    }

    @Override
    public long updateTradeStatus(Long itemId, TradeStatus status) {
        return queryFactory.update(item)
                .set(item.tradeStatus, status)
                .where(item.id.eq(itemId))
                .execute();
    }

    @Override
    public Tuple findItemAndUserByItemId(Long itemId) {
        return queryFactory
                .select(item, item.user)
                .from(item)
                .where(item.id.eq(itemId))
                .fetchOne();
    }

    @Override
    public List<Item> findByKeywordWithLimit(List<String> keywords, Long itemId) {

        BooleanBuilder builder = new BooleanBuilder();

        if(itemId != 0) {
            builder.and(item.id.lt(itemId));
        }

        for (String keyword:keywords) {
            builder.and(item.name.contains(keyword));
        }

        return queryFactory
                .select(item)
                .from(item)
                .where(builder.and(item.deletedAt.isNull()))
                .orderBy(item.updatedAt.desc())
                .limit(PropertyUtil.PAGEABLE_SIZE + 1)
                .fetch();
    }

    @Override
    public List<Tuple> findByKeywordWithUserItemAndLimit(Long userId, List<String> keywords, Long itemId) {
        BooleanBuilder builder = new BooleanBuilder();

        if(itemId != 0) {
            builder.and(item.id.lt(itemId));
        }

        for (String keyword:keywords) {
            builder.and(item.name.contains(keyword));
        }

        return queryFactory
                .select(item, userItem)
                .from(item)
                .leftJoin(userItem).on(userItem.user.id.eq(userId), userItem.item.id.eq(item.id))
                .where(builder.and(item.deletedAt.isNull()))
                .orderBy(item.updatedAt.desc())
                .limit(PropertyUtil.PAGEABLE_SIZE + 1)
                .fetch();
    }

    private NumberExpression<Integer> getStatusCompareExpression() {
        return new CaseBuilder()
                .when(item.tradeStatus.eq(RESERVING)).then(1)
                .otherwise(0);
    }
}
