package com.hangout.core.post_api.dto.event;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import com.hangout.core.post_api.dto.ActionType;

public record HeartEvent(ActionType actionType, UUID postId, BigInteger userId, LocalDateTime actionTime) {

}
