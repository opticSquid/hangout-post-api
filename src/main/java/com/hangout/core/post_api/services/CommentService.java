package com.hangout.core.post_api.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hangout.core.post_api.dto.CommentCreationResponse;
import com.hangout.core.post_api.dto.CommentDTO;
import com.hangout.core.post_api.dto.NewCommentRequest;
import com.hangout.core.post_api.dto.Reply;
import com.hangout.core.post_api.dto.Session;
import com.hangout.core.post_api.entities.Comment;
import com.hangout.core.post_api.entities.HierarchyKeeper;
import com.hangout.core.post_api.entities.Post;
import com.hangout.core.post_api.exceptions.NoDataFound;
import com.hangout.core.post_api.exceptions.UnauthorizedAccessException;
import com.hangout.core.post_api.projections.FetchCommentProjection;
import com.hangout.core.post_api.repositories.CommentRepo;
import com.hangout.core.post_api.repositories.HierarchyKeeperRepo;
import com.hangout.core.post_api.repositories.PostRepo;
import com.hangout.core.post_api.utils.AuthorizationService;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final PostRepo postRepo;
    private final CommentRepo commentRepo;
    private final HierarchyKeeperRepo hkRepo;
    private final AuthorizationService authorizationService;

    @WithSpan(value = "create-top-level-comment service")
    @Transactional
    public CommentCreationResponse createTopLevelComment(String authToken, NewCommentRequest comment) {
        Session session = authorizationService.authorizeUser(authToken);
        if (session.userId() != null) {
            Optional<Post> post = postRepo.findById(comment.postId());
            if (post.isPresent()) {
                Comment topLevelComment = new Comment(post.get(), session.userId(), comment.comment(), true);
                postRepo.increaseCommentCount(post.get().getPostId());
                topLevelComment = commentRepo.save(topLevelComment);
                return new CommentCreationResponse("comment posted", topLevelComment.getCommentId());
            } else {
                throw new NoDataFound("no post found");
            }
        } else {
            throw new UnauthorizedAccessException("user unauthorized. Can not create comment");
        }
    }

    @WithSpan(value = "reply-to-comment service")
    @Transactional
    public CommentCreationResponse createSubComments(String authToken, Reply reply) {
        Session session = authorizationService.authorizeUser(authToken);
        if (session.userId() != null) {
            Optional<Comment> maybeParentComment = commentRepo.findById(reply.parentCommentId());
            if (maybeParentComment.isPresent()) {
                Comment parentComment = maybeParentComment.get();
                Optional<Post> post = postRepo.findById(parentComment.getPost().getPostId());
                Comment childComment = new Comment(post.get(), session.userId(), reply.comment(), false);
                postRepo.increaseCommentCount(post.get().getPostId());
                commentRepo.increaseReplyCount(parentComment.getCommentId());
                childComment = commentRepo.save(childComment);
                HierarchyKeeper hierarchy = new HierarchyKeeper(parentComment, childComment);
                hkRepo.save(hierarchy);
                return new CommentCreationResponse("comment posted", childComment.getCommentId());
            } else {
                throw new NoDataFound("no parent comment found");
            }
        } else {
            throw new UnauthorizedAccessException("user unauthorized. Can not create comment");
        }
    }

    @WithSpan(value = "get-all-top-level-comments service")
    public List<CommentDTO> fetchTopLevelCommentsForAPost(UUID postId) {
        UUID postIdAsUUID = postId;
        List<FetchCommentProjection> model = commentRepo.fetchTopLevelComments(postIdAsUUID);
        return model.stream()
                .map(comment -> new CommentDTO(comment.getCommentId(),
                        comment.getCreatedAt(),
                        comment.getText(), comment.getUserId(), comment.getReplies()))
                .toList();
    }

    @WithSpan(value = "get-particular-comment service")
    public CommentDTO fetchParticularComment(UUID commentId) {
        Optional<FetchCommentProjection> comment = commentRepo.fetchCommentById(commentId);
        if (comment.isPresent()) {
            return new CommentDTO(comment.get().getCommentId(),
                    comment.get().getCreatedAt(), comment.get().getText(),
                    comment.get().getUserId(), comment.get().getReplies());
        } else {
            throw new NoDataFound("No Comment was found with the given id");
        }

    }

    @WithSpan(kind = SpanKind.SERVER, value = "get-replies-to-a-comment service")
    public List<CommentDTO> fetchAllChildCommentsForAComment(UUID parentCommentId) {
        UUID parentCommentIdUUID = parentCommentId;
        List<FetchCommentProjection> model = hkRepo.findAllChildComments(parentCommentIdUUID);
        return model.stream()
                .map(comment -> new CommentDTO(comment.getCommentId(),
                        comment.getCreatedAt(),
                        comment.getText(), comment.getUserId(), comment.getReplies()))
                .toList();
    }
}
