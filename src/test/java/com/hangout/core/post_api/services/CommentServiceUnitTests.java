package com.hangout.core.post_api.services;

import com.hangout.core.post_api.dto.CommentCreationResponse;
import com.hangout.core.post_api.dto.NewCommentRequest;
import com.hangout.core.post_api.dto.Reply;
import com.hangout.core.post_api.dto.Session;
import com.hangout.core.post_api.entities.Comment;
import com.hangout.core.post_api.entities.HierarchyKeeper;
import com.hangout.core.post_api.entities.Post;
import com.hangout.core.post_api.exceptions.NoDataFound;
import com.hangout.core.post_api.exceptions.UnauthorizedAccessException;
import com.hangout.core.post_api.repositories.CommentRepo;
import com.hangout.core.post_api.repositories.HierarchyKeeperRepo;
import com.hangout.core.post_api.repositories.PostRepo;
import com.hangout.core.post_api.utils.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CommentServiceUnitTests {

    private CommentService commentService;

    private PostRepo postRepo;
    private CommentRepo commentRepo;
    private HierarchyKeeperRepo hkRepo;
    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        postRepo = mock(PostRepo.class);
        commentRepo = mock(CommentRepo.class);
        hkRepo = mock(HierarchyKeeperRepo.class);
        authorizationService = mock(AuthorizationService.class);

        commentService = new CommentService(postRepo, commentRepo, hkRepo, authorizationService);
    }

    @Test
    void testCreateTopLevelComment_success() {
        Session session = new Session(new BigInteger("1"), "user1", true);
        when(authorizationService.authorizeUser("token")).thenReturn(session);

        UUID postId = UUID.randomUUID();
        Post post = new Post();
        ReflectionTestUtils.setField(post, "postId", postId);
        when(postRepo.findById(postId)).thenReturn(Optional.of(post));

        UUID commentId = UUID.randomUUID();
        when(commentRepo.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "commentId", commentId);
            return c;
        });

        NewCommentRequest request = new NewCommentRequest(postId, "this is a comment");
        CommentCreationResponse response = commentService.createTopLevelComment("token", request);

        assertNotNull(response);
        assertEquals("comment posted", response.message());
        assertEquals(commentId, response.commentId());

        verify(postRepo, times(1)).increaseCommentCount(postId);
        verify(commentRepo, times(1)).save(any(Comment.class));
    }

    @Test
    void testCreateTopLevelComment_postNotFound_throwsException() {
        Session session = new Session(new BigInteger("1"), "user1", true);
        when(authorizationService.authorizeUser("token")).thenReturn(session);

        UUID postId = UUID.randomUUID();
        when(postRepo.findById(postId)).thenReturn(Optional.empty());

        NewCommentRequest request = new NewCommentRequest(postId, "this is a comment");

        assertThrows(NoDataFound.class, () -> {
            commentService.createTopLevelComment("token", request);
        });
    }

    @Test
    void testCreateTopLevelComment_unauthorized_throwsException() {
        Session session = new Session(null, null, false);
        when(authorizationService.authorizeUser("token")).thenReturn(session);

        NewCommentRequest request = new NewCommentRequest(UUID.randomUUID(), "comment");

        assertThrows(UnauthorizedAccessException.class, () -> {
            commentService.createTopLevelComment("token", request);
        });
    }

    @Test
    void testCreateSubComment_success() {
        Session session = new Session(new BigInteger("2"), "user2", true);
        when(authorizationService.authorizeUser("token")).thenReturn(session);

        UUID parentCommentId = UUID.randomUUID();
        Post post = new Post();
        UUID postId = UUID.randomUUID();
        ReflectionTestUtils.setField(post, "postId", postId);

        Comment parentComment = new Comment();
        ReflectionTestUtils.setField(parentComment, "commentId", parentCommentId);
        ReflectionTestUtils.setField(parentComment, "post", post);

        when(commentRepo.findById(parentCommentId)).thenReturn(Optional.of(parentComment));
        when(postRepo.findById(postId)).thenReturn(Optional.of(post));

        UUID childCommentId = UUID.randomUUID();
        when(commentRepo.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "commentId", childCommentId);
            return c;
        });

        Reply reply = new Reply(postId, parentCommentId, "reply text");
        CommentCreationResponse response = commentService.createSubComments("token", reply);

        assertNotNull(response);
        assertEquals("comment posted", response.message());
        assertEquals(childCommentId, response.commentId());

        verify(postRepo, times(1)).increaseCommentCount(postId);
        verify(commentRepo, times(1)).increaseReplyCount(parentCommentId);
        verify(commentRepo, times(1)).save(any(Comment.class));
        verify(hkRepo, times(1)).save(any(HierarchyKeeper.class));
    }
}
