package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.exception.common.NotFoundDataException;
import com.ducks.goodsduck.commons.model.dto.LoginUser;
import com.ducks.goodsduck.commons.model.dto.category.CategoryResponse;
import com.ducks.goodsduck.commons.model.dto.home.HomeResponse;
import com.ducks.goodsduck.commons.model.dto.notification.NotificationMessage;
import com.ducks.goodsduck.commons.model.dto.post.PostDetailResponse;
import com.ducks.goodsduck.commons.model.dto.post.PostUpdateRequest;
import com.ducks.goodsduck.commons.model.dto.post.PostUploadRequest;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.Image.Image;
import com.ducks.goodsduck.commons.model.entity.Image.ItemImage;
import com.ducks.goodsduck.commons.model.entity.Image.PostImage;
import com.ducks.goodsduck.commons.model.entity.category.PostCategory;
import com.ducks.goodsduck.commons.model.enums.ActivityType;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import com.ducks.goodsduck.commons.repository.category.PostCategoryRepository;
import com.ducks.goodsduck.commons.repository.device.DeviceRepositoryCustom;
import com.ducks.goodsduck.commons.repository.idol.IdolGroupRepository;
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

    private final ImageUploadService imageUploadService;
    private final MessageSource messageSource;

    public Long upload(PostUploadRequest postUploadRequest, List<MultipartFile> multipartFiles, Long userId) {

        try {
            Post post = new Post(postUploadRequest);

            /** Post-User 연관관계 삽입 **/
            User user = userRepository.findById(userId).get();
            post.setUser(user);

            /** Post-IdolGroup 연관관계 삽입 **/
            IdolGroup idolGroup = idolGroupRepository.findById(postUploadRequest.getIdolGroupId()).get();
            post.setIdolGroup(idolGroup);

            /** Post-Category 연관관계 삽입 **/
            PostCategory postCategory = postCategoryRepository.findById(postUploadRequest.getPostCategoryId()).get();
            post.setPostCategory(postCategory);

            /** 이미지 업로드 처리 & Image-Post 연관관계 삽입 **/
            if(multipartFiles != null) {
                List<Image> images = imageUploadService.uploadImages(multipartFiles, ImageType.POST, user.getNickName());
                for (Image image : images) {
                    PostImage postImage = new PostImage(image);
                    post.addImage(postImage);
                    imageRepository.save(postImage);
                }
            }

            postRepository.save(post);

            if (user.gainExpByType(ActivityType.POST) >= 100){
                if (user.getLevel() == null) user.setLevel(1);
                user.levelUp();
                List<String> registrationTokensByUserId = deviceRepositoryCustom.getRegistrationTokensByUserId(user.getId());
                FcmUtil.sendMessage(NotificationMessage.ofLevelUp(), registrationTokensByUserId);
            }

            return post.getId();
        } catch (Exception e) {
            return -1L;
        }
    }

    public PostDetailResponse showDetailWithLike(Long userId, Long postId) {

        Tuple postTupleWithUserPost = postRepositoryCustom.findByIdWithUserPost(userId, postId);
        Post post = postTupleWithUserPost.get(0, Post.class);
        UserPost userPost = postTupleWithUserPost.get(1, UserPost.class);
        PostDetailResponse postDetailResponse = new PostDetailResponse(post);
        
        // 조회수 증가
        post.increaseView();

        // 포스트 작성자 체크
        if(post.getUser().getId().equals(userId)) {
            postDetailResponse.myItem();
        }

        // 좋아요 체크
        if(userPost != null) {
            postDetailResponse.likesOfMe();
        }

        return postDetailResponse;
    }

    public Long edit(Long postId, PostUpdateRequest postUpdateRequest, List<MultipartFile> multipartFiles, Long userId) throws IOException {

        try {
            /**
             * 기존 포스트 정보 수정
             * 제목
             * 내용
             * 이미지
             */
            Post post = postRepository.findById(postId).get();
            post.setContent(postUpdateRequest.getContent());
            PostCategory postCategory = postCategoryRepository.findById(postUpdateRequest.getPostCategoryId()).get();
            post.setPostCategory(postCategory);

            /**
             * 기존 이미지 수정 (Url)
             * case1. 기존 이미지 유지한 경우 -> 따로 변경할 필요없음
             * case2. 기존 이미지 전부 삭제한 경우 (null, empty)
             * case3. 기존 이미지 일부 삭제한 경우
             *
             * 새로운 이미지 수정 (파일)
             * case4. 새로운 이미지 추가하지 않은 경우 -> 따로 변경할 필요없음
             * case5. 새로운 이미지 추가한 경우
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

            // user's post 목록 삭제
            User user = deletePost.getUser();
            List<Post> postsOfUser = user.getPosts();
            postsOfUser.remove(deletePost);

            // image 연관 삭제
            List<PostImage> deleteImages = deletePost.getImages();
            postImageRepository.deleteInBatch(deleteImages);

            // userPost 연관 삭제
            List<UserPost> deleteUserPosts = userPostRepository.findByPostId(postId);
            userPostRepository.deleteInBatch(deleteUserPosts);

            // post 삭제
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

            // user's post 목록 삭제
            User user = deletePost.getUser();
            List<Post> postsOfUser = user.getPosts();
            postsOfUser.remove(deletePost);

            // image 연관 삭제
            List<PostImage> deleteImages = deletePost.getImages();
            for (Image deleteImage : deleteImages) {
                deleteImage.setDeletedAt(LocalDateTime.now());
            }

            // userPost 연관 삭제
            List<UserPost> deleteUserPosts = userPostRepository.findByPostId(postId);
            for (UserPost deleteUserPost : deleteUserPosts) {
                deleteUserPost.setDeletedAt(LocalDateTime.now());
            }

            // post 삭제
            deletePost.setDeletedAt(LocalDateTime.now());

            return 1L;
        } catch (Exception e) {
            return -1L;
        }
    }

    public List<PostDetailResponse> getPostListForUser(Long userId, Long postId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("Not find user in PostService.getPosts"));

        return postRepositoryCustom.findBylikeIdolGroupsWithUserPost(userId, user.getUserIdolGroups(), postId)
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

    public List<PostDetailResponse> getPostListFilterByIdolGroupForUser(Long userId, Long idolGroupId, Long postId) {

        return postRepositoryCustom.findByUserIdolGroupWithUserPost(userId, idolGroupId, postId)
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

        List<PostDetailResponse> postList = getPostListFilterByIdolGroupForUser(userId, idolGroupId, postId);
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
