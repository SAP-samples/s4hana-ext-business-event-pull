package com.sap.cldfnd.behsampleapp.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath(BehApplication.PATH)
public class BehApplication extends Application {

	public static final String PATH = "/api";
	
}
