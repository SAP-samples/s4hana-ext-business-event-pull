package com.sap.cldfnd.behsampleapp.rest;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * Custom JSON serializer for CXF
 * <p>
 * {@link Produces} annotation is needed to override the default JSON provider.
 * Please check Apache CXF <a href=
 * "http://cxf.apache.org/docs/jax-rs.html#JAX-RS-CXF3.1.2ProviderSortingChanges">documentation</a>
 * for further details on how providers are sorted.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JsonProvider extends JacksonJsonProvider {

	@Override
	public ObjectMapper locateMapper(Class<?> type, MediaType mediaType) {
		final ObjectMapper om = super.locateMapper(type, mediaType);

		// recommended: resolve conflict between Jackson naming and Java Bean
		// naming for properties which names start with two or more upper-case
		// letters
		// see https://github.com/FasterXML/jackson-databind/issues/1824
		om.enable(MapperFeature.USE_STD_BEAN_NAMING);

		// optional: include only properties with non-null values
		om.setSerializationInclusion(Include.NON_NULL);

		// optional: pretty print
		om.enable(SerializationFeature.INDENT_OUTPUT);
		
		// do not use getters when serializing entities: 
		// this may lead to unnecessary and even dead-lock calls 
		// to SAP S/4HANA Cloud system when VDM entities are serialized
		// because they contain get...OrFetch methods that fetch values 
		// of OData navigational properties
		om.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		om.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		
		// needed for testing to unwrap ErrorResponse
		om.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);

		return om;
	}

}
