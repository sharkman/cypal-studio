/*
 * Copyright 2006 - 2009 Cypal Solutions (tools@cypal.in)
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
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

	public static GwtRuntime getRuntime(IJavaProject project) throws JavaModelException {
		IClasspathEntry gwtLibraryEntry = getGwtLibraryEntry(project);
		String runtimeId = gwtLibraryEntry.getPath().lastSegment();
		GwtRuntime runtime = getRuntime(runtimeId);
		return runtime;
	}

	public static IPath getGwtUserLibPath(IJavaProject project) throws JavaModelException {
		GwtRuntime runtime = getRuntime(project);
		return new Path(runtime.getLocation()).append(getGwtUserLibJarName());//$NON-NLS-1$
	}

	public static IPath getGwtServletLibPath(IJavaProject project) throws JavaModelException {
		GwtRuntime runtime = getRuntime(project);
		return new Path(runtime.getLocation()).append(getGwtServletJarName());//$NON-NLS-1$
	}

	public static String getGwtServletJarName() {
		return "gwt-servlet.jar";
	}

	private static String gwtDevLibJarName;

	public static String getGwtDevLibJarName() {

		if (gwtDevLibJarName == null) {
			if (Platform.getOS().equals(Platform.OS_MACOSX))
				gwtDevLibJarName = "gwt-dev-mac.jar";//$NON-NLS-1$
			else if (Platform.getOS().equals(Platform.OS_WIN32))
				gwtDevLibJarName = "gwt-dev-windows.jar";//$NON-NLS-1$
			else
				// the default is linux
				gwtDevLibJarName = "gwt-dev-linux.jar"; //$NON-NLS-1$
		}
		return gwtDevLibJarName;

	}

	public static String getGwtUserLibJarName() {
		return "gwt-user.jar";
	}

	public static IPath getGwtDevLibPath(IJavaProject project) throws JavaModelException {
		GwtRuntime runtime = getRuntime(project);
		return new Path(runtime.getLocation()).append(getGwtDevLibJarName());//$NON-NLS-1$
	}

	public static boolean isModuleXml(IResource resource) {

		boolean isModuleXml;
		if (resource != null && resource instanceof IFile && resource.getName().endsWith("gwt.xml"))//$NON-NLS-1$
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

		List<IJavaProject> gwtProjects = new ArrayList<IJavaProject>(javaProjects.length);

		for (int i = 0; i < javaProjects.length; i++) {

			try {
				if (javaProjects[i].getProject().hasNature(Constants.NATURE_ID)) {
					gwtProjects.add(javaProjects[i]);
				}
			} catch (CoreException e) {
				Activator.logException(e);
			}
		}

		return gwtProjects.toArray(new IJavaProject[gwtProjects.size()]);

	}

	public static String getWarLocation() {
		return "war";
	}

	public static IFile getWebXml(IProject project) {
		IFolder warFolder = (IFolder) project.findMember(Util.getWarLocation());
		IFolder webInfFolder = warFolder.getFolder("WEB-INF");
		IFile webXmlFile = webInfFolder.getFile("web.xml");
		return webXmlFile;
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
			if (javaProject != null) {
				hasGwtNature = hasGwtNature(javaProject.getProject());
			}
		} catch (Exception e) {
			Activator.logException(e);
		}
		return hasGwtNature;
	}

	public static IProgressMonitor getNonNullMonitor(IProgressMonitor monitor) {

		if (monitor == null)
			monitor = new NullProgressMonitor();

		return monitor;
	}

	public static List<IFile> findModules(IJavaProject javaProject) throws CoreException {

		List<IFile> moduleFiles = new ArrayList<IFile>();

		for (int i = 0; i < javaProject.getPackageFragmentRoots().length; i++) {

			IPackageFragmentRoot aRoot = javaProject.getPackageFragmentRoots()[i];
			// check only in source folders. Skip others
			if (aRoot.getKind() != IPackageFragmentRoot.K_SOURCE) {
				continue;
			}

			for (int j = 0; j < aRoot.getChildren().length; j++) {
				IJavaElement aPackage = aRoot.getChildren()[j];
				// look only for packages. Skip others
				if (!(aPackage instanceof IPackageFragment)) {
					continue;
				}
				Object[] nonJavaResources = ((IPackageFragment) aPackage).getNonJavaResources();
				for (int k = 0; k < nonJavaResources.length; k++) {

					Object aResource = nonJavaResources[k];
					// look only files. Skip others
					if (!(aResource instanceof IFile)) {
						continue;
					}

					IFile aFile = (IFile) aResource;
					if (aFile.getName().endsWith(Constants.GWT_XML_EXT)) {
						moduleFiles.add(aFile);
					}
				}
			}
		}
		return moduleFiles;
	}

	public static List<IFile> findRemoteServices(IJavaProject javaProject) throws CoreException {

		List<IFile> remoteServiceFiles = new ArrayList<IFile>();

		IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
		for (int i = 0; i < packageFragmentRoots.length; i++) {
			IPackageFragmentRoot aRoot = packageFragmentRoots[i];
			// check only in source folders. Skip others
			if (aRoot.getKind() != IPackageFragmentRoot.K_SOURCE) {
				continue;
			}

			IJavaElement[] children = aRoot.getChildren();
			for (int j = 0; j < children.length; j++) {

				IJavaElement aPackage = children[j];
				// look only for packages. Skip others
				if (!(aPackage instanceof IPackageFragment)) {
					continue;
				}

				ICompilationUnit[] compilationUnits = ((IPackageFragment) aPackage).getCompilationUnits();
				for (int k = 0; k < compilationUnits.length; k++) {

					ICompilationUnit cu = compilationUnits[k];

					IResource resource = cu.getCorrespondingResource();
					if (aPackage.getResource().getName().equals(Constants.CLIENT_PACKAGE) && resource instanceof IFile && resource.getName().endsWith(".java")) {//$NON-NLS-1$
						// java file. Check whether its a remote service ...

						// for every type declared in the java file
						IType[] types = cu.getTypes();
						for (int l = 0; l < types.length; l++) {
							IType someType = types[l];
							// for every interface implemented by that type

							String[] superInterfaceNames = someType.getSuperInterfaceNames();
							for (int m = 0; m < superInterfaceNames.length; m++) {
								String aSuperInterface = superInterfaceNames[m];
								if (aSuperInterface.equals(Constants.REMOTE_SERVICE_CLASS)) {
									remoteServiceFiles.add((IFile) resource);
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
		if (resource != null && resource instanceof IFile && resource.getName().endsWith(".java")) {//$NON-NLS-1$

			ICompilationUnit cu = (ICompilationUnit) JavaCore.create(resource);
			isRemoteService = isRemoteService(cu);
		}

		return isRemoteService;
	}

	public static boolean isRemoteService(ICompilationUnit cu) throws JavaModelException {

		boolean isRemoteService = false;
		if (cu != null) {
			IType[] types = cu.getTypes();
			for (int i = 0; i < types.length; i++) {
				IType someType = types[i];
				// for every interface implemented by that type
				String[] superInterfaceNames = someType.getSuperInterfaceNames();
				for (int j = 0; j < superInterfaceNames.length; j++) {
					String aSuperInterface = superInterfaceNames[j];
					if (aSuperInterface.equals(Constants.REMOTE_SERVICE_CLASS)) {
						isRemoteService = true;
					}
				}
			}
		}
		return isRemoteService;
	}

	public static boolean isGwtModuleFile(IFile file) {

		boolean isGwtModuleFile;
		if (file != null && file.getName().endsWith(Constants.GWT_XML_EXT)) {
			isGwtModuleFile = true;
		} else {
			isGwtModuleFile = false;
		}

		return isGwtModuleFile;
	}

	public static String getSimpleName(IResource iResource) {

		String simpleName = "";//$NON-NLS-1$
		if (iResource != null) {
			simpleName = iResource.getName();
			int index = simpleName.indexOf(Constants.GWT_XML_EXT);
			simpleName = simpleName.substring(0, index - 1);
		}
		return simpleName;
	}

	public static String getQualifiedName(IResource iResource) {

		String qualifiedName = "";//$NON-NLS-1$
		if (iResource != null) {
			StringBuilder builder = new StringBuilder();

			String[] segments = getSegmentsFromSourceFolder(iResource);
			for (int i = 0; i < segments.length; i++) {
				builder.append(segments[i]);
				builder.append('.');
			}
			qualifiedName = builder.substring(0, builder.length() - Constants.GWT_XML_EXT.length() - 2);
		}
		return qualifiedName;

	}

	/**
	 * @param iResource
	 * @return
	 */
	private static String[] getSegmentsFromSourceFolder(IResource iResource) {

		int removeCount = 1;// by default, just remove the source folder;
		try {
			IJavaProject javaProject = JavaCore.create(iResource.getProject());
			IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
			for (int i = 0; i < classpathEntries.length; i++) {

				if (classpathEntries[i].getEntryKind() != IClasspathEntry.CPE_SOURCE)
					continue;// we are interested only in source folders
				IPath path = classpathEntries[i].getPath();
				if (path.isPrefixOf(iResource.getFullPath())) {
					removeCount = path.segmentCount() - 1;
				}

			}
		} catch (Throwable e) {
			Activator.logException(new Exception(e));
		}

		IPath filePath = iResource.getProjectRelativePath().removeFirstSegments(removeCount);
		return filePath.segments();
	}

	public static String getGwtOutputFolder() {
		return Preferences.getString(Constants.GWT_OUTPUT_PREFERENCE);

	}

	public static IProject getProject(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}

	public static IStatus getErrorStatus(String errorMessage) {
		return new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, errorMessage, null);
	}

	// public static void deleteModules(IProject project) throws CoreException {
	//
	// }

	public static void createModuleEntry(IProject project, String moduleName) {

		// IVirtualComponent component = ComponentCore.createComponent(project);
		// IVirtualFolder folder = component.getRootFolder().getFolder("/");
		// IContainer[] underlyingFolders = folder.getUnderlyingFolders();
		// IResource[] underlyingResources = folder.getUnderlyingResources();
		// IVirtualResource[] members = folder.members();
		// // IVirtualResource[] members = folder.members();
		// // IVirtualFolder folder2 = folder.getFolder(new
		// // Path(getGwtOutputFolder()).append(moduleName));
		// // IContainer[] underlyingFolders = folder.getUnderlyingFolders();
		//		IVirtualFolder moduleOutputFolder = component.getRootFolder().getFolder("/"); //$NON-NLS-1$
		// moduleOutputFolder.createLink(new
		// Path(getGwtOutputFolder()).append(moduleName), IResource.FORCE,
		// null);

	}

	public static boolean shouldUse1_5(IProject project) {
		boolean shouldUseGenerics = true;
		// try {
		// IFacetedProject facetedProject;
		// facetedProject = ProjectFacetsManager.create(project);
		// Set<IProjectFacetVersion> projectFacets =
		// facetedProject.getProjectFacets();
		// for (IProjectFacetVersion projectFacetVersion : projectFacets) {
		// if
		// (projectFacetVersion.getProjectFacet().getId().equals(Constants.FACET_ID))
		// {
		//
		// // 1.0 doesn't support generics, all above versions should
		// // be supporting generics
		// if (!projectFacetVersion.getVersionString().equals("1.0"))
		// shouldUseGenerics = true;
		//
		// break;
		// }
		// }
		// } catch (CoreException e) {
		// Activator.logException(e);
		// }
		return shouldUseGenerics;
	}

	public static GwtRuntime[] getRuntimes() {
		int runtimeCount = Preferences.getInt(Constants.GWT_RUNTIME_COUNT, 0);
		GwtRuntime[] runtimes = new GwtRuntime[runtimeCount];
		for (int i = 0; i < runtimes.length; i++) {
			runtimes[i] = new GwtRuntime();
			runtimes[i].setName(Preferences.getString(Constants.GWT_RUNTIME_NAME + i));
			runtimes[i].setLocation(Preferences.getString(Constants.GWT_RUNTIME_LOCATION + i));
			runtimes[i].setWorkspaceDefault(Preferences.getBoolean(Constants.GWT_RUNTIME_DEFAULT + i));
		}
		return runtimes;
	}

	public static GwtRuntime getRuntime(String runtimeId) {

		boolean isWorkspaceDefault = runtimeId.equals(Constants.GWT_RUNTIME_WORKSPACE_DEFAULT);
		GwtRuntime runtime = null;
		for (GwtRuntime gwtRuntime : getRuntimes()) {
			if (runtimeId.equals(gwtRuntime.getName()) || isWorkspaceDefault && gwtRuntime.isWorkspaceDefault()) {
				runtime = gwtRuntime;
				break;
			}
		}
		return runtime;
	}

	public static void addNature(IProject project, IProgressMonitor monitor) throws CoreException {

		Activator.debugMessage("Adding Cypal Studio for GWT nature to project '" + project.getName() + "'");
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

	public static IClasspathEntry getGwtLibraryEntry(IJavaProject javaProject) throws JavaModelException {

		IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
		IClasspathEntry entry = null;
		for (IClasspathEntry iClasspathEntry : classpathEntries) {
			if (Constants.GWT_LIBRARY_CLASSPATH.isPrefixOf(iClasspathEntry.getPath())) {
				entry = iClasspathEntry;
				break;
			}
		}
		return entry;
	}

}