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

import in.cypal.studio.gwt.core.common.Util;
import in.cypal.studio.gwt.samples.Activator;
import in.cypal.studio.gwt.ui.wizards.GwtHomeConfirmationPage;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * 
 * @author Prakash G.R.
 *
 */
public class SamplesWizard extends Wizard implements INewWizard {

	private IProject initialSelection;
	private AddSamplePage samplePage;
	
	public SamplesWizard() {
		setWindowTitle("Add GWT Sample");
	}

	public void addPages() {

		if(!Util.isGwtHomeSet())
			addPage(new GwtHomeConfirmationPage());

		samplePage = new AddSamplePage(initialSelection);
		addPage(samplePage);
	}

	private static final String SAMPLE_SRC = "samples_src";

	public boolean performFinish() {
		
		try {

			IProject project = samplePage.getProject();
			IFileStore samplesFolder = samplePage.getSamplesFolder().getChild(samplePage.getSample());
			
			IJavaProject javaProject = JavaCore.create(project);
			ensureSourceFolder(javaProject);
			IFileStore srcFolder = samplesFolder.getChild("src");
			
			copy(srcFolder, project);
			
			IFolder folder = project.getFolder(SAMPLE_SRC);
			folder.refreshLocal(IResource.DEPTH_INFINITE, null);
			
		} catch (Exception e) {
			Activator.logException(e);
			MessageDialog.openError(getShell(), "Error creating sample", "An exception occured while creating the sample. Please see the log for more details");
		}
		return true;
	}

	
	private void copy(IFileStore sampleSrcFolder, IProject project) throws CoreException {
		
		IFolder srcFolder = project.getFolder(SAMPLE_SRC);
		if(!srcFolder.exists())
			srcFolder.create(true, true, null);
//		IFolder comFolder = srcFolder.getFolder("com");
//		if(!comFolder.exists());
//			comFolder.create(true, true, null);
		IFileStore workspaceFolder = EFS.getLocalFileSystem().getStore(srcFolder.getLocation());
		sampleSrcFolder.copy(workspaceFolder, EFS.NONE, null);
	}

	public IClasspathEntry ensureSourceFolder(IJavaProject javaProject) throws JavaModelException {
		IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
		IClasspathEntry sampleSrc = null;
		for (int i = 0; i < classpathEntries.length; i++) {
			if(classpathEntries[i].getEntryKind() != IClasspathEntry.CPE_SOURCE)
				continue;// we are interested only in source folders
			String folderName = classpathEntries[i].getPath().lastSegment();
			if(folderName.equals(SAMPLE_SRC)) {
				sampleSrc = classpathEntries[i];
				break;
			}
		}
		if(sampleSrc == null) {
			addSourceFolder(javaProject, classpathEntries);
		}
		return sampleSrc;
	}
	
	private void addSourceFolder(IJavaProject javaProject, IClasspathEntry[] classpathEntries) throws JavaModelException {
		IClasspathEntry[] newClasspathEntries = new IClasspathEntry[classpathEntries.length+1];
		System.arraycopy(classpathEntries, 0, newClasspathEntries, 0, classpathEntries.length);
		newClasspathEntries[classpathEntries.length]= JavaCore.newSourceEntry(javaProject.getProject().getFullPath().append(SAMPLE_SRC));
		javaProject.setRawClasspath(newClasspathEntries, null);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		Object firstElement = selection.getFirstElement();
		if(firstElement instanceof IResource) {
			IProject project = ((IResource)firstElement).getProject();
			if(Util.hasGwtNature(project))
				initialSelection = project;
		}
	}

}
