package com.hangout.core.post_api.dto;

import java.util.UUID;

public record NewCommentRequest(UUID postId, String comment) {

}
