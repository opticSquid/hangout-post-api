package com.hangout.core.post_api.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hangout.core.post_api.dto.DefaultResponse;
import com.hangout.core.post_api.dto.NewHeartRequest;
import com.hangout.core.post_api.services.HeartServiceKafkaProducer;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/heart")
public class HeartController {
    private HeartServiceKafkaProducer heartKafkaProducer;

    @PostMapping("/")
    public DefaultResponse addHeart(@RequestHeader(name = "Authorization") String authToken,
            @RequestBody NewHeartRequest heartRequest) {
        return heartKafkaProducer.addHeart(authToken, heartRequest);
    }

    @DeleteMapping("/")
    public DefaultResponse removeHeart(@RequestHeader(name = "Authorization") String authToken,
            @RequestBody NewHeartRequest heartRequest) {
        return heartKafkaProducer.removeHeart(authToken, heartRequest);
    }

    @GetMapping("/{postId}")
    public String hasHearted(@RequestHeader(name = "Authorization") String authToken, @PathVariable UUID postId) {
        return new String();
    }

}
