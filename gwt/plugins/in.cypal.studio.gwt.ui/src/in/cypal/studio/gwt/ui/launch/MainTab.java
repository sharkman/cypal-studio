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

import in.cypal.studio.gwt.ui.Activator;
import in.cypal.studio.gwt.ui.common.Constants;
import in.cypal.studio.gwt.ui.common.Util;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * 
 * @author Prakash G.R.
 * 
 */
public class MainTab extends AbstractLaunchConfigurationTab {

	String projectName;
	String moduleName;
	private Combo projectCombo;
	private Combo moduleCombo;

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label projectLabel = new Label(composite, SWT.NONE);
		projectLabel.setText("Project:");

		projectCombo = new Combo(composite, SWT.READ_ONLY);
		projectCombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		projectCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateModule();
				updateLaunchConfigurationDialog();
			}
		});

		Label moduleLabel = new Label(composite, SWT.NONE);
		moduleLabel.setText("Module:");

		moduleCombo = new Combo(composite, SWT.READ_ONLY);
		moduleCombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		moduleCombo.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		init();

		setControl(composite);
	}

	private void init() {

		IJavaProject[] gwtProjects = Util.getGwtProjects();
		for (int i = 0; i < gwtProjects.length; i++) {
			projectCombo.add(gwtProjects[i].getElementName());
			projectCombo.setData(gwtProjects[i].getElementName(), gwtProjects[i]);
		}

	}

	protected void updateModule() {

		moduleCombo.removeAll();
		List modules;
		try {
			IJavaProject project = (IJavaProject) projectCombo.getData(projectCombo.getText());
			if (project == null)
				return;
			modules = Util.findModules(project);
			for (Iterator i = modules.iterator(); i.hasNext();) {
				IFile moduleFile = (IFile) i.next();
				moduleCombo.add(Util.getQualifiedName(moduleFile));
			}

			if (modules.size() > 0) {
				moduleCombo.select(0);
			}

		} catch (Exception e) {
			Activator.logException(e);
		}

	}

	public String getName() {
		return "Main";
	}

	public Image getImage() {
		return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		String projectName = "";
		String moduleName = "";
		try {
			projectName = configuration.getAttribute(Constants.LAUNCH_ATTR_PROJECT_NAME, "");
			moduleName = configuration.getAttribute(Constants.LAUNCH_ATTR_MODULE_NAME, "");
			if (projectName.equals("")) {
				// support previous versions...
				projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			}
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		projectCombo.setText(projectName);
		updateModule();
		moduleCombo.setText(moduleName);
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		configuration.setAttribute(Constants.LAUNCH_ATTR_PROJECT_NAME, projectCombo.getText());
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectCombo.getText());
		configuration.setAttribute(Constants.LAUNCH_ATTR_MODULE_NAME, moduleCombo.getText());
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

		IJavaProject[] gwtProjects = Util.getGwtProjects();
		if (gwtProjects.length > 0) {
			projectName = gwtProjects[0].getElementName();
			try {
				List modules = Util.findModules(gwtProjects[0]);
				if (modules.size() > 0) {
					moduleName = Util.getQualifiedName((IFile) modules.get(0));
				}
			} catch (CoreException e) {
				Activator.logException(e);
			}
		}
	}

}
