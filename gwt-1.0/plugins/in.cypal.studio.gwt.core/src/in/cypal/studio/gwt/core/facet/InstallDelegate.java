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

package in.cypal.studio.gwt.core.facet;

import in.cypal.studio.gwt.core.Activator;
import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.core.common.Util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;

/**
 * 
 * @author Prakash G.R.
 * 
 */
public class InstallDelegate implements IDelegate {

	public void execute(IProject project, IProjectFacetVersion facetVersion, Object config, IProgressMonitor monitor) throws CoreException {

		Activator.debugMessage("Installing Cypal Studio for GWT Facet...");
		long start = System.currentTimeMillis();
		monitor = Util.getNonNullMonitor(monitor);

		try {

			monitor.beginTask("", 3);

			addNature(project, new SubProgressMonitor(monitor, 1));
			addUserLibToClassPath(project, new SubProgressMonitor(monitor, 1));
			addServletLibToWebInf(project, new SubProgressMonitor(monitor, 1));

		} catch (CoreException e) {
			monitor.setCanceled(true);
			Activator.logException(e);
		} finally {
			long end = System.currentTimeMillis();
			Activator.debugMessage("Done installing Cypal Studio for GWT Facet in "+(end-start)+" msecs");
			monitor.done();
		}
	}

	public static void addUserLibToClassPath(IProject project, IProgressMonitor monitor) {

		Activator.debugMessage("Adding gwt-user.jar to classpath of project '"+project.getName()+"'");

		monitor = Util.getNonNullMonitor(monitor);
		try {

			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] oldClasspath = javaProject.getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length + 1];
			System.arraycopy(oldClasspath, 0, newClasspath, 0, oldClasspath.length);
			IClasspathEntry gwtuserJarEntry = JavaCore.newVariableEntry(Util.getGwtUserLibPath(), null, null);
			gwtuserJarEntry = JavaCore.newVariableEntry(gwtuserJarEntry.getPath(), null, null, new IAccessRule[0], new IClasspathAttribute[0], false);
			newClasspath[oldClasspath.length] = gwtuserJarEntry;
			javaProject.setRawClasspath(newClasspath, monitor);

		} catch (CoreException e) {
			// the jar is already in the classpath.
			Activator.logException(e);
		} finally {
			monitor.done();
		}

	}

	public static void addServletLibToWebInf(IProject project, IProgressMonitor monitor) {

		Activator.debugMessage("Adding gwt-servlet.jar to classpath of project '"+project.getName()+"'");
		monitor = Util.getNonNullMonitor(monitor);

		try {
			monitor.beginTask("", 1);

			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] oldClasspath = javaProject.getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length + 1];
			System.arraycopy(oldClasspath, 0, newClasspath, 0, oldClasspath.length);
			IClasspathEntry gwtServletJarEntry = JavaCore.newVariableEntry(Util.getGwtServletLibPath(), null, null);
			IClasspathAttribute attr = JavaCore.newClasspathAttribute("org.eclipse.jst.component.dependency", "/WEB-INF/lib");
			gwtServletJarEntry = JavaCore.newVariableEntry(gwtServletJarEntry.getPath(), null, null, new IAccessRule[0], new IClasspathAttribute[] { attr }, false);
			newClasspath[oldClasspath.length] = gwtServletJarEntry;
			javaProject.setRawClasspath(newClasspath, monitor);

		} catch (JavaModelException e) {
			// the jar is already in the classpath.
			Activator.logException(e);
		} finally {
			monitor.done();
		}
	}

	private void addNature(IProject project, IProgressMonitor monitor) throws CoreException {

		Activator.debugMessage("Adding Cypal Studio for GWT nature to project '"+project.getName()+"'");
		monitor = Util.getNonNullMonitor(monitor);

		try {

			monitor.beginTask("", 1);

			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 1, prevNatures.length);
			newNatures[0] = Constants.NATURE_ID;
			description.setNatureIds(newNatures);

			project.setDescription(description, IResource.FORCE, null);

		} finally {
			monitor.done();
		}
	}

}
