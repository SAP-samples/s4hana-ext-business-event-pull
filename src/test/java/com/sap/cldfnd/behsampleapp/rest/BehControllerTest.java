package com.sap.cldfnd.behsampleapp.rest;

import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

import static org.hamcrest.Matchers.*;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sap.cldfnd.behsampleapp.model.BusinessEvent;
import com.sap.cldfnd.behsampleapp.model.TopResults;
import com.sap.cldfnd.behsampleapp.rest.BehController;
import com.sap.cldfnd.behsampleapp.service.BusinessEventService;
import com.sap.cldfnd.behsampleapp.service.SalesOrderService;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesseventqueue.Behqueuedata;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.salesorder.SalesOrder;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class BehControllerTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Mock
	public BusinessEventService businessEventService;
	
	@Mock
	public SalesOrderService salesOrderService;
	
	@Captor
	public ArgumentCaptor<Collection<String>> keysCaptor;
	
	@InjectMocks
	BehController behController;
	
	@Test
	public void testGetUpdatedSalesOrdersCallsSalesOrderServiceGetByKeysWithCorrectKeysList() throws NotAuthorizedException, ForbiddenException, ODataException, JsonProcessingException {
		final List<String> businessObjectKeys = Arrays.asList(
				"1", 
				"02", "002", 
				"0003", "00003", "000003");
		
		when(businessEventService.getSalesOrderEvents()).thenReturn(randomSalesOrderEventsWithBusinessObjectKeys(businessObjectKeys));
		
		final RuntimeException stopper = new RuntimeException("It is thrown to stop the execution");
		when(salesOrderService.getByKeys(any())).thenThrow(stopper);

		try {
			behController.getUpdatedSalesOrders();
		} catch (RuntimeException e) {
			assertThat("Method should throw an expected exception", e, is(stopper));
		}
		
		verify(salesOrderService).getByKeys(keysCaptor.capture());
		final Collection<String> keysArgument = keysCaptor.getValue();
		assertThat("SalesOrderService::getByKeys() should have been called with the correct argument", 
				keysArgument, 
				containsInAnyOrder("1", "2", "3"));
	}
	
	@Test
	public void testGetUpdatedSalesOrdersDoesNotReturnSalesOrderAsNullWhenNotFound() throws NotAuthorizedException, ForbiddenException, ODataException, JsonProcessingException {
		final List<String> salesOrdersKeys = Arrays.asList("OrderThatDoesntExist");
		
		final List<Behqueuedata> salesOrderEvents = randomSalesOrderEventsWithBusinessObjectKeys(salesOrdersKeys);
		when(businessEventService.getSalesOrderEvents()).thenReturn(salesOrderEvents);
		when(salesOrderService.getByKeys(any())).thenReturn(Collections.emptyList());
		
		final TopResults<BusinessEvent<SalesOrder>> result = behController.getUpdatedSalesOrders();
		assertThat("Total number of results should not be limited", !result.isLimited());
		
		assertThat("Results", result.getResults(), is(empty()));
	}
	
	@Test
	public void testGetUpdatedSalesOrders() throws NotAuthorizedException, ForbiddenException, ODataException, JsonProcessingException {
		final List<String> businessObjectKeys = Arrays.asList(
				"1", 
				"02", "002", 
				"0003", "00003", "000003");
		
		final List<Behqueuedata> salesOrderEvents = randomSalesOrderEventsWithBusinessObjectKeys(businessObjectKeys);
		when(businessEventService.getSalesOrderEvents()).thenReturn(salesOrderEvents);
		
		final List<String> salesOrderKeys = Arrays.asList("1", "2", "3");
		final List<SalesOrder> salesOrders = randomSalesOrdersWithKeys(salesOrderKeys);
		when(salesOrderService.getByKeys(any())).thenReturn(salesOrders);
		
		final TopResults<BusinessEvent<SalesOrder>> result = behController.getUpdatedSalesOrders();
		assertThat("Total number of results should not be limited", !result.isLimited());
		
		final List<BusinessEvent<SalesOrder>> results = behController.getUpdatedSalesOrders().getResults();
		assertThat("Results", results, is(not(empty())));

		final List<Behqueuedata> businessEventsFromResult = results.stream()
				.map(event -> event.getBusinessEvent())
				.collect(toList());
		assertThat("Business events from result", businessEventsFromResult, containsInAnyOrder(salesOrderEvents.toArray()));
		
		final List<SalesOrder> distinctSalesOrdersFromResult = results.stream()
				.map(event -> event.getBusinessObject())
				.distinct()
				.collect(toList());
		assertThat("Distinct sales orders from result", distinctSalesOrdersFromResult, containsInAnyOrder(salesOrders.toArray()));
		
		results.forEach(event -> assertThat("Key of each sales order should be the same as in the event",
				event.getBusinessObject().getSalesOrder(), 
				is(equalTo(StringUtils.stripStart(event.getBusinessEvent().getSAPBusinessObjectKey1(), "0")))));
	}
	
	@Test
	public void testGetUpdatedSalesOrdersLimitsTheResult() throws NotAuthorizedException, ForbiddenException, ODataException, JsonProcessingException {
		final TopResults<BusinessEvent<SalesOrder>> result = testWithNEvents(BehController.RESULT_SIZE_LIMIT + 1);
		assertThat("Total number of results should be limited", result.isLimited());
	}
	
	@Test
	public void testGetUpdatedSalesOrdersDoesNotLimitTheResultWhenEventsCountIsEqualToResultSizeLimit() throws NotAuthorizedException, ForbiddenException, ODataException, JsonProcessingException {
		final TopResults<BusinessEvent<SalesOrder>> result = testWithNEvents(BehController.RESULT_SIZE_LIMIT);
		assertThat("Total number of results should NOT be limited", !result.isLimited());
	}

	public TopResults<BusinessEvent<SalesOrder>> testWithNEvents(final int expectedTotalCount) throws ODataException {
		final List<String> allBusinessObjectKeys = IntStream.iterate(1, key -> ++key)
				.limit(expectedTotalCount)
				.mapToObj(String::valueOf)
				.collect(toList());
		
		final List<Behqueuedata> salesOrderEvents = randomSalesOrderEventsWithBusinessObjectKeys(allBusinessObjectKeys);		
		when(businessEventService.getSalesOrderEvents()).thenReturn(salesOrderEvents);
		
		final List<String> topBusinessObjectKeys = allBusinessObjectKeys.stream()
				.limit(BehController.RESULT_SIZE_LIMIT)
				.collect(toList());
		final List<SalesOrder> salesOrders = randomSalesOrdersWithKeys(topBusinessObjectKeys);
		when(salesOrderService.getByKeys(any())).thenReturn(salesOrders);		
		
		final TopResults<BusinessEvent<SalesOrder>> result = behController.getUpdatedSalesOrders();
		
		verify(salesOrderService).getByKeys(keysCaptor.capture());
		final Collection<String> keysArgument = keysCaptor.getValue();
		assertThat("Only top " + BehController.RESULT_SIZE_LIMIT + " sales order should have been requested from S/4HANA", 
				keysArgument, 
				containsInAnyOrder(topBusinessObjectKeys.toArray()));
		
		final List<BusinessEvent<SalesOrder>> salesOrdersFromResult = result.getResults();
		assertThat(salesOrdersFromResult, is(not(empty())));

		final List<String> distinctSalesOrdersFromResult = salesOrdersFromResult.stream()
				.map(event -> event.getBusinessObject().getSalesOrder())
				.distinct()
				.collect(toList());
		assertThat("Keys of returned sales orders", 
				distinctSalesOrdersFromResult, 
				containsInAnyOrder(topBusinessObjectKeys.toArray()));
		
		return result;
	}
	
	@Test
	public void testGetUpdatedSalesOrdersWhenSalesOrderNotFoundThenItIsNotIncludedInTheResult() throws NotAuthorizedException, ForbiddenException, ODataException, JsonProcessingException {
		final List<String> businessObjectKeys = Arrays.asList(
				"1", 
				"02", "002", 
				"0003", "00003", "000003");
		final List<Behqueuedata> salesOrderEvents = randomSalesOrderEventsWithBusinessObjectKeys(businessObjectKeys);
		when(businessEventService.getSalesOrderEvents()).thenReturn(salesOrderEvents);
		
		final List<String> salesOrderKeys = Arrays.asList("1", "3"); // order 2 is missing
		final List<SalesOrder> salesOrders = randomSalesOrdersWithKeys(salesOrderKeys);
		when(salesOrderService.getByKeys(any())).thenReturn(salesOrders);
		
		final TopResults<BusinessEvent<SalesOrder>> result = behController.getUpdatedSalesOrders();
		assertThat("Total number of results should not be limited", !result.isLimited());
		
		final List<BusinessEvent<SalesOrder>> results = result.getResults();
		assertThat("Results", results, is(not(empty())));

		final List<Behqueuedata> businessEventsFromResult = results.stream()
				.map(event -> event.getBusinessEvent())
				.collect(toList());
		
		final List<Behqueuedata> salesOrderEventsWithout2 = salesOrderEvents.stream() // order 2 is missing
				.filter(event -> !event.getSAPBusinessObjectKey1().contains("2"))
				.collect(toList());
		
		assertThat("Keys of returned sales orders",
				businessEventsFromResult, 
				containsInAnyOrder(salesOrderEventsWithout2.toArray()));
		
		final List<SalesOrder> distinctSalesOrdersFromResult = results.stream()
				.map(event -> event.getBusinessObject())
				.distinct()
				.collect(toList());
		assertThat(distinctSalesOrdersFromResult, containsInAnyOrder(salesOrders.toArray()));

		results.forEach(event -> assertThat("Key of each sales order should be the same as in the event",
				event.getBusinessObject().getSalesOrder(), 
				is(equalTo(StringUtils.stripStart(event.getBusinessEvent().getSAPBusinessObjectKey1(), "0")))));
	}

	private List<Behqueuedata> randomSalesOrderEventsWithBusinessObjectKeys(List<String> keys) {
		return keys.stream()
				.map(key -> Behqueuedata.builder()
								.businessEvent(UUID.randomUUID())
								.sAPBusinessObjectKey1(key)
								.build())
				.collect(toList());
	}
	
	private List<SalesOrder> randomSalesOrdersWithKeys(List<String> keys) {
		return keys.stream()
				.map(key -> SalesOrder.builder()
								.salesOrder(key)
								.totalNetAmount(BigDecimal.valueOf(100.2))
								.build())
				.collect(toList());
	}

}
