package com.sap.cldfnd.behsampleapp.rest.errorhandling;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.odatav2.connectivity.ODataExceptionType;

public class DefaultExceptionMapperTest {

	DefaultExceptionMapper testee = new DefaultExceptionMapper();
	
	@Test
	public void testToResponseUnwrappedWithNullCause() throws JsonParseException, JsonMappingException, IOException {
		// Given an ODataException which cause is null
		final ODataException exception = new ODataException(ODataExceptionType.INPUT_DATA_SERIALIZATION_FAILED, 
				"Exception message", 
				/* cause = */ null);
		exception.setCode("405");
		
		// When
		final Response response = testee.toResponse(exception);
		
		// Then
		assertErrorResponse(response, 405, exception);

	}
	
	@Test
	public void testToResponseUnwrapped() throws JsonParseException, JsonMappingException, IOException {
		// Given an ODataException which cause is not an ODataException
		final ODataException exception = new ODataException(ODataExceptionType.INVALID_PROPERTY_NAME, 
				"Exception message", 
				new Throwable("Cause"));
		exception.setCode("405");
		
		// When
		final Response response = testee.toResponse(exception);
		
		// Then 
		assertErrorResponse(response, 405, exception);
	}
	
	@Test
	public void testToResponseWrapped() throws JsonParseException, JsonMappingException, IOException {
		// Given an ODataException which cause is ODataException as well 
		final ODataException cause = new ODataException(ODataExceptionType.OTHER, 
				"Cause exception's message",
				new Throwable("Not important 2nd level cause"));
		cause.setCode("503");
		
		final ODataException exception = new ODataException(ODataExceptionType.METADATA_FETCH_FAILED, 
				"Outer exception's message", 
				cause);
		exception.setCode("This code should not matter since the code from cause should be taken");
		
		// When
		final Response response = testee.toResponse(exception);
		
		// Then
		assertErrorResponse(response, 503, exception);
	}

	private void assertErrorResponse(final Response response, final int expectedStatusCode, Throwable exception) throws JsonParseException, JsonMappingException, IOException {
		assertThat("response code", response.getStatus(), is(expectedStatusCode));
		
		assertThat("response entity type", response.getEntity(), is(instanceOf(ErrorResponse.class)));
		ErrorResponse errorEntity = (ErrorResponse) response.getEntity();
		
		while (exception != null) {
			assertThat("error response / innen error was null while there was an exception / cause", errorEntity != null);
			assertThat("error response's message", errorEntity.getMessage().getValue(), is(exception.getMessage()));
			assertThat("error response's code", errorEntity.getCode(), is(exception.getClass().getSimpleName()));
			exception = exception.getCause();
			errorEntity = errorEntity.getInnerError();
		}
	}
	
	@Test
	public void testGetHttpStatusCode() {
		// Given an ODataException with code = 404
		final ODataException exception = new ODataException();
		exception.setCode("404");
		
		// When
		final int httpStatusCode = DefaultExceptionMapper.getHttpStatusCode(exception);
		
		// Then
		assertThat("DefaultExceptionMapper.getHttpStatusCode()", httpStatusCode, is(404));
	}
	
	@Test
	public void testGetHttpStatusCodeWhenInvalidCodeThen500() {
		// given an ODataException with invalid HTTP Code
		final ODataException exception = new ODataException();
		exception.setCode("1000");
		
		// When
		final int httpStatusCode = DefaultExceptionMapper.getHttpStatusCode(exception);
		
		// Then status code is default (500)
		assertThat("DefaultExceptionMapper.getHttpStatusCode()", httpStatusCode, is(500));
	}
	
	@Test
	public void testGetHttpStatusCodeWhenCodeIsNullThen500() {
		// given an ODataException code = null
		final ODataException exception = new ODataException();
		exception.setCode(null);
		
		// When
		final int httpStatusCode = DefaultExceptionMapper.getHttpStatusCode(exception);
		
		// Then status code is default (500)
		assertThat("DefaultExceptionMapper.getHttpStatusCode()", httpStatusCode, is(500));
	}
	
	@Test
	public void testGetHttpStatusCodeWhenCodeIsNotANumberThen500() {
		// given an ODataException which code is not a number
		final ODataException exception = new ODataException();
		exception.setCode("Not a number");
		
		// When
		final int httpStatusCode = DefaultExceptionMapper.getHttpStatusCode(exception);
		
		// Then status code is default (500)
		assertThat("DefaultExceptionMapper.getHttpStatusCode()", httpStatusCode, is(500));
	}
	
	@Test
	public void testGetHttpStatusCodeWhenNotAnODataExceptionThen500() {
		// given
		final RuntimeException exception = new RuntimeException("I am not an OData exception");
		
		// When
		final int httpStatusCode = DefaultExceptionMapper.getHttpStatusCode(exception);
		
		// Then status code is default (500)
		assertThat("DefaultExceptionMapper.getHttpStatusCode()", httpStatusCode, is(500));
	}
	
	@Test
	public void testGetHttpStatusCodeWhenNullExceptionThen500() {
		// When
		final int httpStatusCode = DefaultExceptionMapper.getHttpStatusCode(null);
		
		// Then status code is default (500)
		assertThat("DefaultExceptionMapper.getHttpStatusCode()", httpStatusCode, is(500));
	}
	
}
