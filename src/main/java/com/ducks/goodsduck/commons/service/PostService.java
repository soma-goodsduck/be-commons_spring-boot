package com.ducks.goodsduck.commons.service;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import com.ducks.goodsduck.commons.exception.common.NotFoundDataException;
import com.ducks.goodsduck.commons.exception.image.ImageProcessException;
import com.ducks.goodsduck.commons.model.dto.LoginUser;
import com.ducks.goodsduck.commons.model.dto.category.CategoryResponse;
import com.ducks.goodsduck.commons.model.dto.home.HomeResponse;
import com.ducks.goodsduck.commons.model.dto.notification.NotificationMessage;
import com.ducks.goodsduck.commons.model.dto.post.PostDetailResponse;
import com.ducks.goodsduck.commons.model.dto.post.PostUpdateRequest;
import com.ducks.goodsduck.commons.model.dto.post.PostUploadRequest;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.Image.Image;
import com.ducks.goodsduck.commons.model.entity.Image.PostImage;
import com.ducks.goodsduck.commons.model.entity.category.PostCategory;
import com.ducks.goodsduck.commons.model.enums.ActivityType;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import com.ducks.goodsduck.commons.repository.category.PostCategoryRepository;
import com.ducks.goodsduck.commons.repository.device.DeviceRepositoryCustom;
import com.ducks.goodsduck.commons.repository.idol.IdolGroupRepository;
import com.ducks.goodsduck.commons.repository.idol.IdolGroupVoteRedisTemplate;
import com.ducks.goodsduck.commons.repository.image.ImageRepository;
import com.ducks.goodsduck.commons.repository.image.ImageRepositoryCustom;
import com.ducks.goodsduck.commons.repository.image.PostImageRepository;
import com.ducks.goodsduck.commons.repository.post.PostRepository;
import com.ducks.goodsduck.commons.repository.post.PostRepositoryCustom;
import com.ducks.goodsduck.commons.repository.post.UserPostRepository;
import com.ducks.goodsduck.commons.repository.user.UserRepository;
import com.ducks.goodsduck.commons.util.FcmUtil;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final PostRepositoryCustom postRepositoryCustom;
    private final UserRepository userRepository;
    private final IdolGroupRepository idolGroupRepository;
    private final PostImageRepository postImageRepository;
    private final ImageRepository imageRepository;
    private final ImageRepositoryCustom imageRepositoryCustom;
    private final UserPostRepository userPostRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final DeviceRepositoryCustom deviceRepositoryCustom;

    private final IdolGroupVoteRedisTemplate idolGroupVoteRedisTemplate;

    private final ImageUploadService imageUploadService;
    private final MessageSource messageSource;

    public Long upload(PostUploadRequest postUploadRequest, List<MultipartFile> multipartFiles, Long userId) throws IOException, MetadataException {

        Post post = new Post(postUploadRequest);

        /** Post-User ???????????? ?????? **/
        User user = userRepository.findById(userId).get();
        post.setUser(user);

        /** Post-IdolGroup ???????????? ?????? **/
        IdolGroup idolGroup = idolGroupRepository.findById(postUploadRequest.getIdolGroupId()).get();
        post.setIdolGroup(idolGroup);

        /** Post-Category ???????????? ?????? **/
        PostCategory postCategory = postCategoryRepository.findById(postUploadRequest.getPostCategoryId()).get();
        post.setPostCategory(postCategory);

        try {
            /** ????????? ????????? ?????? & Image-Post ???????????? ?????? **/
            if(multipartFiles != null) {
                List<Image> images = imageUploadService.uploadImages(multipartFiles, ImageType.POST, user.getNickName());
                for (Image image : images) {
                    PostImage postImage = new PostImage(image);
                    post.addImage(postImage);
                    imageRepository.save(postImage);
                }
            }

        } catch (ImageProcessingException e) {
            throw new ImageProcessException(e);
        }

        idolGroupVoteRedisTemplate.addCountUploadByUserId(userId);
        postRepository.save(post);

        if (user.gainExpByType(ActivityType.POST) >= 100){
            if (user.getLevel() == null) user.setLevel(1);
            boolean success = user.levelUp();
            if(success) {
                List<String> registrationTokensByUserId = deviceRepositoryCustom.getRegistrationTokensByUserId(user.getId());
                FcmUtil.sendMessage(NotificationMessage.ofLevelUp(), registrationTokensByUserId);
            }
        }

        return post.getId();
    }

    public PostDetailResponse showDetailWithLike(Long userId, Long postId) {

        Tuple postTupleWithUserPost = postRepositoryCustom.findByIdWithUserPost(userId, postId);
        Post post = postTupleWithUserPost.get(0, Post.class);
        UserPost userPost = postTupleWithUserPost.get(1, UserPost.class);
        PostDetailResponse postDetailResponse = new PostDetailResponse(post);
        
        // ????????? ??????
        post.increaseView();

        // ????????? ????????? ??????
        if(post.getUser().getId().equals(userId)) {
            postDetailResponse.myItem();
        }

        // ????????? ??????
        if(userPost != null) {
            postDetailResponse.likesOfMe();
        }

        return postDetailResponse;
    }

    public Long edit(Long postId, PostUpdateRequest postUpdateRequest, List<MultipartFile> multipartFiles, Long userId) throws IOException {

        try {
            /**
             * ?????? ????????? ?????? ??????
             * ??????
             * ??????
             * ?????????
             */
            Post post = postRepository.findById(postId).get();
            post.setContent(postUpdateRequest.getContent());
            PostCategory postCategory = postCategoryRepository.findById(postUpdateRequest.getPostCategoryId()).get();
            post.setPostCategory(postCategory);

            /**
             * ?????? ????????? ?????? (Url)
             * case1. ?????? ????????? ????????? ?????? -> ?????? ????????? ????????????
             * case2. ?????? ????????? ?????? ????????? ?????? (null, empty)
             * case3. ?????? ????????? ?????? ????????? ??????
             *
             * ????????? ????????? ?????? (??????)
             * case4. ????????? ????????? ???????????? ?????? ?????? -> ?????? ????????? ????????????
             * case5. ????????? ????????? ????????? ??????
             */
            List<PostImage> existImages = post.getImages();
            List<String> updateImageUrls = postUpdateRequest.getImageUrls();

            // case2
            if (updateImageUrls.isEmpty()) {
                List<Image> deleteImages = new ArrayList<>();
                Iterator<PostImage> iter = existImages.iterator();
                while (iter.hasNext()) {
                    Image existImage = iter.next();
                    deleteImages.add(existImage);
                    iter.remove();
                }

                imageRepository.deleteInBatch(deleteImages);
            }
            // case3
            else {
                List<Image> updateImages = imageRepositoryCustom.findByImageUrls(updateImageUrls);

                if (!(existImages.containsAll(updateImages) && updateImages.containsAll(existImages))) {

                    HashMap<Long, String> imageMap = new HashMap<>();
                    for (Image updateExistImage : updateImages) {
                        imageMap.put(updateExistImage.getId(), updateExistImage.getUrl());
                    }

                    List<Image> deleteImages = new ArrayList<>();
                    Iterator<PostImage> iter = existImages.iterator();
                    while (iter.hasNext()) {
                        Image existImage = iter.next();
                        if (imageMap.get(existImage.getId()) == null) {
                            deleteImages.add(existImage);
                            iter.remove();
                        }
                    }

                    imageRepository.deleteInBatch(deleteImages);
                }
            }

            // case5
            if (multipartFiles != null) {
                User user = userRepository.findById(userId).get();
                List<Image> images = imageUploadService.uploadImages(multipartFiles, ImageType.POST, user.getNickName());
                for (Image image : images) {
                    PostImage postImage = new PostImage(image);
                    post.addImage(postImage);
                    imageRepository.save(postImage);
                }
            }

            return post.getId();
        } catch (Exception e) {
            return -1L;
        }
    }

    public Long delete(Long postId) {

        try {
            Post deletePost = postRepository.findById(postId).get();

            // image ?????? ??????
            List<PostImage> deleteImages = deletePost.getImages();
            postImageRepository.deleteInBatch(deleteImages);

            // userPost ?????? ??????
            List<UserPost> deleteUserPosts = userPostRepository.findAllByPostId(postId);
            userPostRepository.deleteInBatch(deleteUserPosts);

            // post ??????
            postRepository.delete(deletePost);

            return 1L;
        } catch (Exception e) {
            return -1L;
        }
    }

    public Long deleteV2(Long postId) {

        try {
            Post deletePost = postRepository.findById(postId)
                    .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                            new Object[]{"Post"}, null)));

            // image ?????? ??????
            List<PostImage> deleteImages = deletePost.getImages();
            for (Image deleteImage : deleteImages) {
                deleteImage.setDeletedAt(LocalDateTime.now());
            }

            // userPost ?????? ??????
            List<UserPost> deleteUserPosts = userPostRepository.findAllByPostId(postId);
            for (UserPost deleteUserPost : deleteUserPosts) {
                deleteUserPost.setDeletedAt(LocalDateTime.now());
            }

            // post ??????
            deletePost.setDeletedAt(LocalDateTime.now());

            return 1L;
        } catch (Exception e) {
            return -1L;
        }
    }

    public List<PostDetailResponse> getPostListForUser(Long userId, Long postId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("Not find user in PostService.getPosts"));

        return postRepositoryCustom.findBylikeIdolGroupsWithUserPost(userId, user.getUserIdolGroups(), postId, user.getBlockedUserIds(), user.getBlockedPostIds())
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

    public List<PostDetailResponse> getPostListFilterByIdolGroupForUser(Long userId, Long idolGroupId, Long postId, List<Long> blockedUserIdList, List<Long> blockedPostIdList) {

        return postRepositoryCustom.findByUserIdolGroupWithUserPost(userId, idolGroupId, postId, blockedUserIdList, blockedPostIdList)
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

    public HomeResponse getPostList(Long userId, Long postId) {

        int pageableSize = PropertyUtil.POST_PAGEABLE_SIZE;
        Boolean hasNext = false;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("Not find user in PostController.getPosts"));

        List<PostDetailResponse> postList = getPostListForUser(userId, postId);
        if(postList.size() == pageableSize + 1) {
            hasNext = true;
            postList.remove(pageableSize);
        }

        return new HomeResponse(hasNext, new LoginUser(user), postList);
    }

    public HomeResponse getPostListFilterByIdolGroup(Long userId, Long idolGroupId, Long postId) {

        int pageableSize = PropertyUtil.POST_PAGEABLE_SIZE;
        Boolean hasNext = false;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("Not find user in PostController.getPostsWithFilterIdolGroup"));

        List<PostDetailResponse> postList = getPostListFilterByIdolGroupForUser(userId, idolGroupId, postId, user.getBlockedUserIds(), user.getBlockedPostIds());
        if(postList.size() == pageableSize + 1) {
            hasNext = true;
            postList.remove(pageableSize);
        }

        return new HomeResponse(hasNext, new LoginUser(user), postList);
    }

    public List<CategoryResponse> getPostCategory() {
        return postCategoryRepository.findAll()
                .stream()
                .map(postCategory -> new CategoryResponse(postCategory))
                .collect(Collectors.toList());
    }
}
