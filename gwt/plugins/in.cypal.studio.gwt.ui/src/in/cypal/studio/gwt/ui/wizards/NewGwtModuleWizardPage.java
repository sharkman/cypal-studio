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


package in.cypal.studio.gwt.ui.wizards;

import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.ui.Activator;
import in.cypal.studio.gwt.ui.Common;

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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;



/**
 * @author Prakash (techieguy@gmail.com)
 * 
 */
public class NewGwtModuleWizardPage extends NewClassWizardPage {

	private boolean shouldAppendClient;
	private HashMap templateVars;
	private IPackageFragment basePackageFragment;

	public NewGwtModuleWizardPage() {
		setTitle("GWT Module"); 
		setDescription("Creates a new GWT Module");  
	}
	
	public void createControl(Composite parent) {
		
		initializeDialogUnits(parent);
		
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		
		int nColumns= 4;
		
		GridLayout layout= new GridLayout();
		layout.numColumns= nColumns;		
		composite.setLayout(layout);
		
		// pick & choose the wanted UI components
		
		createContainerControls(composite, nColumns);	
		createPackageControls(composite, nColumns);	
//		createEnclosingTypeControls(composite, nColumns);
				
		createSeparator(composite, nColumns);
		
		createTypeNameControls(composite, nColumns);
//		createModifierControls(composite, nColumns);
			
		createSuperClassControls(composite, nColumns);
		createSuperInterfacesControls(composite, nColumns);
				
		createCommentControls(composite, nColumns);
		enableCommentControl(true);
		
		setControl(composite);
			
		Dialog.applyDialogFont(composite);
		
		List superInterfaces = new ArrayList(1);
		superInterfaces.add("com.google.gwt.core.client.EntryPoint"); //$NON-NLS-1$
		setSuperInterfaces(superInterfaces, true);
	}
	
//	@Override
	public void createType(IProgressMonitor monitor) throws CoreException, InterruptedException {
	
		monitor = Common.getNonNullMonitor(monitor);

		basePackageFragment = getPackageFragment();
		// Create server package
		getPackageFragmentRoot().createPackageFragment(basePackageFragment.getElementName().concat("."+Constants.SERVER_PACKAGE), true, null); //$NON-NLS-1$

		//create public folder
		IProject project = basePackageFragment.getResource().getProject();
		IFolder publicFolder = project.getFolder(basePackageFragment.getResource().getProjectRelativePath().append(Constants.PUBLIC_FOLDER));
		if(!publicFolder.exists())
			publicFolder.create(true, true, null);
		
		shouldAppendClient = true;
		super.createType(monitor);
		shouldAppendClient = false;
		
		try {
			initTemplateVars();

			IFile moduleHtml = project.getFile(publicFolder.getProjectRelativePath().append(getTypeName() + ".html"));  //$NON-NLS-1$
			Common.writeFile("Module.html.template", moduleHtml, templateVars); //$NON-NLS-1$

			IFile moduleXml = project.getFile(basePackageFragment.getResource().getProjectRelativePath().append(getTypeName()+'.'+Constants.GWT_XML_EXT));
			Common.writeFile("Module.gwt.xml.template", moduleXml, templateVars); //$NON-NLS-1$
			
			createModuleEntry(project);
			
		} catch (IOException e) {
			Activator.logException(e);
			throw new CoreException(Common.errorStatus);
		}
		
	}

	
	private void createModuleEntry(IProject project) throws CoreException {
		
		IVirtualComponent component = ComponentCore.createComponent(project);
		IVirtualFolder moduleOutputFolder = component.getRootFolder().getFolder("/");  //$NON-NLS-1$
		moduleOutputFolder.createLink(new Path(Constants.OUTPUT_FOLDER).append(basePackageFragment.getElementName()+'.'+getTypeName()), IResource.FORCE, null); 
		
	}

//	@Override
	public IPackageFragment getPackageFragment() {
		
		IPackageFragment fragment = super.getPackageFragment();
		if(shouldAppendClient)
			fragment = getPackageFragmentRoot().getPackageFragment(fragment.getElementName().concat("."+Constants.CLIENT_PACKAGE)); //$NON-NLS-1$
		return fragment;
	}

//	@Override
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
	}

	
}

