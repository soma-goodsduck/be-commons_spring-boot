package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findByUserId(Long userId);
}
