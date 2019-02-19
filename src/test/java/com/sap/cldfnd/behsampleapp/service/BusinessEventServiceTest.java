package com.sap.cldfnd.behsampleapp.service;

import org.junit.Before;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import com.sap.cldfnd.behsampleapp.service.BusinessEventService;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesseventqueue.Behqueuedata;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultBusinessEventQueueService;

public class BusinessEventServiceTest extends GenericServiceTest {

	private BusinessEventService businessEventService;

	protected BusinessEventService getBusinessEventService() {
		return businessEventService;
	}

	/**
	 * This method replaces the CDI injection<br>
	 * 
	 * BusinessEventQueueService is injected in BusinessEventService using CDI,
	 * thus BusinessEventService should also be injected in tests. The problem
	 * is that then tests should run in a container environment (e.g. an OpenEJB
	 * container) and they run way too long there.<br>
	 * 
	 */
	@Before
	public void setUp() {
		businessEventService = new BusinessEventService();
		businessEventService.businessEventQueueService = new DefaultBusinessEventQueueService();
	}

	@Override
	protected void callService() throws ODataException {
		getBusinessEventService().getSalesOrderEvents();
	}

	/**
	 * @see /behsampleapp/src/test/resources/mappings/C_BEHQUEUEDATA_CDS/Behqueuedata_two_results.json
	 *      for the mocked response
	 */
	@Test
	public void testGetUpdatedSalesOrders() throws ODataException {
		final List<Behqueuedata> salesOrderEvents = getBusinessEventService().getSalesOrderEvents();

		assertThat(salesOrderEvents, hasSize(2));
	}

	@Test
	public void testGetSalesOrderEventsWhenFilterReturnsNoResults() throws ODataException {
		final String emptyResultJson = "{\"d\": { \"results\": [] } }";
		givenThat(get(urlMatching("(.*)Behqueuedata(.*)")).willReturn(status(200).withBody(emptyResultJson)));

		final List<Behqueuedata> salesOrderEvents = getBusinessEventService().getSalesOrderEvents();

		assertThat(salesOrderEvents, is(empty()));
	}

}
