package com.sap.cldfnd.behsampleapp.service;

import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.salesorder.SalesOrder;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.salesorder.SalesOrderItem;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultSalesOrderService;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SalesOrderServiceTest extends GenericServiceTest {
	
	protected SalesOrderService getSalesOrderService() {
		return new SalesOrderService(new DefaultSalesOrderService());
	}

	@Override
	protected void callService() throws ODataException {
		getSalesOrderService().getByKeys(Collections.singletonList("Programmatically simulated error responce (using WireMock)"));
	}

	@Test
	public void testGetUpdatesSalesOrdersReturnsEmptyListWhenPassesIdsListIsEmpty() throws ODataException {
		final List<SalesOrder> salesOrders = getSalesOrderService().getByKeys(Collections.emptyList());
		assertThat("Sales Orders", salesOrders.isEmpty());
	}
	
	@Test
	public void testGetUpdatesSalesOrdersThrowsIllegalArgumentExceptionWhenPassesIdsListIsNull() throws ODataException {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("keys is marked @NonNull but is null");
		
		getSalesOrderService().getByKeys(null);
	}

	/**
	 * @see /behsampleapp/src/test/resources/mappings/API_SALES_ORDER_SRV/A_SalesOrders_1_2_3.json
	 *      for the mocked response
	 */
	@Test
	public void testGetUpdatedSalesOrders() throws ODataException {
		final List<SalesOrder> salesOrders = getSalesOrderService().getByKeys(Collections.singletonList("ThreeElements123"));
		assertThat(salesOrders, hasSize(3));
		
		final List<String> keys = salesOrders.stream()
				.map(SalesOrder::getSalesOrder)
				.collect(toList());
		
		assertThat("SalesOrders keys", keys, containsInAnyOrder("164", "165", "166"));
		
		final List<String> itemTexts = salesOrders.stream()
				.flatMap(order -> order
						.getItemIfPresent()
						.orElse(Collections.emptyList())
						.stream())
				.map(SalesOrderItem::getSalesOrderItemText)
				.collect(toList());
		
		assertThat("Sales Order Items texts", 
				itemTexts, 
				containsInAnyOrder(
					"RAW124, VB, Verbrauch, Fixlagerplatz",
					"QM - Regular",
					"FIN01, LF, ME-Integration, A"
				));
	}
	
	/**
	 * @see /src/test/resources/mappings/API_SALES_ORDER_SRV/A_SalesOrders_empty_result.json
	 *      for the mocked response
	 */
	@Test
	public void testGetUpdatedSalesOrdersWhenFilterReturnsNoResults() throws ODataException {
		final List<SalesOrder> salesOrders = getSalesOrderService().getByKeys(Collections.singletonList("FilterNotFound"));
		assertThat("Sales Orders", salesOrders, is(empty())); 
	}
	
}
