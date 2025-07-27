package com.hangout.core.post_api.repositories;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hangout.core.post_api.dto.GetNearbyPostsProjection;
import com.hangout.core.post_api.dto.GetParticularPostProjection;
import com.hangout.core.post_api.entities.Post;

import jakarta.transaction.Transactional;

public interface PostRepo extends JpaRepository<Post, UUID> {
        @Modifying
        @Transactional
        @Query(value = "UPDATE POST SET COMMENTS = Comments+1, INTERACTIONS = INTERACTIONS+1 where POST_ID = :postId", nativeQuery = true)
        void increaseCommentCount(@Param("postId") UUID postId);

        @Modifying
        @Transactional
        @Query(value = "UPDATE POST SET HEARTS = HEARTS+1, INTERACTIONS = INTERACTIONS+1 where POST_ID = :postId", nativeQuery = true)
        void increaseHeartCount(@Param("postId") UUID postId);

        @Modifying
        @Transactional
        @Query(value = "UPDATE POST SET HEARTS = CASE WHEN HEARTS > 0  THEN HEARTS - 1 ELSE 0 END, INTERACTIONS = INTERACTIONS where POST_ID = :postId", nativeQuery = true)
        void decreaseHeartCount(@Param("postId") UUID postId);

        @Modifying
        @Transactional
        @Query(value = "UPDATE POST SET INTERACTIONS = INTERACTIONS+1 WHERE POST_ID = :postId", nativeQuery = true)
        void increaseInteractionCount(@Param("postId") UUID postId);

        @Query(value = "SELECT P.POST_ID, P.OWNER_ID, P.FILENAME, P.POST_DESCRIPTION, P.HEARTS, P.COMMENTS, P.INTERACTIONS, P.CREATED_AT, P.STATE, P.CITY, P.LOCATION, ST_DISTANCE(:userLocation, P.LOCATION) AS DISTANCE FROM POST P JOIN MEDIA M ON P.FILENAME = M.FILENAME WHERE ST_DWITHIN(:userLocation, P.LOCATION, :maxSearchRadius) AND NOT ST_DWITHIN(:userLocation, P.LOCATION, :minSearchRadius) AND M.PROCESS_STATUS = 'SUCCESS' ORDER BY P.LOCATION <-> :userLocation ASC OFFSET :offset LIMIT :limit;", nativeQuery = true)
        List<GetNearbyPostsProjection> getAllNearbyPosts(
                        @Param("userLocation") Point userLocation,
                        @Param("minSearchRadius") Double minSearchRadius,
                        @Param("maxSearchRadius") Double maxSearchRadius,
                        @Param("offset") Integer offset,
                        @Param("limit") Integer limit);

        @Query(value = "SELECT COUNT(P.POST_ID) AS POST_COUNT  FROM POST AS P JOIN MEDIA AS M ON P.FILENAME = M.FILENAME WHERE ST_DWITHIN(:userLocation, LOCATION, :maxSearchRadius) AND NOT ST_DWITHIN(:userLocation, LOCATION, :minSearchRadius) AND M.PROCESS_STATUS = 'SUCCESS';", nativeQuery = true)
        Integer getAllNearbyPostsCount(
                        @Param("userLocation") Point userLocation,
                        @Param("minSearchRadius") Double minSearchRadius,
                        @Param("maxSearchRadius") Double maxSearchRadius);

        @Query(value = "SELECT P.POST_ID, P.OWNER_ID, M.FILENAME, M.CONTENT_TYPE, P.POST_DESCRIPTION, P.HEARTS, P.COMMENTS, P.INTERACTIONS, P.CREATED_AT, P.STATE, P.CITY, P.LOCATION FROM POST P JOIN MEDIA M ON P.FILENAME = M.FILENAME WHERE P.POST_ID = :postId;", nativeQuery = true)
        Optional<GetParticularPostProjection> getParticularPost(
                        @Param("postId") UUID postId);

        @Query(value = "SELECT P.POST_ID, P.FILENAME, M.PROCESS_STATUS, P.HEARTS, P.COMMENTS, P.INTERACTIONS, P.CREATED_AT, P.STATE, P.CITY, P.LOCATION FROM POST P JOIN MEDIA M ON P.FILENAME = M.FILENAME WHERE P.OWNER_ID = :ownerId ORDER BY P.CREATED_AT DESC OFFSET :offset LIMIT :limit;", nativeQuery = true)
        List<GetParticularPostProjection> getPostsByOwnerId(
                        @Param("ownerId") BigInteger ownerId,
                        @Param("offset") Integer offset,
                        @Param("limit") Integer limit);

        @Query(value = "SELECT COUNT(P.POST_ID) FROM POST P WHERE OWNER_ID = :ownerId", nativeQuery = true)
        Integer getPostCountByOwnerId(@Param("ownerId") BigInteger ownerId);
}
