package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.dto.item.ItemDetailResponse;
import com.ducks.goodsduck.commons.model.entity.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private QUserItem userItem = QUserItem.userItem;
    private QItem item = QItem.item;
    private QUser user = QUser.user;
    private QIdolMember idolMember = QIdolMember.idolMember;
    private QIdolGroup idolGroup = QIdolGroup.idolGroup;
    private QCategoryItem categoryItem = QCategoryItem.categoryItem;

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

    // 태호
//    @Override
//    public List<Tuple> findAllWithUserItem(Long userId, Pageable pageable) {
//
//        JPAQuery<Tuple> query = queryFactory.select(item, new CaseBuilder()
//                    .when(userItem.user.id.eq(userId)).then(1L)
//                    .otherwise(0L)
//                    .sum(), idolMember, idolGroup, categoryItem, user)
//                    .from(item)
//                    .groupBy(item, idolMember, idolGroup, categoryItem, user)
//                    .leftJoin(userItem).on(userItem.item.id.eq(item.id))
//                    .join(idolMember).on(item.idolMember.eq(idolMember))
//                    .join(idolGroup).on(idolMember.idolGroup.eq(idolGroup))
//                    .join(categoryItem).on(item.categoryItem.eq(categoryItem))
//                    .join(user).on(item.user.eq(user));
//
//        pageable.getSort().stream().forEach(order -> {
//            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
//
//            PathBuilder orderByExpression = new PathBuilder(Item.class, "item");
//            query.orderBy(new OrderSpecifier(direction, orderByExpression.get(order.getProperty())));
//        });
//
//        return query.fetch();
//    }

    // 경원1
    // FEAT : 체크용
    @Override
    public List<Tuple> findAllWithUserItem(Long userId) {
        return queryFactory
                .select(item, userItem, idolMember, idolGroup, categoryItem)
                .from(item)
                .leftJoin(userItem).on(userItem.item.id.eq(item.id))
                .join(item.idolMember, idolMember)
                .join(item.categoryItem, categoryItem)
                .join(item.user, user)
                .join(idolGroup).on(idolMember.idolGroup.eq(idolGroup))
                .orderBy(item.createdAt.desc())
                .fetch();
    }

    // 경원2
    // FEAT : 회원별 필터링 기능 적용 전
    @Override
    public List<Tuple> findAllWithUserItem(Long userId, Pageable pageable) {
        return queryFactory
                .select(item, userItem, idolGroup, idolMember, categoryItem)
                .from(item)
                .leftJoin(userItem).on(userItem.item.id.eq(item.id))
                .join(item.idolMember, idolMember)
                .join(item.idolMember.idolGroup, idolGroup)
                .join(item.categoryItem, categoryItem)
                .join(item.user, user)
                .where()
                .orderBy(item.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()+1)
                .fetch();
    }

    // 경원3
    // FEAT : 좋아요, 회원별 좋아하는 아이돌 필터링 기능 적용 (태호-쿼리문(4번), 경원3-(5번->1번이 앞쪽에서 userId 파악시에 더 추가))
    @Override
    public List<Tuple> findAllWithUserItemIdolGroup(Long userId, List<UserIdolGroup> userIdolGroups, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();
        if(userIdolGroups.size() != 0) {
            for (UserIdolGroup userIdolGroup : userIdolGroups) {
                builder.or(idolGroup.id.eq(userIdolGroup.getIdolGroup().getId()));
            }
        }

        return queryFactory
                .select(item, userItem, idolGroup, idolMember, categoryItem)
                .from(item)
                .leftJoin(userItem).on(userItem.item.id.eq(item.id))
                .join(item.idolMember, idolMember)
                .join(item.idolMember.idolGroup, idolGroup)
                .join(item.categoryItem, categoryItem)
                .join(item.user, user)
                .where(builder)
                .orderBy(item.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()+1)
                .fetch();

//        return queryFactory.select(item, new CaseBuilder()
//                .when(userItem.user.id.eq(userId)).then(1L)
//                .otherwise(0L)
//                .sum(), idolMember, idolGroup, categoryItem, user)
//                .from(item)
//                .groupBy(item, idolMember, idolGroup, categoryItem, user)
//                .leftJoin(userItem).on(userItem.item.id.eq(item.id))
//                .join(idolMember).on(item.idolMember.eq(idolMember))
//                .join(idolGroup).on(idolMember.idolGroup.eq(idolGroup))
//                .join(categoryItem).on(item.categoryItem.eq(categoryItem))
//                .join(user).on(item.user.eq(user))
//                .where(builder)
//                .orderBy(item.createdAt.desc())
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize()+1)
//                .fetch();
    }

    @Override
    public Tuple findByIdWithUserItem(Long userId, Long itemId) {
        return queryFactory.select(item, new CaseBuilder().when(userItem.user.id.eq(userId)).then(1L).otherwise(0L))
                .from(item)
                .leftJoin(userItem).on(userItem.item.eq(item))
                .where(item.id.eq(itemId))
                .fetchOne();

    }
}
