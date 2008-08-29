/*
 * Copyright 2006-2007 Cypal Solutions (tools@cypal.in)
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

package in.cypal.studio.gwt.samples.wizards;

import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.core.common.Util;
import in.cypal.studio.gwt.samples.Activator;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Prakash G.R.
 * 
 */
public class AddSamplePage extends WizardPage {

	private final IProject initialSelection;
	private IProject project;
	private String sample;
	private Combo projectCombo;
	private Combo sampleCombo;

	public AddSamplePage(IProject initialSelection) {
		super("AddSamplePage");
		this.initialSelection = initialSelection;
		setTitle("Select Project and Sample");
		setDescription("Select the sample and the project you want to copy into");
	}

	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label sampleLabel = new Label(composite, SWT.NONE);
		sampleLabel.setText("Sample:");

		sampleCombo = new Combo(composite, SWT.READ_ONLY);
		sampleCombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		Label projectLabel = new Label(composite, SWT.NONE);
		projectLabel.setText("Project:");

		projectCombo = new Combo(composite, SWT.READ_ONLY);
		projectCombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		initData();

		projectCombo.addSelectionListener(listener);
		sampleCombo.addSelectionListener(listener);

		setControl(composite);
	}

	SelectionListener listener = new SelectionAdapter() {

		public void widgetSelected(SelectionEvent e) {

			project = (IProject) projectCombo.getData(projectCombo.getText());
			sample = sampleCombo.getText();
			update();
			if (pageStatus.getSeverity() == IStatus.ERROR)
				setErrorMessage(pageStatus.getMessage());
			else
				setErrorMessage(null);
		}
	};

	private IStatus pageStatus;
	private IFileStore samplesFolder;

	private void initData() {

		initProjectData();
		initSamplesCombo();

		update();
	}

	private void update() {

		IStatus projectStatus = projectCombo.getText().equals("") ? Util.getErrorStatus("No Dynamic web Projects with Cypal's GWT Facet found") : Util.okStatus; //$NON-NLS-2$
		IStatus sampleStatus = sampleCombo.getText().equals("") ? Util.getErrorStatus("No Samples found in your GWT installation") : Util.okStatus; //$NON-NLS-2$
		pageStatus = projectStatus.getSeverity() > sampleStatus.getSeverity() ? projectStatus : sampleStatus;

		setPageComplete(pageStatus.getSeverity() != IStatus.ERROR);
	}

	private void initSamplesCombo() {
		try {
			IPath samplesFolderPath = ResourcesPlugin.getWorkspace().getPathVariableManager().getValue(Constants.GWT_HOME_CPE).append("samples");
			samplesFolder = EFS.getLocalFileSystem().getStore(samplesFolderPath);
			String[] childNames = samplesFolder.childNames(EFS.NONE, null);
			for (int i = 0; i < childNames.length; i++) {
				sampleCombo.add(childNames[i]);
			}
		} catch (CoreException e) {
			Activator.logException(e);
		}

		sampleCombo.select(0);
		sample = sampleCombo.getText();
	}

	private void initProjectData() {

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (!Util.hasGwtNature(projects[i]))
				continue;
			String projectName = projects[i].getName();
			projectCombo.add(projectName);
			projectCombo.setData(projectName, projects[i]);
			if (initialSelection != null && initialSelection.getName().equals(projectName)) {
				projectCombo.setText(projectName);
				project = projects[i];
			}
		}

		if (projectCombo.getItemCount() > 0 && projectCombo.getSelectionIndex() == -1) {
			projectCombo.select(0);
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectCombo.getText());
		}
	}

	public String getSample() {
		return sample;
	}

	public IProject getProject() {
		return project;
	}

	public IFileStore getSamplesFolder() {
		return samplesFolder;
	}
}
