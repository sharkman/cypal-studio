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

package in.cypal.studio.gwt.core.builder;

import in.cypal.studio.gwt.core.common.GwtProject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.model.PublishOperation;

/**
 * @author Prakash (techieguy@gmail.com)
 * 
 */
public class GwtCompileOperation extends PublishOperation {

	private IProject project;

	public GwtCompileOperation(IProject project) {
		this.project = project;
	}

//	@Override
	public void execute(IProgressMonitor monitor, IAdaptable info) throws CoreException {
		
		GwtProject gwtProject = GwtProject.create(project);
		gwtProject.doCompile();
	}
	
//	
//	private void compileModule(IFile moduleFile, IProgressMonitor monitor) throws CoreException {
//
//		
////		String moduleName = Util.getSimpleName(moduleFile);
////		String projectName = project.getName();
////		
////		ILaunchConfiguration launchConfig = Helper.findOrCreateLaunch(moduleName, projectName, false);
////		ILaunchConfigurationWorkingCopy workingCopy = launchConfig.getWorkingCopy();
////		
////		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
//////		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, vmInstall.getName());
////		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, vmInstall.getId());
////		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, Helper.getCompilerArgs(launchConfig));
////		
////		
////		List classpath = Helper.getClasspath(javaProject);
////		
////		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
////		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);		
////		IFolder outputFolder = Helper.getOutputLocation(project);
////		if(!outputFolder.exists())
////			outputFolder.create(true, true, null);
////		IPath outputLocation = outputFolder.getLocation();
////		
////		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, Helper.getVMArguments());
////		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, outputLocation.toOSString());
////		
////		final ILaunchConfiguration toLaunch = workingCopy.doSave(); 
////		
////		DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
////
////			public void handleDebugEvents(DebugEvent[] events) {
////
////				for(int i=0;i<events.length;i++) {
////					DebugEvent event = events[i];
////					Object source = event.getSource();
////					if(event.getKind() == DebugEvent.TERMINATE && source instanceof IProcess) {
////		                ILaunch launch = ((IProcess) source).getLaunch();
////
////		                if(launch.getLaunchConfiguration().equals(toLaunch)) {
////
////							DebugPlugin.getDefault().removeDebugEventListener(this);
////
////							synchronized (GwtCompileOperation.this) {
////								GwtCompileOperation.this.notify();
////							}
////		                }
////					}
////				}
////			}
////					
////		});
////
////		DebugUITools.launch(toLaunch, ILaunchManager.RUN_MODE);
////		
////		try {
////			synchronized (this) {
////				int i = 0;
////				while(++i < 1) {
////					wait(5000);
////				}
////			}
////		} catch (InterruptedException e) {
////			// do nothing
////		}
////
////		outputFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
//	}
//

//	@Override
	public int getOrder() {
		return -1;
	}
	
//	@Override
	public int getKind() {
		return REQUIRED;
	}

}
