package com.sap.cldfnd.behsampleapp.rest.errorhandling;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cldfnd.behsampleapp.rest.JsonProvider;
import com.sap.cldfnd.behsampleapp.rest.errorhandling.ErrorResponse.ErrorMessage;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.odatav2.connectivity.ODataExceptionType;

public class ODataExceptionMapperTest {
	
	ODataExceptionMapper testee = new ODataExceptionMapper();
	
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
		assertResponse(response, 405, exception);
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
		assertResponse(response, 405, exception);
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
		assertResponse(response, 503, cause);
	}

	public void assertResponse(final Response response, final int expectedStatusCode, final ODataException exception) throws JsonParseException, JsonMappingException, IOException {
		assertThat("response code", response.getStatus(), is(expectedStatusCode));
		
		final ObjectMapper om = new JsonProvider().locateMapper(ErrorMessage.class, MediaType.APPLICATION_JSON_TYPE);
		final ErrorResponse errorEntity = om
				.enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
				.readValue(response.getEntity().toString(), ErrorResponse.class);
		
		assertThat("error response's message", errorEntity.getMessageText(), is(exception.getMessage()));
		assertThat("error response's code", errorEntity.getCode(), is(exception.getCode()));
	}
	
	@Test
	public void testGetHttpStatusCode() {
		// Given an ODataException with code = 404
		final ODataException exception = new ODataException();
		exception.setCode("404");
		
		// When
		final int httpStatusCode = ODataExceptionMapper.getHttpStatusCode(exception);
		
		// Then
		assertThat("ODataExceptionMapper.getHttpStatusCode", httpStatusCode, is(404));
	}
	
	@Test
	public void testGetHttpStatusCodeWhenInvalidCodeThen500() {
		// given an ODataException with invalid HTTP Code
		final ODataException exception = new ODataException();
		exception.setCode("1000");
		
		// When
		final int httpStatusCode = ODataExceptionMapper.getHttpStatusCode(exception);
		
		// Then status code is default (500)
		assertThat("ODataExceptionMapper.getHttpStatusCode", httpStatusCode, is(500));
	}
	
	@Test
	public void testGetHttpStatusCodeWhenCodeIsNullThen500() {
		// given an ODataException code = null
		final ODataException exception = new ODataException();
		exception.setCode(null);
		
		// When
		final int httpStatusCode = ODataExceptionMapper.getHttpStatusCode(exception);
		
		// Then status code is default (500)
		assertThat("ODataExceptionMapper.getHttpStatusCode", httpStatusCode, is(500));
	}
	
	@Test
	public void testGetHttpStatusCodeWhenCodeIsNotANumberThen500() {
		// given an ODataException which code is not a number
		final ODataException exception = new ODataException();
		exception.setCode("Not a number");
		
		// When
		final int httpStatusCode = ODataExceptionMapper.getHttpStatusCode(exception);
		
		// Then status code is default (500)
		assertThat("ODataExceptionMapper.getHttpStatusCode", httpStatusCode, is(500));
	}
	
}
