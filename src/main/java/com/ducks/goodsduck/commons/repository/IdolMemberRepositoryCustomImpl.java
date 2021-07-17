//package com.ducks.goodsduck.commons.repository;
//
//import com.ducks.goodsduck.commons.model.entity.IdolMember;
//import com.ducks.goodsduck.commons.model.entity.QIdolGroup;
//import com.ducks.goodsduck.commons.model.entity.QIdolMember;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import org.springframework.stereotype.Repository;
//
//import javax.persistence.EntityManager;
//import java.util.List;
//
//@Repository
//public class IdolMemberRepositoryCustomImpl implements IdolMemberRepositoryCustom {
//
//    private final JPAQueryFactory queryFactory;
//
//    public IdolMemberRepositoryCustomImpl(EntityManager em) {
//        this.queryFactory = new JPAQueryFactory(em);
//    }
//
//    @Override
//    public List<IdolMember> findAllByIdolGroupId(Long idolGroupId) {
//
//        QIdolMember idolMember = QIdolMember.idolMember;
//        QIdolGroup idolGroup = QIdolGroup.idolGroup;
//
//        return queryFactory
//                .selectFrom(idolMember)
//                .where(idolGroup.id.eq(idolGroupId))
//                .fetch();
//    }
//}
