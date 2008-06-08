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
package in.cypal.studio.gwt.core.nature;

import in.cypal.studio.gwt.core.common.Constants;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Prakash G.R.
 * 
 */
public class GWTNature implements IProjectNature {

	private IProject project;

	public void configure() throws CoreException {

		IProjectDescription description = project.getDescription();
		ICommand[] oldBuilders = description.getBuildSpec();
		ICommand[] newBuilders = new ICommand[oldBuilders.length + 1];
		System.arraycopy(oldBuilders, 0, newBuilders, 1, oldBuilders.length);
		newBuilders[0] = description.newCommand();
		newBuilders[0].setBuilderName(Constants.BUILDER_ID);
		description.setBuildSpec(newBuilders);

		project.setDescription(description, IResource.FORCE, null);

	}

	public void deconfigure() throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] oldBuilders = description.getBuildSpec();
		ICommand[] newBuilders = new ICommand[oldBuilders.length - 1];
		int i = 0;
		for (int j = 0; j < oldBuilders.length; j++) {
			ICommand aCommand = oldBuilders[j];
			if (!aCommand.getBuilderName().equals(Constants.BUILDER_ID))
				newBuilders[i++] = aCommand;
		}

		description.setBuildSpec(newBuilders);
		project.setDescription(description, IResource.FORCE, null);
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

}
