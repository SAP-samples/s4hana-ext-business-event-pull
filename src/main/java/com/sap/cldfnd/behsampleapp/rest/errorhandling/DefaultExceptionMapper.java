package com.sap.cldfnd.behsampleapp.rest.errorhandling;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import com.google.common.annotations.VisibleForTesting;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;

/**
 * Custom exception mapper for CXF to be used with exception types for which no
 * other specific mapper exists
 * <p>
 * The mapper overrides the default CXF mapper that always returns HTTP 500
 * Error code and the body with the exception's message in HTML format
 * <p>
 * Returns an error response with the default HTTP error code 500 and the JSON
 * body in {@link ErrorResponse} format. ErrorResponse.code is the exception's
 * class name and ErrorResponse.message is the exception's message.
 *
 */
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

	@Override
	public Response toResponse(Throwable exception) {
		final ErrorResponse errorResponse = toErrorResponse(exception);
		
		return Response
				.status(getHttpStatusCode(exception))
				.type(MediaType.APPLICATION_JSON)
				.entity(errorResponse)
				.build();
	}

	public static ErrorResponse toErrorResponse(Throwable exception) {
		final String errorCode = exception.getClass().getSimpleName();
		final String errorMessage = exception.getMessage();
		final ErrorResponse errorResponse = ErrorResponse.of(errorCode, errorMessage);

		if (exception.getCause() != null) {
			errorResponse.setInnerError(toErrorResponse(exception.getCause()));
		}
		
		return errorResponse;
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
	protected static int getHttpStatusCode(Throwable exception) {
		for ( ; exception != null ; exception = exception.getCause()) {
			if (exception instanceof ODataException) {
				final String errorCode = ((ODataException) exception).getCode();
				
				try {
					// throws a NumberFormatException if a string cannot be converted to integer 
					int statusCodeCandidate = Integer.valueOf(errorCode);

					// throws an IllegalArgumentException if statusCode is not a valid HTTP code
					Response.status(statusCodeCandidate);
					
					return statusCodeCandidate;
				} catch (IllegalArgumentException e) {
					// continue
				}
			};
		};
		
		return Status.INTERNAL_SERVER_ERROR.getStatusCode();
	}
	
}
