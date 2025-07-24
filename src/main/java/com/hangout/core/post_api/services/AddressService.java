package com.hangout.core.post_api.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.hangout.core.post_api.dto.Session;
import com.hangout.core.post_api.dto.internal.AddressResponse;
import com.hangout.core.post_api.dto.response.AddressDetails;
import com.hangout.core.post_api.exceptions.UnauthorizedAccessException;
import com.hangout.core.post_api.utils.AuthorizationService;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;

@Service
@EnableCaching
public class AddressService {
    private final AuthorizationService authorizationService;
    private final RestClient restClient;

    public AddressService(AuthorizationService authorizationService,
            @Value("${hangout.address-api.base-url}") String apiUrl) {
        this.authorizationService = authorizationService;
        this.restClient = RestClient.builder().baseUrl(apiUrl).build();
    }

    @Value("${hangout.address-api.api-key}")
    private String apiKey;

    @WithSpan(value = "get details of address from location service")
    public Optional<AddressDetails> getAddressDetails(String authToken, Double lat, Double lon) {
        Session session = authorizationService.authorizeUser(authToken);
        if (!session.trustedDevice()) {
            throw new UnauthorizedAccessException("Can not create new post from an untrusted device");
        } else {
            return callReverseGeoCodingApi(lat, lon);
        }
    }

    @WithSpan(kind = SpanKind.CLIENT, value = "external api call")
    @Cacheable("findAddress")
    private Optional<AddressDetails> callReverseGeoCodingApi(Double lat, Double lon) {
        ResponseEntity<AddressResponse> response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("v1/geocode/reverse")
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
