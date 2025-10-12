package com.hangout.core.post_api.entities;

import java.math.BigInteger;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "heart")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Heart {

    @EmbeddedId
    private HeartId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("postId") // Maps the postId field of HeartId to the actual Post entity
    @JoinColumn(name = "post_id", nullable = false, foreignKey = @ForeignKey(name = "fk_heart_post"))
    private Post post;

    @JsonProperty(access = Access.READ_ONLY)
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    public Heart(Post post, BigInteger userId) {
        this.id = new HeartId(post.getPostId(), userId);
        this.post = post;
        this.createdAt = ZonedDateTime.now();
    }
}