package com.ducks.goodsduck.commons.repository.user;

import com.ducks.goodsduck.commons.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByBcryptId(String bcryptId);
    User findByPhoneNumber(String phoneNumber);
    User findByNickName(String nickName);
    User findByEmail(String email);

    @Query("select u from User u where u.deletedAt is not null and u.phoneNumber is not Null")
    List<User> findAllWithDeleted();
}
