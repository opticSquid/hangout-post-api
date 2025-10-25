package com.hangout.core.post_api.repositories;

import java.math.BigInteger;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hangout.core.post_api.entities.Heart;
import com.hangout.core.post_api.entities.HeartId;

public interface HeartRepo extends JpaRepository<Heart, HeartId> {

    @Query(value = "SELECT EXISTS(SELECT 1 FROM heart WHERE post_id = :postId AND user_id = :userId)", nativeQuery = true)
    Boolean hasHearted(@Param("postId") UUID postId, @Param("userId") BigInteger userId);

}
