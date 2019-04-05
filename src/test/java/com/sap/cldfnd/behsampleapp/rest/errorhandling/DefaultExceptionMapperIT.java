package com.sap.cldfnd.behsampleapp.rest.errorhandling;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.IOException;
import java.net.URL;

import org.apache.http.HttpStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.ArquillianTest;
import org.jboss.arquillian.junit.ArquillianTestClass;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sap.cldfnd.behsampleapp.TestUtil;
import com.sap.cldfnd.behsampleapp.model.BusinessEvent;
import com.sap.cldfnd.behsampleapp.model.TopResults;
import com.sap.cldfnd.behsampleapp.rest.BehApplication;
import com.sap.cldfnd.behsampleapp.rest.BehController;
import com.sap.cldfnd.behsampleapp.rest.BehControllerIT;
import com.sap.cldfnd.behsampleapp.rest.JsonProvider;
import com.sap.cldfnd.behsampleapp.service.BusinessEventService;
import com.sap.cldfnd.behsampleapp.service.SalesOrderService;
import com.sap.cldfnd.behsampleapp.service.WireMockUtil;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultBusinessEventQueueService;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultSalesOrderService;
import com.sap.cloud.sdk.testutil.MockUtil;

import io.restassured.RestAssured;

public class DefaultExceptionMapperIT {
	
	private static final MockUtil mockUtil = new MockUtil();
	
	private static String expectedErrorJson;
	
	@ClassRule
	public static ArquillianTestClass arquillianTestClass = new ArquillianTestClass();

	@Rule
	public ArquillianTest arquillianTest = new ArquillianTest();
	
	@ArquillianResource
	private URL baseUrl;
	
	@Rule
	public WireMockRule wireMockRule = mockUtil.mockErpServer();

	@Deployment
	public static WebArchive createDeployment() {
		return TestUtil.createDeployment(
			BehController.class,
			BehApplication.class,
			DefaultExceptionMapper.class,
			DefaultBusinessEventQueueService.class,		 
			DefaultSalesOrderService.class,
			BusinessEventService.class,
			SalesOrderService.class,
			JsonProvider.class,
			BusinessEvent.class,
			TopResults.class,
			// this class should not be picked up by CXF as provider thanks
			// for the configuration in
			// src/main/resources/META-INF/openejb-jar.xml
			org.apache.olingo.odata2.core.rest.ODataExceptionMapperImpl.class
		);
	}

	@BeforeClass
	public static void beforeClass() throws IOException {
		mockUtil.mockDefaults();
		mockUtil.mockErpDestination();
		
		expectedErrorJson = TestUtil.loadFileAsString("/expectedResponses/expectedError.json");
	}

	@Before
	public void before() {
		RestAssured.baseURI = baseUrl.toExternalForm();
	}

	@Test
	public void testWhenS4CallFailedThenExceptionMapperCreatedDetailedErrorResponse() {
		WireMockUtil.mockMalformedResponseChunk();

		given().
		when().
			get(BehControllerIT.SERVICE_PATH).
		then().
			statusCode(is(500)).
			body(sameJSONAs(expectedErrorJson).allowingExtraUnexpectedFields());
	}
	
	@Test
	public void testHttpStatusIsTakenFromODataException() {
		final int expectedErrorCode = HttpStatus.SC_EXPECTATION_FAILED;
		
		givenThat(get(anyUrl()).
				willReturn(aResponse().withStatus(expectedErrorCode)));		
		
		given().
		when().
			get(BehControllerIT.SERVICE_PATH).
		then().
			statusCode(is(expectedErrorCode)).
			body(sameJSONAs(expectedErrorJson).allowingExtraUnexpectedFields());
	}
	
}
