///*
// * Copyright 2007 - 2008 Cypal Solutions (tools@cypal.in)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// *     http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// */
//
//package in.cypal.studio.gwt.ui.refactor;
//
//import in.cypal.studio.gwt.core.common.Constants;
//import in.cypal.studio.gwt.core.common.Util;
//import in.cypal.studio.gwt.ui.Activator;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;
//
//import org.eclipse.core.resources.IFile;
//import org.eclipse.core.resources.IProject;
//import org.eclipse.core.resources.IResource;
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.OperationCanceledException;
//import org.eclipse.core.runtime.SubProgressMonitor;
//import org.eclipse.jdt.core.ICompilationUnit;
//import org.eclipse.jdt.core.IJavaElement;
//import org.eclipse.jdt.core.IPackageFragment;
//import org.eclipse.jdt.core.IPackageFragmentRoot;
//import org.eclipse.ltk.core.refactoring.Change;
//import org.eclipse.ltk.core.refactoring.RefactoringStatus;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.w3c.dom.Text;
//
///**
// * @author Prakash G.R.
// * 
// */
//public class DeleteRemoteServiceChange extends Change {
//
//	private final ICompilationUnit compilationUnit;
//
//	public DeleteRemoteServiceChange(ICompilationUnit compilationUnit) {
//		this.compilationUnit = compilationUnit;
//	}
//
//	@Override
//	public Object getModifiedElement() {
//		return compilationUnit.getResource();
//	}
//
//	@Override
//	public String getName() {
//		return "Remote Service Delete Change";
//	}
//
//	@Override
//	public void initializeValidationData(IProgressMonitor pm) {
//
//	}
//
//	@Override
//	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
//		return new RefactoringStatus();
//	}
//
//	@Override
//	public Change perform(IProgressMonitor pm) throws CoreException {
//
//		IPackageFragment clientPackage = (IPackageFragment) compilationUnit.getParent();
//		ICompilationUnit asyncInterface = clientPackage.getCompilationUnit(getRemoteInterfaceName().concat("Async.java"));
//		IResource asyncFile = asyncInterface.getResource();
//		// if (asyncFile.exists())
//		// asyncFile.delete(true, null);
//
//		IPackageFragmentRoot sourceFolder = (IPackageFragmentRoot) clientPackage.getParent();
//		IJavaElement[] subPackages = sourceFolder.getChildren();
//		for (int i = 0; i < subPackages.length; i++) {
//
//			IPackageFragment packageFragment = (IPackageFragment) subPackages[i];
//			if (subPackages[i].getElementName().endsWith(Constants.SERVER_PACKAGE)) {
//				ICompilationUnit implClass = packageFragment.getCompilationUnit(getRemoteInterfaceName().concat("Impl.java"));
//				IResource implFile = implClass.getResource();
//				// if (implFile.exists()) {
//				// implFile.delete(true, null);
//				// }
//			}
//		}
//
//		
////		IFolder moduleFolder = (IFolder) sourceFolder.getCorrespondingResource();
////		moduleFolder.accept(new IResourceVisitor() {
////
////			public boolean visit(IResource resource) throws CoreException {
////
////				if (Util.isModuleXml(resource)) {
////					try {
////						deleteFromGwtXml((IFile) resource, null);
////					} catch (Exception e) {
////						Activator.logException(e);
////					}
////				}
////				return true;
////			}
////
////		});
//
//		IProject project = sourceFolder.getJavaProject().getProject();
//		try {
//			deleteFromWebXml(project, null);
//		} catch (Exception e) {
//			Activator.logException(e);
//		}
//
//		return null;
//	}
//
//	private String getRemoteInterfaceName() {
//		return compilationUnit.getElementName().substring(0, compilationUnit.getElementName().length() - 5);
//	}
//
//	private void deleteFromWebXml(IProject project, IProgressMonitor monitor) throws Exception {
//
//		 monitor = Util.getNonNullMonitor(monitor);
//		
//		 try {
//		
//			IFile webXml = Util.getWebXml(project);
//
//			monitor.beginTask("", 2);
//
//			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//
//			DocumentBuilder builder = factory.newDocumentBuilder();
//
//			Document document = builder.parse(webXml.getContents());
//			Element webapp = document.getDocumentElement();
//
//			// <servlet>
//			// <servlet-name>SomeNameHere</servlet-name>
//			// <servlet-class>fully.qualified.name.of.TheServlet</servlet-class>
//			// </servlet>
//			String servletNameText = null;
//			NodeList servlets = webapp.getElementsByTagName("servlet");
//			outer:
//			for (int i = 0; i < servlets.getLength(); i++) {
//				Element aServlet = (Element) servlets.item(i);
//				NodeList childNodes = aServlet.getChildNodes();
//				for (int j = 0; j < childNodes.getLength(); j++) {
//					Node aNode = childNodes.item(j);
//
//				}
//				Node servletName;
//				String nodeValue = servletClass.getNodeName();
//				if (servletClass instanceof Text) {
//					nodeValue = ((Text) servletClass).getTextContent();
//				}
//				if (!nodeValue.equals("servlet-class")) {
//					servletName = servletClass;
//					servletClass = aServlet.getFirstChild();
//				} else {
//					servletName = aServlet.getFirstChild();
//				}
//				String textContent = servletClass.getTextContent();
//				if (textContent.endsWith(getRemoteInterfaceName().concat("Impl"))) {
//					servletNameText = servletName.getTextContent();
//					webapp.removeChild(aServlet);
//					break;
//				}
//			}
//
//			// <servlet-mapping>
//			// <servlet-name>SomeNameHere</servlet-name>
//			// <url-pattern>/module.name/serviceUri</url-pattern>
//			// </servlet-mapping>
//
//			NodeList servletMappings = webapp.getElementsByTagName("servlet-mapping");
//			for (int i = 0; i < servletMappings.getLength(); i++) {
//				Node aServletMapping = servlets.item(i);
//				Node servletName = aServletMapping.getLastChild();
//				if (!servletName.getNodeName().equals("servlet-name")) {
//					servletName = aServletMapping.getFirstChild();
//				}
//				if (servletName.getTextContent().equals(servletNameText)) {
//					webapp.removeChild(aServletMapping);
//					break;
//				}
//			}
//			Transformer writer = TransformerFactory.newInstance().newTransformer();
//
//			writer.transform(new DOMSource(document), new StreamResult(webXml.getLocation().toFile()));
//			monitor.worked(1);
//
//			webXml.refreshLocal(IResource.DEPTH_ONE, new SubProgressMonitor(monitor, 1));
//
//		} finally {
//			monitor.done();
//		}
//
//	}
//
// }
