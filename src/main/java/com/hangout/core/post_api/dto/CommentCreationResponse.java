package com.hangout.core.post_api.dto;

import java.util.UUID;

public record CommentCreationResponse(String message, UUID commentId) {

}
