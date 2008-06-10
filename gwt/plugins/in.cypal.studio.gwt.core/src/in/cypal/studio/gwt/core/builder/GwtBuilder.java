/*
 * Copyright 2006 - 2008 Cypal Solutions (tools@cypal.in)
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

package in.cypal.studio.gwt.core.builder;

import in.cypal.studio.gwt.core.Activator;
import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.core.common.GwtProject;
import in.cypal.studio.gwt.core.common.Preferences;
import in.cypal.studio.gwt.core.common.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Prakash G.R.
 * 
 */
public class GwtBuilder extends IncrementalProjectBuilder {

	GwtProject gwtProject;

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {

		monitor = Util.getNonNullMonitor(monitor);

		try {

			monitor.beginTask("Building GWT sources...", 2);

			gwtProject = GwtProject.create(getProject());

			// Do a update of Async files only if the preference is not set. If
			// set, its manual
			if (!Preferences.getBoolean(Constants.UPDATE_ASYNC_PREFERENCE, true))
				updateAsyncFiles(new SubProgressMonitor(monitor, 1));

			// Do a compile only in full build and preference is set.
			if (kind == IncrementalProjectBuilder.FULL_BUILD && Preferences.getBoolean(Constants.COMPILE_AT_FULLBUILD_PREFERENCE, true)) {
				gwtProject.doCompile();
			}

		} catch (Exception e) {
			Activator.logException(e);
			monitor.setCanceled(true);
		} finally {
			monitor.done();
		}

		return null;
	}

	private void updateAsyncFiles(IProgressMonitor monitor) throws CoreException, BadLocationException {

		monitor = Util.getNonNullMonitor(monitor);

		try {

			IResourceDelta delta = getDelta(getProject());
			List remoteServices = gwtProject.getRemoteServices(delta);
			monitor.beginTask("Updating Async files...", remoteServices.size());

			for (Iterator i = remoteServices.iterator(); i.hasNext();) {

				IFile aRemoteServiceFile = (IFile) i.next();

				IPackageFragment clientPackage = (IPackageFragment) JavaCore.create(aRemoteServiceFile.getParent());

				ICompilationUnit asyncContents = (ICompilationUnit) JavaCore.create(aRemoteServiceFile);
				String source = asyncContents.getBuffer().getContents();
				Document document = new Document(source);

				ASTParser parser = ASTParser.newParser(AST.JLS3);
				parser.setSource(asyncContents);
				CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
				AST ast = astRoot.getAST();

				astRoot.recordModifications();

				// Modify imports (+AsyncCallback, -RemoteService, -*Exception)
				List imports = astRoot.imports();
				List importsToBeRemoved = new ArrayList();

				for (Iterator j = imports.iterator(); j.hasNext();) {

					ImportDeclaration anImportDecl = (ImportDeclaration) j.next();
					String importName = anImportDecl.getName().getFullyQualifiedName();
					if (importName.endsWith("Exception") || //$NON-NLS-1$
							importName.equals("com.google.gwt.core.client.GWT") || //$NON-NLS-1$
							importName.equals("com.google.gwt.user.client.rpc.ServiceDefTarget") || //$NON-NLS-1$
							importName.equals("com.google.gwt.user.client.rpc.RemoteService")//$NON-NLS-1$
					)
						importsToBeRemoved.add(anImportDecl);
				}

				imports.removeAll(importsToBeRemoved);

				ImportDeclaration importDecl = ast.newImportDeclaration();
				importDecl.setName(ast.newName("com.google.gwt.user.client.rpc.AsyncCallback")); //$NON-NLS-1$
				astRoot.imports().add(importDecl);

				// Add Async to the name
				TypeDeclaration aRemoteService = (TypeDeclaration) astRoot.types().get(0);
				String remoteServiceAsyncName = aRemoteService.getName().getFullyQualifiedName() + "Async"; //$NON-NLS-1$
				aRemoteService.setName(astRoot.getAST().newSimpleName(remoteServiceAsyncName));

				// Remote all interfaces
				aRemoteService.superInterfaceTypes().clear();

				// Change methods, fields and inner classes
				List bodyDeclarations = aRemoteService.bodyDeclarations();
				List declarationsToDelete = new ArrayList();
				for (Iterator k = bodyDeclarations.iterator(); k.hasNext();) {

					Object currDeclaration = k.next();

					if (currDeclaration instanceof MethodDeclaration) {
						// Make return type void
						MethodDeclaration aMethod = (MethodDeclaration) currDeclaration;
						Type returnType = aMethod.getReturnType2();
						aMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

						// Add AsyncCallback parameter
						SingleVariableDeclaration asyncCallbackParam = ast.newSingleVariableDeclaration();
						asyncCallbackParam.setName(ast.newSimpleName("callback")); //$NON-NLS-1$
						ParameterizedType parameterizedType = createAsyncCallbackType(ast, returnType);
						asyncCallbackParam.setType(parameterizedType);
						aMethod.parameters().add(asyncCallbackParam);

						// Remove throws
						aMethod.thrownExceptions().clear();

						// Remove @gwt tags
						Javadoc jdoc = aMethod.getJavadoc();
						if (jdoc != null) {
							List tags = jdoc.tags();
							List tagsToRemove = new ArrayList();
							for (Iterator itTags = tags.iterator(); itTags.hasNext();) {
								TagElement tag = (TagElement) itTags.next();
								if (tag.toString().contains("@gwt")) {
									tagsToRemove.add(tag);
								}
							}
							tags.removeAll(tagsToRemove);
						}

					} else if (currDeclaration instanceof FieldDeclaration || currDeclaration instanceof TypeDeclaration) {

						// Remove the fields and inner classes
						declarationsToDelete.add(currDeclaration);
					}

				}

				bodyDeclarations.removeAll(declarationsToDelete);

				// computation of the text edits
				TextEdit edits = astRoot.rewrite(document, asyncContents.getJavaProject().getOptions(true));

				// computation of the new source code
				edits.apply(document);
				String newSource = document.get();

				// update of the compilation unit
				clientPackage.createCompilationUnit(remoteServiceAsyncName + ".java", newSource, true, monitor); //$NON-NLS-1$

				monitor.worked(1);
			}

		} finally {
			monitor.done();
		}
	}

	private ParameterizedType createAsyncCallbackType(AST ast, Type returnType) {

		ParameterizedType parameterizedType = ast.newParameterizedType(ast.newSimpleType(ast.newName("AsyncCallback"))); //$NON-NLS-1$
		Type type;

		if (!returnType.isPrimitiveType()) {
			type = returnType;
		} else {
			PrimitiveType primitiveType = (PrimitiveType) returnType;
			if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.BOOLEAN)) {
				type = ast.newSimpleType(ast.newName("Boolean"));
			} else if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.INT)) {
				type = ast.newSimpleType(ast.newName("Integer"));
			} else if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.BYTE)) {
				type = ast.newSimpleType(ast.newName("Byte"));
			} else if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.LONG)) {
				type = ast.newSimpleType(ast.newName("Long"));
			} else if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.FLOAT)) {
				type = ast.newSimpleType(ast.newName("Float"));
			} else if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.DOUBLE)) {
				type = ast.newSimpleType(ast.newName("Double"));
			} else if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.CHAR)) {
				type = ast.newSimpleType(ast.newName("Character"));
			} else {
				type = ast.newWildcardType(); // for void
			}
		}

		parameterizedType.typeArguments().add(type);

		return parameterizedType;
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {

		gwtProject = GwtProject.create(getProject());
		gwtProject.doClean(monitor);
	}

}