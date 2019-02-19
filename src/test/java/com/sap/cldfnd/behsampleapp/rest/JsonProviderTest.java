package com.sap.cldfnd.behsampleapp.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.salesorder.SalesOrder;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.salesorder.SalesOrderItem;

public class JsonProviderTest {
	
	JsonProvider jsonProvider;
	
	@Before
	public void setUp() {
		jsonProvider = new JsonProvider();
	}
	
	@Test
	public void testSerializedEntityContainsAnnotatedNotNull() throws JsonProcessingException {

		// given a test entity 
		class Testee {
			@JsonProperty("NonNullProperty")
			private final String nonNullProperty = "Not null";
			
			@JsonProperty("NullProperty")
			private final String nULLProperty = null;
			
			@JsonProperty("JAVABeanNaming")
			private final String jAVABeanNaming = "Java Bean naming";
			
			@JsonProperty("BigDecimalProperty")
			private final BigDecimal bigDecimalProperty = BigDecimal.valueOf(-1.5);
			
			@SuppressWarnings("unused")
			public String getJAVABeanNaming() {
				return jAVABeanNaming;
			}
		}
		
		// when it is serialized
		final String json = serialize(new Testee());

		// then only non-null properties are included
		assertThat(json, allOf(containsString("\"NonNullProperty\""), not(containsString("\"NullProperty\""))));
		
		// and it is pretty-printed
		assertThat(json, containsString("\n"));
		
		// and there is no conflict between Java Bean and Jackson naming policies
		assertThat(json, allOf(containsString("JAVABeanNaming"), not(containsString("javabeanNaming"))));
		
		// and the BigDecimal property is included as well
		assertThat(json, containsString("-1.5"));
	}
	
	@Test
	public void testSalesOrderExpandedToItemContainsToItem() throws JsonProcessingException {
		final String json = serialize(salesOrderWithItem());
		
		assertThat(json, containsString("to_Item"));
	}
	
	@Test
	public void testSalesOrderContainsBigDecimalProperty() throws JsonProcessingException {
		final BigDecimal bigDecimalValue = BigDecimal.valueOf(100.20);
		final String json = serialize(SalesOrder.builder().totalNetAmount(bigDecimalValue).build());
		
		assertThat(json, containsString(bigDecimalValue.toString()));
	}
	
	public SalesOrder salesOrderWithItem() {
		final SalesOrder salesOrder = SalesOrder.builder()
			.salesOrder("SalesOrder")
			.item(SalesOrderItem.builder()
						.salesOrder("SalesOrderItem")
						.build())
			.build();
		return salesOrder;
	}

	public <T> String serialize(final T entity) throws JsonProcessingException {
		final ObjectMapper om = jsonProvider.locateMapper(entity.getClass(), MediaType.APPLICATION_JSON_TYPE);
		return om.writeValueAsString(entity);
	}

}
