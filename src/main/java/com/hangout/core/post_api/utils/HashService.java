package com.hangout.core.post_api.utils;

import java.io.IOException;
import java.math.BigInteger;
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
     * Computes the sha3-512 hash of a file and concatenates with original file
     * extesion to give the new file name
     * 
     * @param file
     * @return new file name for internal use
     * @throws FileUploadException
     */
    @WithSpan(value = "compute internal filename")
    public String computeInternalFilename(MultipartFile file) throws FileUploadException {
        byte[] data;
        try {
            data = file.getBytes();
            try {
                byte[] hash = MessageDigest.getInstance(ALGORITHM).digest(data);
                String checksum = new BigInteger(1, hash).toString(16);
                int lastDotIndex = file.getOriginalFilename().lastIndexOf('.');
                String originalfileExtension = file.getOriginalFilename().substring(lastDotIndex + 1);
                checksum += "." + originalfileExtension;
                return checksum;
            } catch (NoSuchAlgorithmException ex) {
                throw new IllegalArgumentException(ex);
            }
        } catch (IOException e) {
            throw new FileUploadException(
                    "the file contents can not be processed may be the file is corrupted. Please check and reupload");
        }
    }
}
