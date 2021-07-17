//package com.ducks.goodsduck.commons.service;
//
//import com.ducks.goodsduck.commons.model.entity.IdolMember;
//import com.ducks.goodsduck.commons.repository.IdolMemberRepository;
//import com.ducks.goodsduck.commons.repository.IdolMemberRepositoryCustom;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.persistence.EntityManager;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@Transactional
//public class IdolMemberService {
//
//    private final IdolMemberRepository idolMemberRepository;
//    private final IdolMemberRepositoryCustom idolMemberRepositoryCustom;
//    private final JPAQueryFactory queryFactory;
//
//    public IdolMemberService(IdolMemberRepository idolMemberRepository, IdolMemberRepositoryCustom idolMemberRepositoryCustom, EntityManager em) {
//        this.idolMemberRepository = idolMemberRepository;
//        this.idolMemberRepositoryCustom = idolMemberRepositoryCustom;
//        queryFactory = new JPAQueryFactory(em);
//    }
//
//    public List<IdolMember> findIdolMembersOfGroup(Long idolMemerId) {
//        return idolMemberRepository.findAllByIdolGroupId(idolMemerId);
//    }
//
//    public List<IdolMember> findAllIdolMembers() {
//        return idolMemberRepository.findAll();
//    }
//
//    public Optional<IdolMember> findIdolMemberById(Long idolMemberId) {
//        return idolMemberRepository.findById(idolMemberId);
//    }
//}
