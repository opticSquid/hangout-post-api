package com.hangout.core.post_api.utils;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

public class HashServiceTests {

    private final HashService hashService = new HashService();

    @Test
    void testComputeInternalFilename_withExtension() throws FileUploadException {
        byte[] content = "Hello World".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", content);

        String result = hashService.computeInternalFilename(file);

        assertNotNull(result);
        assertTrue(result.endsWith(".mp4"));
        assertEquals(128 + 4, result.length()); // SHA3-512 produces a 128 character hex string + ".mp4"
    }

    @Test
    void testComputeInternalFilename_withoutExtension() throws FileUploadException {
        byte[] content = "Hello World".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test", "video/mp4", content);

        String result = hashService.computeInternalFilename(file);

        assertNotNull(result);
        assertFalse(result.contains("."));
        assertEquals(128, result.length()); // SHA3-512 produces 128 character hex string
    }

    @Test
    void testComputeInternalFilename_emptyFile() throws FileUploadException {
        byte[] content = new byte[0];
        MockMultipartFile file = new MockMultipartFile("file", "empty.mp4", "video/mp4", content);

        String result = hashService.computeInternalFilename(file);

        assertNotNull(result);
        assertTrue(result.endsWith(".mp4"));
    }
}
