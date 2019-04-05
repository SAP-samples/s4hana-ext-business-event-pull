package com.sap.cldfnd.behsampleapp.service;

import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.s4hana.datamodel.odata.helper.ExpressionFluentHelper;
import com.sap.cloud.sdk.s4hana.datamodel.odata.helper.FluentHelperRead;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.salesorder.SalesOrder;

import lombok.NonNull;

import org.slf4j.Logger;
import javax.inject.Inject;

import java.util.*;

public class SalesOrderService {

	private static final Logger log = CloudLoggerFactory.getLogger(SalesOrderService.class);
	
	final com.sap.cloud.sdk.s4hana.datamodel.odata.services.SalesOrderService salesOrderService;

	@Inject
	public SalesOrderService(com.sap.cloud.sdk.s4hana.datamodel.odata.services.SalesOrderService salesOrderService) {
		this.salesOrderService = salesOrderService;
	}

	/**
	 * Calls {@code GET API_SALES_ORDER_SRV/A_SalesOrder} through
	 * {@link FluentHelperRead} to get the SalesOrder events expanded to sales
	 * order items and filtered by {@code keys} list
	 *
	 * @param keys
	 *			the list of sales orders IDs to be fetched
	 * @return the list of sales orders or an empty list if {@code keys} is empty
	 *
	 * @throws ODataException
	 *			 in case the request was not successful
	 * @throws IllegalArgumentException
	 *			 in case {@code keys} is null
	 *
	 * @see //ProcessSalesOrderService#getAllSalesOrder()
	 * @see <a href=
	 *	  "https://api.sap.com/shell/discover/contentpackage/SAPS4HANACloud/api/API_SALES_ORDER_SRV?resource=A_SalesOrder&operation=get_A_SalesOrder">SAP
	 *	  API Business Hub</a> for details of
	 *	  {@code GET API_SALES_ORDER_SRV/A_SalesOrder} endpoint
	 */
	public List<SalesOrder> getByKeys(@NonNull Collection<String> keys) throws IllegalArgumentException, ODataException {
		if (keys.size() == 0) {
			log.debug("Keys list is empty, returning empty response");
			return Collections.emptyList();
		}
		
		log.debug("Request sales orders with keys {}", keys);

		// create OData $filter with all keys 
		final ExpressionFluentHelper<SalesOrder> filter = keys.stream()
				.map(key -> SalesOrder.SALES_ORDER.eq(key))
				.reduce(ExpressionFluentHelper::or)
				.get();

		return salesOrderService.getAllSalesOrder()
			.select(SalesOrder.ALL_FIELDS, SalesOrder.TO_ITEM)
			.filter(filter)
			.execute();
	}

}
