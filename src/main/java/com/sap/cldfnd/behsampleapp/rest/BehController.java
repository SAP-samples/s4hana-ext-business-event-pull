package com.sap.cldfnd.behsampleapp.rest;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;

import com.sap.cldfnd.behsampleapp.model.BusinessEvent;
import com.sap.cldfnd.behsampleapp.model.TopResults;
import com.sap.cldfnd.behsampleapp.service.BusinessEventService;
import com.sap.cldfnd.behsampleapp.service.SalesOrderService;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.s4hana.datamodel.odata.helper.FluentHelperRead;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesseventqueue.Behqueuedata;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.salesorder.SalesOrder;

@Path(BehController.PATH)
public class BehController {
	
	public static final String PATH = "/beh/updatedSalesOrders";

	/**
	 * Due to the limitation on the length of a query to S/4HANA OData service,
	 * we recommend to always limit the number of requested entities.
	 * <p>
	 * If you need to show more items, please consider implementing your own
	 * pagination logic using {@link FluentHelperRead#top(Number)} and
	 * {@link FluentHelperRead#skip(Number)} methods.
	 * 
	 */
	public static final int RESULT_SIZE_LIMIT = 100;
	
	@Inject
	protected BusinessEventService businessEventService;
	
	@Inject
	protected SalesOrderService salesOrderService;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TopResults<BusinessEvent<SalesOrder>> getUpdatedSalesOrders() throws ODataException {
		// Get business events 
		final List<Behqueuedata> allBusinessEvents = businessEventService.getSalesOrderEvents();
		
		// Get keys of sales orders from business events (without prefixes)
		final Set<String> salesOrdersKeys = allBusinessEvents.stream()
				.limit(RESULT_SIZE_LIMIT)
				.map(Behqueuedata::getSAPBusinessObjectKey1)
				.map(BehController::trimLeadingZeroes)
				.collect(toSet());
		
		// Get sales orders from business events grouping by key
		final Map<String, SalesOrder> salesOrders = salesOrderService
				.getByKeys(salesOrdersKeys)
				.stream()
				.collect(toMap(SalesOrder::getSalesOrder, salesOrder -> salesOrder));
		
		// Construct response
		final List<BusinessEvent<SalesOrder>> businessEventsSalesOrders = allBusinessEvents.stream()
				.limit(RESULT_SIZE_LIMIT)
				.map(businessEvent -> BusinessEvent.of(
						businessEvent, 
						salesOrders.get(trimLeadingZeroes(businessEvent.getSAPBusinessObjectKey1()))))
				.filter(businessEvent -> businessEvent.getBusinessObject() != null)
				.collect(Collectors.toList());
		
		return TopResults.of(businessEventsSalesOrders, allBusinessEvents.size() > RESULT_SIZE_LIMIT);
	}

	public static String trimLeadingZeroes(String zerosPrefixedKey) {
		return StringUtils.stripStart(zerosPrefixedKey, "0");
	}
	
}
