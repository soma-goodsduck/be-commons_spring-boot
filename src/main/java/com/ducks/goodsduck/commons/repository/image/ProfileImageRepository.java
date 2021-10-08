package com.ducks.goodsduck.commons.repository.image;

import com.ducks.goodsduck.commons.model.entity.Image.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
}
