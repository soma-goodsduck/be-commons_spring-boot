package com.ducks.goodsduck.commons.repository.idol;

import com.ducks.goodsduck.commons.model.entity.IdolMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdolMemberRepository extends JpaRepository<IdolMember, Long> {
}
