package com.ducks.goodsduck.commons.model.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@DiscriminatorValue("Profile")
public class ProfileImage extends Image {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public ProfileImage(Image image) {
        this.setOriginName(image.getOriginName());
        this.setUploadName(image.getUploadName());
        this.setUrl(image.getUrl());
    }
}
