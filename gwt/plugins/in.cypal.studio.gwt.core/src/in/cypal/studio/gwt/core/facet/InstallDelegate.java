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

package in.cypal.studio.gwt.core.facet;

import in.cypal.studio.gwt.core.Activator;
import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.core.common.Util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;


/**
 * @author Prakash (techieguy@gmail.com)
 *
 */
public class InstallDelegate implements IDelegate{

	public void execute(IProject project, IProjectFacetVersion facetVersion, Object config, IProgressMonitor monitor) throws CoreException {
		
		monitor = Util.getNonNullMonitor(monitor);
		
		try {
		
			monitor.beginTask("", 3); 
			
			addNature(project, new SubProgressMonitor(monitor, 1)); 
			addUserLibToClassPath(project, new SubProgressMonitor(monitor, 1));
			addServletLibToWebInf(project, new SubProgressMonitor(monitor, 1));
			
		}catch(CoreException e) {
			monitor.setCanceled(true);
			Activator.logException(e);
		}finally {
			monitor.done();
		}
	}

	
	private void addServletLibToWebInf(IProject project, IProgressMonitor monitor){
		
		monitor = Util.getNonNullMonitor(monitor);
		try {
			
	    	IPath webContent = ComponentCore.createComponent(project).getRootFolder().getProjectRelativePath();
			IFile theLink = project.getFile(webContent.append("WEB-INF").append("lib").append("gwt-servlet.jar")); 
			IPath actualLocation = new Path(Constants.GWT_HOME_PATH+"/gwt-servlet.jar");  
			theLink.createLink(actualLocation, IResource.REPLACE, null);  
		} catch (CoreException e) {
			monitor.setCanceled(true);
			Activator.logException(e);
		}finally {
			monitor.done();
		}
		
	}


	private void addUserLibToClassPath(IProject project, IProgressMonitor monitor){
		
		monitor = Util.getNonNullMonitor(monitor);

		try {
			monitor.beginTask("", 1); 
			
			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] oldClasspath = javaProject.getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+1];
			System.arraycopy(oldClasspath, 0, newClasspath, 0, oldClasspath.length);
			newClasspath[oldClasspath.length] = JavaCore.newVariableEntry(Util.getGwtUserLibPath(), null, null);
			javaProject.setRawClasspath(newClasspath, monitor);

		} catch (JavaModelException e) {
			monitor.setCanceled(true);
			Activator.logException(e);
		}finally {
			monitor.done();
		}
	}


	private void addNature(IProject project, IProgressMonitor monitor) throws CoreException {
		
		monitor = Util.getNonNullMonitor(monitor);
		
		try {
			
			monitor.beginTask("", 1); 
			
			IProjectDescription description = project.getDescription();
			String[] prevNatures= description.getNatureIds();
			String[] newNatures= new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 1, prevNatures.length);
			newNatures[0]= Constants.NATURE_ID;
			description.setNatureIds(newNatures);
			
			project.setDescription(description, IResource.FORCE, null);

		}finally {
			monitor.done();
		}
	}
	
	
	

}
