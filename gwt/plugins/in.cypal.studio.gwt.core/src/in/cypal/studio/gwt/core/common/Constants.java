package in.cypal.studio.gwt.core.common;

import in.cypal.studio.gwt.core.Activator;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

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

	// Attributes for Lauch configuration
	
	public static final String LAUNCH_CONFIG_TYPE = "in.cypal.studio.gwt.core.launchConfigurationType"; //$NON-NLS-1$
	public static final String LAUNCH_ATTR_USE_DEFAULT_URL = PLUGIN_ID+".launchAttrUseDefaultUrl"; //$NON-NLS-1$
	public static final String LAUNCH_ATTR_URL = PLUGIN_ID+".launchAttrUrl"; //$NON-NLS-1$
	public static final String LAUNCH_ATTR_USE_EMBEDDED_SERVER = PLUGIN_ID+".launchAttrUseEmbeddedServer"; //$NON-NLS-1$
	public static final String LAUNCH_ATTR_HEADLESS = PLUGIN_ID+".launchAttrHeadless"; //$NON-NLS-1$
	public static final String LAUNCH_ATTR_PORT = PLUGIN_ID+".launchAttrPort";//$NON-NLS-1$
	public static final String LAUNCH_ATTR_LOGLEVEL = PLUGIN_ID+".launchAttrLogLevel";//$NON-NLS-1$
	public static final String LAUNCH_ATTR_OUTDIR = PLUGIN_ID+".launchAttrOutDir";//$NON-NLS-1$
	public static final String LAUNCH_ATTR_STYLE = PLUGIN_ID+".launchAttrStyle";//$NON-NLS-1$
	public static final String LAUNCH_ATTR_MAIN_TYPE = IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME;
	public static final String LAUNCH_ATTR_PROJECT_NAME = IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME;

	
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
