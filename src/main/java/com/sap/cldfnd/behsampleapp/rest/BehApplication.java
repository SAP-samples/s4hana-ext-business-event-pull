package com.sap.cldfnd.behsampleapp.rest;

import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ExceptionMapper;

import com.google.common.collect.Sets;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;

@ApplicationPath("/api")
public class BehApplication extends Application {
	
	@Inject
	BehController behController;

	@Inject
	JsonProvider jsonProvider;
	
	@Inject
	ExceptionMapper<ODataException> oDataExceptionMapper;
	
	@Inject
	ExceptionMapper<Throwable> defaultExceptionMapper;
	
	@Override
	public Set<Object> getSingletons() {
		return Sets.newHashSet(
				behController,
				jsonProvider,
				oDataExceptionMapper,
				defaultExceptionMapper);
	}

}
