/*
 * Copyright 2007 Cypal Solutions (tools@cypal.in)
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

package in.cypal.studio.gwt.ui.refactor;

import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.core.common.Util;
import in.cypal.studio.gwt.ui.Activator;

import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
import org.eclipse.jst.j2ee.webapplication.Servlet;
import org.eclipse.jst.j2ee.webapplication.ServletMapping;
import org.eclipse.jst.j2ee.webapplication.ServletType;
import org.eclipse.jst.j2ee.webapplication.WebApp;
import org.eclipse.jst.j2ee.webapplication.WebType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Prakash G.R.
 *
 */
public class DeleteRemoteServiceChange extends Change {

	private final ICompilationUnit compilationUnit;

	public DeleteRemoteServiceChange(ICompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public Object getModifiedElement() {
		return compilationUnit.getResource();
	}

	public String getName() {
		return "Remote Service Delete Change";
	}

	public void initializeValidationData(IProgressMonitor pm) {

	}

	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	public Change perform(IProgressMonitor pm) throws CoreException {

		IPackageFragment clientPackage = (IPackageFragment) compilationUnit.getParent();
		ICompilationUnit asyncInterface = clientPackage.getCompilationUnit(getRemoteInterfaceName().concat("Async.java"));
		IResource asyncFile = asyncInterface.getResource();
		if (asyncFile.exists())
			asyncFile.delete(true, null);

		IPackageFragmentRoot sourceFolder = (IPackageFragmentRoot) clientPackage.getParent();
		IJavaElement[] subPackages = sourceFolder.getChildren();
		for (int i = 0; i < subPackages.length; i++) {
			
			IPackageFragment packageFragment = (IPackageFragment) subPackages[i];
			if (subPackages[i].getElementName().endsWith(Constants.SERVER_PACKAGE)) {
				ICompilationUnit implClass = packageFragment.getCompilationUnit(getRemoteInterfaceName().concat("Impl.java"));
				IResource implFile = implClass.getResource();
				if(implFile.exists()) {
					implFile.delete(true, null);
				}
			}
		}
		
		
		IFolder moduleFolder = (IFolder) sourceFolder.getCorrespondingResource();
		moduleFolder.accept(new IResourceVisitor() {

			public boolean visit(IResource resource) throws CoreException {

				if(Util.isModuleXml(resource)) {
					try {
						deleteFromGwtXml((IFile) resource, null);
					} catch (Exception e) {
						Activator.logException(e);
					}
				}
				return true;
			}
			
		});
		
		deleteFromWebXml(null);
		
		return null;
	}

	private String getRemoteInterfaceName() {
		return compilationUnit.getElementName().substring(0, compilationUnit.getElementName().length()-5);
	}
	
	private void deleteFromGwtXml(IFile moduleFile, IProgressMonitor monitor) throws Exception {

		monitor = Util.getNonNullMonitor(monitor);

		try {

			monitor.beginTask("", 1);  

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document document = builder.parse(moduleFile.getContents());
			Node module = document.getDocumentElement();
			
			NodeList childNodes = module.getChildNodes();
			for(int i=0; i< childNodes.getLength(); i++) {
				Node item = childNodes.item(i);
				if(item instanceof Element && ((Element)item).getTagName().equals("servlet")) {
					String servletClass = ((Element)item).getAttribute("class");
					if(servletClass.endsWith(getRemoteInterfaceName().concat("Impl"))) {
						module.removeChild(item);
					}
				}
			}

			Transformer writer = TransformerFactory.newInstance().newTransformer();

			writer.transform(new DOMSource(document), new StreamResult(moduleFile.getLocation().toFile()));


		} finally {
			monitor.done();
		}
	}
	
	private void deleteFromWebXml(IProgressMonitor monitor) {

		monitor = Util.getNonNullMonitor(monitor);

		try {

			monitor.beginTask("", 1);  

			IVirtualComponent component = ComponentCore.createComponent(compilationUnit.getJavaProject().getProject());
			WebArtifactEdit artifactEdit = WebArtifactEdit.getWebArtifactEditForWrite(component);
			WebApp webApp = (WebApp) artifactEdit.getContentModelRoot();
			EList servlets = webApp.getServlets();
			for (Iterator i = servlets.iterator(); i.hasNext();) {
				Servlet servlet = (Servlet) i.next();
				WebType webType = servlet.getWebType();
				if(webType instanceof ServletType && ((ServletType)webType).getClassName().endsWith(getRemoteInterfaceName().concat("Impl"))){
					ServletMapping servletMapping = webApp.getServletMapping(servlet);
					servlets.remove(servlet);
					webApp.getServletMappings().remove(servletMapping);
				}
			}
			
			artifactEdit.saveIfNecessary(monitor);

			artifactEdit.dispose();

		} finally {
			monitor.done();
		}


	}


}
