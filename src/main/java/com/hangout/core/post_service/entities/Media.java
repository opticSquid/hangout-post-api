package com.hangout.core.post_service.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Entity
@Getter
public class Media {
    @Id
    private String hashedFilename;
    private String contentType;
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    public Media(String hashedFilename, String contentType, Post post) {
        this.hashedFilename = hashedFilename;
        this.contentType = contentType;
        this.post = post;
    }

}
