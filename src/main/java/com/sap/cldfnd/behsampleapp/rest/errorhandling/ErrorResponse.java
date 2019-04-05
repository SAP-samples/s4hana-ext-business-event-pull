package com.sap.cldfnd.behsampleapp.rest.errorhandling;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.Getter;
import lombok.Setter;

/**
 * A DTO to represent the error response body in JSON
 * <p>
 * {@link JsonTypeName} and {@link JsonTypeInfo} are used as an alternative to
 * {@link JsonRootName} because the latter requires enabling
 * {@link SerializationFeature#WRAP_ROOT_VALUE}
 * 
 * @see <a href=
 *      "https://api.sap.com/shell/discover/contentpackage/SAPS4HANACloud/api/API_SALES_ORDER_SRV">Response
 *      model for an rrror for Process Sales Order API</a>
 *
 */
@JsonTypeName(value = "error")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class ErrorResponse {
	
	public static class ErrorMessage {
		
		@JsonProperty("lang")
		private final String lang = "en";

		@Getter
		@JsonProperty("value")
		private String value;
	}

	@Getter
	@JsonProperty("code")
	private String code;
	
	@Getter
	@JsonProperty("message")
	private ErrorResponse.ErrorMessage message = new ErrorMessage();

	@Getter
	@Setter
	@JsonProperty("innerError")
	private  ErrorResponse innerError;

	private ErrorResponse(String code, String message,  ErrorResponse innerError) {
		this.message.value = message;
		this.code = code;
		this.innerError = innerError;
	}
	
	public static ErrorResponse of(String code, String message) {
		return of(code, message, null);
	}

	public static ErrorResponse of(String code , String message, ErrorResponse innerError) {
		return new ErrorResponse(code, message, innerError);
	}

}