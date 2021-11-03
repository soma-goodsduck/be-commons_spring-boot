package com.ducks.goodsduck.commons.repository.post;

import com.ducks.goodsduck.commons.model.entity.Post;
import com.ducks.goodsduck.commons.model.entity.UserIdolGroup;
import com.querydsl.core.Tuple;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepositoryCustom {

    // 좋아요 확인
    Tuple findByIdWithUserPost(Long userId, Long postId);

    // 포스트 목록 조회 (좋아하는 아이돌 전체)
    List<Tuple> findBylikeIdolGroupsWithUserPost(Long userId, List<UserIdolGroup> userIdolGroups, Long postId);
    List<Tuple> findBylikeIdolGroupsWithUserPost(Long userId, List<UserIdolGroup> userIdolGroups, Long postId, List<Long> blockedPostIdList);

    // 나눔글 포스트 목록 조회 (좋아하는 아이돌 전체)
    List<Tuple> findFreeBylikeIdolGroupsWithUserPost(Long userId, List<UserIdolGroup> userIdolGroups, Long postId);
    List<Tuple> findFreeBylikeIdolGroupsWithUserPost(Long userId, List<UserIdolGroup> userIdolGroups, Long postId, List<Long> blockedPostIdList);

    // 포스트 목록 조회 (아이돌 1개)
    List<Tuple> findByUserIdolGroupWithUserPost(Long userId, Long idolGroupId, Long postId);
    List<Tuple> findByUserIdolGroupWithUserPost(Long userId, Long idolGroupId, Long postId, List<Long> blockedPostIdList);

    // 나눔글 포스트 목록 조회 (아이돌 1개)
    List<Tuple> findFreeByUserIdolGroupWithUserPost(Long userId, Long idolGroupId, Long postId);
    List<Tuple> findFreeByUserIdolGroupWithUserPost(Long userId, Long idolGroupId, Long postId, List<Long> blockedPostIdList);

    // 내가 작성한 포스트 목록 조회
    List<Tuple> findByUserId(Long userId, Long postId);
    
    // 좋아요한 포스트 목록 조회
    List<Tuple> findAllWithUserPost(Long userId, Long postId);
    List<Tuple> findAllWithUserPost(Long userId, Long postId, List<Long> blockedPostIdList);
}
