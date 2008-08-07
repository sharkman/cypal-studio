/*
 * Copyright 2006 -2008 Cypal Solutions (tools@cypal.in)
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

import in.cypal.studio.gwt.core.Activator;
import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.core.common.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

/**
 * @author Prakash G.R.
 * 
 */
public class LaunchConfigurationDelegate extends JavaLaunchDelegate {

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		super.launch(configuration, mode, launch, monitor);
	}

	public String getMainTypeName(ILaunchConfiguration configuration) throws CoreException {

		if (configuration.getAttribute(Constants.LAUNCH_ATTR_GWT_COMPILE, false))
			return Constants.GWT_COMPILER_CLASS;
		return Constants.GWT_SHELL_CLASS;
	}

	public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {

		if (configuration.getAttribute(Constants.LAUNCH_ATTR_GWT_COMPILE, false))
			return Helper.getArgs(configuration, true);
		return Helper.getShellArgs(configuration);
	}

	public String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {
		
		String projectName = configuration.getAttribute(Constants.LAUNCH_ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
		Activator.debugMessage("Calculating GWT classpath for project '"+projectName+"'");
		IJavaProject project = JavaCore.create(Util.getProject(projectName));
		List classpath = new ArrayList();

		classpath.addAll(getSourceFolders(project));

		String[] requiredProjectNames = project.getRequiredProjectNames();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (String requiredProjectName : requiredProjectNames) {
			IJavaProject requiredProject = JavaCore.create(root.getProject(requiredProjectName));
			classpath.addAll(getSourceFolders(requiredProject));
		}

		String[] classpath2 = super.getClasspath(configuration);
		for (int i = 0; i < classpath2.length; i++) {
			String aClasspath = classpath2[i];
			classpath.add(aClasspath);
		}

		classpath.add(Util.getGwtDevLibPath().toPortableString());
		
		StringBuilder classpathString = new StringBuilder();
		Activator.debugMessage("GWT Classpath:");
		for (Iterator i = classpath.iterator(); i.hasNext();) {
			Activator.debugMessage("\t"+(String) i.next());
		}

		return (String[]) classpath.toArray(new String[classpath.size()]);

	}

	private List getSourceFolders(IJavaProject project) throws JavaModelException {

		Activator.debugMessage("Adding source folders of project '"+project.getProject().getName()+"' to classpath");
		List sourceFolders = new ArrayList();
		IPackageFragmentRoot[] packageFragmentRoots = project.getPackageFragmentRoots();
		for (int i = 0; i < packageFragmentRoots.length; i++) {
			IPackageFragmentRoot packageFragmentRoot = packageFragmentRoots[i];
			if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
				sourceFolders.add(packageFragmentRoot.getResource().getLocation().toOSString());
			}
		}

		return sourceFolders;
	}

	public String getVMArguments(ILaunchConfiguration configuration) throws CoreException {

		return super.getVMArguments(configuration) + Helper.getVMArguments(configuration);
	}

}
