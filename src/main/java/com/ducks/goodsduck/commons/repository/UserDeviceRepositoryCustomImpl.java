package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.QUser;
import com.ducks.goodsduck.commons.model.entity.QUserDevice;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class UserDeviceRepositoryCustomImpl implements UserDeviceRepositoryCustom {

//    private final QNotification notification = QNotification.notification;
    private final QUserDevice userDevice = QUserDevice.userDevice;
    private final QUser user = QUser.user;
    private final JPAQueryFactory queryFactory;

    public UserDeviceRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<String> getRegistrationTokensByUserId(Long userId) {
        return queryFactory.select(userDevice.registrationToken)
                .from(userDevice)
                .where(userDevice.user.id.eq(userId))
                .fetch();
    }

    @Override
    public Tuple getTupleByUserIdAndRegistrationToken(Long userId, String registrationToken) {
        return queryFactory.select(user, userDevice)
                .from(user)
                .join(userDevice).on(user.eq(userDevice.user))
                .where(user.id.eq(userId).and(
                        userDevice.registrationToken.eq(registrationToken)
                ))
                .fetchOne();
    }
}
