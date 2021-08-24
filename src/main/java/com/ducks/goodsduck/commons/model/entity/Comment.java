package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.comment.CommentUploadRequest;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;
    private Integer level;
    private Boolean isDeleted;
    private Boolean isSecret;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    private List<Comment> childComments = new ArrayList<>();

    public Comment(User user, Post post, Comment parentComment, CommentUploadRequest commentUploadRequest) {
        this.user = user;
        this.post = post;
        this.parentComment = parentComment;
        this.level = parentComment != null ? parentComment.getLevel() + 1 : 1;
        this.isDeleted = false;
        this.isSecret = commentUploadRequest.getIsSecret();
        this.content = commentUploadRequest.getContent();
    }
}
