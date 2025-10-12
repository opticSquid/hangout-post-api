package com.hangout.core.post_api.dto.internal;

import java.math.BigInteger;
import java.util.UUID;

public record PostUserKey(UUID postId, BigInteger userId) {

}
