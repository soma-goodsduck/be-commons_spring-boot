package com.ducks.goodsduck.commons.model.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat {

    @Id
    @Column(name = "chat_id")
    private String id;
    LocalDateTime deletedAt;

    public Chat(String id) {
        this.id = id;
    }
}
