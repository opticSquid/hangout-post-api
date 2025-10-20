package com.hangout.core.post_api.exceptions.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.hangout.core.post_api.exceptions.AwsS3ClientException;
import com.hangout.core.post_api.exceptions.ConnectionFailed;
import com.hangout.core.post_api.exceptions.FileUploadFailed;
import com.hangout.core.post_api.exceptions.NoDataFound;
import com.hangout.core.post_api.exceptions.UnauthorizedAccessException;
import com.hangout.core.post_api.exceptions.UnsupportedMediaType;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	@ExceptionHandler(UnauthorizedAccessException.class)
	public ProblemDetail exceptionHandler(UnauthorizedAccessException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
		problem.setTitle("Access Denied");
		return problem;
	}

	@ExceptionHandler(UnsupportedMediaType.class)
	public ProblemDetail exceptionHandler(UnsupportedMediaType ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
		problem.setTitle("Unsupported Media Type");
		return problem;
	}

	@ExceptionHandler(FileUploadFailed.class)
	public ProblemDetail exceptionHandler(FileUploadFailed ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		problem.setTitle("File Upload Failed due to technical Errors. Please try after some time");
		return problem;
	}

	@ExceptionHandler(NoDataFound.class)
	public ProblemDetail exceptionHandler(NoDataFound ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NO_CONTENT, ex.getMessage());
		problem.setTitle("No Data Found");
		return problem;
	}

	@ExceptionHandler(ConnectionFailed.class)
	public ProblemDetail exceptionHandler(ConnectionFailed ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NO_CONTENT, ex.getMessage());
		problem.setTitle("Connection Failed...");
		return problem;
	}

	@ExceptionHandler(AwsS3ClientException.class)
	public ProblemDetail exceptionHandler(AwsS3ClientException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NO_CONTENT, ex.getMessage());
		problem.setTitle("Connection Failed to S3...");
		return problem;
	}
}