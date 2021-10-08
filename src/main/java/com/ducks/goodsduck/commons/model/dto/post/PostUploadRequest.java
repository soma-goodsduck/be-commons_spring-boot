package com.ducks.goodsduck.commons.model.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PostUploadRequest {

    private String content;
    private Long idolGroupId;
    private Long postCategoryId;
}
