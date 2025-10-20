package com.hangout.core.post_api.utils;

import com.hangout.core.post_api.exceptions.FileUploadFailed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;

@Service
@Slf4j
public class FileUploadService {

    @Autowired
    private S3Client s3Client;

    @Value("${hangout.media.upload-bucket}")
    private String uploadBucket;

    @WithSpan(kind = SpanKind.CLIENT, value = "cloud storage upload file call")
    public void uploadFile(String internalName, MultipartFile multipartFile) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(uploadBucket)
                    .key(internalName)
                    .contentType(multipartFile.getContentType())
                    .build();

            PutObjectResponse response = s3Client.putObject(
                    putRequest,
                    RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));

            log.info("File '{}' uploaded to bucket '{}' with eTag: {}",
                    internalName, uploadBucket, response.eTag());

        } catch (IOException e) {
            log.error("I/O error while uploading '{}': {}", multipartFile.getOriginalFilename(), e.getMessage());
            throw new FileUploadFailed("Failed to upload file: " + multipartFile.getOriginalFilename());
        } catch (Exception e) {
            log.error("Failed to upload '{}' to S3: {}", multipartFile.getOriginalFilename(), e.getMessage(), e);
            throw new FileUploadFailed(multipartFile.getOriginalFilename() + " failed to upload");
        }
    }
}
