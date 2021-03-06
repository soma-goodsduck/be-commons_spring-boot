package com.ducks.goodsduck.commons.repository.notification;

import com.ducks.goodsduck.commons.model.entity.Notification;
import com.ducks.goodsduck.commons.model.enums.NotificationType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId, Sort sort);
    boolean existsByUserIdAndTypeNotAndIsReadFalse(Long userId, NotificationType type);
    boolean existsByUserIdAndTypeIsAndIsReadFalse(Long userId, NotificationType type);
}
