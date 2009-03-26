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

package in.cypal.studio.gwt.ui.launch;

import in.cypal.studio.gwt.core.launch.Helper;
import in.cypal.studio.gwt.ui.Activator;
import in.cypal.studio.gwt.ui.common.GwtLabelProvider;
import in.cypal.studio.gwt.ui.common.Util;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Prakash G.R.
 * 
 */
public class LaunchShortcut implements ILaunchShortcut {

	public void launch(ISelection selection, String mode) {

		Object selectedItem = ((IStructuredSelection) selection).getFirstElement();
		IFile moduleFile;
		if (selectedItem instanceof IFile && Util.isGwtModuleFile((IFile) selectedItem)) {
			moduleFile = (IFile) ((IStructuredSelection) selection).getFirstElement();
		} else {
			moduleFile = getModuleFileToLaunch(selectedItem);
		}

		if(moduleFile !=null)
			launch(moduleFile, mode);

	}

	private IFile getModuleFileToLaunch(Object selectedItem) {
		IFile moduleFile;
		try {
			IJavaProject javaProject;
			if (selectedItem instanceof IJavaElement)
				javaProject = ((IJavaElement) selectedItem).getJavaProject();
			else if (selectedItem instanceof IFile) {
				IProject project = ((IFile)selectedItem).getProject();
				javaProject = JavaCore.create(project);
			}
			else {
				IProject project = (IProject) Platform.getAdapterManager().getAdapter(selectedItem, IProject.class);
				javaProject = JavaCore.create(project);
			}
			List<IFile> modules = Util.findModules(javaProject);
			switch (modules.size()) {
			case 0:
				String message = "No GWT Modules found in project.";
				ErrorDialog.openError(new Shell(), "Error launching", "Cannot launch GWT hosted mode on project '" + javaProject.getProject().getName() + "'.", new Status(IStatus.ERROR, Activator.PLUGIN_ID, message));
				moduleFile = null;
				break;
			case 1:
				moduleFile = modules.get(0);
				break;
			default:
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(new Shell(), new GwtLabelProvider());
				dialog.setMultipleSelection(false);
				dialog.setTitle("Module Selection");
				dialog.setMessage("Select the module to launch:");
				dialog.setElements(modules.toArray());
				dialog.open();
				moduleFile = (IFile) dialog.getFirstResult();
			}
		} catch (CoreException e) {
			Activator.logException(e);
			ErrorDialog.openError(new Shell(), "Error launching", "An exception occured while launching GWT Hosted mode", e.getStatus());
			moduleFile = null;
		}
		return moduleFile;
	}

	public void launch(IEditorPart editor, String mode) {

		IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
		if (!Util.isGwtModuleFile(file))
			file = getModuleFileToLaunch(file);
		launch(file, mode);
	}

	public void launch(IFile moduleFile, String mode) {

		try {

			String moduleName = Util.getQualifiedName(moduleFile);
			String projectName = moduleFile.getProject().getName();

			ILaunchConfiguration toLaunch = Helper.findOrCreateLaunch(moduleName, projectName, true);

			// we need to save, if its a working copy
			if (toLaunch instanceof ILaunchConfigurationWorkingCopy)
				toLaunch = ((ILaunchConfigurationWorkingCopy) toLaunch).doSave();

			DebugUITools.launch(toLaunch, mode);

		} catch (CoreException e) {
			Activator.handleException(e, null, "Launch Error", "Exception during launching. More details in log");
		}
	}

}
