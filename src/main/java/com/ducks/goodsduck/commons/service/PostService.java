package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.post.PostDetailResponse;
import com.ducks.goodsduck.commons.model.dto.post.PostUpdateRequest;
import com.ducks.goodsduck.commons.model.dto.post.PostUploadRequest;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import com.ducks.goodsduck.commons.repository.*;
import com.ducks.goodsduck.commons.repository.post.PostRepository;
import com.ducks.goodsduck.commons.repository.post.PostRepositoryCustom;
import com.ducks.goodsduck.commons.repository.post.UserPostRepositoryCustom;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
    private final UserPostRepositoryCustom userPostRepositoryCustom;

    private final ImageUploadService imageUploadService;

    public Long upload(PostUploadRequest postUploadRequest, List<MultipartFile> multipartFiles, Long userId) {

        try {
            Post post = new Post(postUploadRequest);

            /** Post-User 연관관계 삽입 **/
            User user = userRepository.findById(userId).get();
            post.setUser(user);

            /** Post-IdolGroup 연관관계 삽입 **/
            IdolGroup idolGroup = idolGroupRepository.findById(postUploadRequest.getIdolGroupId()).get();
            post.setIdolGroup(idolGroup);

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

        // TODO : 좋아요 수 체크

        return postDetailResponse;
    }

    public Long edit(Long postId, PostUpdateRequest postUpdateRequest, List<MultipartFile> multipartFiles, Long userId) throws IOException {

        try {
            /**
             * 기존 포스트 정보 수정
             * 제목
             * 내용
             */
            Post post = postRepository.findById(postId).get();
            post.setTitle(postUpdateRequest.getTitle());
            post.setContent(postUpdateRequest.getContent());

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

            // comment 삭제

            // post 삭제
            postRepository.delete(deletePost);

            return 1L;
        } catch (Exception e) {
            return -1L;
        }

    }
}