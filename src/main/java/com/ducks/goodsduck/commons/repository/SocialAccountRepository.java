package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, String> {

    Optional<SocialAccount> findById(String id);
}
