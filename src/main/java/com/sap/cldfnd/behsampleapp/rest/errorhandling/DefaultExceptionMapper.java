package com.sap.cldfnd.behsampleapp.rest.errorhandling;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

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
		final String errorMessage = exception.getMessage();
		final String errorCode = exception.getClass().getName();
		
		return Response
				.status(Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.entity(ErrorResponse.of(errorMessage, errorCode).toJson())
				.build();
	}

}
