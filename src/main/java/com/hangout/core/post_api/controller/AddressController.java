package com.hangout.core.post_api.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.hangout.core.post_api.dto.response.AddressDetails;
import com.hangout.core.post_api.services.AddressService;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/address")
public class AddressController {
    private final AddressService addressService;

    @WithSpan(kind = SpanKind.SERVER, value = "get details of address from location")
    @GetMapping(path = "/get-details")
    public ResponseEntity<AddressDetails> getAddressDetails(
            @RequestHeader(name = "Authorization") String authToken, @RequestPart(value = "lat") Double lat,
            @RequestPart(value = "lon") Double lon) {
        Optional<AddressDetails> addressDetails = addressService.getAddressDetails(authToken, lat, lon);
        if (addressDetails.isPresent()) {
            return ResponseEntity.ok().body(addressDetails.get());
        } else {
            return ResponseEntity.notFound().build();
        }

    }
}
