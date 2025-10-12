package com.hangout.core.post_api.repositories;

import java.math.BigInteger;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hangout.core.post_api.entities.Heart;

public interface HeartRepo extends JpaRepository<Heart, BigInteger> {

    @Query(value = "SELECT EXISTS(SELECT 1 FROM heart WHERE post_id = :postId AND user_id = :userId)", nativeQuery = true)
    Boolean hasHearted(@Param("postId") UUID postId, @Param("userId") BigInteger userId);

}
