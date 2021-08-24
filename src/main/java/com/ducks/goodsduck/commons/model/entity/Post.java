package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.post.PostUploadRequest;
import com.ducks.goodsduck.commons.model.enums.PostType;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private PostType postType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idol_group_id")
    private IdolGroup idolGroup;

    @OneToMany(mappedBy = "post")
    private List<PostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    public Post(PostUploadRequest postUploadRequest) {
        this.title = postUploadRequest.getTitle();
        this.content = postUploadRequest.getContent();
//        this.postType = postUploadRequest.g
        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
        this.createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
        this.updatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
    }

    public void addImage(PostImage image) {
        image.setPost(this);
        this.images.add(image);
    }

    public void increaseView() {
        this.viewCount++;
    }

    public Post like() {
        this.likeCount++;
        return this;
    }

    public Post dislike() {
        this.likeCount--;
        return this;
    }
}
