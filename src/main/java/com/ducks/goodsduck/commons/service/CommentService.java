package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.comment.CommentDto;
import com.ducks.goodsduck.commons.model.dto.comment.CommentSimpleDto;
import com.ducks.goodsduck.commons.model.dto.comment.CommentUpdateRequest;
import com.ducks.goodsduck.commons.model.dto.comment.CommentUploadRequest;
import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.Comment;
import com.ducks.goodsduck.commons.model.entity.Post;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.repository.user.UserRepository;
import com.ducks.goodsduck.commons.repository.comment.CommentRepository;
import com.ducks.goodsduck.commons.repository.comment.CommentRepositoryCustom;
import com.ducks.goodsduck.commons.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentRepositoryCustom commentRepositoryCustom;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public Long uploadComment(CommentUploadRequest commentUploadRequest, Long userId) {

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoResultException("Not Find User in CommentService.uploadComment"));

            Post post = postRepository.findById(commentUploadRequest.getPostId())
                    .orElseThrow(() -> new NoResultException("Not Find Post in CommentService.uploadComment"));

            Comment parentComment = commentUploadRequest.getParentCommentId() != null ?
                    commentRepository.findById(commentUploadRequest.getParentCommentId())
                            .orElseThrow(() -> new NoResultException("Not Find SuperComment in CommentService.uploadComment")) : null;

            Comment comment = new Comment(user, post, parentComment, commentUploadRequest);

            if(parentComment != null) {
                parentComment.getChildComments().add(comment);
            }

            commentRepository.save(comment);

            user.gainExp(5);

            return comment.getId();
        } catch (Exception e) {
            return -1L;
        }
    }

    public Long updateComment(Long commentId, CommentUpdateRequest commentUpdateReuqest) {

        try {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new NoResultException("Not Find comment in CommentService.updateComment"));

            comment.setContent(commentUpdateReuqest.getContent());

            return 1L;
        } catch (Exception e) {
            return -1L;
        }
    }

    public Long deleteComment(Long commentId) {

        try {
            Comment deleteComment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new NoResultException("Not Find comment in CommentService.deleteComment"));

            // 삭제하려는 댓글에, 자식 댓글이 있을 경우
            if(deleteComment.getChildComments().size() != 0) {
                deleteComment.setIsDeleted(true);
                deleteComment.setContent("삭제된 댓글입니다.");
            } else {
                Comment deleteAncestorComment = getDeleteAncestorComment(deleteComment);
                commentRepository.delete(deleteAncestorComment);
            }

            return 1L;
        } catch (Exception e) {
            return -1L;
        }
    }

    // 삭제 가능한 조상 댓글 검색
    private Comment getDeleteAncestorComment(Comment comment) {

        Comment parentComment = comment.getParentComment();

        if(parentComment != null && parentComment.getChildComments().size() == 1 && parentComment.getIsDeleted().equals(true)) {
            return getDeleteAncestorComment(parentComment);
        }

        return comment;
    }

    public List<CommentDto> getCommentsOfPost(Long userId, Long postId) {

        List<Comment> comments = commentRepositoryCustom.findAllByPostId(postId);
        List<CommentDto> topCommentDtos = new ArrayList<>();
        Map<Long, CommentDto> map = new HashMap<>();

        comments.stream().forEach(comment -> {
                    CommentDto commentDto = new CommentDto(comment.getUser(), comment);
                    map.put(comment.getId(), commentDto);

                    if(comment.getParentComment() != null) {
                        commentDto.setReceiver(new UserSimpleDto(comment.getParentComment().getUser()));
                        map.get(comment.getParentComment().getId()).getChildComments().add(commentDto);
                    } else {
                        topCommentDtos.add(commentDto);
                    }

                    if(commentDto.getWriter() != null && commentDto.getWriter().getUserId().equals(userId)) {
                        commentDto.setIsSecret(false);
                    } else if(commentDto.getReceiver() != null && commentDto.getReceiver().getUserId().equals(userId)) {
                        commentDto.setIsSecret(false);
                    }
                });

        return topCommentDtos;
    }

    public List<CommentSimpleDto> getCommentsOfPostV2(Long userId, Long postId) {

        List<Comment> comments = commentRepositoryCustom.findAllByPostId(postId);
        List<CommentDto> topCommentDtos = new ArrayList<>();
        Map<Long, CommentDto> map = new HashMap<>();

        comments.stream().forEach(comment -> {
            CommentDto commentDto = new CommentDto(comment.getUser(), comment);
            map.put(comment.getId(), commentDto);

            if(comment.getParentComment() != null) {
                commentDto.setReceiver(new UserSimpleDto(comment.getParentComment().getUser()));
                map.get(comment.getParentComment().getId()).getChildComments().add(commentDto);
            } else {
                topCommentDtos.add(commentDto);
            }

            if(commentDto.getWriter() != null && commentDto.getWriter().getUserId().equals(userId)) {
                commentDto.setIsSecret(false);
            } else if(commentDto.getReceiver() != null && commentDto.getReceiver().getUserId().equals(userId)) {
                commentDto.setIsSecret(false);
            }
        });

        List<CommentSimpleDto> commentSimpleDtos = new ArrayList<>();
        convertCommentSimpleDtos(topCommentDtos, commentSimpleDtos);
        return commentSimpleDtos;
    }

    private void convertCommentSimpleDtos(List<CommentDto> commentDtos, List<CommentSimpleDto> commentSimpleDtos) {

        for (CommentDto commentDto : commentDtos) {

            commentSimpleDtos.add(new CommentSimpleDto(commentDto));

            if(commentDto.getChildComments() != null) {
                convertCommentSimpleDtos(commentDto.getChildComments(), commentSimpleDtos);
            }
        }
    }
}
