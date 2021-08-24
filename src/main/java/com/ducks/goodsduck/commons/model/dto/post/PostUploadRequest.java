package com.ducks.goodsduck.commons.model.dto.post;

import com.ducks.goodsduck.commons.model.enums.PostType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class PostUploadRequest {

    private String title;
    private String content;
    private Long idolGroupId;
    private PostType postType;
}
