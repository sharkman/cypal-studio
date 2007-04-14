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
import in.cypal.studio.gwt.core.common.Util;
import in.cypal.studio.gwt.ui.Activator;
import in.cypal.studio.gwt.ui.Common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.NewInterfaceWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
import org.eclipse.jst.j2ee.webapplication.Servlet;
import org.eclipse.jst.j2ee.webapplication.ServletMapping;
import org.eclipse.jst.j2ee.webapplication.ServletType;
import org.eclipse.jst.j2ee.webapplication.WebApp;
import org.eclipse.jst.j2ee.webapplication.WebapplicationFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;



/**
 * @author Prakash (techieguy@gmail.com)
 *
 */
public class NewGwtRemoteServiceWizardPage extends NewInterfaceWizardPage {

	protected IStatus moduleStatus = Common.okStatus;
	protected IStatus projectStatus = Common.okStatus;
	protected IStatus serviceUriStatus = Common.okStatus;

	private String serviceUri = "";//$NON-NLS-1$
	private Map templateVars;
	private Combo moduleCombo;
	private String moduleText="";//$NON-NLS-1$
	private Combo projectCombo;
	private String projectText="";//$NON-NLS-1$
	private IJavaProject[] gwtProjects;
	private boolean isImplCreation;
	private Text serviceUriText;
	private IFile modifiedResource;

	public NewGwtRemoteServiceWizardPage() {
		super();
		setTitle(""); 
		setDescription(""); 
	}


//	@Override
	public void createControl(Composite parent) {

		initializeDialogUnits(parent);
		
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		
		int nColumns= 4;

		GridLayout layout= new GridLayout();
		layout.numColumns= nColumns;		
		composite.setLayout(layout);
		
		createProjectControls(composite, nColumns);	
		createModuleControls(composite, nColumns);	
				
		createSeparator(composite, nColumns);
		
		createTypeNameControls(composite, nColumns);

		createSuperInterfacesControls(composite, nColumns);
		
		createServiceUriControls(composite, nColumns);
		
		createCommentControls(composite, nColumns);
		enableCommentControl(true);
		
		setControl(composite);
		
		Dialog.applyDialogFont(composite);
		
		List superInterfaces = new ArrayList(1);
		superInterfaces.add("com.google.gwt.user.client.rpc.RemoteService");//$NON-NLS-1$
		setSuperInterfaces(superInterfaces, false);
		
		projectChanged();

	}

	private void createServiceUriControls(Composite parent, int columns) {

		Label serviceUriLabel = new Label(parent, SWT.NONE);
		serviceUriLabel.setText(""); 
		serviceUriLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		serviceUriText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		serviceUriText.setLayoutData(data);
		serviceUriText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				serviceUri = serviceUriText.getText();
				if(serviceUri.startsWith("/"))//$NON-NLS-1$
					serviceUri = serviceUri.substring(1); 
				doStatusUpdate();
			}
		});
	}


	public void createProjectControls(Composite parent, int nColumns) {

		Label locationLabel = new Label(parent, SWT.NONE);
		locationLabel.setText(""); 
		locationLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		projectCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		projectCombo.setLayoutData(data);
		projectCombo.addSelectionListener(new SelectionAdapter() {
//			@Override
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
	
	
	public void createModuleControls(Composite parent, int nColumns) {

		Label moduleLabel = new Label(parent, SWT.None);
		moduleLabel.setText(""); 
		moduleLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		moduleCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalSpan = 2;
		
		moduleCombo.setLayoutData(gridData);
		moduleCombo.addSelectionListener(new SelectionAdapter() {
//			@Override
			public void widgetSelected(SelectionEvent e) {
				moduleText = moduleCombo.getText();
				doStatusUpdate();
			}
		});

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
				for (Iterator i = modulesList.iterator(); i.hasNext();) {
					IFile file = (IFile) i.next();
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
	
	
	protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor) throws CoreException {
	
		imports.addImport("com.google.gwt.core.client.GWT");
		imports.addImport("com.google.gwt.user.client.rpc.ServiceDefTarget");
		newType.createField("public static final String SERVICE_URI = \"/"+serviceUri+"\";", null, true, monitor); //$NON-NLS-1$ //$NON-NLS-2$
		newType.createType(getUtilClassContents(), null, true, monitor);
		super.createTypeMembers(newType, imports, monitor);
	}
 
	private String getUtilClassContents() {
		
		return  "    public static class Util{\n\n" + //$NON-NLS-1$
				"      public static "+getTypeName()+"Async getInstance(){\n\n" +  //$NON-NLS-1$ //$NON-NLS-2$
				"            "+getTypeName()+"Async instance = ("+getTypeName()+"Async)GWT.create("+getTypeName()+".class);\n" +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"            ServiceDefTarget target = (ServiceDefTarget)instance;\n" +  //$NON-NLS-1$
				"            target.setServiceEntryPoint(GWT.getModuleBaseURL()+SERVICE_URI);\n" +  //$NON-NLS-1$
				"        	return instance;\n" +  //$NON-NLS-1$
				"      }\n" +  //$NON-NLS-1$
				"    }"; //$NON-NLS-1$
	}


//	@Override
	public void createType(IProgressMonitor monitor) throws CoreException, InterruptedException {

		
		isImplCreation = false;
		super.createType(monitor);
		
		isImplCreation = true;

		try {
			addServletToWebXml(new SubProgressMonitor(monitor, 1));
			createRemoteServiceImpl(new SubProgressMonitor(monitor, 2));
			addServletToGwtXml(new SubProgressMonitor(monitor, 3));
		} catch (Exception e) {
			throw new CoreException(Common.errorStatus);
		}
	}
	
//	@Override
	public IResource getModifiedResource() {
		try {
			return getCreatedType().getCompilationUnit().getCorrespondingResource();
		} catch (JavaModelException e) {
			return null;
		}
	}
	
//	@Override
	public IPackageFragmentRoot getPackageFragmentRoot() {

		IPackageFragmentRoot root = null;
		if(!projectText.equals("")) {//$NON-NLS-1$

			try {

				IJavaProject project = JavaCore.create(Util.getProject(projectText));
				IPath moduleXmlPath = new Path(moduleText);
				root = project.findPackageFragmentRoot(new Path("/").append(projectText).append(moduleXmlPath.segment(0)));//$NON-NLS-1$

			} catch (JavaModelException e) {
				Activator.logException(e);
			}
		}
		return root;
	}

//	@Override
	public IPackageFragment getPackageFragment() {

		IPackageFragment packageFragment = null;
		if(!projectText.equals("")) {//$NON-NLS-1$

			try {
				IJavaProject project = JavaCore.create(Util.getProject(projectText));
				IPath moduleXmlPath = new Path(moduleText);
				IPath packageFragmentPath = new Path("/").append(projectText).append(moduleXmlPath.removeLastSegments(1));//$NON-NLS-1$
				if(isImplCreation)
					packageFragmentPath = packageFragmentPath.append("server");//$NON-NLS-1$
				else
					packageFragmentPath = packageFragmentPath.append("client");//$NON-NLS-1$
				packageFragment = project.findPackageFragment(packageFragmentPath);
			} catch (JavaModelException e) {
				Activator.logException(e);
			}
		}
		
		return packageFragment;
	}
	
	
	public IPackageFragment getBasePackageFragment() {

		IPackageFragment packageFragment = null;
		if(!projectText.equals("")) {//$NON-NLS-1$
			
			try {
				IJavaProject project = JavaCore.create(Util.getProject(projectText));
				IPath moduleXmlPath = new Path(moduleText);
				IPath packageFragmentPath = new Path("/").append(projectText).append(moduleXmlPath.removeLastSegments(1));//$NON-NLS-1$
				packageFragment = project.findPackageFragment(packageFragmentPath);
			} catch (JavaModelException e) {
				Activator.logException(e);
			}
		}
		
		return packageFragment;
	}
	
	/**
	 * @param monitor
	 * @throws Exception 
	 */
	private void addServletToGwtXml(IProgressMonitor monitor) throws Exception {

		monitor = Util.getNonNullMonitor(monitor);

		try {

			monitor.beginTask("", 1);  

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			DocumentBuilder builder = factory.newDocumentBuilder();
			
			File moduleFile = getModuleFile();
			
			Document document = builder.parse(moduleFile);
			Node module = document.getDocumentElement();


			Element newServlet = document.createElement("servlet"); 
			newServlet.setAttribute("path", "/" + serviceUri);  //$NON-NLS-2$
			newServlet.setAttribute("class", getPackageFragment().getElementName()+'.'+ getTypeName()+"Impl");  //$NON-NLS-2$

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


	private void createRemoteServiceImpl(IProgressMonitor monitor) throws IOException, CoreException {

		monitor = Util.getNonNullMonitor(monitor);

		try {

			monitor.beginTask("", 1);  

			IFile remoteServiceImpl = Util.getProject(projectText).getFile(getPackageFragment().getResource().getProjectRelativePath().append(getTypeName() + "Impl.java")); 
//			modifiedResource = remoteServiceImpl;
			initTemplateVars(getTypeName(), "", getBasePackageFragment().getElementName()); 
			Common.writeFile("RemoteService.ServiceImpl.template", remoteServiceImpl, templateVars); 
		} finally {
			monitor.done();
		}
	}

//	@SuppressWarnings("unchecked")
	private void addServletToWebXml(IProgressMonitor monitor) {

		monitor = Util.getNonNullMonitor(monitor);

		try {

			monitor.beginTask("", 1);  

			WebapplicationFactory factory = WebapplicationFactory.eINSTANCE;

			Servlet servlet = factory.createServlet();
			servlet.setServletName(getTypeName());

			ServletType servletType = factory.createServletType();
			servletType.setClassName(getPackageFragment().getElementName() + '.' + getTypeName() + "Impl"); 
			servlet.setWebType(servletType);

			IVirtualComponent component = ComponentCore.createComponent(Util.getProject(projectText));
			WebArtifactEdit artifactEdit = WebArtifactEdit.getWebArtifactEditForWrite(component);
			WebApp webApp = (WebApp) artifactEdit.getContentModelRoot();
			webApp.getServlets().add(servlet);

			ServletMapping mapping = WebapplicationFactory.eINSTANCE.createServletMapping();
			mapping.setServlet(servlet);
			mapping.setName(servlet.getServletName());
			mapping.setUrlPattern(serviceUri);
			webApp.getServletMappings().add(mapping);

			artifactEdit.saveIfNecessary(monitor);

			artifactEdit.dispose();


		} finally {
			monitor.done();
		}


	}

	private void initTemplateVars(String serviceName, String serviceUri, String basePackage) {

		templateVars = new HashMap();
		templateVars.put("@serviceName", serviceName); 
		templateVars.put("@basePackage", basePackage); 
		templateVars.put("@serviceUri", serviceUri); 
	}
	
//	@Override
	protected void handleFieldChanged(String fieldName) {

		super.handleFieldChanged(fieldName);
		doStatusUpdate();
	}

	protected void doStatusUpdate() {

		if(projectCombo!=null) {
			projectStatus = projectText.equals("")? Common.getErrorStatus(""):Common.okStatus;  //$NON-NLS-2$
		}
		
		if(moduleCombo != null) {
			moduleStatus = moduleText.equals("")? Common.getErrorStatus(""):Common.okStatus;  //$NON-NLS-2$
		}
		
		if(serviceUriText !=null) {
			serviceUriStatus = serviceUri.equals("")?Common.getErrorStatus(""):Common.okStatus;  //$NON-NLS-2$
		}

		IStatus[] status= new IStatus[] {
			projectStatus,
			moduleStatus,
			fTypeNameStatus,
			fSuperInterfacesStatus,
			serviceUriStatus
		};

		updateStatus(status);
	}

}



