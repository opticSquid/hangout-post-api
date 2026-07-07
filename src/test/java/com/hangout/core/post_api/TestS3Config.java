package com.hangout.core.post_api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.mockito.Mockito;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@TestConfiguration
public class TestS3Config {

    @Bean
    @Primary
    public S3Client testS3Client() {
        S3Client mockS3 = Mockito.mock(S3Client.class);

        Mockito.when(mockS3.headBucket(Mockito.any(HeadBucketRequest.class)))
                .thenReturn(HeadBucketResponse.builder().build());

        Mockito.when(mockS3.createBucket(Mockito.any(CreateBucketRequest.class)))
                .thenReturn(CreateBucketResponse.builder().build());

        Mockito.when(mockS3.putObject(Mockito.any(PutObjectRequest.class), Mockito.any(RequestBody.class)))
                .thenAnswer(invocation -> {
                    PutObjectRequest request = invocation.getArgument(0);
                    RequestBody body = invocation.getArgument(1);
                    String bucket = request.bucket();
                    String key = request.key();

                    Path targetDir = Paths.get("target", "test-s3-uploads", bucket);
                    Files.createDirectories(targetDir);
                    Path targetFile = targetDir.resolve(key);

                    try (InputStream is = body.contentStreamProvider().newStream()) {
                        Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    }

                    return PutObjectResponse.builder().eTag("test-etag").build();
                });

        return mockS3;
    }
}
