package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.comment.CommentUploadRequest;
import com.ducks.goodsduck.commons.model.dto.comment.CommentUploadRequestV2;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;
    private Long receiveCommentId;
    private Integer level;
    private Boolean isDeleted;
    private Boolean isSecret;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.REMOVE)
    private List<Comment> childComments = new ArrayList<>();

    // v1
    public Comment(User user, Post post, Comment parentComment, CommentUploadRequest commentUploadRequest) {
        this.user = user;
        this.post = post;
        this.parentComment = parentComment;
        this.level = parentComment != null ? parentComment.getLevel() + 1 : 1;
        this.isDeleted = false;
        this.isSecret = commentUploadRequest.getIsSecret();
        this.content = commentUploadRequest.getContent();
        this.createdAt = LocalDateTime.now();
    }

    // v2
    public Comment(User user, Post post, Comment parentComment, CommentUploadRequest commentUploadRequest, Boolean V2) {
        this.user = user;
        this.post = post;
        this.parentComment = parentComment;
        this.level = parentComment != null ? 2 : 1;
        this.isDeleted = false;
        this.isSecret = commentUploadRequest.getIsSecret();
        this.content = commentUploadRequest.getContent();
        this.createdAt = LocalDateTime.now();
    }

    // v3
    public Comment(User user, Post post, Comment receiveComment, CommentUploadRequestV2 commentUploadRequest) {
        this.user = user;
        this.post = post;
        this.receiveCommentId = commentUploadRequest.getReceiveCommentId() != 0 ? commentUploadRequest.getReceiveCommentId() : null;
        this.level = commentUploadRequest.getReceiveCommentId() != 0 ? 2 : 1;
        this.isDeleted = false;
        this.isSecret = commentUploadRequest.getIsSecret();
        this.content = commentUploadRequest.getContent();
        this.createdAt = LocalDateTime.now();
    }
}
