package com.sap.cldfnd.behsampleapp.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;

import com.github.tomakehurst.wiremock.http.Fault;

public class WireMockUtil {
	
	public static void mockTimeout() {
		givenThat(get(anyUrl()).
				willReturn(aResponse().withFixedDelay(3*60*1000)));	
	}
	
	public static void mockMalformedResponseChunk() {
		givenThat(get(anyUrl()).
				willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));		
	}
	
	public static void mockFaultEmptyResponse() {
		givenThat(get(anyUrl()).
				willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));		
	}
	
	// Connection timeout
	public static void mockConnectionRestByPeer() {
		givenThat(get(anyUrl()).
				willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));		
	}
	
	public static void mockRandomDataThenClose() {
		givenThat(get(anyUrl()).
				willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));		
	}
	
}
