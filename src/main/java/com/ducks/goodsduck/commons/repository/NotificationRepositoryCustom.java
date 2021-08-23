package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Notification;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepositoryCustom {
    List<Notification> findByUserIdExceptChat(Long userId);
}
