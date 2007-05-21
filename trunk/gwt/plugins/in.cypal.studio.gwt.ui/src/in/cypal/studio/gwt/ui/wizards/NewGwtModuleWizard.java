/**
 * 
 */
package in.cypal.studio.gwt.ui.wizards;

import in.cypal.studio.gwt.ui.Activator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * 
 * @author Prakash G.R.
 * 
 */
public class NewGwtModuleWizard extends Wizard implements INewWizard, IExecutableExtension {

	private NewGwtModuleWizardPage wizardPage;

	private IConfigurationElement config;

	private IStructuredSelection selection;

	private IWorkbench workbench;

	public NewGwtModuleWizard() {
		setDefaultPageImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "/icons/gwt_icon48.png")); //$NON-NLS-1$
		setWindowTitle("New GWT Module");
	}

	public void addPages() {
		wizardPage = new NewGwtModuleWizardPage();
		wizardPage.init(selection);
		addPage(wizardPage);
	}

	public boolean performFinish() {
		try {

			wizardPage.createType(null);
			IResource resource = wizardPage.getModifiedResource();
			BasicNewResourceWizard.selectAndReveal(resource, workbench.getActiveWorkbenchWindow());
			openResource((IFile) resource);
			BasicNewProjectResourceWizard.updatePerspective(config);

		} catch (Exception e) {
			Activator.logException(e);
			MessageDialog.openError(getShell(), "Error", "Error creating GWT Module. See log file for more details.");
		}
		return true;
	}

	protected void openResource(final IFile resource) {

		final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
		if (activePage != null) {
			final Display display = getShell().getDisplay();
			if (display != null) {
				display.asyncExec(new Runnable() {
					public void run() {
						try {
							IDE.openEditor(activePage, resource, true);
						} catch (PartInitException e) {
							Activator.logException(e);
						}
					}
				});
			}
		}
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		this.config = config;
	}

}
