package in.cypal.studio.gwt.core.common;

import in.cypal.studio.gwt.core.Activator;

import org.eclipse.core.runtime.QualifiedName;

public class Constants {
	
	public static final String PLUGIN_ID = Activator.PLUGIN_ID;
	
	// GWT related stuff
	public static final String GWT_SHELL_CLASS = "com.google.gwt.dev.GWTShell"; //$NON-NLS-1$
	public static final String GWT_COMPILER_CLASS = "com.google.gwt.dev.GWTCompiler"; //$NON-NLS-1$
	public static final Object REMOTE_SERVICE_CLASS = "RemoteService";
	public static final String GWT_XML_EXT = "gwt.xml";
	public static final String CLIENT_PACKAGE = "client";
	public static final String SERVER_PACKAGE = "server";
	public static final String PUBLIC_FOLDER = "public";

	//Folders
	public static final String OUTPUT_FOLDER= "build/gwtOutput"; //$NON-NLS-1$
	
	//Preference keys
	public static final String GWT_HOME_PREFERENCE = "GWT_HOME_PREFERENCE";
	public static final String UPDATE_ASYNC_PREFERENCE = "UPDATE_ASYNC_PREFERENCE";
	public static final String COMPILE_AT_FULLBUILD_PREFERENCE = "COMPILE_AT_FULLBUILD_PREFERENCE";
	public static final String COMPILE_AT_PUBLISH_PREFERENCE = "COMPILE_AT_PUBLISH_PREFERENCE";	
	
	// Plugin.xml defined
	private static final String QUALIFIER = PLUGIN_ID+".";
	public static final Object FACET_ID = QUALIFIER+"gwtFacet";
	public static final String NATURE_ID = QUALIFIER+"gwtNature";
	public static final String BUILDER_ID = QUALIFIER+"gwtBuilder";
	public static final String MODULE_XML_CONTENT_ID=QUALIFIER+"moduleXml";

	
	public static final String GWT_HOME_CPE = "GWT_HOME";
	public static final QualifiedName IS_HOSTED_DEPLOY_MODE = null;
	public static final String GWT_HOME_PATH = "GWT_HOME";



}
