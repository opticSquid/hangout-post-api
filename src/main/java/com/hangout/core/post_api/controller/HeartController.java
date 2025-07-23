package com.hangout.core.post_api.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hangout.core.post_api.dto.DefaultResponse;
import com.hangout.core.post_api.dto.HasHearted;
import com.hangout.core.post_api.dto.NewHeartRequest;
import com.hangout.core.post_api.services.HeartService;
import com.hangout.core.post_api.services.HeartServiceKafkaProducer;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/heart")
public class HeartController {
    private final HeartServiceKafkaProducer heartKafkaProducer;
    private final HeartService heartService;

    @WithSpan(kind = SpanKind.SERVER, value = "add heart")
    @PutMapping
    public DefaultResponse addHeart(@RequestHeader(name = "Authorization") String authToken,
            @RequestBody NewHeartRequest heartRequest) {
        return heartKafkaProducer.addHeart(authToken, heartRequest);
    }

    @WithSpan(kind = SpanKind.SERVER, value = "remove heart")
    @PutMapping
    public DefaultResponse removeHeart(@RequestHeader(name = "Authorization") String authToken,
            @RequestBody NewHeartRequest heartRequest) {
        return heartKafkaProducer.removeHeart(authToken, heartRequest);
    }

    @WithSpan(kind = SpanKind.SERVER, value = "get heart status")
    @GetMapping("/{postId}")
    public HasHearted hasHearted(@RequestHeader(name = "Authorization") String authToken, @PathVariable UUID postId) {
        return heartService.hasHearted(authToken, postId);
    }

}
