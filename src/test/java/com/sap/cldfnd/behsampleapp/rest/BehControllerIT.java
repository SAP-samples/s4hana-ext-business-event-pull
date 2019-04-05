package com.sap.cldfnd.behsampleapp.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.IOException;
import java.net.URL;

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
import com.sap.cldfnd.behsampleapp.rest.errorhandling.DefaultExceptionMapper;
import com.sap.cldfnd.behsampleapp.service.BusinessEventService;
import com.sap.cldfnd.behsampleapp.service.SalesOrderService;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultBusinessEventQueueService;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultSalesOrderService;
import com.sap.cloud.sdk.testutil.MockUtil;

import io.restassured.RestAssured;

public class BehControllerIT {

	public static final String SERVICE_PATH = BehApplication.PATH + BehController.PATH;

	@ClassRule
	public static ArquillianTestClass arquillianTestClass = new ArquillianTestClass();

	@Rule
	public ArquillianTest arquillianTest = new ArquillianTest();
	
	@Rule
	public WireMockRule wireMockRule = mockUtil.mockErpServer();

	private static final MockUtil mockUtil = new MockUtil();

	@ArquillianResource
	private URL baseUrl;

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
			TopResults.class
		);
	}

	@BeforeClass
	public static void beforeClass() {
		mockUtil.mockDefaults();
		mockUtil.mockErpDestination();
	}

	@Before
	public void before() {
		RestAssured.baseURI = baseUrl.toExternalForm();
	}

	@Test
	public void testGetWhenS4HanaRespondsWithThreeEntities() throws IOException {
		final String expectedResponseJson = TestUtil.loadFileAsString("/expectedResponses/threeEntitiesResponse.json");
		
		given().
		when().
			get(SERVICE_PATH).
		then().
			statusCode(is(200)).
			body(sameJSONAs(expectedResponseJson).allowingExtraUnexpectedFields().allowingAnyArrayOrdering());
	}

}
