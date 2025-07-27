package com.hangout.core.post_api.dto;

import java.time.Instant;
import java.util.UUID;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;

import com.hangout.core.post_api.entities.ProcessStatus;

public interface GetParticularPostProjection {
    UUID getPostId();

    String getFilename();

    ProcessStatus getProcessStatus();

    Integer getHearts();

    Integer getComments();

    Integer getInteractions();

    Instant getCreatedAt();

    String getState();

    String getCity();

    Point<G2D> getLocation();
}
