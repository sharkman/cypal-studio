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
import in.cypal.studio.gwt.core.launch.Helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Prakash G.R.
 * 
 */
public class GwtProject {

	IJavaProject javaProject;
	IProject project;
	List moduleFiles;
	List remoteServiceFiles;

	private GwtProject(IJavaProject javaProject) {
		this.javaProject = javaProject;
		this.project = javaProject.getProject();
	}

	public static GwtProject create(IProject project) {

		IJavaProject javaProject = JavaCore.create(project);
		return new GwtProject(javaProject);
	}

	public List getModules() {

		if (moduleFiles == null) {
			try {
				moduleFiles = Util.findModules(javaProject);
			} catch (CoreException e) {
				Activator.logException(e);
				moduleFiles = new ArrayList();
			}
		}
		return moduleFiles;
	}
	
	public List getRemoteServices(){

		if (remoteServiceFiles == null) {
			try {
				remoteServiceFiles = Util.findRemoteServices(javaProject);
			} catch (CoreException e) {
				Activator.logException(e);
				remoteServiceFiles = new ArrayList();
			}
		}
		return remoteServiceFiles;
	}
	
	public void doCompile() {
		
		List modules = getModules();
		for (Iterator i = modules.iterator(); i.hasNext();) {
			IFile aModule  = (IFile) i.next();
			try {
				compileModule(aModule);
			} catch (CoreException e) {
				// log the error and allow the other modules to compile
				Activator.logException(e);
			}
		}
	}

	public void doClean(IProgressMonitor monitor) throws CoreException {
		
		monitor = Util.getNonNullMonitor(monitor);
		try {
			List modules = getModules();
			monitor.beginTask("Starting clean...", modules.size()*2);
			IFolder folder = project.getFolder(Util.getGwtOutputFolder());
			for(int i=0;i<modules.size();i++) {
				IFile aModuleFile = (IFile) modules.get(i);
				String moduleName = Util.getQualifiedName(aModuleFile);
				IFolder moduleOutputFolder = folder.getFolder(moduleName);
				if(moduleOutputFolder.exists()) {
					moduleOutputFolder.delete(true, new SubProgressMonitor(monitor, (i*2)+1));
					moduleOutputFolder.refreshLocal(IResource.DEPTH_ONE, new SubProgressMonitor(monitor, (i*2)+2));
				}
			}
		}finally {
			monitor.done();
		}
		
	}
	
	private void compileModule(IFile moduleFile) throws CoreException {
		
		String moduleName = Util.getQualifiedName(moduleFile);
		String projectName = project.getName();
		IProject project = Util.getProject(projectName);
		
		Util.createModuleEntry(project, moduleName);
		
		ILaunchConfiguration launchConfig = Helper.findOrCreateLaunch(moduleName, projectName, true);
		ILaunchConfigurationWorkingCopy workingCopy = launchConfig.getWorkingCopy();
		workingCopy.setAttribute(Constants.LAUNCH_ATTR_GWT_COMPILE, true);
//		workingCopy.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
		workingCopy.launch(ILaunchManager.RUN_MODE, null, false, true );
//		launch(launchConfig);
		
	}
//
//	private void launch(ILaunchConfiguration launchConfig) throws CoreException {
//		
//		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
//		IVMRunner vmRunner = vmInstall.getVMRunner(ILaunchManager.RUN_MODE);
//		
//		List classpath = Helper.getClasspath(javaProject);
//		IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
//		for (int i = 0; i < packageFragmentRoots.length; i++) {
//
//			IPackageFragmentRoot aRoot = packageFragmentRoots[i];
//			if(aRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
//				IResource resource = aRoot.getResource();
//				IPath location = resource.getLocation();
//				classpath.add(location.toOSString());
//			}
//		}
//			
//		VMRunnerConfiguration vmConfig = new VMRunnerConfiguration(Constants.GWT_COMPILER_CLASS, (String[]) classpath.toArray(new String[classpath.size()]));
//		vmConfig.setWorkingDirectory(project.getLocation().toOSString());
//		List compilerArgs = Helper.getCompilerArgs(launchConfig);
//		vmConfig.setProgramArguments((String[]) compilerArgs.toArray(new String[compilerArgs.size()]));
//		final ILaunch compiler = new Launch(null, ILaunchManager.RUN_MODE, null);
//		compiler.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, "true");
//		compiler.setAttribute(Constants.LAUNCH_ATTR_GWT_COMPILE, "true");
//		
//		DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
//
//			public void handleDebugEvents(DebugEvent[] events) {
//
//				for(int i = 0;i<events.length;i++) {
//
//					DebugEvent event = events[i];
//					
//		            Object source = event.getSource();
//		            if (source instanceof IProcess && event.getKind() == DebugEvent.TERMINATE) {
//
//				        ILaunch launch = ((IProcess) source).getLaunch();
//				        if (compiler.equals(launch)) {
//				        	DebugPlugin.getDefault().removeDebugEventListener(this);
//
//							// wakeup the publisher
//							synchronized (GwtProject.this) {
//								GwtProject.this.notify();
//							}
//		                }
//					}
//				
//				}
//			}
//		});
//
//		vmRunner.run(vmConfig, compiler, null);
//		
//					
//		try {
//			synchronized (this) {
//				int i = 0;
//				while(!compiler.isTerminated() && i < 8) {
//					wait(5000);
//					i++;
//				}
//				// TODO: we throw an exception on timeout?
//			}
//		} catch (InterruptedException e) {
//			// ok;
//		}
//					
//		IFolder outputLocation = Helper.getOutputLocation(project);
//		outputLocation.refreshLocal(IResource.DEPTH_INFINITE, null);
//		
//	}

	public IProject getProject() {
		return project;
	}
	
	public IJavaProject getJavaProject() {
		return javaProject;
	}

	public List getRemoteServices(IResourceDelta delta) {
		
		if(delta == null) {
			remoteServiceFiles = getRemoteServices();
		}else {
			remoteServiceFiles = new ArrayList();
			try {
				delta.accept(new ResourceDeltaVisitor());
			} catch (CoreException e) {
				Activator.logException(e);
			}
		}
		return remoteServiceFiles;
	}
	
	private class ResourceDeltaVisitor implements IResourceDeltaVisitor{

		public boolean visit(IResourceDelta delta) throws CoreException {
			boolean interested = true;
			IResource resource = delta.getResource();

			switch (resource.getType()) {
			case IResource.PROJECT:
				IProject project = (IProject) resource;
				// we are not interested in non gwt projects
				if (!project.hasNature(Constants.NATURE_ID))
					interested = false;
				break;
			case IResource.FILE:
				if (Util.isRemoteService(resource) && delta.getKind() != IResourceDelta.REMOVED) {
					remoteServiceFiles.add((IFile) resource);
				}
				break;
			}
			return interested;
		}
	}
}
