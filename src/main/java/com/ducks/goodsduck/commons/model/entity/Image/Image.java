package com.ducks.goodsduck.commons.model.entity.Image;

import com.ducks.goodsduck.commons.model.dto.ImageDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "image_type")
public class Image {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;
    private String originName;
    private String uploadName;
    private String url;
    private LocalDateTime deletedAt;

    public Image(ImageDto imageDto) {
        this.originName = imageDto.getOriginName();
        this.uploadName = imageDto.getUploadName();
        this.url = imageDto.getUrl();
    }

    public Image(String originName, String uploadName, String url) {
        this.originName = originName;
        this.uploadName = uploadName;
        this.url = url;
    }
}
