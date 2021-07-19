package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.QIdolGroup;
import com.ducks.goodsduck.commons.model.entity.QIdolMember;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class IdolMemberRepositoryCustomImpl implements IdolMemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public IdolMemberRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    QIdolMember idolMember = QIdolMember.idolMember;
    QIdolGroup idolGroup = QIdolGroup.idolGroup;

    @Override
    public List<Tuple> findAllByIdolGroupId(Long idolGroupId) {

        return queryFactory
                .select(idolMember, idolMember.idolGroup)
                .from(idolMember)
                .where(idolGroup.id.eq(idolGroupId))
                .fetch();
    }

    @Override
    public List<Tuple> findAll() {
        return queryFactory.select(idolMember, idolMember.idolGroup)
                .from(idolMember)
                .fetch();
    }
}
