/*
 * Copyright 2009 Cypal Solutions (tools@cypal.in)
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
package in.cypal.studio.gwt.ui.wizards;

import in.cypal.studio.gwt.core.common.Util;
import in.cypal.studio.gwt.ui.common.GwtRuntimeComposite;
import in.cypal.studio.gwt.ui.common.GwtRuntimeComposite.IStatusListener;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.WorkingSetGroup;

public class NewProjectPage extends WizardPage implements IStatusListener {

	private Text projectNameText;
	private final IStructuredSelection selection;
	private WorkingSetGroup workingSetGroup;
	private String projectName;
	private GwtRuntimeComposite runtimeComposite;
	private IStatus nameStatus = Status.OK_STATUS;
	private IStatus versionStatus = Status.OK_STATUS;

	protected NewProjectPage(IStructuredSelection selection) {
		super("NewProjectPage");
		this.selection = selection;
		setTitle("GWT Project");
		setDescription("Create a new GWT Project");
	}

	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label projectNameLabel = new Label(composite, SWT.NONE);
		projectNameLabel.setText("Project Name:");

		projectNameText = new Text(composite, SWT.BORDER);
		projectNameText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		runtimeComposite = new GwtRuntimeComposite(composite, this);

		Composite workingSetComposite = new Composite(composite, SWT.NONE);
		workingSetComposite.setLayout(new GridLayout());
		workingSetComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));

		String[] workingSetIds = new String[] { "org.eclipse.ui.resourceWorkingSetPage", "org.eclipse.jdt.ui.JavaWorkingSetPage" };
		workingSetGroup = new WorkingSetGroup(workingSetComposite, selection, workingSetIds);

		setPageComplete(false);

		setControl(composite);

		addListeners();
	}

	private void addListeners() {

		projectNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				update();
			}
		});
	}

	protected void update() {

		projectName = projectNameText.getText().trim();
		if (projectName.length() == 0)
			nameStatus = Util.getErrorStatus("A name should be specified for the project");
		else if (Util.getProject(projectName).exists())
			nameStatus = Util.getErrorStatus("A project already exists with that name");
		else
			nameStatus = Status.OK_STATUS;

		IStatus severeStatus = nameStatus.getSeverity() >= versionStatus.getSeverity() ? nameStatus : versionStatus;
		if (severeStatus.getSeverity() == IStatus.ERROR) {
			setPageComplete(false);
			setErrorMessage(severeStatus.getMessage());
		} else {
			setPageComplete(true);
			setErrorMessage(null);
		}
	}

	public String getProjectName() {
		return projectName;
	}

	public String getVersion() {
		return runtimeComposite.getVersion();
	}

	public IWorkingSet[] getSelectedWorkingSets() {
		return workingSetGroup.getSelectedWorkingSets();
	}

	public IClasspathEntry getClasspathEntry() {
		return runtimeComposite.getClasspathEntry();
	}

	public void updateStatus(IStatus newStatus) {
		versionStatus = newStatus;
		update();
	}

}
