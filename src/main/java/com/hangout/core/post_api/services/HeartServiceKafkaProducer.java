package com.hangout.core.post_api.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.hangout.core.post_api.dto.ActionType;
import com.hangout.core.post_api.dto.DefaultResponse;
import com.hangout.core.post_api.dto.NewHeartRequest;
import com.hangout.core.post_api.dto.Session;
import com.hangout.core.post_api.dto.event.HeartEvent;
import com.hangout.core.post_api.utils.AuthorizationService;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HeartServiceKafkaProducer {
    private final KafkaTemplate<String, HeartEvent> kafkaTemplate;
    private final AuthorizationService authorizationService;
    @Value("${hangout.kafka.heart.topic}")
    private String heartTopic;

    @WithSpan(kind = SpanKind.PRODUCER)
    public DefaultResponse addHeart(String authToken, NewHeartRequest heartRequest) {
        Session session = authorizationService.authorizeUser(authToken);
        if (session.userId() != null) {
            kafkaTemplate.send(heartTopic, heartRequest.postId().toString(),
                    new HeartEvent(ActionType.ADD, heartRequest.postId(), session.userId(), LocalDateTime.now()));
            return new DefaultResponse("hearted post");
        } else {
            return new DefaultResponse("user not authorized can not heart post");
        }
    }

    @WithSpan(kind = SpanKind.PRODUCER)
    public DefaultResponse removeHeart(String authToken, NewHeartRequest heartRequest) {
        Session session = authorizationService.authorizeUser(authToken);
        if (session.userId() != null) {
            kafkaTemplate.send(heartTopic, heartRequest.postId().toString(),
                    new HeartEvent(ActionType.REMOVE, heartRequest.postId(), session.userId(), LocalDateTime.now()));
            return new DefaultResponse("remvoed heart from post");
        } else {
            return new DefaultResponse("user not authorized can not remove heart from post");
        }
    }

}
