package com.sap.cldfnd.behsampleapp.service;

import java.util.List;

import javax.inject.Inject;

import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesseventqueue.Behqueuedata;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.BusinessEventQueueService;

public class BusinessEventService {	
	
	public static final String BUS_EVENT_SUBSCRIBER_CODE_VALUE = "SCP1";
	public static final String SALES_ORDER = "SalesOrder";
	
	@Inject
	BusinessEventQueueService businessEventQueueService;
	
	/**
	 * Calls {@code GET C_BEHQUEUEDATA_CDS/Behqueuedata} through
	 * {@link BusinessEventService} of SAP S/4HANA SDK's Virtual Data Model to
	 * get all SalesOrder events
	 * 
	 * @return the list of SalesOrder events in the events queue
	 * 
	 * @throws ODataException
	 *             in case the request was not successful
	 * 
	 * @see BusinessEventQueueService#getAllBehqueuedata
	 * @see <a href=
	 *      "https://api.sap.com/shell/discover/contentpackage/SAPS4HANACloud/api/C_BEHQUEUEDATA_CDS?resource=Behqueuedata&operation=get_Behqueuedata">
	 *      SAP API Business Hub</a> for details of
	 *      {@code GET C_BEHQUEUEDATA_CDS/Behqueuedata} endpoint
	 */
	public List<Behqueuedata> getSalesOrderEvents() throws ODataException {
		return businessEventQueueService
					.getAllBehqueuedata()
					.filter(Behqueuedata.BUS_EVENT_SUBSCRIBER_CODE.eq(BUS_EVENT_SUBSCRIBER_CODE_VALUE)
								.and(Behqueuedata.SAP_OBJECT_TYPE.eq(SALES_ORDER)))
					.execute();
	}
	
}
