package com.ducks.goodsduck.commons.model.dto.post;

import com.ducks.goodsduck.commons.model.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class MyPost {

    private Long postId;
    private String content;
    private Long idolGroupId;
    private List<PostDetailResponseImage> images = new ArrayList<>();
    private LocalDateTime createdAt;
    private PostCategoryDto postCategory;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;

    public MyPost(Post post) {
        this.postId = post.getId();
        this.content = post.getContent();
        this.idolGroupId = post.getIdolGroup().getId();
        this.images = post.getImages().stream()
                .map(image -> new PostDetailResponseImage(image))
                .collect(Collectors.toList());
        this.createdAt = post.getCreatedAt();
        this.postCategory = new PostCategoryDto(post.getPostCategory());
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
    }
}
