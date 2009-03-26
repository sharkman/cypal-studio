/*
 * Copyright 2006 - 2009 Cypal Solutions (tools@cypal.in)
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.NewInterfaceWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Prakash G.R.
 * 
 */
public class NewGwtRemoteServiceWizardPage extends NewInterfaceWizardPage {

	protected IStatus moduleStatus = in.cypal.studio.gwt.core.common.Util.okStatus;
	protected IStatus projectStatus = in.cypal.studio.gwt.core.common.Util.okStatus;
	protected IStatus serviceUriStatus = in.cypal.studio.gwt.core.common.Util.okStatus;

	private String serviceUri = "";//$NON-NLS-1$
	private Map<String, String> templateVars;
	private Combo moduleCombo;
	private String moduleText = "";//$NON-NLS-1$
	private Combo projectCombo;
	private String projectText = "";//$NON-NLS-1$
	private IJavaProject[] gwtProjects;
	private boolean isImplCreation;
	private boolean shouldCreateImpl = true;
	private Text serviceUriText;

	// private IFile modifiedResource;
	private IFile selectedModule;

	private String selectedProject;
	private Button implCreationButton;
	private IJavaProject selectedJavaProject;

	public NewGwtRemoteServiceWizardPage() {
		super();
		setTitle("Remote Service");
		setDescription("Create a new GWT Remote Service");
	}

	// @Override
	@Override
	public void createControl(Composite parent) {

		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());

		int nColumns = 4;

		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
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

		List<String> superInterfaces = new ArrayList<String>(1);
		superInterfaces.add("com.google.gwt.user.client.rpc.RemoteService");//$NON-NLS-1$
		setSuperInterfaces(superInterfaces, false);

		projectChanged();

	}

	private void createServiceUriControls(Composite parent, int columns) {

		Label serviceUriLabel = new Label(parent, SWT.NONE);
		serviceUriLabel.setText("Service URI:");
		serviceUriLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		serviceUriText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		serviceUriText.setLayoutData(data);
		serviceUriText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				serviceUri = serviceUriText.getText();
				if (serviceUri.startsWith("/"))//$NON-NLS-1$
					serviceUri = serviceUri.substring(1);
				doStatusUpdate();
			}
		});

		createImplOptionControls(parent);
	}

	/**
	 * @param parent
	 */
	private void createImplOptionControls(Composite parent) {
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);

		implCreationButton = new Button(parent, SWT.CHECK);
		implCreationButton.setText("Also create default implementation file");
		implCreationButton.setSelection(true);
		implCreationButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));
		implCreationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shouldCreateImpl = implCreationButton.getSelection();
			}
		});
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
			@Override
			public void widgetSelected(SelectionEvent e) {
				projectText = projectCombo.getText();
				projectChanged();
			}
		});
		gwtProjects = Util.getGwtProjects();
		for (int i = 0; i < gwtProjects.length; i++) {
			IJavaProject gwtProject = gwtProjects[i];
			String name = gwtProject.getProject().getName();
			projectCombo.add(name);
			if (name.equals(selectedProject))
				projectCombo.select(i);
		}
		if (projectCombo.getSelectionIndex() == -1)
			projectCombo.select(0);

		new Label(parent, SWT.NONE);
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
			// @Override
			@Override
			public void widgetSelected(SelectionEvent e) {
				moduleText = moduleCombo.getText();
				doStatusUpdate();
			}
		});

		new Label(parent, SWT.NONE);
	}

	protected void projectChanged() {

		projectText = projectCombo.getText();
		selectedJavaProject = null;
		for (int i = 0; i < gwtProjects.length; i++) {
			IJavaProject gwtProject = gwtProjects[i];
			if (projectText.equals(gwtProject.getProject().getName())) {
				selectedJavaProject = gwtProject;
				break;
			}
		}

		if (selectedJavaProject != null) {
			try {
				moduleCombo.removeAll();
				List<IFile> modulesList = Util.findModules(selectedJavaProject);
				for (IFile file : modulesList) {
					IPath projectRelativePath = file.getProjectRelativePath();
					String fileName = file.getName();
					String moduleName = fileName.substring(0, fileName.length() - Constants.GWT_XML_EXT.length() - 1);
					moduleCombo.add(projectRelativePath.toString());
					moduleCombo.setData(moduleName, file);
				}
				int i = modulesList.indexOf(selectedModule);
				if (i == -1)
					i = 0;
				moduleCombo.select(i);
				moduleText = moduleCombo.getText();
			} catch (CoreException e) {
				Activator.logException(e);
			}
		}
		doStatusUpdate();
	}

	@Override
	protected String constructCUContent(ICompilationUnit cu, String typeContent, String lineDelimiter) throws CoreException {

		if (Util.shouldUse1_5(selectedJavaProject.getProject()))
			typeContent = "@RemoteServiceRelativePath(\"" + serviceUri + "\")" + lineDelimiter + typeContent;
		return super.constructCUContent(cu, typeContent, lineDelimiter);
	}

	@Override
	protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor) throws CoreException {

		imports.addImport("com.google.gwt.core.client.GWT");
		imports.addImport("com.google.gwt.user.client.rpc.ServiceDefTarget");
		if (Util.shouldUse1_5(selectedJavaProject.getProject())) {
			imports.addImport("com.google.gwt.user.client.rpc.RemoteServiceRelativePath");
		} else {
			newType.createField("public static final String SERVICE_URI = \"" + serviceUri + "\";", null, true, monitor); //$NON-NLS-1$ //$NON-NLS-2$
		}
		newType.createType(getUtilClassContents(), null, true, monitor);
		super.createTypeMembers(newType, imports, monitor);
	}

	private String getUtilClassContents() {
		boolean shouldUse1_5 = Util.shouldUse1_5(selectedJavaProject.getProject());

		if (shouldUse1_5) {
			return "    public static class Util{\n\n" + //$NON-NLS-1$
					"      public static " + getTypeName() + "Async getInstance(){\n\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"            return GWT.create(" + getTypeName() + ".class);\n" + //$NON-NLS-1$ //$NON-NLS-2$ 
					"      }\n" + //$NON-NLS-1$
					"    }"; //$NON-NLS-1$
		} else {
			return "    public static class Util{\n\n" + //$NON-NLS-1$
					"      public static " + getTypeName() + "Async getInstance(){\n\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"            " + getTypeName() + "Async instance = (" + getTypeName() + "Async)GWT.create(" + getTypeName() + ".class);\n" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					"            ServiceDefTarget target = (ServiceDefTarget)instance;\n" + //$NON-NLS-1$
					"            target.setServiceEntryPoint(GWT.getModuleBaseURL()+SERVICE_URI);\n" + //$NON-NLS-1$
					"        	return instance;\n" + //$NON-NLS-1$
					"      }\n" + //$NON-NLS-1$
					"    }"; //$NON-NLS-1$
		}
	}

	// @Override
	@Override
	public void createType(IProgressMonitor monitor) throws CoreException, InterruptedException {

		monitor = Util.getNonNullMonitor(monitor);
		isImplCreation = false;
		super.createType(monitor);

		isImplCreation = true;

		try {
			if (shouldCreateImpl)
				createRemoteServiceImpl(new SubProgressMonitor(monitor, 1));
			addServletToWebXml(new SubProgressMonitor(monitor, 1));
			// addServletToGwtXml(new SubProgressMonitor(monitor, 1));
		} catch (Exception e) {
			Activator.logException(e);
			throw new CoreException(Util.getErrorStatus(e.getMessage()));
		}
	}

	// @Override
	@Override
	public IResource getModifiedResource() {
		try {
			return getCreatedType().getCompilationUnit().getCorrespondingResource();
		} catch (JavaModelException e) {
			return null;
		}
	}

	// @Override
	@Override
	public IPackageFragmentRoot getPackageFragmentRoot() {

		IPackageFragmentRoot root = null;
		if (!projectText.equals("")) {//$NON-NLS-1$

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

	// @Override
	@Override
	public IPackageFragment getPackageFragment() {

		IPackageFragment packageFragment = null;
		if (!projectText.equals("")) {//$NON-NLS-1$

			try {
				IJavaProject javaProject = JavaCore.create(Util.getProject(projectText));
				IPath moduleXmlPath = new Path(moduleText);
				IPath packageFragmentPath = new Path("/").append(projectText).append(moduleXmlPath.removeLastSegments(1));//$NON-NLS-1$
				if (isImplCreation)
					packageFragmentPath = packageFragmentPath.append("server");//$NON-NLS-1$
				else
					packageFragmentPath = packageFragmentPath.append("client");//$NON-NLS-1$

				IFolder folder = javaProject.getProject().getFolder(packageFragmentPath.removeFirstSegments(1));
				if (!folder.exists())
					folder.create(true, true, null);
				packageFragment = javaProject.findPackageFragment(packageFragmentPath);
			} catch (Exception e) {
				Activator.logException(e);
			}
		}

		return packageFragment;
	}

	public IPackageFragment getBasePackageFragment() {

		IPackageFragment packageFragment = null;
		if (!projectText.equals("")) {//$NON-NLS-1$

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

	// /**
	// * @param monitor
	// * @throws Exception
	// */
	// private void addServletToGwtXml(IProgressMonitor monitor) throws
	// Exception {
	//
	// monitor = Util.getNonNullMonitor(monitor);
	//
	// try {
	//
	// monitor.beginTask("", 2);
	//
	// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	//
	// DocumentBuilder builder = factory.newDocumentBuilder();
	//
	// File moduleFile = getModuleFile();
	//
	// Document document = builder.parse(moduleFile);
	// Node module = document.getDocumentElement();
	//
	// addServlet(document, module);
	//
	// Transformer writer = TransformerFactory.newInstance().newTransformer();
	//
	// writer.transform(new DOMSource(document), new StreamResult(moduleFile));
	//
	// monitor.worked(1);
	//
	// getModuleResource().refreshLocal(IResource.DEPTH_ONE, new
	// SubProgressMonitor(monitor, 1));
	//
	// } finally {
	// monitor.done();
	// }
	// }

	private File getModuleFile() throws CoreException {

		IResource resource = getModuleResource();
		return resource.getLocation().toFile();
	}

	private IResource getModuleResource() {
		return Util.getProject(projectText).findMember(moduleText);
	}

	private File getWebXmlFile() {
		IFile webXmlFile = Util.getWebXml(Util.getProject(projectText));
		return webXmlFile.getLocation().toFile();
	}

	private void createRemoteServiceImpl(IProgressMonitor monitor) throws IOException, CoreException {

		monitor = Util.getNonNullMonitor(monitor);

		try {

			monitor.beginTask("", 1);

			String typeName = getTypeName() + "Impl.java";
			IPath file = getPackageFragment().getResource().getProjectRelativePath().append(typeName);
			IFile remoteServiceImpl = Util.getProject(projectText).getFile(file);
			// modifiedResource = remoteServiceImpl;
			initTemplateVars(getTypeName(), "", getBasePackageFragment().getElementName());
			Util.writeFile("/RemoteService.ServiceImpl.template", remoteServiceImpl, templateVars);
		} finally {
			monitor.done();
		}
	}

	// @SuppressWarnings("unchecked")
	private void addServletToWebXml(IProgressMonitor monitor) throws Exception {

		monitor = Util.getNonNullMonitor(monitor);

		try {

			monitor.beginTask("", 2);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			DocumentBuilder builder = factory.newDocumentBuilder();

			File webXmlFile = getWebXmlFile();

			Document document = builder.parse(webXmlFile);

			Node webApp = document.getDocumentElement();

			addServlet(document, webApp);

			Transformer writer = TransformerFactory.newInstance().newTransformer();

			writer.transform(new DOMSource(document), new StreamResult(webXmlFile));

			monitor.worked(1);

			getModuleResource().refreshLocal(IResource.DEPTH_ONE, new SubProgressMonitor(monitor, 1));

		} finally {
			monitor.done();
		}

		monitor = Util.getNonNullMonitor(monitor);

		try {

			monitor.beginTask("", 1);

			// WebapplicationFactory factory = WebapplicationFactory.eINSTANCE;
			//
			// Servlet servlet = factory.createServlet();
			// servlet.setServletName(getTypeName());
			//
			// ServletType servletType = factory.createServletType();
			// servletType.setClassName(getPackageFragment().getElementName() +
			// '.' + getTypeName() + "Impl");
			// servlet.setWebType(servletType);
			//
			// IVirtualComponent component =
			// ComponentCore.createComponent(Util.getProject(projectText));
			// WebArtifactEdit artifactEdit =
			// WebArtifactEdit.getWebArtifactEditForWrite(component);
			// WebApp webApp = (WebApp) artifactEdit.getContentModelRoot();
			// webApp.getServlets().add(servlet);
			//
			// ServletMapping mapping =
			// WebapplicationFactory.eINSTANCE.createServletMapping();
			// mapping.setServlet(servlet);
			// mapping.setName(servlet.getServletName());
			// mapping.setUrlPattern("/" + serviceUri);
			// webApp.getServletMappings().add(mapping);
			//
			// artifactEdit.saveIfNecessary(monitor);
			//
			// artifactEdit.dispose();

		} finally {
			monitor.done();
		}

	}

	private void addServlet(Document document, Node webApp) {

		// <servlet>
		// <servlet-name>SomeNameHere</servlet-name>
		// <servlet-class>fully.qualified.name.of.TheServlet</servlet-class>
		// </servlet>

		Element servletName1 = document.createElement("servlet-name");
		servletName1.setTextContent(getTypeName());
		Element servletClass = document.createElement("servlet-class");
		servletClass.setTextContent(getPackageFragment().getElementName() + '.' + getTypeName() + "Impl");

		Element servlet = document.createElement("servlet");
		servlet.appendChild(servletName1);
		servlet.appendChild(servletClass);

		// <servlet-mapping>
		// <servlet-name>SomeNameHere</servlet-name>
		// <url-pattern>/module.name/serviceUri</url-pattern>
		// </servlet-mapping>

		Element servletName2 = document.createElement("servlet-name");
		servletName2.setTextContent(getTypeName());
		Element urlPattern = document.createElement("url-pattern");
		urlPattern.setTextContent(Util.getQualifiedName(getModuleResource()) + '/' + serviceUri);

		Element servletMapping = document.createElement("servlet-mapping");
		servletMapping.appendChild(servletName2);
		servletMapping.appendChild(urlPattern);

		webApp.appendChild(servlet);
		webApp.appendChild(servletMapping);

	}

	private void initTemplateVars(String serviceName, String serviceUri, String basePackage) {

		templateVars = new HashMap<String, String>();
		templateVars.put("@serviceName", serviceName);
		templateVars.put("@basePackage", basePackage);
		templateVars.put("@serviceUri", serviceUri);
	}

	// @Override
	@Override
	protected void handleFieldChanged(String fieldName) {

		super.handleFieldChanged(fieldName);
		doStatusUpdate();
	}

	protected void doStatusUpdate() {

		if (projectCombo != null) {
			projectStatus = projectText.trim().equals("") ? Util.getErrorStatus("Project cannot be empty") : in.cypal.studio.gwt.core.common.Util.okStatus; //$NON-NLS-2$
		}

		if (moduleCombo != null) {
			moduleStatus = moduleText.trim().equals("") ? Util.getErrorStatus("Module cannot be empty") : in.cypal.studio.gwt.core.common.Util.okStatus; //$NON-NLS-2$
		}

		if (serviceUriText != null) {
			serviceUriStatus = serviceUri.trim().equals("") ? Util.getErrorStatus("Service URI cannot be empty") : in.cypal.studio.gwt.core.common.Util.okStatus; //$NON-NLS-2$
		}

		IStatus[] status = new IStatus[] { projectStatus, moduleStatus, fTypeNameStatus, fSuperInterfacesStatus, serviceUriStatus };

		updateStatus(status);
	}

	@Override
	public void init(IStructuredSelection selection) {
		if (selection != null && selection instanceof IStructuredSelection) {
			Object firstElement = (selection).getFirstElement();
			if (firstElement instanceof IFile && Util.isModuleXml((IFile) firstElement)) {

				// if the selection is module xml, then our job is easy
				this.selectedModule = (IFile) firstElement;
				this.selectedProject = ((IFile) firstElement).getProject().getName();
			} else if (firstElement instanceof IResource) {

				// if its a resource, then find the appropriate project and
				// assign
				try {
					IProject project = ((IResource) firstElement).getProject();
					if (project.hasNature(Constants.NATURE_ID)) {
						this.selectedProject = project.getName();
						IJavaProject javaProject = JavaCore.create(project);
						List modulesList = Util.findModules(javaProject);
						if (modulesList.size() > 0) {
							this.selectedModule = (IFile) modulesList.get(0);
						}
					}
				} catch (CoreException e) {
					Activator.logException(e);
				}
			} else if (firstElement instanceof IJavaElement) {

				// its a JavaElement, then get the java project and assign
				try {
					IJavaProject javaProject = ((IJavaElement) firstElement).getJavaProject();
					this.selectedProject = javaProject.getProject().getName();
					List modulesList = Util.findModules(javaProject);
					if (modulesList.size() > 0) {
						this.selectedModule = (IFile) modulesList.get(0);
					}
				} catch (CoreException e) {
					Activator.logException(e);
				}
			}
		}
		super.init(selection);

	}

	/**
	 * @return the serviceUri
	 */
	public String getServiceUri() {
		return serviceUri;
	}

}
