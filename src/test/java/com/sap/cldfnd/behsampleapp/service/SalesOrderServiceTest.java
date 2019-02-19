package com.sap.cldfnd.behsampleapp.service;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.salesorder.SalesOrder;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.salesorder.SalesOrderItem;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.salesorder.SalesOrderLink;

public class SalesOrderServiceTest extends GenericServiceTest {
	
	protected SalesOrderService getSalesOrderService() {
		return new SalesOrderService();
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
		thrown.expectMessage("keys is null");
		
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
		
		assertThat("SalesOrders keys", keys, containsInAnyOrder("1", "2", "3"));
		
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
						"Handelsware 20, Bestellpunkt, Serialnr.", 
						"Trad.Good 11,PD,Reg.Trading"));
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
	
	/* tests for SalesOrderService.CustomSalesOrderReader */
	
	@Test
	public void testExpandWithNoParameter() {
		// Given
		final SalesOrderService.CustomSalesOrderReader customSalesOrderReader = new SalesOrderService.CustomSalesOrderReader();
		
		// When expand() is called with no params
		customSalesOrderReader.expand();
		
		// Then $expand parameter is not set to the OData query
		final String oDataQuery = customSalesOrderReader.toQuery().toString();
		assertThat("oDataQuery", oDataQuery, not(containsString("$expand")));
	}
	
	@Test
	public void testExpandWithNull() {
		// Given
		final SalesOrderService.CustomSalesOrderReader customSalesOrderReader = new SalesOrderService.CustomSalesOrderReader();
		
		// When expand() is called with a single null parameter
		customSalesOrderReader.expand((SalesOrderLink<?>) null);
		
		// Then $expand parameter is not set to the OData query
		final String oDataQuery = customSalesOrderReader.toQuery().toString();
		assertThat("oDataQuery", oDataQuery, not(containsString("$expand")));
	}
	
	@Test
	public void testExpandWithNullAsWell() {
		// Given
		final SalesOrderService.CustomSalesOrderReader customSalesOrderReader = new SalesOrderService.CustomSalesOrderReader();
		
		// When expand() is called with two null parameters, one non-null parameter and two duplicated parameters 
		customSalesOrderReader.expand(SalesOrder.TO_ITEM, null, SalesOrder.TO_ITEM, null, SalesOrder.TO_PARTNER);
		
		// Then $expand parameter contains both non-null parameters
		final String oDataQuery = customSalesOrderReader.toQuery().toString();
		final String expectedExpand = String.format("$expand=%s,%s",
				SalesOrder.TO_ITEM.getFieldName(), 
				SalesOrder.TO_PARTNER.getFieldName());
		
		assertThat("oDataQuery", oDataQuery, containsString(expectedExpand));
	}
	
}
