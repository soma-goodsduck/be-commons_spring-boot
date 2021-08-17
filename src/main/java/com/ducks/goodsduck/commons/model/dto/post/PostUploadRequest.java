package com.ducks.goodsduck.commons.model.dto.post;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@NoArgsConstructor
public class PostUploadRequest {

    private String title;
    private String content;
    private Long idolGroupId;
}
