package com.hangout.core.post_api.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.hangout.core.post_api.dto.Session;
import com.hangout.core.post_api.dto.internal.AddressResponse;
import com.hangout.core.post_api.dto.response.AddressDetails;
import com.hangout.core.post_api.exceptions.UnauthorizedAccessException;
import com.hangout.core.post_api.utils.AuthorizationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AuthorizationService authorizationService;
    private final RestClient restClient;

    @Value("${hangout.address-api.base-url}")
    private String apiUrl;
    @Value("${hangout.address-api.api-key}")
    private String apiKey;

    public Optional<AddressDetails> getAddressDetails(String authToken, Double lat, Double lon) {
        Session session = authorizationService.authorizeUser(authToken);
        // check if the session is trusted
        if (!session.trustedDevice()) {
            throw new UnauthorizedAccessException("Can not create new post from an untrusted device");
        } else {
            ResponseEntity<AddressResponse> response = restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(apiUrl)
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("lang", "en")
                            .queryParam("apiKey", apiKey)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(AddressResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.of(new AddressDetails(response.getBody().features().getFirst().properties().state(),
                        response.getBody().features().getFirst().properties().city()));
            } else {
                return Optional.empty();
            }
        }
    }
}
