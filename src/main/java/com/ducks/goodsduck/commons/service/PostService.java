package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.item.ItemDetailResponse;
import com.ducks.goodsduck.commons.model.dto.post.PostDetailResponse;
import com.ducks.goodsduck.commons.model.dto.post.PostUpdateRequest;
import com.ducks.goodsduck.commons.model.dto.post.PostUploadRequest;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import com.ducks.goodsduck.commons.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.NoResultException;
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
    private final UserRepository userRepository;
    private final IdolGroupRepository idolGroupRepository;
    private final PostImageRepository postImageRepository;
    private final ImageRepositoryCustom imageRepositoryCustom;

    private final ImageUploadService imageUploadService;

    public Long upload(PostUploadRequest postUploadRequest, List<MultipartFile> multipartFiles, Long userId, Long idolGroupId) {

        try {
            Post post = new Post(postUploadRequest);

            /** Post-User 연관관계 삽입 **/
            User user = userRepository.findById(userId).get();
            post.setUser(user);

            /** Post-IdolGroup 연관관계 삽입 **/
            IdolGroup idolGroup = idolGroupRepository.findById(idolGroupId).get();
            post.setIdolGroup(idolGroup);

            /** 이미지 업로드 처리 & Image-Post 연관관계 삽입 **/
            List<Image> images = imageUploadService.uploadImages(multipartFiles, ImageType.POST, user.getNickName());
            List<PostImage> postImages = images.stream()
                    .map(image -> new PostImage(image))
                    .collect(Collectors.toList());
            for (PostImage postImage : postImages) {
                post.addImage(postImage);
                postImageRepository.save(postImage);
            }

            postRepository.save(post);

            return post.getId();
        } catch (Exception e) {
            return -1L;
        }
    }

    public PostDetailResponse showDetail(Long postId, Long userId) {

        Post post = postRepository.findById(postId).get();
        post.increaseView();

        PostDetailResponse postDetailResponse = new PostDetailResponse(post);

        // 좋아요

        if(post.getUser().getId().equals(userId)) {
            postDetailResponse.myItem();
        }

        return postDetailResponse;
    }

//    public Long edit(Long postId, PostUpdateRequest postUpdateRequest, List<MultipartFile> multipartFiles, Long userId) {
//
//        /**
//         * 기존 포스트 정보 수정
//         * 제목
//         * 내용
//         */
//        Post post = postRepository.findById(postId).get();
//        post.setTitle(postUpdateRequest.getTitle());
//        post.setContent(postUpdateRequest.getContent());
//
//        /**
//         * 기존 이미지 수정 (Url)
//         * case1. 기존 이미지 유지한 경우 -> 따로 변경할 필요없음
//         * case2. 기존 이미지 전부 삭제한 경우 (null, empty)
//         * case3. 기존 이미지 1개 이상 남기고 삭제한 경우
//         *
//         * 새로운 이미지 수정 (파일)
//         * case4. 새로운 이미지 추가하지 않은 경우 -> 따로 변경할 필요없음
//         * case5. 새로운 이미지 추가한 경우
//         */
//        List<PostImage> existImages = post.getImages();
//        List<String> updateImageUrls = postUpdateRequest.getImageUrls();
//
//        // case2
//        if(updateImageUrls.isEmpty()) {
//            List<PostImage> deleteImages = new ArrayList<>();
//            Iterator<PostImage> iter = existImages.iterator();
//            while(iter.hasNext()) {
//                PostImage existImage = iter.next();
//                deleteImages.add(existImage);
//                iter.remove();
//            }
//
//            postImageRepository.deleteInBatch(deleteImages);
//        }
//        // case3
//        else {
//            List<Image> updateImages = imageRepositoryCustom.findByImageUrls(updateImageUrls);
//
//
//            if(!(existImages.containsAll(updateImages) && updateImages.containsAll(existImages))) {
//
//                HashMap<Long, String> imageMap = new HashMap<>();
//                for (Image updateExistImage : updateImages) {
//                    imageMap.put(updateExistImage.getId(), updateExistImage.getUrl());
//                }
//
//                List<Image> deleteImages = new ArrayList<>();
//                Iterator<Image> iter = existImages.iterator();
//                while(iter.hasNext()) {
//                    Image existImage = iter.next();
//                    if(imageMap.get(existImage.getId()) == null) {
//                        deleteImages.add(existImage);
//                        iter.remove();
//                    }
//                }
//
//                imageRepository.deleteInBatch(deleteImages);
//            }
//        }
//
//        // case5
//        if(multipartFiles != null) {
//            User user = userRepository.findById(userId).get();
//            List<Image> images = imageUploadService.uploadImages(multipartFiles, ImageType.ITEM, user.getNickName());
//            for (Image image : images) {
//                item.addImage(image);
//                imageRepository.save(image);
//            }
//        }
//
//        return item.getId();
//    } catch (Exception e) {
//        return -1L;
//    }
//
//        return 1L;
//    }

    public void delete(Long postId) {

    }
}
