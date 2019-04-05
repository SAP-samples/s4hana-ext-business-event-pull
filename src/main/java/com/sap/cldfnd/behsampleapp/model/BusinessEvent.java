package com.sap.cldfnd.behsampleapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.cloud.sdk.s4hana.datamodel.odata.helper.VdmEntity;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesseventqueue.Behqueuedata;

import lombok.Getter;

/**
 * A model to store both a business event and the associated business object
 *
 * @param <T>
 *            type of business object associated with the business event
 */
@Getter
public class BusinessEvent<T extends VdmEntity<T>> {
	
	@JsonProperty("BusinessEvent")
	private Behqueuedata businessEvent;
	
	@JsonProperty("BusinessObject")
	private T businessObject;
	
	public static <T extends VdmEntity<T>> BusinessEvent<T> of(Behqueuedata businessEvent, T businessObject) {
		return new BusinessEvent<T>(businessEvent, businessObject);
	}
	
	protected BusinessEvent(Behqueuedata businessEvent, T businessObject) {
		super();
		this.businessEvent = businessEvent;
		this.businessObject = businessObject;
	}
}
