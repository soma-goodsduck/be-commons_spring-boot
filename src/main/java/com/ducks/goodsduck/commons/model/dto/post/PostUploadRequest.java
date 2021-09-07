package com.ducks.goodsduck.commons.model.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostUploadRequest {

    private String title;
    private String content;
    private Long idolGroupId;
    private String postCategory;
}
