/*
 * Copyright 2006 Cypal Solutions (tools@cypal.in)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package in.cypal.studio.gwt.core.launch;

import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.core.common.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * @author Prakash G.R.
 *
 */
public class Helper {

	public static final String []logLevels= {"ERROR", "WARN", "INFO", "TRACE", "DEBUG", "SPAM"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	public static final String []styles= {"PRETTY", "DETAILED", "OBFUSCATED"};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	
	public static ILaunchConfiguration findOrCreateLaunch(String moduleName, String projectName, boolean shouldSave) throws CoreException {
		
		ILaunchConfiguration toLaunch = findLaunch(moduleName, projectName);

		if(toLaunch == null) {
			toLaunch = createLaunch(moduleName, projectName);
		}

		return toLaunch;
	}
	
	
	private static ILaunchConfiguration createLaunch(String moduleName, String projectName) throws CoreException {
		
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType configType = launchManager.getLaunchConfigurationType(Constants.LAUNCH_CONFIG_TYPE);
		ILaunchConfigurationWorkingCopy copy = configType.newInstance(null, launchManager.generateUniqueLaunchConfigurationNameFrom(moduleName));
		copy.setAttribute(Constants.LAUNCH_ATTR_MODULE_NAME, moduleName);
		copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
		copy.setAttribute(Constants.LAUNCH_ATTR_PROJECT_NAME, projectName);
		
		return copy.doSave();
	}


	public static ILaunchConfiguration findLaunch(String moduleName, String projectName) throws CoreException {
		
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType configType = launchManager.getLaunchConfigurationType(Constants.LAUNCH_CONFIG_TYPE);
		ILaunchConfiguration[] launchConfigurations = launchManager.getLaunchConfigurations(configType);
		ILaunchConfiguration toLaunch = null;

		for (int i = 0; i < launchConfigurations.length; i++) {
			ILaunchConfiguration configuration = launchConfigurations[i];

			if(moduleName.equals(configuration.getAttribute(Constants.LAUNCH_ATTR_MODULE_NAME, "")) //$NON-NLS-1$
					&& projectName.equals(configuration.getAttribute(Constants.LAUNCH_ATTR_PROJECT_NAME, ""))){//$NON-NLS-1$
				toLaunch = configuration;
				break;
			}
		}
		return toLaunch;
	}
	
	public static List getClasspath(IJavaProject project) throws CoreException {

		String[] defaultClasspath = JavaRuntime.computeDefaultRuntimeClassPath(project);
		
		List classpath = new ArrayList();
		classpath.addAll(Arrays.asList(defaultClasspath));

		classpath.add(Util.getGwtDevLibPath().toPortableString());

		return classpath;
	}
	
	public static IFolder getOutputLocation(IProject project) {

		return project.getFolder(Constants.OUTPUT_FOLDER);
//		return project.getLocation().append(Constants.OUTPUT_FOLDER);
	}
	
	public static List getCompilerArgs(ILaunchConfiguration configuration) throws CoreException {
		
		String moduleName = configuration.getAttribute(Constants.LAUNCH_ATTR_MODULE_NAME, "");//$NON-NLS-1$

		List commonArgs = getCommonArgs(configuration);
		commonArgs.add(moduleName);
		
		return commonArgs;
	}
	
	public static String getShellArgs(ILaunchConfiguration configuration) throws CoreException{

		boolean useDefaultUrl = configuration.getAttribute(Constants.LAUNCH_ATTR_USE_DEFAULT_URL, true);
		
		String urlArg;
		if(useDefaultUrl) {
			
			String moduleName = configuration.getAttribute(Constants.LAUNCH_ATTR_MODULE_NAME, "");//$NON-NLS-1$
			int index = moduleName.lastIndexOf('.');
			String moduleHtml = moduleName.substring(index+1)+".html";//$NON-NLS-1$
			urlArg = " "+moduleName+"/"+moduleHtml;//$NON-NLS-1$ //$NON-NLS-2$
		}else {
			urlArg = configuration.getAttribute(Constants.LAUNCH_ATTR_URL, "<no url specified>"); //$NON-NLS-1$
		}

		String port = configuration.getAttribute(Constants.LAUNCH_ATTR_PORT, "8888");//$NON-NLS-1$
		String portArg = " -port "+port+' '; //$NON-NLS-1$

		String noServer = configuration.getAttribute(Constants.LAUNCH_ATTR_USE_EMBEDDED_SERVER, true)?" ":" -noserver ";//$NON-NLS-1$ //$NON-NLS-2$

		// headless is not working anyway. We will add it later
		StringBuilder args = new StringBuilder();
		List commonArgs = getCommonArgs(configuration);
		boolean isOut = false;
		for (Iterator i = commonArgs.iterator(); i.hasNext();) {
			String aCommonArg = (String) i.next();
			if(isOut) {
				isOut = false;
				aCommonArg = "\""+aCommonArg+"\"";
			}
			args.append(aCommonArg).append(' ');
			if(aCommonArg.equals("-out"))
				isOut = true;
		}
		args.append(portArg);
		args.append(urlArg);
		args.append(noServer);
		
		return args.toString();
	}
	
	public static List getCommonArgs(ILaunchConfiguration configuration) throws CoreException {
		
		String projectName = configuration.getAttribute(Constants.LAUNCH_ATTR_PROJECT_NAME, "");//$NON-NLS-1$

		IFolder outputDir = Helper.getOutputLocation(Util.getProject(projectName));
    	String outArg = outputDir.getLocation().toPortableString();

		int logLevel = configuration.getAttribute(Constants.LAUNCH_ATTR_LOGLEVEL, 3);
		String logLevelArg = logLevels[logLevel];

		int style = configuration.getAttribute(Constants.LAUNCH_ATTR_STYLE, 1);
		String styleArg = ""+styles[style];//$NON-NLS-1$
		
		List commonArgs = new ArrayList();
		commonArgs.add("-out");//$NON-NLS-1$
		commonArgs.add(outArg);
		commonArgs.add("-logLevel");//$NON-NLS-1$
		commonArgs.add(logLevelArg);
		commonArgs.add("-style");//$NON-NLS-1$
		commonArgs.add(styleArg);
		return commonArgs;
	}

	public static String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		
		String args = configuration.getAttribute(Constants.LAUNCH_ATTR_VMOPTIONS, "");
		if (Platform.OS_MACOSX.equals(Platform.getOS())) {
			args = " -XstartOnFirstThread ";//$NON-NLS-1$
		}
		return args;
	}


}
