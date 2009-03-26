///*
// * Copyright 2008 Cypal Solutions (tools@cypal.in)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// *     http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// */
//package in.cypal.studio.gwt.core.facet;
//
//import in.cypal.studio.gwt.core.Activator;
//import in.cypal.studio.gwt.core.common.Util;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.eclipse.core.resources.IFile;
//import org.eclipse.core.resources.IProject;
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.core.runtime.IPath;
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.SubProgressMonitor;
//import org.eclipse.jdt.core.IClasspathEntry;
//import org.eclipse.jdt.core.IJavaProject;
//import org.eclipse.jdt.core.JavaCore;
//import org.eclipse.jdt.core.JavaModelException;
//import org.eclipse.wst.common.componentcore.ComponentCore;
//import org.eclipse.wst.common.project.facet.core.IDelegate;
//import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
//
///**
// * @author Prakash G.R.
// * 
// */
//public class UpgradeDelegate implements IDelegate {
//
//	public void execute(IProject project, IProjectFacetVersion facetVersion, Object config, IProgressMonitor monitor) throws CoreException {
//
//		monitor = Util.getNonNullMonitor(monitor);
//		monitor.beginTask("Updating classpath...", 3);
//		try {
//
//			removeGWTEntries(project, new SubProgressMonitor(monitor, 1));
//			InstallDelegate.addUserLibToClassPath(project, new SubProgressMonitor(monitor, 1));
//			InstallDelegate.addServletLibToWebInf(project, new SubProgressMonitor(monitor, 1));
//
//		} catch (CoreException e) {
//			Activator.logException(e);
//		} finally {
//			monitor.done();
//		}
//
//	}
//
//	private void removeGWTEntries(IProject project, IProgressMonitor monitor) throws JavaModelException {
//
//		monitor = Util.getNonNullMonitor(monitor);
//		monitor.beginTask("Removing old entries...", 2);
//		try {
//
//			IJavaProject javaProject = JavaCore.create(project);
//			List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
//
//			IClasspathEntry[] oldClasspath = javaProject.getRawClasspath();
//			for (IClasspathEntry classpathEntry : oldClasspath) {
//				if (!classpathEntry.getPath().lastSegment().startsWith("gwt"))
//					classpathEntries.add(classpathEntry);
//			}
//			javaProject.setRawClasspath(classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]), new SubProgressMonitor(monitor, 1));
//
//			IPath webContent = ComponentCore.createComponent(project).getRootFolder().getProjectRelativePath();
//			IFile theLink = project.getFile(webContent.append("WEB-INF").append("lib").append("gwt-servlet.jar"));
//			theLink.delete(true, new SubProgressMonitor(monitor, 1));
//
//		} catch (CoreException e) {
//			Activator.logException(e);
//		} finally {
//			monitor.done();
//		}
//	}
//
// }
