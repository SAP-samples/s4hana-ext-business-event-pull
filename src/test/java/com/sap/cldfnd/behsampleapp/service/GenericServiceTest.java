package com.sap.cldfnd.behsampleapp.service;

import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import javax.ws.rs.core.MediaType;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sap.cldfnd.behsampleapp.rest.errorhandling.ErrorResponse;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.testutil.MockUtil;

/**
 * Common test cases for both kind of services: those that use VDM and those that use ODataQueryBuilder 
 */
public abstract class GenericServiceTest {
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(31337);
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private static final MockUtil mockUtil = new MockUtil();
	
	protected abstract void callService() throws ODataException;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		mockUtil.mockDefaults();
		mockUtil.mockErpDestination();
	}
	
	/**
	 * The test case covers generic 500 error returned by S/4HANA 
	 */
	@Test
	public void testCallServiceWhen500ThenODataException() throws ODataException {
		// Given that S/4HANA returns 500 with a dummy error message
		final int httpErrorCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
		final String errorMessage = "Dummy error message";
		final String errorJson = ErrorResponse.of(errorMessage, httpErrorCode).toJson();
		givenThat(get(anyUrl()).willReturn(status(httpErrorCode).withBody(errorJson)));

		thrown.expect(ODataException.class);
		
		try {
			// When the service is called
			callService();
		} catch (ODataException e) { // Then an ODataException is thrown ...
			assertThat(e.getCause(), is(instanceOf(ODataException.class)));
			e = (ODataException) e.getCause();
			assertThat(e.getCode(), is(String.valueOf(httpErrorCode))); // ... that contains the HTTP error code returned by S/4HANA ...
			assertThat(e.getMessage(), containsString(errorMessage)); // ... that contains the original error message
			throw e;
		}
	}
	
	/**
	 * The test case covers a case when HTTP Error code differs from the code mentioned in the response body<br>
	 * In such case, the code from the message body should be included in the exception's message
	 */
	@Test
	public void testCallServiceWhen401AndErrorCodeInResponseDiffersFromHttpErrorCodeThenODataExceptionContainsCodeFromResponse() throws ODataException {
		// Given that S/4HANA returns 401 with a body that contains a dummy error message and a code that differs from HTTP error code 
		final int httpErrorCode = HttpStatus.SC_UNAUTHORIZED;
		final String errorCodeFromResponseBody = "Error code from response body";
		final String errorMessage = "Dummy error message";
		final String errorJson = ErrorResponse.of(errorMessage, errorCodeFromResponseBody).toJson();
		givenThat(get(anyUrl()).willReturn(status(httpErrorCode).withBody(errorJson)));

		thrown.expect(ODataException.class);
		
		try {
			// When the service is called
			callService();
		} catch (ODataException e) { // Then an ODataException is thrown ...			
			assertThat(e.getCause(), is(instanceOf(ODataException.class)));
			e = (ODataException) e.getCause();
			assertThat(e.getCode(), is(String.valueOf(httpErrorCode))); // ... that contains the HTTP error code returned by S/4HANA ...
			assertThat(e.getMessage(), containsString(errorCodeFromResponseBody)); // ... that contains the error code from response body as a part of the message ...
			assertThat(e.getMessage(), containsString(errorMessage)); // ... that contains the original error message
			throw e;
		}
	}

	/**
	 * The test case covers S/4HANA under maintenance 
	 */
	@Test
	public void testCallServiceWhen503AndNonJsonBodyThenODataException() throws ODataException {
		final int errorCode = HttpStatus.SC_SERVICE_UNAVAILABLE;
		givenThat(get(anyUrl())
				.willReturn(status(errorCode)
				.withBody("<!DOCTYPE html>")
				.withHeader("Content-Type", MediaType.TEXT_HTML)));

		thrown.expect(ODataException.class);
		
		try {
			// When the service is called
			callService();
		} catch (ODataException e) { // Then an ODataException is thrown ...
			assertThat(e.getCause(), is(instanceOf(ODataException.class)));
			e = (ODataException) e.getCause();
			assertThat(e.getCode(), is(String.valueOf(errorCode))); // ... that contains the HTTP error code returned by S/4HANA ...
			throw e;
		}
	}

}
