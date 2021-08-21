package com.ducks.goodsduck.commons.model.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class PostUploadRequest {

    private String title;
    private String content;
    private Long idolGroupId;
}
