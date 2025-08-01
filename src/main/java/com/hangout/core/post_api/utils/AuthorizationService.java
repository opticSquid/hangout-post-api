package com.hangout.core.post_api.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.hangout.core.post_api.dto.Session;
import com.hangout.core.post_api.dto.UserValidationRequest;
import com.hangout.core.post_api.exceptions.UnauthorizedAccessException;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
    @Value("${hangout.auth-service.url}")
    private String authServiceURL;
    private final RestClient restClient;

    @WithSpan(kind = SpanKind.CLIENT, value = "authorize user service - auth api call")
    public Session authorizeUser(String authToken) {
        try {
            ResponseEntity<Session> response = restClient
                    .post()
                    .uri(authServiceURL + "/internal/validate")
                    .body(new UserValidationRequest(authToken))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(Session.class);
            return response.getBody();
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new UnauthorizedAccessException(
                        "User's token has expired or user token is not valid");
            } else {
                throw exception;
            }
        }

    }

}
