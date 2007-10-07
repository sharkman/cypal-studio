/*
 * Copyright 2006 Cypal Solutions (tools@cypal.in)
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

import in.cypal.studio.gwt.core.common.GwtProject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.model.PublishOperation;

/**
 * @author Prakash G.R.
 * 
 */
public class GwtCompileOperation extends PublishOperation {

	private IProject project;

	public GwtCompileOperation(IProject project) {
		this.project = project;
	}

	public void execute(IProgressMonitor monitor, IAdaptable info) throws CoreException {
		
		GwtProject gwtProject = GwtProject.create(project);
		gwtProject.doCompile();
	}
	public int getOrder() {
		return -1;
	}
	
	public int getKind() {
		return REQUIRED;
	}

}
