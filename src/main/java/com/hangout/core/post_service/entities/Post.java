package com.hangout.core.post_service.entities;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID postId;
    private String ownerName;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Media> postMedias;
    @Column(length = 500)
    private String postDescription;
    @JsonProperty(access = Access.READ_ONLY)
    private final Integer hearts = 0;
    @JsonProperty(access = Access.READ_ONLY)
    private final Integer comments = 0;
    @JsonProperty(access = Access.READ_ONLY)
    private final Integer interactions = 0;
    @JsonProperty(access = Access.READ_ONLY)
    private final ZonedDateTime createdAt = ZonedDateTime.now(ZoneOffset.UTC);
    @JsonProperty(access = Access.READ_ONLY)
    private final Boolean publish = true;

    public Post(String ownerName, String postDescription) {
        this.ownerName = ownerName;
        this.postDescription = postDescription;
    }
}
