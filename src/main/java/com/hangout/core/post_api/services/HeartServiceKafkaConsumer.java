package com.hangout.core.post_api.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.hangout.core.post_api.dto.ActionType;
import com.hangout.core.post_api.dto.event.HeartEvent;
import com.hangout.core.post_api.entities.Heart;
import com.hangout.core.post_api.entities.Post;
import com.hangout.core.post_api.repositories.HeartRepo;
import com.hangout.core.post_api.repositories.PostRepo;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HeartServiceKafkaConsumer {
    private final HeartRepo heartRepo;
    private final PostRepo postRepo;

    @WithSpan(kind = SpanKind.CONSUMER, value = "heart topic batch consumer")
    @KafkaListener(topics = "${hangout.kafka.heart.topic}", groupId = "${spring.application.name}", containerFactory = "batchEventContainerFactory")
    public void consumeHeartEvent(List<HeartEvent> heartEvent) {
        List<HeartEvent> addHeartList = new ArrayList<>();
        List<HeartEvent> removeHeartList = new ArrayList<>();
        heartEvent.stream().forEach(h -> {
            if (h.actionType() == ActionType.ADD) {
                addHeartList.add(h);
            } else {
                removeHeartList.add(h);
            }
        });
        addHeart(addHeartList);
        removeHeart(removeHeartList);
    }

    @WithSpan(value = "add Heart function - iterative")
    @Transactional
    private void addHeart(List<HeartEvent> heartEvents) {
        List<Heart> heartList = new ArrayList<>();
        heartEvents.stream().forEach(h -> {
            Optional<Post> post = postRepo.findById(h.postId());
            if (post.isPresent()) {
                Heart heart = new Heart(post.get(), h.userId());
                // TODO: rather than calling this each time we can create a map<Post, like>
                // which can be used to count which post got how many likes and the like counter
                // for that post can be increased in one go
                postRepo.increaseHeartCount(post.get().getPostId());
                heartList.add(heart);
            }
        });
        heartRepo.saveAll(heartList);
    }

    @WithSpan(value = "remove Heart function - iterative")
    @Transactional
    private void removeHeart(List<HeartEvent> heartEvents) {
        heartEvents.forEach(h -> {
            Optional<Post> post = postRepo.findById(h.postId());
            if (post.isPresent()) {
                postRepo.decreaseHeartCount(post.get().getPostId());
                heartRepo.removeHeart(post.get().getPostId(), h.userId());
            }
        });
    }
}
