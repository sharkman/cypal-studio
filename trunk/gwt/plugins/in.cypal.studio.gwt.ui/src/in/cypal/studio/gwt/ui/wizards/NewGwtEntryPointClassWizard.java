/*
 * Copyright 2006 - 2008  Ravi (kkravikumar@gmail.com)
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

/**
 * @author Ravi (kkravikumar@gmail.com)
 * 
 */
public class NewGwtEntryPointClassWizard extends NewElementWizard implements IExecutableExtension {

	private NewGwtEntryPointClassWizardPage entryPointClassWizardPage;
	private IConfigurationElement config;

	public NewGwtEntryPointClassWizard() {
		setDefaultPageImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "/icons/gwt_icon48.png"));//$NON-NLS-1$
		setWindowTitle("New GWT Entry Point class");
		setDialogSettings(JavaPlugin.getDefault().getDialogSettings());
	}

	@Override
	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
		entryPointClassWizardPage.createType(monitor);
	}

	@Override
	public IJavaElement getCreatedElement() {
		return entryPointClassWizardPage.getCreatedType();
	}

	@Override
	public void addPages() {

		entryPointClassWizardPage = new NewGwtEntryPointClassWizardPage();
		entryPointClassWizardPage.init(getSelection());
		addPage(entryPointClassWizardPage);
		setNeedsProgressMonitor(true);
		super.addPages();
	}

	@Override
	public boolean performFinish() {
		warnAboutTypeCommentDeprecation();
		boolean response = super.performFinish();
		if (response) {
			IResource resource = entryPointClassWizardPage.getModifiedResource();
			if (resource != null) {
				selectAndReveal(resource);
				openResource((IFile) resource);
			}
		}
		BasicNewProjectResourceWizard.updatePerspective(config);
		return response;
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		this.config = config;
	}
}
