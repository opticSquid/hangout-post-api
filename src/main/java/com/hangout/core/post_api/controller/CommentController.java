package com.hangout.core.post_api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hangout.core.post_api.dto.CommentCreationResponse;
import com.hangout.core.post_api.dto.CommentDTO;
import com.hangout.core.post_api.dto.NewCommentRequest;
import com.hangout.core.post_api.dto.Reply;
import com.hangout.core.post_api.services.CommentService;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/comment")
public class CommentController {
    private final CommentService commentService;

    @WithSpan(kind = SpanKind.SERVER, value = "create-top-level-comment controller")
    @PostMapping
    public CommentCreationResponse createTopLevelComment(@RequestHeader(name = "Authorization") String authToken,
            @RequestBody NewCommentRequest comment) {
        return commentService.createTopLevelComment(authToken, comment);
    }

    @WithSpan(kind = SpanKind.SERVER, value = "reply-to-comment controller")
    @PostMapping("/reply")
    public CommentCreationResponse createSubComment(@RequestHeader(name = "Authorization") String authToken,
            @RequestBody Reply reply) {
        return commentService.createSubComments(authToken, reply);
    }

    @WithSpan(kind = SpanKind.SERVER, value = "get-all-top-level-comments controller")
    @GetMapping("/all/{postId}")
    public List<CommentDTO> getAllTopLevelComments(@PathVariable UUID postId) {
        return commentService.fetchTopLevelCommentsForAPost(postId);
    }

    @WithSpan(kind = SpanKind.SERVER, value = "get-particular-comment controller")
    @GetMapping("/{commentId}")
    public CommentDTO getParticularComment(@PathVariable UUID commentId) {
        return commentService.fetchParticularComment(commentId);
    }

    @WithSpan(kind = SpanKind.SERVER, value = "get-replies-to-a-comment controller")
    @GetMapping("/{commentId}/replies")
    public List<CommentDTO> getAllChildCommentsOfAParentComment(@PathVariable UUID commentId) {
        return commentService.fetchAllChildCommentsForAComment(commentId);
    }
}
