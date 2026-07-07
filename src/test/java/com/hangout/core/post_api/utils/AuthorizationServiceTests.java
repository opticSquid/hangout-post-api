package com.hangout.core.post_api.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hangout.core.post_api.dto.Session;
import com.hangout.core.post_api.exceptions.UnauthorizedAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class AuthorizationServiceTests {

    private AuthorizationService authorizationService;
    private MockRestServiceServer mockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        authorizationService = new AuthorizationService(restClient);

        org.springframework.test.util.ReflectionTestUtils.setField(
                authorizationService, "authServiceURL", "http://localhost:5011");
    }

    @Test
    void testAuthorizeUser_success() throws Exception {
        Session expectedSession = new Session(new BigInteger("123"), "ROLE_USER", true);
        String sessionJson = objectMapper.writeValueAsString(expectedSession);

        mockServer.expect(requestTo("http://localhost:5011/internal/validate"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(content().json("{\"accessToken\":\"valid-token\"}"))
                .andRespond(withSuccess(sessionJson, MediaType.APPLICATION_JSON));

        Session actualSession = authorizationService.authorizeUser("valid-token");

        assertNotNull(actualSession);
        assertEquals(new BigInteger("123"), actualSession.userId());
        assertEquals("ROLE_USER", actualSession.role());
        assertTrue(actualSession.trustedDevice());
        mockServer.verify();
    }

    @Test
    void testAuthorizeUser_unauthorized() {
        mockServer.expect(requestTo("http://localhost:5011/internal/validate"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThrows(UnauthorizedAccessException.class, () -> {
            authorizationService.authorizeUser("invalid-token");
        });
        mockServer.verify();
    }
}
