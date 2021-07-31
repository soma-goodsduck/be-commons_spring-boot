package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.ImageDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;
    private String originName;
    private String uploadName;
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    public Image(ImageDto imageDto) {
        this.originName = imageDto.getOriginName();
        this.uploadName = imageDto.getUploadName();
        this.url = imageDto.getUrl();
    }
}
