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

package in.cypal.studio.gwt.ui.launch;

import in.cypal.studio.gwt.core.launch.Helper;
import in.cypal.studio.gwt.ui.Activator;
import in.cypal.studio.gwt.ui.common.Util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;


/**
 * @author Prakash G.R.
 *
 */
public class LaunchShortcut implements ILaunchShortcut {

	public void launch(ISelection selection, String mode) {
		
		IFile moduleFile = (IFile) ((IStructuredSelection) selection).getFirstElement();
		launch(moduleFile, mode);
	}

	public void launch(IEditorPart editor, String mode) {

		IFile moduleFile = ((FileEditorInput) editor.getEditorInput()).getFile();
		launch(moduleFile, mode);

	}

	public void launch(IFile moduleFile, String mode) {

		try {
			
			String moduleName = Util.getQualifiedName(moduleFile);
			String projectName = moduleFile.getProject().getName();
			
			ILaunchConfiguration toLaunch = Helper.findOrCreateLaunch(moduleName, projectName, true);
			
			// we need to save, if its a working copy
			if(toLaunch instanceof ILaunchConfigurationWorkingCopy)
				toLaunch = ((ILaunchConfigurationWorkingCopy)toLaunch).doSave();

			DebugUITools.launch(toLaunch, mode);
			
		} catch (CoreException e) {
			Activator.handleException(e, null, "Launch Error", "Exception during launching. More details in log");
		}
	}

}
