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

package in.cypal.studio.gwt.core.common;


import in.cypal.studio.gwt.core.Activator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;


/**
 * @author Prakash G.R.
 *
 */
public class Util {
	
	public static final String SRC_FOLDER = "src";
	public static final String BIN_FOLDER = "bin";
	public static final IStatus okStatus = Status.OK_STATUS;
	public static final IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Error", null);

	public static IPath getGwtUserLibPath() {
		return new Path(Constants.GWT_HOME_CPE).append("gwt-user.jar");//$NON-NLS-1$
	}
	
	public static IPath getGwtDevLibPath() {
		IPath gwtHome = JavaCore.getClasspathVariable(Constants.GWT_HOME_CPE);
		IPath devLibPath;
		if(Platform.getOS().equals(Platform.OS_MACOSX))
			devLibPath = gwtHome.append("gwt-dev-mac.jar");//$NON-NLS-1$
		else if(Platform.getOS().equals(Platform.OS_WIN32))
			devLibPath = gwtHome.append("gwt-dev-windows.jar");//$NON-NLS-1$
		else 
			// the default is linux
			devLibPath = gwtHome.append("gwt-dev-linux.jar"); //$NON-NLS-1$
		return devLibPath;
	}
	
	public static boolean isModuleXml(IResource resource) {
		
		boolean isModuleXml;
		if(resource!=null && resource instanceof IFile && resource.getName().endsWith("gwt.xml"))//$NON-NLS-1$
			isModuleXml = true;
		else 
			isModuleXml = false;
		
		return isModuleXml;
	}
	
	
	public static IJavaProject[] getGwtProjects() {
		
		IJavaProject[] gwtProjects = new IJavaProject[0];
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IJavaModel javaModel = JavaCore.create(root);
			gwtProjects = filterGwtProjects(javaModel.getJavaProjects());
		} catch (JavaModelException e) {
			Activator.logException(e);
		}
		return gwtProjects;
	}
	
	public static IJavaProject[] filterGwtProjects(IJavaProject[] javaProjects) {

		List gwtProjects = new ArrayList(javaProjects.length);

		for (int i = 0; i < javaProjects.length; i++) {
			
			try {
				if(javaProjects[i].getProject().hasNature(Constants.NATURE_ID)) {
					gwtProjects.add(javaProjects[i]);
				}
			} catch (CoreException e) {
				Activator.logException(e);
			}
		}

		return (IJavaProject[]) gwtProjects.toArray(new IJavaProject[gwtProjects.size()]);

	}
	
	public static String getOutputLocation(String projectName, String moduleName) {

		return getOutputLocation(Util.getProject(projectName), moduleName);
	}
	
	private static String getOutputLocation(IProject project, String moduleName) {

		IPath outputDir = project.getLocation().append(getGwtOutputFolder());
    	return outputDir.toPortableString();
	}

	public static boolean hasGwtNature(IProject project) {

		boolean hasGwtNature;
		try {
			hasGwtNature = project.isAccessible() && project.hasNature(Constants.NATURE_ID);
		} catch (Exception e) {
			Activator.logException(e);
			hasGwtNature = false;
		}
		return hasGwtNature;
	}
	
	public static boolean hasGwtNature(IJavaProject javaProject) {

		boolean hasGwtNature = false;
		try {
			if(javaProject != null) {
				hasGwtNature = hasGwtNature(javaProject.getProject());
			}
		} catch (Exception e) {
			Activator.logException(e);
		}
		return hasGwtNature;
	}
	
	public static IProgressMonitor getNonNullMonitor(IProgressMonitor monitor) {

		if(monitor ==null)
			monitor = new NullProgressMonitor();
		
		return monitor;
	}

	public static List findModules(IJavaProject javaProject) throws CoreException {

		List moduleFiles = new ArrayList();

		for (int i = 0;i< javaProject.getPackageFragmentRoots().length; i++) {

			IPackageFragmentRoot aRoot = javaProject.getPackageFragmentRoots()[i];
			// check only in source folders. Skip others
			if(aRoot.getKind() != IPackageFragmentRoot.K_SOURCE) {
				continue;
			}
			
			for (int j = 0; j < aRoot.getChildren().length; j++) {
				IJavaElement aPackage = aRoot.getChildren()[j];
				// look only for packages. Skip others
				if(!(aPackage instanceof IPackageFragment)) { 
					continue;
				}
				Object[] nonJavaResources = ((IPackageFragment)aPackage).getNonJavaResources();
				for (int k = 0; k < nonJavaResources.length; k++) {
					
					Object aResource = nonJavaResources[k];
					// look only files. Skip others
					if(!(aResource instanceof IFile)) {
						continue;
					}
				
					IFile aFile = (IFile) aResource;
					if(aFile.getName().endsWith(Constants.GWT_XML_EXT)) {
						moduleFiles.add(aFile);
					}
				}
			}
		}
		return moduleFiles;
	}
	
	public static List findRemoteServices(IJavaProject javaProject) throws CoreException {
		
		List remoteServiceFiles = new ArrayList();
		
		IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
		for (int i = 0; i < packageFragmentRoots.length; i++) {
			IPackageFragmentRoot aRoot = packageFragmentRoots[i]; 
			// check only in source folders. Skip others
			if(aRoot.getKind() != IPackageFragmentRoot.K_SOURCE) {
				continue;
			}
			
			IJavaElement[] children = aRoot.getChildren();
			for (int j = 0; j < children.length; j++) {

				IJavaElement aPackage = children[j];
				// look only for packages. Skip others
				if(!(aPackage instanceof IPackageFragment)) { 
					continue;
				}
				
				ICompilationUnit[] compilationUnits = ((IPackageFragment)aPackage).getCompilationUnits();
				for (int k = 0; k < compilationUnits.length; k++) {

					ICompilationUnit cu = compilationUnits[k];
					
					IResource resource = cu.getCorrespondingResource();
					if(aPackage.getResource().getName().equals(Constants.CLIENT_PACKAGE) && resource instanceof IFile && resource.getName().endsWith(".java")){//$NON-NLS-1$
						// java file. Check whether its a remote service ...

						// for every type declared in the java file
						IType[] types = cu.getTypes();
						for (int l = 0; l < types.length; l++) {
							IType someType = types[l];
							// for every interface implemented by that type
							
							String[] superInterfaceNames = someType.getSuperInterfaceNames();
							for (int m = 0; m < superInterfaceNames.length; m++) {
								String aSuperInterface = superInterfaceNames[m];
								if(aSuperInterface.equals(Constants.REMOTE_SERVICE_CLASS)) {
									remoteServiceFiles.add((IFile)resource);
								}
							}
						}
					}
				}
			}
		}
		return remoteServiceFiles;
	}
	
	public static boolean isRemoteService(IResource resource) throws JavaModelException {
		
		boolean isRemoteService = false;
		if(resource != null && resource instanceof IFile && resource.getName().endsWith(".java")) {//$NON-NLS-1$
			
			ICompilationUnit cu = (ICompilationUnit) JavaCore.create(resource);
			isRemoteService = isRemoteService(cu);
		}
		
		return isRemoteService;
	}
	
	
	public static boolean isRemoteService(ICompilationUnit cu) throws JavaModelException {
		
		boolean isRemoteService = false;
		if(cu != null) {
			IType[] types = cu.getTypes();
			for (int i = 0; i < types.length; i++) {
				IType someType = types[i];
				// for every interface implemented by that type
				String[] superInterfaceNames = someType.getSuperInterfaceNames();
				for (int j = 0; j < superInterfaceNames.length; j++) {
					String aSuperInterface = superInterfaceNames[j];
					if(aSuperInterface.equals(Constants.REMOTE_SERVICE_CLASS)) {
						isRemoteService = true;
					}
				}
			}
		}
		return isRemoteService;
	}
	
	public static boolean isGwtModuleFile(IFile file) {
		
		boolean isGwtModuleFile;
		if(file != null && file.getName().endsWith(Constants.GWT_XML_EXT)){
			isGwtModuleFile = true;
		}else {
			isGwtModuleFile = false;
		}
		
		return isGwtModuleFile;
	}
	
	public static String getSimpleName(IFile file) {
		
		String simpleName = "";//$NON-NLS-1$
		if(file!=null) {
			simpleName = file.getName();
			int index = simpleName.indexOf(Constants.GWT_XML_EXT);
			simpleName = simpleName.substring(0, index-1);
		}
		return simpleName;
	}
	
	public static String getQualifiedName(IFile file) {
		
		
		String qualifiedName = "";//$NON-NLS-1$
		if(file!=null) {
			StringBuilder builder = new StringBuilder();
			
			String [] segments = getSegmentsFromSourceFolder(file);
			for(int i = 0; i<segments.length;i++) {
				builder.append(segments[i]);
				builder.append('.');
			}
			qualifiedName = builder.substring(0, builder.length()-Constants.GWT_XML_EXT.length()-2);
		}
		return qualifiedName;
	
	}
	
	/**
	 * @param file 
	 * @return
	 */
	private static String[] getSegmentsFromSourceFolder(IFile file) {
		
		int removeCount = 1;// by default, just remove the source folder;
		try {
			IJavaProject javaProject = JavaCore.create(file.getProject());
			IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
			for (int i = 0; i < classpathEntries.length; i++) {
				
				if(classpathEntries[i].getEntryKind() != IClasspathEntry.CPE_SOURCE)
					continue;// we are interested only in source folders
				IPath path = classpathEntries[i].getPath();
				// Ugly way to ensure the right source folder when many are present. 
				// Should work anyway
				removeCount = path.segmentCount()-1;
				
			}
		} catch (Throwable e) {
			Activator.logException(new Exception(e));
		}
		
		IPath filePath = file.getProjectRelativePath().removeFirstSegments(removeCount);
		return filePath.segments();
	}

	public static String getGwtOutputFolder() {
		return Preferences.getString(Constants.GWT_OUTPUT_PREFERENCE);
		
	}
	
	public static boolean isGwtHomeSet(){
		
		boolean set= false;
		if(JavaCore.getClasspathVariable(Constants.GWT_HOME_CPE) != null) {
			File gwtUserJar = new File(Preferences.getString(Constants.GWT_HOME_PREFERENCE)+"/gwt-user.jar");
			set = gwtUserJar.exists(); 
		}
		return set;
	}
	
	public static IProject getProject(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}

	public static IStatus getErrorStatus(String errorMessage) {
		return new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, errorMessage, null);
	}
}