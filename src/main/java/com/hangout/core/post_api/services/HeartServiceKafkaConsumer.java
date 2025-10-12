package com.hangout.core.post_api.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.hangout.core.post_api.dto.event.HeartEvent;
import com.hangout.core.post_api.dto.internal.PostUserKey;
import com.hangout.core.post_api.utils.HeartProcessor;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HeartServiceKafkaConsumer {
    private final HeartProcessor heartProcessor;

    @WithSpan(kind = SpanKind.CONSUMER, value = "heart topic batch consumer")
    @KafkaListener(topics = "${hangout.kafka.heart.topic}", groupId = "${spring.application.name}", containerFactory = "batchEventContainerFactory")
    public void consumeHeartEvent(List<HeartEvent> heartEvents, Acknowledgment ack) {
        try {
            Map<PostUserKey, HeartEvent> lastForKey = dedupeEventsKeepLast(heartEvents);
            // group by postId
            Map<UUID, List<HeartEvent>> byPost = lastForKey.values().stream()
                    .collect(Collectors.groupingBy(HeartEvent::postId));

            for (Map.Entry<UUID, List<HeartEvent>> e : byPost.entrySet()) {
                heartProcessor.processPostEvents(e.getKey(), e.getValue()); // transactional per-post
            }
            ack.acknowledge();
        } catch (Exception ex) {
            // TODO: Handle it properly later
            throw new RuntimeException(ex);
        }

    }

    @WithSpan(value = "deduplicate Heart events")
    private Map<PostUserKey, HeartEvent> dedupeEventsKeepLast(List<HeartEvent> events) {
        return events
                .parallelStream()
                .collect(Collectors.toConcurrentMap(
                        ev -> new PostUserKey(ev.postId(), ev.userId()),
                        ev -> ev,
                        (existing, replacement) -> {
                            return replacement.actionTime().isAfter(existing.actionTime()) ? replacement : existing;
                        }));
    }
}
