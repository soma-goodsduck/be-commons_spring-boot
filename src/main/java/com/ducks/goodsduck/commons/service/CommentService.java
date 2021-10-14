package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.exception.common.NotFoundDataException;
import com.ducks.goodsduck.commons.exception.user.UnauthorizedException;
import com.ducks.goodsduck.commons.model.dto.comment.*;
import com.ducks.goodsduck.commons.model.dto.notification.NotificationMessage;
import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.Comment;
import com.ducks.goodsduck.commons.model.entity.Post;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.ActivityType;
import com.ducks.goodsduck.commons.repository.device.DeviceRepositoryCustom;
import com.ducks.goodsduck.commons.model.redis.NotificationRedis;
import com.ducks.goodsduck.commons.repository.notification.NotificationRedisTemplate;
import com.ducks.goodsduck.commons.repository.user.UserRepository;
import com.ducks.goodsduck.commons.repository.comment.CommentRepository;
import com.ducks.goodsduck.commons.repository.comment.CommentRepositoryCustom;
import com.ducks.goodsduck.commons.repository.post.PostRepository;
import com.ducks.goodsduck.commons.util.FcmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.enums.NotificationType.COMMENT;
import static com.ducks.goodsduck.commons.model.enums.NotificationType.REPLY_COMMENT;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentRepositoryCustom commentRepositoryCustom;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final DeviceRepositoryCustom deviceRepositoryCustom;

    private final NotificationRedisTemplate notificationRedisTemplate;

    private final MessageSource messageSource;

    public Long uploadComment(CommentUploadRequest commentUploadRequest, Long userId) {

        try {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoResultException("Not Find User in CommentService.uploadComment"));

            Post post = postRepository.findById(commentUploadRequest.getPostId())
                    .orElseThrow(() -> new NoResultException("Not Find Post in CommentService.uploadComment"));

            Comment parentComment = commentUploadRequest.getParentCommentId() != 0L ?
                    commentRepository.findById(commentUploadRequest.getParentCommentId())
                            .orElseThrow(() -> new NoResultException("Not Find SuperComment in CommentService.uploadComment")) : null;

            Comment comment = new Comment(user, post, parentComment, commentUploadRequest);

            if(parentComment != null) {
                parentComment.getChildComments().add(comment);
            }

            commentRepository.save(comment);

            if (user.gainExpByType(ActivityType.COMMENT) >= 100){
                if (user.getLevel() == null) user.setLevel(1);
                user.levelUp();
                List<String> registrationTokensByUserId = deviceRepositoryCustom.getRegistrationTokensByUserId(user.getId());
                FcmUtil.sendMessage(NotificationMessage.ofLevelUp(), registrationTokensByUserId);
            }

            return comment.getId();
        } catch (Exception e) {
            return -1L;
        }
    }

    public Long uploadCommentV2(CommentUploadRequest commentUploadRequest, Long userId) {

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoResultException("Not Find User in CommentService.uploadComment"));

            Post post = postRepository.findById(commentUploadRequest.getPostId())
                    .orElseThrow(() -> new NoResultException("Not Find Post in CommentService.uploadComment"));

            Comment parentComment = commentUploadRequest.getParentCommentId() != 0L ?
                    commentRepository.findById(commentUploadRequest.getParentCommentId())
                            .orElseThrow(() -> new NoResultException("Not Find SuperComment in CommentService.uploadComment")) : null;

            Comment comment = new Comment(user, post, parentComment, commentUploadRequest, true);

            if(parentComment != null) {
                parentComment.getChildComments().add(comment);
            }

            post.increaseCommentCount();
            commentRepository.save(comment);

            if (user.gainExpByType(ActivityType.COMMENT) >= 100){
                if (user.getLevel() == null) user.setLevel(1);
                user.levelUp();
                List<String> registrationTokensByUserId = deviceRepositoryCustom.getRegistrationTokensByUserId(user.getId());
                FcmUtil.sendMessage(NotificationMessage.ofLevelUp(), registrationTokensByUserId);
            }

            return comment.getId();
        } catch (Exception e) {
            return -1L;
        }
    }

    public Long uploadCommentV3(CommentUploadRequestV2 commentUploadRequest, Long userId) {

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoResultException("Not Find User in CommentService.uploadComment"));

            Post post = postRepository.findById(commentUploadRequest.getPostId())
                    .orElseThrow(() -> new NoResultException("Not Find Post in CommentService.uploadComment"));

            Comment receiveComment = commentUploadRequest.getReceiveCommentId() != 0 ?
                            commentRepository.findById(commentUploadRequest.getReceiveCommentId())
                            .orElseThrow(() -> new NoResultException("Not Find SuperComment in CommentService.uploadComment")) : null;

            Comment comment = new Comment(user, post, receiveComment, commentUploadRequest);

            if(receiveComment != null && receiveComment.getParentComment() == null) {
                receiveComment.getChildComments().add(comment);
                comment.setParentComment(receiveComment);
            } else if(receiveComment != null && receiveComment.getParentComment() != null) {
                receiveComment.getParentComment().getChildComments().add(comment);
                comment.setParentComment(receiveComment.getParentComment());
            }

            post.increaseCommentCount();
            commentRepository.save(comment);

            List<String> registrationTokensByUserId = new ArrayList<>();

            if (user.gainExpByType(ActivityType.COMMENT) >= 100){
                if (user.getLevel() == null) user.setLevel(1);
                user.levelUp();
                registrationTokensByUserId = deviceRepositoryCustom.getRegistrationTokensByUserId(user.getId());
                FcmUtil.sendMessage(NotificationMessage.ofLevelUp(), registrationTokensByUserId);
            }

            User postWriter = post.getUser();
            NotificationMessage notificationMessageOfPostWriter = NotificationMessage.ofComment(user, post, COMMENT);
            List<String> registrationTokensOfPostWriter = new ArrayList<>();

            NotificationRedis notificationRedis = new NotificationRedis(comment.getId(), post.getId(), commentUploadRequest.getReceiveCommentId(), user.getNickName(), user.getImageUrl());

            // HINT: 게시글 주인이 댓글 다는 경우는 보내지 않음.
            if (!postWriter.getId().equals(user.getId())) {
                registrationTokensOfPostWriter = deviceRepositoryCustom.getRegistrationTokensByUserId(postWriter.getId());
                notificationRedisTemplate.saveNotificationKeyAndValueByUserId(postWriter.getId(), notificationRedis);
            }


            // 대댓글인 경우
            if (!commentUploadRequest.getReceiveCommentId().equals(0L) || commentUploadRequest.getReceiveCommentId() == null) {
                User receiver = receiveComment.getUser();
                NotificationMessage notificationMessageOfReplyComment = NotificationMessage.ofComment(user, post, REPLY_COMMENT);
                List<String> registrationTokensByReceiver = new ArrayList<>();

                // HINT: 자신한테 대댓글 다는 경우는 제외
                if (!receiver.getId().equals(user.getId()) && !postWriter.getId().equals(user.getId())) {
                    registrationTokensByReceiver = deviceRepositoryCustom.getRegistrationTokensByUserId(receiver.getId());
                    notificationRedisTemplate.saveNotificationKeyAndValueByUserId(receiver.getId(), notificationRedis);
                }
                if (!registrationTokensByReceiver.isEmpty()) FcmUtil.sendMessage(notificationMessageOfReplyComment, registrationTokensByReceiver);
            }

            if (!registrationTokensOfPostWriter.isEmpty()) FcmUtil.sendMessage(notificationMessageOfPostWriter, registrationTokensOfPostWriter);

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

    public Long deleteComment(Long userId, Long commentId) {

        try {
            Comment deleteComment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new NoResultException("Not Find comment in CommentService.deleteComment"));
            Post post = deleteComment.getPost();

            if(!deleteComment.getUser().getId().equals(userId)) {
                throw new UnauthorizedException("You don't have authority to delete comment");
            }

            // 삭제하려는 댓글에, 자식 댓글이 있을 경우
            if(deleteComment.getChildComments().size() != 0) {
                deleteComment.setIsDeleted(true);
                deleteComment.setContent("삭제된 댓글입니다.");
            } else {
                Comment deleteAncestorComment = getDeleteAncestorComment(deleteComment);
                commentRepository.delete(deleteAncestorComment);
            }

            // 댓글 개수 반영
            Integer commentCount = commentRepository.countByPostId(post.getId());
            post.setCommentCount(commentCount);

            return 1L;
        } catch (Exception e) {
            return -1L;
        }
    }

    public Long deleteCommentV2(Long userId, Long commentId) {

        try {
            Comment deleteComment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new NoResultException("Not Find comment in CommentService.deleteComment"));
            Post post = deleteComment.getPost();

            if(!deleteComment.getUser().getId().equals(userId)) {
                throw new UnauthorizedException("You don't have authority to delete comment");
            }

            post.decreaseCommentCount();
            deleteComment.setDeletedAt(LocalDateTime.now());

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

        List<CommentDto> topCommentDtos = new ArrayList<>();
        Map<Long, CommentDto> map = new HashMap<>();

        List<Comment> comments = commentRepositoryCustom.findAllByPostId(postId);

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
                commentDto.setIsLoginUserComment(true);
            }

            if(commentDto.getReceiver() != null && commentDto.getReceiver().getUserId().equals(userId)) {
                commentDto.setIsSecret(false);
            }

            if(comment.getPost().getUser().equals(userId)) {
                commentDto.setIsPostOwnerComment(true);
            }
        });

        return topCommentDtos;
    }

    public List<CommentSimpleDto> getCommentsOfPostV2(Long userId, Long postId) {

        List<CommentDto> topCommentDtos = new ArrayList<>();
        Map<Long, CommentDto> map = new HashMap<>();

        List<Comment> comments = commentRepositoryCustom.findAllByPostId(postId);

        comments.stream().forEach(comment -> {
            CommentDto commentDto = new CommentDto(comment.getUser(), comment);
            map.put(comment.getId(), commentDto);

            if(comment.getParentComment() != null) {
                commentDto.setReceiver(new UserSimpleDto(comment.getParentComment().getUser()));
                map.get(comment.getParentComment().getId()).getChildComments().add(commentDto);
            } else {
                topCommentDtos.add(commentDto);
            }

            // 댓글작성자 = 로그인유저
            if(commentDto.getWriter() != null && commentDto.getWriter().getUserId().equals(userId)) {
                commentDto.setIsSecret(false);
                commentDto.setIsLoginUserComment(true);
            }

            // 댓글수신자 = 로그인유저
            if(commentDto.getReceiver() != null && commentDto.getReceiver().getUserId().equals(userId)) {
                commentDto.setIsSecret(false);
            }

            // 댓글수신자 = 포스트주인
            if(commentDto.getReceiver() == null && comment.getPost().getUser().getId().equals(userId)) {
                commentDto.setIsSecret(false);
            }

            // 댓글작성자 = 포스트주인
            if(commentDto.getWriter() != null && commentDto.getWriter().getUserId().equals(comment.getPost().getUser().getId())) {
                commentDto.setIsPostOwnerComment(true);
            }

            // 비밀댓글 체크
            if(commentDto.getIsSecret()) {
                commentDto.setContent("비밀 댓글입니다.");
            }
            commentDto.setIsSecret(comment.getIsSecret());
        });

        List<CommentSimpleDto> commentSimpleDtos = new ArrayList<>();
        convertCommentSimpleDtos(topCommentDtos, commentSimpleDtos);
        return commentSimpleDtos;
    }

    public List<CommentDto> getCommentsOfPostV3(Long userId, Long postId) {

        return commentRepositoryCustom.findTopCommentsByPostId(postId)
                .stream()
//                .filter(comment -> comment.getDeletedAt() == null)
                .map(comment -> {

                    CommentDto topCommentDto = new CommentDto(comment.getUser(), comment);
                    List<Comment> childComments = comment.getChildComments();

                    // 삭제댓글 체크
                    if(comment.getDeletedAt() != null) {
                        topCommentDto.setContent("삭제된 댓글입니다.");
                    }

                    // 대댓글 체크
                    for (Comment childComment : childComments) {

                        if(childComment.getDeletedAt() == null) {
                            CommentDto childCommentDto = new CommentDto(childComment.getUser(), childComment);
                            Comment receiveComment = commentRepository.findById(childComment.getReceiveCommentId()).get();
                            childCommentDto.setReceiver(new UserSimpleDto(receiveComment.getUser()));

                            // 비밀댓글 체크
                            checkSecretComment(userId, childComment, childCommentDto);

                            // 댓글의 자식으로 대댓글 삽입
                            topCommentDto.getChildComments().add(childCommentDto);
                        }
                    }

                    // 비밀댓글 체크
                    checkSecretComment(userId, comment, topCommentDto);

                    return topCommentDto;
                })
                .collect(Collectors.toList());
    }

    public List<CommentSimpleDto> getCommentsOfPostV4(Long userId, Long postId) {

        List<CommentDto> topCommentDtos = commentRepositoryCustom.findTopCommentsByPostId(postId)
                .stream()
//                .filter(comment -> comment.getDeletedAt() == null)
                .map(comment -> {

                    CommentDto topCommentDto = new CommentDto(comment.getUser(), comment);
                    List<Comment> childComments = comment.getChildComments();

                    // 삭제댓글 체크
                    if (comment.getDeletedAt() != null) {
                        topCommentDto.setContent("삭제된 댓글입니다.");
                    }

                    // 대댓글 체크
                    for (Comment childComment : childComments) {

                        if(childComment.getDeletedAt() == null) {
                            CommentDto childCommentDto = new CommentDto(childComment.getUser(), childComment);
                            Comment receiveComment = commentRepository.findById(childComment.getReceiveCommentId()).get();
                            childCommentDto.setReceiver(new UserSimpleDto(receiveComment.getUser()));

                            // 비밀댓글 체크
                            checkSecretComment(userId, childComment, childCommentDto);

                            // 댓글의 자식으로 대댓글 삽입
                            topCommentDto.getChildComments().add(childCommentDto);
                        }
                    }

                    // 비밀댓글 체크
                    checkSecretComment(userId, comment, topCommentDto);

                    return topCommentDto;
                })
                .collect(Collectors.toList());

        List<CommentSimpleDto> commentSimpleDtos = new ArrayList<>();
        convertCommentSimpleDtos(topCommentDtos, commentSimpleDtos);
        return commentSimpleDtos;
    }

    private void checkSecretComment(Long userId, Comment comment, CommentDto commentDto) {

        // 댓글작성자 = 로그인유저
        if(commentDto.getWriter() != null && commentDto.getWriter().getUserId().equals(userId)) {
            commentDto.setIsSecret(false);
            commentDto.setIsLoginUserComment(true);
        }

        // 댓글수신자 = 로그인유저
        if(commentDto.getReceiver() != null && commentDto.getReceiver().getUserId().equals(userId)) {
            commentDto.setIsSecret(false);
        }

        // 댓글수신자 = 포스트주인
        if(commentDto.getReceiver() == null && comment.getPost().getUser().getId().equals(userId)) {
            commentDto.setIsSecret(false);
        }

        // 댓글작성자 = 포스트주인
        if(commentDto.getWriter() != null && commentDto.getWriter().getUserId().equals(comment.getPost().getUser().getId())) {
            commentDto.setIsPostOwnerComment(true);
        }

        // 비밀댓글 체크
        if(commentDto.getIsSecret()) {
            commentDto.setContent("비밀 댓글입니다.");
        }
        commentDto.setIsSecret(comment.getIsSecret());
    }

    private void convertCommentSimpleDtos(List<CommentDto> commentDtos, List<CommentSimpleDto> commentSimpleDtos) {

        for (CommentDto commentDto : commentDtos) {

            commentSimpleDtos.add(new CommentSimpleDto(commentDto));

            if(commentDto.getChildComments() != null) {
                convertCommentSimpleDtos(commentDto.getChildComments(), commentSimpleDtos);
            }
        }
    }

    public Boolean changeCommentState(Long userId, Long commentId) {

        try {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                            new Object[]{"Comment"}, null)));

            // 댓글 주인이 아닌 경우
            if(!comment.getUser().getId().equals(userId)) {
                throw new UnauthorizedException("You don't have authority to delete comment");
            }

            if(comment.getIsSecret()) {
                comment.setIsSecret(false);
            } else {
                comment.setIsSecret(true);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
