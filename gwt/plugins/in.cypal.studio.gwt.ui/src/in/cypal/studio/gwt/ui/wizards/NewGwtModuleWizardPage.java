/*
 * Copyright 2007 - 2008 Cypal Solutions (tools@cypal.in)
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

import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.ui.Activator;
import in.cypal.studio.gwt.ui.common.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Prakash G.R.
 * 
 */
public class NewGwtModuleWizardPage extends NewTypeWizardPage {

	private boolean shouldAppendClient;

	private HashMap templateVars;

	private IPackageFragment basePackageFragment;

	private Button standardThemeButton;

	private Button chromeThemeButton;

	private Button darkThemeButton;

	// private IStatus containerStatus = Status.OK_STATUS;
	// private IStatus packageStatus = Status.OK_STATUS;
	// private IStatus nameStatus = Status.OK_STATUS;

	public NewGwtModuleWizardPage() {
		super(true, "NewGwtModuleWizardPage");
		setTitle("GWT Module");
		setDescription("Creates a new GWT Module");
	}

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());

		int nColumns = 4;

		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
		composite.setLayout(layout);

		// pick & choose the wanted UI components

		createContainerControls(composite, nColumns);
		createPackageControls(composite, nColumns);
		// createEnclosingTypeControls(composite, nColumns);

		createSeparator(composite, nColumns);

		createTypeNameControls(composite, nColumns);
		// createModifierControls(composite, nColumns);

		// createSuperClassControls(composite, nColumns);
		createSuperInterfacesControls(composite, nColumns);

		createSeparator(composite, nColumns);

		createThemeControls(composite, nColumns);
		// createMethodStubSelectionControls(composite, nColumns);

		createCommentControls(composite, nColumns);
		enableCommentControl(true);

		setControl(composite);

		Dialog.applyDialogFont(composite);

		List superInterfaces = new ArrayList(1);
		superInterfaces.add("com.google.gwt.core.client.EntryPoint"); //$NON-NLS-1$
		setSuperInterfaces(superInterfaces, true);
	}

	private void createThemeControls(Composite composite, int columns) {

		Label label = new Label(composite, SWT.NONE);
		label.setText("Select the GWT themes you want to use in the module:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, columns, 1));

		new Label(composite, SWT.NONE);
		standardThemeButton = new Button(composite, SWT.CHECK);
		standardThemeButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, columns - 1, 1));
		standardThemeButton.setText("Standard");
		standardThemeButton.setSelection(true);

		new Label(composite, SWT.NONE);
		chromeThemeButton = new Button(composite, SWT.CHECK);
		chromeThemeButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, columns - 1, 1));
		chromeThemeButton.setText("Chrome");

		new Label(composite, SWT.NONE);
		darkThemeButton = new Button(composite, SWT.CHECK);
		darkThemeButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, columns - 1, 1));
		darkThemeButton.setText("Dark");

	}

	public void createType(IProgressMonitor monitor) throws CoreException, InterruptedException {

		monitor = Util.getNonNullMonitor(monitor);

		basePackageFragment = getPackageFragment();
		// Create server package
		getPackageFragmentRoot().createPackageFragment(basePackageFragment.getElementName().concat("." + Constants.SERVER_PACKAGE), true, null); //$NON-NLS-1$

		// create public folder
		IProject project = basePackageFragment.getResource().getProject();
		IFolder publicFolder = project.getFolder(basePackageFragment.getResource().getProjectRelativePath().append(Constants.PUBLIC_FOLDER));
		if (!publicFolder.exists())
			publicFolder.create(true, true, null);

		shouldAppendClient = true;
		super.createType(monitor);
		shouldAppendClient = false;

		try {
			initTemplateVars();

			IFile moduleHtml = project.getFile(publicFolder.getProjectRelativePath().append(getTypeName() + ".html")); //$NON-NLS-1$
			Util.writeFile("/Module.html.template", moduleHtml, templateVars); //$NON-NLS-1$

			IFile moduleXml = project.getFile(basePackageFragment.getResource().getProjectRelativePath().append(getTypeName() + '.' + Constants.GWT_XML_EXT));
			Util.writeFile("/Module.gwt.xml.template", moduleXml, templateVars); //$NON-NLS-1$

			Util.createModuleEntry(project, basePackageFragment.getElementName() + '.' + getTypeName());

		} catch (IOException e) {
			Activator.logException(e);
			throw new CoreException(Util.getErrorStatus(e.getMessage()));
		}

	}

	protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor) throws CoreException {
		newType.createMethod("public void onModuleLoad() {\n\t// TODO Auto-generated method stub \n}", null, false, monitor);
		super.createTypeMembers(newType, imports, monitor);
	}

	public IPackageFragment getPackageFragment() {

		IPackageFragment fragment = super.getPackageFragment();
		if (shouldAppendClient)
			fragment = getPackageFragmentRoot().getPackageFragment(fragment.getElementName().concat("." + Constants.CLIENT_PACKAGE)); //$NON-NLS-1$
		return fragment;
	}

	public IResource getModifiedResource() {

		shouldAppendClient = true;
		IResource modifiedResource = super.getModifiedResource();
		shouldAppendClient = false;
		return modifiedResource;
	}

	private void initTemplateVars() throws CoreException {

		templateVars = new HashMap();
		templateVars.put("@className", getTypeName()); //$NON-NLS-1$
		templateVars.put("@basePackage", basePackageFragment.getElementName()); //$NON-NLS-1$
		templateVars.put("@clientPackage", basePackageFragment.getElementName() + '.' + Constants.CLIENT_PACKAGE); //$NON-NLS-1$

		templateVars.put("@standardTheme", standardThemeButton.getSelection() ? "<inherits name=\"com.google.gwt.user.theme.standard.Standard\"/>" + "" : ""); //$NON-NLS-1$
		templateVars.put("@chromeTheme", chromeThemeButton.getSelection() ? "<inherits name=\"com.google.gwt.user.theme.chrome.Chrome\"/>" : ""); //$NON-NLS-1$
		templateVars.put("@darkTheme", darkThemeButton.getSelection() ? "<inherits name=\"com.google.gwt.user.theme.dark.Dark\"/>" : ""); //$NON-NLS-1$

	}

	public void init(IStructuredSelection selection) {
		IJavaElement javaElement = getInitialJavaElement(selection);
		initContainerPage(javaElement);
		initTypePage(javaElement);
	}

	protected IStatus packageChanged() {
		String packageText = getPackageText();
		if (packageText.trim().length() == 0) {
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Default package is not allowed", null);
			updateStatus(status);
			return status;
		}
		return super.packageChanged();
	}

	protected void handleFieldChanged(String fieldName) {
		super.handleFieldChanged(fieldName);
		updateStatus(new IStatus[] { fContainerStatus, fPackageStatus, fTypeNameStatus });
	}

}
