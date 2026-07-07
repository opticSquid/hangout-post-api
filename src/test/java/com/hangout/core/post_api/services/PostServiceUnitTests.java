package com.hangout.core.post_api.services;

import com.hangout.core.post_api.dto.FileUploadEvent;
import com.hangout.core.post_api.dto.PostCreationResponse;
import com.hangout.core.post_api.dto.Session;
import com.hangout.core.post_api.entities.Media;
import com.hangout.core.post_api.entities.Post;
import com.hangout.core.post_api.exceptions.UnauthorizedAccessException;
import com.hangout.core.post_api.exceptions.UnsupportedMediaType;
import com.hangout.core.post_api.repositories.MediaRepo;
import com.hangout.core.post_api.repositories.PostRepo;
import com.hangout.core.post_api.utils.AuthorizationService;
import com.hangout.core.post_api.utils.FileUploadService;
import com.hangout.core.post_api.utils.HashService;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PostServiceUnitTests {

    private PostService postService;

    private PostRepo postRepo;
    private MediaRepo mediaRepo;
    private AuthorizationService authorizationService;
    private HashService hashService;
    private FileUploadService fileUploadService;
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setUp() {
        postRepo = mock(PostRepo.class);
        mediaRepo = mock(MediaRepo.class);
        authorizationService = mock(AuthorizationService.class);
        hashService = mock(HashService.class);
        fileUploadService = mock(FileUploadService.class);
        kafkaTemplate = mock(KafkaTemplate.class);

        postService = new PostService(
                postRepo,
                mediaRepo,
                authorizationService,
                hashService,
                fileUploadService,
                kafkaTemplate
        );

        ReflectionTestUtils.setField(postService, "topic", "test-topic");
        ReflectionTestUtils.setField(postService, "pageLength", 25);
    }

    @Test
    void testCreatePost_untrustedDevice_throwsException() {
        Session session = new Session(new BigInteger("1"), "user1", false); // untrusted device
        when(authorizationService.authorizeUser("token")).thenReturn(session);

        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "content".getBytes());

        assertThrows(UnauthorizedAccessException.class, () -> {
            postService.create("token", file, Optional.empty(), "State", "City", 12.0, 34.0);
        });
    }

    @Test
    void testCreatePost_existingMedia_success() throws FileUploadException {
        Session session = new Session(new BigInteger("1"), "user1", true);
        when(authorizationService.authorizeUser("token")).thenReturn(session);

        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "content".getBytes());
        when(hashService.computeInternalFilename(file)).thenReturn("hash.mp4");

        Media existingMedia = new Media("hash.mp4", "video/mp4");
        when(mediaRepo.findById("hash.mp4")).thenReturn(Optional.of(existingMedia));

        UUID generatedPostId = UUID.randomUUID();
        when(postRepo.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "postId", generatedPostId);
            return p;
        });

        PostCreationResponse response = postService.create("token", file, Optional.of("Description"), "State", "City", 12.0, 34.0);

        assertNotNull(response);
        assertEquals(generatedPostId, response.postId());

        verify(mediaRepo, times(1)).save(existingMedia);
        verify(postRepo, times(1)).save(any(Post.class));
        verifyNoInteractions(fileUploadService);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void testCreatePost_newMediaVideo_success() throws FileUploadException {
        Session session = new Session(new BigInteger("1"), "user1", true);
        when(authorizationService.authorizeUser("token")).thenReturn(session);

        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "content".getBytes());
        when(hashService.computeInternalFilename(file)).thenReturn("hash.mp4");
        when(mediaRepo.findById("hash.mp4")).thenReturn(Optional.empty());

        when(mediaRepo.save(any(Media.class))).thenAnswer(inv -> inv.getArgument(0));

        UUID generatedPostId = UUID.randomUUID();
        when(postRepo.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "postId", generatedPostId);
            return p;
        });

        PostCreationResponse response = postService.create("token", file, Optional.empty(), "State", "City", 12.0, 34.0);

        assertNotNull(response);
        assertEquals(generatedPostId, response.postId());

        verify(fileUploadService, times(1)).uploadFile("hash.mp4", file);
        verify(mediaRepo, times(2)).save(any(Media.class));
        verify(postRepo, times(1)).save(any(Post.class));

        ArgumentCaptor<FileUploadEvent> eventCaptor = ArgumentCaptor.forClass(FileUploadEvent.class);
        verify(kafkaTemplate, times(1)).send(eq("test-topic"), eventCaptor.capture());
        FileUploadEvent event = eventCaptor.getValue();
        assertEquals("hash.mp4", event.filename());
        assertEquals("video/mp4", event.contentType());
        assertEquals(new BigInteger("1"), event.userId());
    }

    @Test
    void testCreatePost_unsupportedMediaType_throwsException() throws FileUploadException {
        Session session = new Session(new BigInteger("1"), "user1", true);
        when(authorizationService.authorizeUser("token")).thenReturn(session);

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        when(hashService.computeInternalFilename(file)).thenReturn("hash.jpg");
        when(mediaRepo.findById("hash.jpg")).thenReturn(Optional.empty());

        assertThrows(UnsupportedMediaType.class, () -> {
            postService.create("token", file, Optional.empty(), "State", "City", 12.0, 34.0);
        });
    }
}
