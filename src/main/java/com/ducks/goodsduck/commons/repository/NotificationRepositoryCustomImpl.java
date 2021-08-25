package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Notification;
import com.ducks.goodsduck.commons.model.entity.QNotification;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.ducks.goodsduck.commons.model.enums.NotificationType.*;

@Repository
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    QNotification notification = QNotification.notification;

    public NotificationRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Notification> findByUserIdExceptChat(Long userId) {
        return queryFactory
                .select(notification)
                .from(notification)
                .where(notification.user.id.eq(userId).and(notification.type.ne(CHAT)))
                .orderBy(notification.id.desc())
                .fetch();
    }
}
