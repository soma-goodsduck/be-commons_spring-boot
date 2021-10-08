package com.ducks.goodsduck.commons.model.entity.Image;

import com.ducks.goodsduck.commons.model.entity.Post;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("Post")
public class PostImage extends Image {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public PostImage(Image image) {
        this.setOriginName(image.getOriginName());
        this.setUploadName(image.getUploadName());
        this.setUrl(image.getUrl());
    }
}
