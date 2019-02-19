package com.sap.cldfnd.behsampleapp.rest.errorhandling;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import com.google.common.annotations.VisibleForTesting;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;

/**
 * Custom exception mapper for CXF to be used for {@link ODataException}
 * <p>
 * The mapper overrides the default CXF mapper that always returns HTTP 500
 * Error code and the body with the exception's message in HTML format
 * <p>
 * Sets the response code to the error code taken from {@link ODataException} or
 * 500 if the error code of {@link ODataException} is not a valid HTTP status
 * code.
 * <p>
 * Sets the {@link ErrorResponse} in JSON format as a response body.
 * ErrorResponse.code is error code taken from {@link ODataException} name and
 * ErrorResponse.message is the exception's message.
 *
 */
public class ODataExceptionMapper implements ExceptionMapper<ODataException> {
	
	@Override
	public Response toResponse(ODataException exception) {
		// unwrap extension if it is wrapped
		if (exception.getCause() instanceof ODataException) {
			exception = (ODataException) exception.getCause();
		}
		
		final String errorMessage = exception.getMessage();
		final String errorCode = exception.getCode();
		
		return Response
				.status(getHttpStatusCode(exception))
				.type(MediaType.APPLICATION_JSON)
				.entity(ErrorResponse.of(errorMessage, errorCode).toJson())
				.build();
	}

	/**
	 * @return the HTTP error code taken from {@link ODataException} or 500 in
	 *         the error code of {@link ODataException} is not a valid HTTP
	 *         status code
	 * 
	 * @param exception
	 *            an exception from which an HTTP error code should be taken
	 * @see Response#status(int) to see how status code is validated in CXF
	 */
	@VisibleForTesting
	protected static int getHttpStatusCode(ODataException exception) {
		try {
			int statusCode = Integer.valueOf(exception.getCode());
			
			// if statusCode is an incorrect HTTP code, it will throw an IllegalArgumentException
			Response.status(statusCode);
			
			return statusCode;
		} catch (IllegalArgumentException e) {
			return Status.INTERNAL_SERVER_ERROR.getStatusCode();
		}
	}

}
