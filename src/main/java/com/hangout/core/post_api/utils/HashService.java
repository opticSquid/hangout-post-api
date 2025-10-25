package com.hangout.core.post_api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HashService {
    private static final String ALGORITHM = "SHA3-512";

    /**
     * Computes a hash of the file and concatenates with the original file extension
     * to give a safe, deterministic filename.
     */
    @WithSpan(value = "compute internal filename")
    public String computeInternalFilename(MultipartFile file) throws FileUploadException {
        try (InputStream is = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            String checksum = toHex(digest.digest());
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null) {
                int lastDotIndex = originalFilename.lastIndexOf('.');
                if (lastDotIndex != -1 && lastDotIndex < originalFilename.length() - 1) {
                    extension = originalFilename.substring(lastDotIndex + 1);
                }
            }
            return extension.isEmpty() ? checksum : (checksum + "." + extension);
        } catch (IOException e) {
            throw new FileUploadException(
                    "The file contents cannot be processed. The file may be corrupted. Please check and reupload.");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit((b & 0xF), 16));
        }
        return sb.toString();
    }
}
