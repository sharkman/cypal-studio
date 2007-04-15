/*
 * Copyright 2006  Ravi (kkravikumar@gmail.com)
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
import in.cypal.studio.gwt.ui.common.Constants;
import in.cypal.studio.gwt.ui.common.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Ravi (kkravikumar@gmail.com)
 *
 */
public class NewGwtEntryPointClassWizardPage extends NewTypeWizardPage {

	private static final String pageName = "NewGwtEntryPointClassWizardPage";  //$NON-NLS-1$
	private Combo moduleCombo;
	private String moduleText=""; //$NON-NLS-1$
	private String projectText=""; //$NON-NLS-1$
	private Combo projectCombo;
	private IJavaProject[] gwtProjects;
	protected IStatus moduleStatus = Util.okStatus;
	protected IStatus projectStatus = Util.okStatus;
	
	public NewGwtEntryPointClassWizardPage(boolean isClass, String pageName) {
		super(isClass, pageName);
		setTitle("Entry Point Class"); 
		setDescription("Creates a new GWT Entry Point Class");
	}

	public NewGwtEntryPointClassWizardPage() {
		this(true, pageName);
	}
	
	
	public NewGwtEntryPointClassWizardPage(int typeKind, String pageName) {
		super(typeKind, pageName);
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
		createProjectControls(composite, nColumns);	
		
		createModuleControls(composite, nColumns);
		
		createSeparator(composite, nColumns);
		
		createTypeNameControls(composite, nColumns);
		createModifierControls(composite, nColumns);
			
		createSuperClassControls(composite, nColumns);
		createSuperInterfacesControls(composite, nColumns);
				
		createCommentControls(composite, nColumns);
		enableCommentControl(true);
		
		setControl(composite);
			
		Dialog.applyDialogFont(composite);
		
		List interfaceList = new ArrayList(1);
		interfaceList.add("com.google.gwt.core.client.EntryPoint"); //$NON-NLS-1$
		setSuperClass("java.lang.Object", false);//$NON-NLS-1$
		setSuperInterfaces(interfaceList, false);
		
		projectChanged();
	}

	private void doStatusUpdate() {
		
		if(projectCombo!=null) {
			projectStatus = projectText.equals("")? Util.getErrorStatus(""):Util.okStatus;
		}
		
		if(moduleCombo != null) {
			moduleStatus = moduleText.equals("")? Util.getErrorStatus(""):Util.okStatus;
		}
		
		IStatus[] status= new IStatus[] {
			projectStatus,
			moduleStatus,
			fTypeNameStatus,
			fSuperInterfacesStatus
		};
		
		// the mode severe status will be displayed and the OK button enabled/disabled.
		updateStatus(status);
	}
	
	protected void handleFieldChanged(String fieldName) {
		super.handleFieldChanged(fieldName);
		
		doStatusUpdate();
	}
	
//	@Override
	protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor) throws CoreException {
		newType.createMethod("public void onModuleLoad() {\n\t// TODO Auto-generated method stub \n}", null, false, monitor);//$NON-NLS-1$
		super.createTypeMembers(newType, imports, monitor);
	}
	
//	@Override
	public void createType(IProgressMonitor monitor) throws CoreException, InterruptedException {
		
		super.createType(monitor);
		
		try {
			addEntryPointClassToGwtXml(new SubProgressMonitor(monitor, 3));
		} catch (Exception e) {
			throw new CoreException(Util.errorStatus);
		}
	}
	
	private void addEntryPointClassToGwtXml(IProgressMonitor monitor) throws Exception {

		monitor = Util.getNonNullMonitor(monitor);

		try {

			monitor.beginTask("", 1);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			DocumentBuilder builder = factory.newDocumentBuilder();
			
			File moduleFile = getModuleFile();
			
			Document document = builder.parse(moduleFile);
			Node module = document.getDocumentElement();


			Element newServlet = document.createElement("entry-point");//$NON-NLS-1$
			newServlet.setAttribute("class", getPackageFragment().getElementName()+'.'+ getTypeName());//$NON-NLS-1$

			module.appendChild(newServlet);

			Transformer writer = TransformerFactory.newInstance().newTransformer();

			writer.transform(new DOMSource(document), new StreamResult(moduleFile));


		} finally {
			monitor.done();
		}
	}
	
	private File getModuleFile() throws CoreException {
		IResource resource = Util.getProject(projectText).findMember(moduleText);
		return resource.getLocation().toFile();
	}
	
	
	public void createModuleControls(Composite parent, int nColumns) {

		Label moduleLabel = new Label(parent, SWT.None);
		moduleLabel.setText("Module:");
		moduleLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		moduleCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalSpan = 2;
		
		moduleCombo.setLayoutData(gridData);
		moduleCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moduleText = moduleCombo.getText();
				doStatusUpdate();
			}
		});

		new Label(parent, SWT.NONE);
	}
	
	public void createProjectControls(Composite parent, int nColumns) {

		Label locationLabel = new Label(parent, SWT.NONE);
		locationLabel.setText("Project:"); 
		locationLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		projectCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		projectCombo.setLayoutData(data);
		projectCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				projectText = projectCombo.getText();
				projectChanged();
			}
		});
		
		gwtProjects = Util.getGwtProjects();
		
		for (int i = 0; i < gwtProjects.length; i++) {
			IJavaProject gwtProject = gwtProjects[i];
			projectCombo.add(gwtProject.getProject().getName());
		}

		projectCombo.select(0);
		
		new Label(parent, SWT.NONE);
	}
	
	protected void projectChanged()  {

		projectText = projectCombo.getText();
		IJavaProject selectedProject = null;
		for (int i = 0; i < gwtProjects.length; i++) {
			IJavaProject gwtProject = gwtProjects[i];
			if(projectText.equals(gwtProject.getProject().getName())) {
				selectedProject = gwtProject;
				break;
			}
		}
		
		if(selectedProject !=null) {
			try {
				moduleCombo.removeAll();
				List modulesList = Util.findModules(selectedProject);
				for (Iterator j = modulesList.iterator(); j.hasNext();) {
					IFile file  = (IFile) j.next();
					IPath projectRelativePath = file.getProjectRelativePath();
					String fileName = file.getName();
					String moduleName = fileName.substring(0, fileName.length() - Constants.GWT_XML_EXT.length()-1);
					moduleCombo.add(projectRelativePath.toString());
					moduleCombo.setData(moduleName, file);
				}
				moduleCombo.select(0);
				moduleText = moduleCombo.getText();
			} catch (CoreException e) {
				Activator.logException(e);
			}
		}
		doStatusUpdate();
	}
	
//	@Override
	public IPackageFragment getPackageFragment() {
		IPackageFragment packageFragment = null;
		if(!projectText.equals("")) {//$NON-NLS-1$
			try {
				IJavaProject project = JavaCore.create(Util.getProject(projectText));
				IPath moduleXmlPath = new Path(moduleText);
				IPath packageFragmentPath = new Path("/").append(projectText).append(moduleXmlPath.removeLastSegments(1));//$NON-NLS-1$
				packageFragmentPath = packageFragmentPath.append("client");//$NON-NLS-1$
				packageFragment = project.findPackageFragment(packageFragmentPath);
			} catch (JavaModelException e) {
				Activator.logException(e);
			}
		}
		return packageFragment;
	}

	public void init(IStructuredSelection selection) {
		IJavaElement jelem= getInitialJavaElement(selection);
			
		initContainerPage(jelem);
		initTypePage(jelem);
		doStatusUpdate();
	}
	
}
