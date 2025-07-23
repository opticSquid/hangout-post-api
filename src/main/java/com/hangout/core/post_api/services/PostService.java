package com.hangout.core.post_api.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hangout.core.post_api.dto.FileUploadEvent;
import com.hangout.core.post_api.dto.GetNearbyPostsProjection;
import com.hangout.core.post_api.dto.GetParticularPostProjection;
import com.hangout.core.post_api.dto.GetPostsDTO;
import com.hangout.core.post_api.dto.PostCreationResponse;
import com.hangout.core.post_api.dto.PostsList;
import com.hangout.core.post_api.dto.Session;
import com.hangout.core.post_api.entities.Media;
import com.hangout.core.post_api.entities.Post;
import com.hangout.core.post_api.exceptions.FileUploadFailed;
import com.hangout.core.post_api.exceptions.UnauthorizedAccessException;
import com.hangout.core.post_api.exceptions.UnsupportedMediaType;
import com.hangout.core.post_api.repositories.MediaRepo;
import com.hangout.core.post_api.repositories.PostRepo;
import com.hangout.core.post_api.utils.AuthorizationService;
import com.hangout.core.post_api.utils.FileUploadService;
import com.hangout.core.post_api.utils.HashService;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@EnableCaching
@Slf4j
public class PostService {
    private final PostRepo postRepo;
    private final MediaRepo mediaRepo;
    private final AuthorizationService authorizationService;
    private final HashService hashService;
    private final FileUploadService fileUploadService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    @Value("${hangout.kafka.content.topic}")
    private String topic;
    @Value("${hangout.page-length}")
    private Integer pageLength;

    @WithSpan(value = "create-post service")
    @Transactional
    public PostCreationResponse create(String authToken, MultipartFile file,
            Optional<String> postDescription, String state, String city, Double lat, Double lon)
            throws FileUploadException {
        Session session = authorizationService.authorizeUser(authToken);
        // check if the session is trusted
        if (!session.trustedDevice()) {
            throw new UnauthorizedAccessException("Can not create new post from an untrusted device");
        } else {
            Point location = buildPoint(lat, lon);
            // check if media is already present in database
            String internalFilename;
            internalFilename = this.hashService.computeInternalFilename(file);
            Optional<Media> existingMedia = this.mediaRepo.findById(internalFilename);
            if (existingMedia.isPresent()) {
                Media media = existingMedia.get();
                Post post;
                if (postDescription.isPresent()) {
                    post = new Post(session.userId(), media, postDescription.get(), state, city, location);
                } else {
                    post = new Post(session.userId(), media, state, city, location);
                }
                post = this.postRepo.save(post);
                media.addPost(post);
                this.mediaRepo.save(media);
                return new PostCreationResponse(post.getPostId());
            } else {
                // is media is new
                if (file.getContentType().startsWith("video/")) {
                    uploadMedias(session, file, internalFilename);
                    Media media = new Media(internalFilename, file.getContentType());
                    media = this.mediaRepo.save(media);
                    Post post;
                    if (postDescription.isPresent()) {
                        post = new Post(session.userId(), media, postDescription.get(), state, city, location);
                    } else {
                        post = new Post(session.userId(), media, state, city, location);
                    }
                    post = this.postRepo.save(post);
                    media.addPost(post);
                    this.mediaRepo.save(media);
                    return new PostCreationResponse(post.getPostId());

                } else {
                    throw new UnsupportedMediaType(file.getOriginalFilename()
                            + " is not supported. Please upload a supported format of either image or video file");
                }
            }
        }
    }

    @WithSpan(value = "get-near-by-posts service")
    @Cacheable("findNearbyPosts")
    public PostsList findNearByPosts(GetPostsDTO searchParams) {
        log.debug("search params: {}", searchParams);
        Integer pageNumber = searchParams.pageNumber() > 1 ? searchParams.pageNumber() : 1;
        Integer offset = pageLength * (pageNumber - 1);
        Point userLocation = buildPoint(searchParams.lat(), searchParams.lon());
        List<GetNearbyPostsProjection> nearbyPosts = postRepo.getAllNearbyPosts(userLocation,
                searchParams.minSearchRadius(), searchParams.maxSearchRadius(), offset, pageLength);
        PostsList postsList;

        Integer totalCount = postRepo.getAllNearbyPostsCount(userLocation, searchParams.minSearchRadius(),
                searchParams.maxSearchRadius());
        Integer totalPages = (int) Math.ceil((double) totalCount / pageLength);
        postsList = new PostsList(nearbyPosts, pageNumber, totalPages);
        return postsList;
    }

    @WithSpan(value = "get-particular-post service")
    public GetParticularPostProjection getParticularPost(UUID postId) {
        Optional<GetParticularPostProjection> maybepost = postRepo.getParticularPost(postId);
        if (maybepost.isPresent()) {
            return maybepost.get();
        } else {
            return null;
        }
    }

    @WithSpan(value = "get-my-posts service")
    public List<GetParticularPostProjection> getMyPosts(String authToken) {
        Session session = authorizationService.authorizeUser(authToken);
        return postRepo.getPostsByOwnerId(session.userId());
    }

    @WithSpan(value = "increase heart count service")
    public void increaseHeartCount(UUID postId) {
        postRepo.increaseHeartCount(postId);
    }

    /**
     * upload the file given by the user in the post to storage service.
     * internally uploads the file to Minio/s3 bucket
     * also produces a kafka event to trigger storage service to process the new
     * file
     * 
     * @param session
     * @param file
     * @param internalFilename
     */
    @WithSpan(value = "create-post file upload service")
    private void uploadMedias(Session session, MultipartFile file, String internalFilename) {
        fileUploadService.uploadFile(internalFilename, file);
        try {
            this.kafkaTemplate.send(topic,
                    new FileUploadEvent(internalFilename, file.getContentType(), session.userId()));
        } catch (IllegalStateException e) {
            throw new FileUploadFailed("Failed to produce kafka event for file: " + file.getOriginalFilename());
        }
    }

    @WithSpan(value = "convert to Point data type service")
    private Point buildPoint(Double lat, Double lon) {
        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }

}
