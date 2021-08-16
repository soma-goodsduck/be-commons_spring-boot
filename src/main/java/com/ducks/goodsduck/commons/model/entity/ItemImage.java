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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("Item")
public class ItemImage extends Image {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    public ItemImage(Image image) {
        this.setOriginName(image.getOriginName());
        this.setUploadName(image.getUploadName());
        this.setUrl(image.getUrl());
    }
}
