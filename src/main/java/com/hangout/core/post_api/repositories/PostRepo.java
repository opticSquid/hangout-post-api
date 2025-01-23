package com.hangout.core.post_api.repositories;

import java.util.List;
import java.util.UUID;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hangout.core.post_api.dto.GetNearbyPostsProjection;
import com.hangout.core.post_api.entities.Media;
import com.hangout.core.post_api.entities.Post;
import com.hangout.core.post_api.projections.UploadedMedias;

import jakarta.transaction.Transactional;

public interface PostRepo extends JpaRepository<Post, UUID> {
        @Modifying
        @Transactional
        @Query(value = "UPDATE post SET comments = comments+1, interactions = interactions+1 where post_id = :postid", nativeQuery = true)
        void increaseCommentCount(@Param("postid") UUID posUuid);

        @Modifying
        @Transactional
        @Query(value = "UPDATE post SET hearts = hearts+1, interactions = interactions+1 where post_id = :postid", nativeQuery = true)
        void increaseHeartCount(@Param("postid") UUID posUuid);

        @Modifying
        @Transactional
        @Query(value = "UPDATE post SET interactions = interactions+1 where post_id = :postid", nativeQuery = true)
        void increaseInteractionCount(@Param("postid") UUID posUuid);

        @Query(value = "SELECT post_medias from post where post_id = :postid", nativeQuery = true)
        UploadedMedias fetchUploadedMedias(@Param("postid") UUID posUuid);

        @Modifying
        @Query(value = "UPDATE post SET post_medias = :postmedias where post_id = :postid", nativeQuery = true)
        void replaceMedias(@Param("postmedias") List<Media> postMedias, @Param("postid") UUID posUuid);

        @Modifying
        @Query(value = "UPDATE post SET publish = true where post_id = :postid", nativeQuery = true)
        void publish(@Param("postid") UUID posUuid);

        @Query(value = "SELECT P.POST_ID, P.OWNER_ID, M.FILENAME, M.CONTENT_TYPE, P.POST_DESCRIPTION, P.HEARTS, P.COMMENTS, P.INTERACTIONS, P.CREATED_AT, P.STATE, P.CITY, P.LOCATION, ST_DISTANCE(:userLocation, P.LOCATION) AS DISTANCE FROM POST P JOIN MEDIA M ON P.FILENAME = M.FILENAME WHERE ST_DWITHIN(:userLocation, P.LOCATION, :maxSearchRadius) AND NOT ST_DWITHIN(:userLocation, P.LOCATION, :minSearchRadius) OFFSET :offset LIMIT :limit;", nativeQuery = true)
        List<GetNearbyPostsProjection> getAllNearbyPosts(
                        @Param("userLocation") Point userLocation,
                        @Param("minSearchRadius") Double minSearchRadius,
                        @Param("maxSearchRadius") Double maxSearchRadius,
                        @Param("offset") Integer offset,
                        @Param("limit") Integer limit);

        @Query(value = "SELECT COUNT(*) AS POST_COUNT  FROM POST P JOIN MEDIA M ON P.FILENAME = M.FILENAME WHERE ST_DWITHIN(:userLocation, P.LOCATION, :maxSearchRadius) AND NOT ST_DWITHIN(:userLocation, P.LOCATION, :minSearchRadius);", nativeQuery = true)
        Integer getAllNearbyPostsCount(
                        @Param("userLocation") Point userLocation,
                        @Param("minSearchRadius") Double minSearchRadius,
                        @Param("maxSearchRadius") Double maxSearchRadius);
}
