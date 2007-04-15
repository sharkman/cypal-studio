/*
 * Copyright 2006 Prakash (techieguy@gmail.com)
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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaLaunchDelegate;


/**
 * @author Prakash (techieguy@gmail.com)
 *
 */
public class LaunchConfigurationDelegate extends JavaLaunchDelegate {

//	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		super.launch(configuration, mode, launch, monitor);
	}
	
//	@Override
	public String getMainTypeName(ILaunchConfiguration configuration) throws CoreException {

		// ignore the config. Its always the GwtShell 
		return Constants.GWT_SHELL_CLASS;		
	}
	
//	@Override
	public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {

		return Helper.getShellArgs(configuration);
	}

//	@Override
	public String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {

		String projectName = configuration.getAttribute(Constants.LAUNCH_ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
		IJavaProject project = JavaCore.create(Util.getProject(projectName));
		List classpath = new ArrayList(4);
		IPackageFragmentRoot[] packageFragmentRoots = project.getPackageFragmentRoots();
		for (int i = 0; i < packageFragmentRoots.length; i++) {
			IPackageFragmentRoot packageFragmentRoot = packageFragmentRoots[i];
			if(packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE ) {
				classpath.add(packageFragmentRoot.getResource().getLocation().toOSString());
			}
		}
		
		String[] classpath2 = super.getClasspath(configuration);
		for (int i = 0; i < classpath2.length; i++) {
			String aClasspath = classpath2[i];   
			classpath.add(aClasspath);
		}
		
		classpath.add(Util.getGwtDevLibPath().toPortableString());

		return (String[]) classpath.toArray(new String[classpath.size()]);
		
	}
	
//	@Override
	public String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		
		return super.getVMArguments(configuration) + Helper.getVMArguments();
	}
	
}
