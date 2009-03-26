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

import in.cypal.studio.gwt.ui.Activator;
import in.cypal.studio.gwt.ui.common.Util;

import java.io.FileInputStream;
import java.io.StringBufferInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

/**
 * @author Prakash G.R
 */
@SuppressWarnings("deprecation")
public class NewGwtProjectWizard extends Wizard implements INewWizard, IExecutableExtension {

	private NewProjectPage newProjectPage;
	private JavaCapabilityConfigurationPage configurationPage;

	private IStructuredSelection selection;
	private IWorkbench workbench;
	private IConfigurationElement config;

	public NewGwtProjectWizard() {
		setWindowTitle("New GWT Project");
		setNeedsProgressMonitor(true);
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		this.config = config;
	}

	@Override
	public void addPages() {
		newProjectPage = new NewProjectPage(selection);
		configurationPage = new JavaCapabilityConfigurationPage();
		addPage(newProjectPage);
		addPage(configurationPage);
	}

	@Override
	public void setContainer(IWizardContainer wizardContainer) {
		super.setContainer(wizardContainer);
		if (wizardContainer != null)
			((WizardDialog) getContainer()).addPageChangingListener(pageChangingListener);
	}

	@Override
	public boolean performFinish() {
		try {
			if (!containerInitialized)
				initializeBuildPath();
			getContainer().run(true, true, configurationPage.getRunnable());
			getContainer().run(true, true, getCopierRunnable());
			BasicNewProjectResourceWizard.updatePerspective(config);
		} catch (Exception e) {
			Activator.logException(e);
			return false;
		}
		return true;
	}

	private IRunnableWithProgress getCopierRunnable() {
		return new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					IJavaProject javaProject = configurationPage.getJavaProject();
					IFolder libFolder = javaProject.getProject().getFolder(new Path("war/WEB-INF/lib"));
					IFile servletJar = libFolder.getFile(Util.getGwtServletJarName());
					IPath gwtServletLibPath = Util.getGwtServletLibPath(javaProject);
					servletJar.create(new FileInputStream(gwtServletLibPath.toFile()), true, monitor);
				} catch (Exception e) {
					Activator.logException(e);
					throw new InvocationTargetException(e);
				}
			}
		};
	}

	@Override
	public boolean performCancel() {
		boolean cancel = super.performCancel();
		String projectName = newProjectPage.getProjectName();
		if (projectName != null) {
			IProject project = Util.getProject(newProjectPage.getProjectName());
			if (project.exists())
				try {
					project.delete(true, null);
				} catch (CoreException e) {
					Activator.logException(e);
				}
		}
		return cancel;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}

	private final IPageChangingListener pageChangingListener = new IPageChangingListener() {

		public void handlePageChanging(PageChangingEvent event) {
			if (event.getTargetPage() == configurationPage) {
				initializeBuildPath();
			}

		}
	};
	protected boolean containerInitialized;

	protected void initializeBuildPath() {
		try {
			getContainer().run(true, false, getInitRunnable());
			((WizardDialog) getContainer()).removePageChangingListener(pageChangingListener);
		} catch (Exception e) {
			Activator.logException(e);
		}

	}

	private IRunnableWithProgress getInitRunnable() {

		return new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Initializing Build path...", 6);
				try {
					String projectName = newProjectPage.getProjectName();
					IProject project = Util.getProject(projectName);
					project.create(new SubProgressMonitor(monitor, 1));
					project.open(new SubProgressMonitor(monitor, 1));
					Util.addNature(project, new SubProgressMonitor(monitor, 1));
					IJavaProject javaProject = JavaCore.create(project);
					createWarStucture(project, new SubProgressMonitor(monitor, 1));

					List<IClasspathEntry> cpEntries = new ArrayList<IClasspathEntry>();
					cpEntries.add(getSourcePath(projectName));
					cpEntries.addAll(Arrays.asList(PreferenceConstants.getDefaultJRELibrary()));
					cpEntries.add(newProjectPage.getClasspathEntry());
					monitor.worked(1);
					IClasspathEntry[] entries = cpEntries.toArray(new IClasspathEntry[cpEntries.size()]);
					configurationPage.init(javaProject, getOutputLocation(projectName), entries, false);
					monitor.worked(1);
					containerInitialized = true;
				} catch (CoreException e) {
					Activator.logException(e);
				} finally {
					monitor.done();
				}
			}
		};
	}

	protected void createWarStucture(IProject project, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Creating WAR templates...", 3);
		try {
			IFolder warFolder = project.getFolder("war");
			warFolder.create(true, true, new SubProgressMonitor(monitor, 1));
			IFolder webInfFolder = warFolder.getFolder("WEB-INF");
			webInfFolder.create(true, true, new SubProgressMonitor(monitor, 1));
			IFolder libFolder = webInfFolder.getFolder("lib");
			libFolder.create(true, true, new SubProgressMonitor(monitor, 1));
			IFile webxml = webInfFolder.getFile("web.xml");
			webxml.create(new StringBufferInputStream(getWebXmlContents()), true, new SubProgressMonitor(monitor, 1));

		} finally {
			monitor.done();
		}
	}

	private String getWebXmlContents() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<web-app>\r\n</web-app>";
	}

	private IClasspathEntry getSourcePath(String projectPath) throws CoreException {
		IPath sourceFolderPath = new Path(projectPath).makeAbsolute().append("src");
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFolder folder = root.getFolder(sourceFolderPath);
		folder.create(true, true, null);
		return JavaCore.newSourceEntry(sourceFolderPath);
	}

	public IPath getOutputLocation(String projectName) {
		return new Path(projectName).makeAbsolute().append("bin");
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

}
