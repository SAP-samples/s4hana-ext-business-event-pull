package com.sap.cldfnd.behsampleapp.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.odatav2.connectivity.ODataQueryBuilder;
import com.sap.cloud.sdk.s4hana.datamodel.odata.helper.ExpressionFluentHelper;
import com.sap.cloud.sdk.s4hana.datamodel.odata.helper.FluentHelperRead;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.salesorder.SalesOrder;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.salesorder.SalesOrderLink;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.salesorder.SalesOrderSelectable;

public class SalesOrderService {
	
	/**
	 * This class allows to specify OData $expand parameter explicitly
	 *
	 * @see FluentHelperRead#select(Object...)
	 */
	protected static class CustomSalesOrderReader extends FluentHelperRead<CustomSalesOrderReader, SalesOrder, SalesOrderSelectable> {
		
		private SalesOrderLink<?>[] expands = new SalesOrderLink<?>[] {};

		public CustomSalesOrderReader expand(SalesOrderLink<?>... navigationalProperties) {
			expands = navigationalProperties;
			return this;
		}
		
		@Override
		protected ODataQueryBuilder getQueryBuilder() {
			final ODataQueryBuilder queryBuilder = super.getQueryBuilder();
			
			final String[] expandStrings = Arrays.stream(expands)
					.filter(Objects::nonNull)
					.distinct()
					.map(SalesOrderLink::getFieldName)
					.collect(Collectors.toList())
					.toArray(new String[] {});
			
			if (expandStrings.length == 0) {
				return queryBuilder;
			}
			
			return queryBuilder.expand(expandStrings);
		}
		
		@Override
		protected Class<? extends SalesOrder> getEntityClass() {
			return SalesOrder.class;
		}

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
	 * @see ProcessSalesOrderService#getAllSalesOrder()
	 * @see <a href=
	 *	  "https://api.sap.com/shell/discover/contentpackage/SAPS4HANACloud/api/API_SALES_ORDER_SRV?resource=A_SalesOrder&operation=get_A_SalesOrder">SAP
	 *	  API Business Hub</a> for details of
	 *	  {@code GET API_SALES_ORDER_SRV/A_SalesOrder} endpoint
	 */
	public List<SalesOrder> getByKeys(Collection<String> keys) throws IllegalArgumentException, ODataException {
		if (keys == null) {
			throw new IllegalArgumentException("keys is null");
		}
		
		if (keys.size() == 0) {
			return Collections.emptyList();
		}
		
		final ExpressionFluentHelper<SalesOrder> filter = keys.stream()
				.map(id -> SalesOrder.SALES_ORDER.eq(id))
				.reduce(ExpressionFluentHelper::or)
				.get();
		
		return new CustomSalesOrderReader()
				.filter(filter)
				.expand(SalesOrder.TO_ITEM)
				.execute();
	}

}
