package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.QDevice;
import com.ducks.goodsduck.commons.model.entity.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class DeviceRepositoryCustomImpl implements DeviceRepositoryCustom {

    private final QDevice device = QDevice.device;
    private final QUser user = QUser.user;
    private final JPAQueryFactory queryFactory;

    public DeviceRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<String> getRegistrationTokensByUserId(Long userId) {
        return queryFactory.select(device.registrationToken)
                .from(device)
                .where(device.user.id.eq(userId))
                .fetch();
    }

    @Override
    public Long updateRegistrationTokenByUserId(Long userId, String registrationToken) {
        return queryFactory.update(device)
                .set(device.registrationToken, registrationToken)
                .set(device.isAllowed, true)
                .where(device.user.id.eq(userId))
                .execute();
    }

    @Override
    public Long disallowRegistrationTokenByUserId(Long userId) {
        return queryFactory.update(device)
                .set(device.isAllowed, false)
                .where(device.user.id.eq(userId))
                .execute();
    }
}
