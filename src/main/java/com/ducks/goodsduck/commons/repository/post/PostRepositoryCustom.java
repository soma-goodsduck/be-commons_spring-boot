package com.ducks.goodsduck.commons.repository.post;

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

    // 포스트 목록 조회 (아이돌 1개)
    List<Tuple> findByUserIdolGroupWithUserPost(Long userId, Long idolGroupId, Long postId);
}
