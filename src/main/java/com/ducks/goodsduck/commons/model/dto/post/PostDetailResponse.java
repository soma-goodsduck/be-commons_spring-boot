package com.ducks.goodsduck.commons.model.dto.post;

import com.ducks.goodsduck.commons.model.entity.Post;
import com.ducks.goodsduck.commons.model.entity.category.PostCategory;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PostDetailResponse {

    private PostDetailResponsePostOwner postOwner;
    private Long postId;
    private String content;
    private List<PostDetailResponseImage> images = new ArrayList<>();
    private LocalDateTime postCreatedAt;
    private PostCategoryDto postCategory;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLike;
    private Boolean isOwner;

    public PostDetailResponse(Post post) {
        this.postOwner = new PostDetailResponsePostOwner(post.getUser());
        this.postId = post.getId();
        this.content = post.getContent();
        this.images = post.getImages().stream()
                .map(image -> new PostDetailResponseImage(image))
                .collect(Collectors.toList());
        this.postCreatedAt = post.getCreatedAt();
        this.postCategory = new PostCategoryDto(post.getPostCategory());
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.isLike = false;
        this.isOwner = false;
    }

    public void likesOfMe() {
        isLike = true;
    }

    public void myItem() {
        isOwner = true;
    }
}
