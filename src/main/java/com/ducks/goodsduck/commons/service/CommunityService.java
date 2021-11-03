package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.LoginUser;
import com.ducks.goodsduck.commons.model.dto.category.CategoryResponse;
import com.ducks.goodsduck.commons.model.dto.comment.MyComment;
import com.ducks.goodsduck.commons.model.dto.home.HomeResponse;
import com.ducks.goodsduck.commons.model.dto.post.MyPost;
import com.ducks.goodsduck.commons.model.dto.post.PostDetailResponse;
import com.ducks.goodsduck.commons.model.entity.Post;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.entity.UserPost;
import com.ducks.goodsduck.commons.repository.category.CommunityCategoryRepository;
import com.ducks.goodsduck.commons.repository.comment.CommentRepository;
import com.ducks.goodsduck.commons.repository.comment.CommentRepositoryCustom;
import com.ducks.goodsduck.commons.repository.post.PostRepository;
import com.ducks.goodsduck.commons.repository.post.PostRepositoryCustom;
import com.ducks.goodsduck.commons.repository.user.UserRepository;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostRepositoryCustom postRepositoryCustom;
    private final CommentRepository commentRepository;
    private final CommentRepositoryCustom commentRepositoryCustom;
    private final CommunityCategoryRepository communityCategoryRepository;

    public List<CategoryResponse> getCommunityCategory() {
        return communityCategoryRepository.findAll()
                .stream()
                .map(communityCategory -> new CategoryResponse(communityCategory))
                .collect(Collectors.toList());
    }

    public HomeResponse getMyPost(Long userId, Long postId) {

        int pageableSize = PropertyUtil.POST_PAGEABLE_SIZE;
        Boolean hasNext = false;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("Not find user in PostController.getPostsWithFilterIdolGroup"));

        List<PostDetailResponse> postList = postRepositoryCustom.findByUserId(userId, postId)
                .stream()
                .map(tuple -> {
                    Post post = tuple.get(0, Post.class);
                    UserPost userPost = tuple.get(1, UserPost.class);

                    PostDetailResponse postDetailResponse = new PostDetailResponse(post);

                    if(userPost != null) {
                        postDetailResponse.likesOfMe();
                    }

                    if(post.getUser().getId().equals(userId)) {
                        postDetailResponse.myItem();
                    }

                    return postDetailResponse;
                })
                .collect(Collectors.toList());

        if(postList.size() == pageableSize + 1) {
            hasNext = true;
            postList.remove(pageableSize);
        }

        return new HomeResponse(hasNext, new LoginUser(user), postList);
    }

    public HomeResponse getMyComment(Long userId, Long commentId) {

        int pageableSize = PropertyUtil.POST_PAGEABLE_SIZE;
        Boolean hasNext = false;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("Not find user in PostController.getPostsWithFilterIdolGroup"));

        List<MyComment> commentList = commentRepositoryCustom.findByUserId(userId, commentId)
                .stream()
                .map(comment -> new MyComment(comment))
                .collect(Collectors.toList());
        if(commentList.size() == pageableSize + 1) {
            hasNext = true;
            commentList.remove(pageableSize);
        }

        return new HomeResponse(hasNext, new LoginUser(user), commentList);
    }

    public HomeResponse getMyCommentV2(Long userId, Long postId) {

        int pageableSize = PropertyUtil.POST_PAGEABLE_SIZE;
        Boolean hasNext = false;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("Not find user in PostController.getPostsWithFilterIdolGroup"));

        List<PostDetailResponse> commentList = commentRepositoryCustom.findByUserIdV2(userId, postId)
                .stream()
                .map(tuple -> {
                    Post post = tuple.get(0, Post.class);
                    UserPost userPost = tuple.get(1, UserPost.class);

                    PostDetailResponse postDetailResponse = new PostDetailResponse(post);

                    if(userPost != null) {
                        postDetailResponse.likesOfMe();
                    }

                    if(post.getUser().getId().equals(userId)) {
                        postDetailResponse.myItem();
                    }

                    return postDetailResponse;
                })
                .collect(Collectors.toList());

        if(commentList.size() == pageableSize + 1) {
            hasNext = true;
            commentList.remove(pageableSize);
        }

        return new HomeResponse(hasNext, new LoginUser(user), commentList);
    }

    public HomeResponse getMyLikePost(Long userId, Long postId) {

        int pageableSize = PropertyUtil.POST_PAGEABLE_SIZE;
        Boolean hasNext = false;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("Not find user in PostController.getPostsWithFilterIdolGroup"));

        List<PostDetailResponse> postList = postRepositoryCustom.findAllWithUserPost(userId, postId, user.getBlockedUserIds(), user.getBlockedPostIds())
                .stream()
                .map(tuple -> {
                    Post post = tuple.get(0, Post.class);
                    UserPost userPost = tuple.get(1, UserPost.class);

                    PostDetailResponse postDetailResponse = new PostDetailResponse(post);

                    if(userPost != null) {
                        postDetailResponse.likesOfMe();
                    }

                    if(post.getUser().getId().equals(userId)) {
                        postDetailResponse.myItem();
                    }

                    return postDetailResponse;
                })
                .collect(Collectors.toList());

        if(postList.size() == pageableSize + 1) {
            hasNext = true;
            postList.remove(pageableSize);
        }

        return new HomeResponse(hasNext, new LoginUser(user), postList);
    }

    public HomeResponse getFreePostList(Long userId, Long postId) {

        int pageableSize = PropertyUtil.POST_PAGEABLE_SIZE;
        Boolean hasNext = false;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("Not find user in PostController.getPosts"));

        List<PostDetailResponse> postList = getFreePostListForUser(userId, postId);
        if(postList.size() == pageableSize + 1) {
            hasNext = true;
            postList.remove(pageableSize);
        }

        return new HomeResponse(hasNext, new LoginUser(user), postList);
    }

    public HomeResponse getFreePostListFilterByIdolGroup(Long userId, Long idolGroupId, Long postId) {

        int pageableSize = PropertyUtil.POST_PAGEABLE_SIZE;
        Boolean hasNext = false;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("Not find user in PostController.getPostsWithFilterIdolGroup"));

        List<PostDetailResponse> postList = getFreePostListFilterByIdolGroupForUser(userId, idolGroupId, postId, user.getBlockedUserIds(), user.getBlockedPostIds());
        if(postList.size() == pageableSize + 1) {
            hasNext = true;
            postList.remove(pageableSize);
        }

        return new HomeResponse(hasNext, new LoginUser(user), postList);
    }

    public List<PostDetailResponse> getFreePostListForUser(Long userId, Long postId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("Not find user in PostService.getPosts"));

        return postRepositoryCustom.findFreeBylikeIdolGroupsWithUserPost(userId, user.getUserIdolGroups(), postId, user.getBlockedUserIds(), user.getBlockedPostIds())
                .stream()
                .map(tuple -> {
                    Post post = tuple.get(0, Post.class);
                    UserPost userPost = tuple.get(1, UserPost.class);

                    PostDetailResponse postDetailResponse = new PostDetailResponse(post);

                    if(userPost != null) {
                        postDetailResponse.likesOfMe();
                    }

                    if(post.getUser().getId().equals(userId)) {
                        postDetailResponse.myItem();
                    }

                    return postDetailResponse;
                })
                .collect(Collectors.toList());
    }

    public List<PostDetailResponse> getFreePostListFilterByIdolGroupForUser(Long userId, Long idolGroupId, Long postId, List<Long> blockedUserIdList, List<Long> blockedPostIdList) {

        return postRepositoryCustom.findFreeByUserIdolGroupWithUserPost(userId, idolGroupId, postId, blockedUserIdList, blockedPostIdList)
                .stream()
                .map(tuple -> {
                    Post post = tuple.get(0, Post.class);
                    UserPost userPost = tuple.get(1, UserPost.class);

                    PostDetailResponse postDetailResponse = new PostDetailResponse(post);

                    if(userPost != null) {
                        postDetailResponse.likesOfMe();
                    }

                    if(post.getUser().getId().equals(userId)) {
                        postDetailResponse.myItem();
                    }

                    return postDetailResponse;
                })
                .collect(Collectors.toList());
    }
}
