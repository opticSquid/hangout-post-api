package com.hangout.core.post_api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hangout.core.post_api.dto.Session;
import com.hangout.core.post_api.dto.internal.AddressResponse;
import com.hangout.core.post_api.dto.response.AddressDetails;
import com.hangout.core.post_api.exceptions.UnauthorizedAccessException;
import com.hangout.core.post_api.utils.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.math.BigInteger;
import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AddressServiceTests {

    private AddressService addressService;
    private AuthorizationService authorizationService;
    private RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        authorizationService = mock(AuthorizationService.class);
        restClient = mock(RestClient.class);

        // We create the service, then inject the mocked RestClient
        addressService = new AddressService(authorizationService, "http://localhost:5012");
        ReflectionTestUtils.setField(addressService, "restClient", restClient);
        ReflectionTestUtils.setField(addressService, "apiKey", "test-key");
    }

    @Test
    void testGetAddressDetails_success() throws Exception {
        Session session = new Session(new BigInteger("123"), "ROLE_USER", true);
        when(authorizationService.authorizeUser("auth-token")).thenReturn(session);

        // Create a JSON representing the AddressResponse record
        String json = """
                {
                  "type": "FeatureCollection",
                  "features": [
                    {
                      "type": "Feature",
                      "properties": {
                        "state": "West Bengal",
                        "city": "Kolkata"
                      }
                    }
                  ]
                }
                """;

        AddressResponse expectedResponse = objectMapper.readValue(json, AddressResponse.class);

        // Mock the RestClient fluent calls
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(getSpec);
        when(getSpec.uri(any(URI.class))).thenReturn(headersSpec);
        when(headersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(AddressResponse.class)).thenReturn(ResponseEntity.ok(expectedResponse));

        Optional<AddressDetails> result = addressService.getAddressDetails("auth-token", 22.5726, 88.3639);

        assertTrue(result.isPresent());
        assertEquals("West Bengal", result.get().state());
        assertEquals("Kolkata", result.get().city());

        verify(authorizationService, times(1)).authorizeUser("auth-token");
        verify(restClient, times(1)).get();
    }

    @Test
    void testGetAddressDetails_untrustedDevice() {
        Session session = new Session(new BigInteger("123"), "ROLE_USER", false); // untrusted device
        when(authorizationService.authorizeUser("auth-token")).thenReturn(session);

        assertThrows(UnauthorizedAccessException.class, () -> {
            addressService.getAddressDetails("auth-token", 22.5726, 88.3639);
        });
    }
}
