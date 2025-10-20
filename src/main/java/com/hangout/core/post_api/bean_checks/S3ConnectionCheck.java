package com.hangout.core.post_api.bean_checks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.hangout.core.post_api.exceptions.AwsS3ClientException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3ConnectionCheck {
    private final S3Client s3Client;

    @Value("${hangout.media.upload-bucket}")
    private String uploadBucket;

    @PostConstruct
    public void verifyBucket() {
        try {
            log.info("Checking if S3 bucket '{}' exists...", uploadBucket);
            s3Client.headBucket(HeadBucketRequest.builder().bucket(uploadBucket).build());
            log.info("Bucket '{}' exists.", uploadBucket);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.warn("Bucket '{}' not found. Creating...", uploadBucket);
                s3Client.createBucket(CreateBucketRequest.builder().bucket(uploadBucket).build());
                log.info("Bucket '{}' created successfully.", uploadBucket);
            } else {
                throw new AwsS3ClientException("Failed to verify/create S3 bucket", e);
            }
        }
    }
}
