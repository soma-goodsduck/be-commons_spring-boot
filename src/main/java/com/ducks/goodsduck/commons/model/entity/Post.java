package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.post.PostUploadRequest;
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
    private Integer views;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idol_group_id")
    private IdolGroup idolGroup;

    @OneToMany(mappedBy = "post")
    private List<PostImage> images = new ArrayList<>();

    // TODO : 좋아요 속성 추가

    public Post(PostUploadRequest postUploadRequest) {
        this.title = postUploadRequest.getTitle();
        this.content = postUploadRequest.getContent();
        this.views = 0;
        this.commentCount = 0;
        this.createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
        this.updatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
    }

    public void addImage(PostImage postImage) {
        postImage.setPost(this);
        this.images.add(postImage);
    }

    public void increaseView() {
        this.views++;
    }
}
