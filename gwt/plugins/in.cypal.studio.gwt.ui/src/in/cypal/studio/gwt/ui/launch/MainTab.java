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

package in.cypal.studio.gwt.ui.launch;

import in.cypal.studio.gwt.ui.Activator;
import in.cypal.studio.gwt.ui.common.Constants;
import in.cypal.studio.gwt.ui.common.GwtLabelProvider;
import in.cypal.studio.gwt.ui.common.Util;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.launcher.SharedJavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * 
 * @author Prakash (techieguy@gmail.com)
 *
 */
//@SuppressWarnings("restriction") 
public class MainTab extends SharedJavaMainTab {
	
//	@Override
	protected void handleProjectButtonSelected() {
		IJavaProject project = chooseJavaProject();
		if (project != null) {
			fProjText.setText(project.getElementName());
		}
	}
	
	private IJavaProject chooseJavaProject() {

		ILabelProvider labelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle("Project Selection"); 
		dialog.setMessage("Select a Gwt project to constrain your search.");
		dialog.setElements(Util.getGwtProjects());

		IJavaProject javaProject= getJavaProject();
		if (javaProject != null) {
			dialog.setInitialSelections(new Object[] { javaProject });
		}

		IJavaProject result = null;
		if (dialog.open() == Window.OK) {			
			result = (IJavaProject) dialog.getFirstResult();
		}
		return result;		
	}

	public void createControl(Composite parent) {
		Font font= parent.getFont();
		Composite projComp= new Composite(parent, SWT.NONE);
		setControl(projComp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_APPLET_MAIN_TAB);
		GridLayout projLayout= new GridLayout();
		projLayout.verticalSpacing = 0;
		projComp.setLayout(projLayout);
		projComp.setFont(font);
		createProjectEditor(projComp);
		createVerticalSpacer(projComp, 1);
		createMainTypeEditor(projComp, "&Module:", null);
		createVerticalSpacer(projComp, 1);
		
	}
	
	public Image getImage() {
		return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
	}
	
	public String getName() {
		return "Main";
	}

	protected void handleSearchButtonSelected() {
		
		IJavaProject javaProject = getJavaProject();
		if(!Util.hasGwtNature(javaProject)) {
			setErrorMessage("Please select a valid GWT project to search");
		}else {
			try {
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new GwtLabelProvider());
				dialog.setTitle("Select module:"); 
				dialog.setMessage("Select a module to run.");
				List moduleFiles = Util.findModules(javaProject);
				dialog.setElements(moduleFiles.toArray(new IFile[moduleFiles.size()]));
				if(dialog.open() == Window.OK) {
					IFile selectedModule = (IFile) dialog.getFirstResult();
					String moduleName = Util.getQualifiedName(selectedModule);
					fMainText.setText(moduleName);
				}
			}catch(CoreException e) {
				Activator.handleException(e);
			}
		}
	}
	
	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		updateMainTypeFromConfig(config);
	}

	public boolean isValid(ILaunchConfiguration launchConfig) {
		
		boolean isValid = false; 
		setErrorMessage(null);
		setMessage(null);
		String name= fProjText.getText().trim();
		if(name.length() == 0) {
			setErrorMessage("Please specify a project");
		}else {
			
			isValid = isValidProject();
		}

		return isValid;
	}
	
	
	public boolean isValidProject() {
		
		boolean isValid = false;
		IProject project = Util.getProject(fProjText.getText().trim());
		try {
			if(!project.exists()) {
				setErrorMessage("The specified project does not exist");
			}else if(project.getNature(Constants.NATURE_ID)==null) {
				setErrorMessage("The specified project is not a valid GWT project");
			}else {
				// its a valid project with GWT nature
				isValid = true;
			}
		} catch (CoreException e) {
			Activator.logException(e);
		}
		return isValid;
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, fMainText.getText());
		mapResources(config);
	}
	
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {

	}

}
