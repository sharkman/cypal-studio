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
 */package in.cypal.studio.gwt.ui.wizards;

import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.core.common.Util;
import in.cypal.studio.gwt.ui.Activator;
import in.cypal.studio.gwt.ui.common.GwtRuntimeComposite;
import in.cypal.studio.gwt.ui.common.GwtRuntimeComposite.IStatusListener;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author Prakash G.R.
 * 
 */
public class GwtRuntimeContainerPage extends WizardPage implements IClasspathContainerPage, IClasspathContainerPageExtension, IStatusListener {

	private GwtRuntimeComposite runtimeComposite;
	private String gwtRuntimeVersion;
	private IJavaProject javaProject;

	public GwtRuntimeContainerPage() {
		super("GwtRuntimeContainerPage");
		setTitle("GWT Runtime");
		setDescription("Select GWT Runtime for the project");
	}

	public boolean finish() {
		boolean canFinish = true;
		if (!Util.hasGwtNature(javaProject)) {
			try {
				Util.addNature(javaProject.getProject(), null);
			} catch (CoreException e) {
				Activator.logException(e);
				String message = "Could not add GWT nature to this Java Project";
				ErrorDialog.openError(new Shell(), "Error adding library", message, e.getStatus());
				canFinish = false;
			}
		}
		return canFinish;
	}

	public IClasspathEntry getSelection() {
		return runtimeComposite.getClasspathEntry();
	}

	public void setSelection(IClasspathEntry containerEntry) {
		if (containerEntry != null)
			gwtRuntimeVersion = containerEntry.getPath().lastSegment();
		else
			gwtRuntimeVersion = Constants.GWT_RUNTIME_WORKSPACE_DEFAULT;
	}

	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		runtimeComposite = new GwtRuntimeComposite(composite, this);
		runtimeComposite.setVersion(gwtRuntimeVersion);

		setControl(composite);

	}

	public void initialize(IJavaProject project, IClasspathEntry[] currentEntries) {
		this.javaProject = project;
	}

	public void updateStatus(IStatus newStatus) {
		if (newStatus.getSeverity() != IStatus.OK) {
			setPageComplete(false);
			setErrorMessage(newStatus.getMessage());
		} else {
			setPageComplete(true);
			setErrorMessage(null);
		}

	}

}
