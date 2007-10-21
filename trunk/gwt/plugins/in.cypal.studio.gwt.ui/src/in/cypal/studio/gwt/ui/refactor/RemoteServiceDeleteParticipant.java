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


import in.cypal.studio.gwt.ui.Activator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.DeleteParticipant;

/**
 * @author Prakash G.R.
 *
 */
public class RemoteServiceDeleteParticipant extends DeleteParticipant {

	
	private ICompilationUnit compilationUnit;

	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus(); // we are OK to delete
	}

	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return new DeleteRemoteServiceChange(compilationUnit);
	}

	public String getName() {
		return "Remote Service Delete Participant";
	}

	protected boolean initialize(Object element) {
		IJavaElement javaElement = JavaCore.create((IFile) element);
		if(!(javaElement instanceof ICompilationUnit))
			return false;
		
		boolean shouldParticipate = false;
		compilationUnit = (ICompilationUnit) javaElement;
		try {
			IType[] types = compilationUnit.getTypes();
			outer:
			for (int i = 0; i < types.length; i++) {
				
				if(!types[i].isInterface())
					continue; // we are interested only in interfaces
					
				String[] superInterfaceNames = types[i].getSuperInterfaceNames();
				for (int j = 0; j < superInterfaceNames.length; j++) {
					if(superInterfaceNames[j].endsWith("RemoteService")) { // ugly. Can be someother RemoteService also. Lets go for now
						shouldParticipate = true;
						break outer;
					}
				}
			}
		} catch (JavaModelException e) {
			Activator.logException(e);
		}
		
		return shouldParticipate;
	}

}
