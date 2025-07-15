package com.hangout.core.post_api.dto;

import java.math.BigInteger;

public record FileUploadEvent(String filename, String contentType, BigInteger userId) {

}
