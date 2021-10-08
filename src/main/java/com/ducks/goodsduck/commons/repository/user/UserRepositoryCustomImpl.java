package com.ducks.goodsduck.commons.repository.user;

import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private QUser user = QUser.user;

    public UserRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Long updateRoleByUserId(Long userId, UserRole role) {
        return queryFactory
                .update(user)
                .set(user.role, role)
                .where(user.id.eq(userId))
                .execute();
    }

    @Override
    public Long initializeVotedIdolGroupIdAll() {
        return queryFactory
                .update(user)
                .set(user.votedIdolGroupId, 0L)
                .execute();
    }
}
