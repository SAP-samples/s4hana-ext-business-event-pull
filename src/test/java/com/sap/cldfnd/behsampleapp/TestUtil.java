package com.sap.cldfnd.behsampleapp;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationsRequestContextListener;
import com.sap.cloud.sdk.cloudplatform.security.user.UserRequestContextListener;
import com.sap.cloud.sdk.cloudplatform.servlet.RequestContextCallable;
import com.sap.cloud.sdk.cloudplatform.servlet.RequestContextServletFilter;
import com.sap.cloud.sdk.cloudplatform.tenant.TenantRequestContextListener;

public class TestUtil {
	
    public static WebArchive createDeployment(final Class<?>... classesUnderTest) {
        return ShrinkWrap
            .create(WebArchive.class)
            .addClasses(classesUnderTest)
            .addClasses(RequestContextServletFilter.class, RequestContextCallable.class)
            .addClass(TenantRequestContextListener.class)
            .addClass(UserRequestContextListener.class)
            .addClass(DestinationsRequestContextListener.class)
            .addAsManifestResource("arquillian.xml")
            .addAsWebInfResource(new ByteArrayAsset("<beans/>".getBytes()), ArchivePaths.create("beans.xml"))
			.addAsWebInfResource("META-INF/openejb-jar.xml");
    }
    
	/**
	 * Loads a file from classpath
	 * 
	 * @return file contents
	 */
	public static String loadFileAsString(final String filename) throws IOException {
		return IOUtils.toString(TestUtil.class.getResourceAsStream(filename), Charset.defaultCharset());
	}
}
