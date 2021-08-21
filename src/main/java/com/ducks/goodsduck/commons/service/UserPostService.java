package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.post.PostDetailResponse;
import com.ducks.goodsduck.commons.model.entity.Post;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.entity.UserPost;
import com.ducks.goodsduck.commons.repository.UserRepository;
import com.ducks.goodsduck.commons.repository.post.PostRepository;
import com.ducks.goodsduck.commons.repository.post.PostRepositoryCustom;
import com.ducks.goodsduck.commons.repository.post.UserPostRepository;
import com.ducks.goodsduck.commons.repository.post.UserPostRepositoryCustom;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserPostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostRepositoryCustom postRepositoryCustom;
    private final UserPostRepository userPostRepository;
    private final UserPostRepositoryCustom userPostRepositoryCustom;

    public Boolean likePost(Long userId, Long postId) {

        UserPost userPost = userPostRepositoryCustom.findByUserIdAndPostId(userId, postId);
        
        // 이미 좋아요된 상태
        if (userPost != null) {
            return false;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("Not Find User in UserPostService.likePost"));

        Post post = postRepository.findById(postId)
                .map(p -> p.like())
                .orElseThrow(() -> new NoResultException("Not Find Post in UserPostService.likePost"));

        userPostRepository.save(new UserPost(user, post));

        return true;
    }

    public Boolean dislikePost(Long userId, Long postId) {

        UserPost userPost = userPostRepositoryCustom.findByUserIdAndPostId(userId, postId);

        // 이미 좋아요가 풀린 상태
        if (userPost == null) {
            return false;
        }

        postRepository.findById(postId)
                .map(p -> p.dislike())
                .orElseThrow(() -> new NoResultException("Not Find Post in UserPostService.likePost"));

        userPostRepository.delete(userPost);

        return true;
    }
}
