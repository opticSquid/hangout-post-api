package com.hangout.core.post_api.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hangout.core.post_api.dto.HasHearted;
import com.hangout.core.post_api.dto.Session;
import com.hangout.core.post_api.exceptions.UnauthorizedAccessException;
import com.hangout.core.post_api.repositories.HeartRepo;
import com.hangout.core.post_api.utils.AuthorizationService;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HeartService {
    private final AuthorizationService authorizationService;
    private final HeartRepo heartRepo;

    @WithSpan(value = "heart status check")
    @Transactional
    public HasHearted hasHearted(String authToken, UUID postId) {
        Session session = authorizationService.authorizeUser(authToken);
        if (session.userId() != null) {
            return new HasHearted(heartRepo.hasHearted(postId, session.userId()));
        } else {
            throw new UnauthorizedAccessException("User is not authorized");
        }
    }
}
