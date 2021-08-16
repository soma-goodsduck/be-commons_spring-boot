package com.ducks.goodsduck.commons.model.dto.post;

import com.ducks.goodsduck.commons.model.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PostDetailResponse {

    private PostDetailResponsePostOwner postOwner;
    private Long postId;
    private String title;
    private String content;
    private List<PostDetailResponseImage> images = new ArrayList<>();
    private LocalDateTime postCreatedAt;
    private Integer viewCount;
    private Integer likeCount;
    private Boolean isLike;
    private Boolean isOwner;

    public PostDetailResponse(Post post) {
        this.postOwner = new PostDetailResponsePostOwner(post.getUser());
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.images = post.getImages().stream()
                .map(image -> new PostDetailResponseImage(image))
                .collect(Collectors.toList());
        this.postCreatedAt = post.getCreatedAt();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
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
