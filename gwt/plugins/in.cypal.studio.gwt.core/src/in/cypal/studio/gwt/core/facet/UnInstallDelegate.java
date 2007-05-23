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
package in.cypal.studio.gwt.core.facet;

import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.core.common.Util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;

/**
 * @author Prakash G.R.
 *
 */
public class UnInstallDelegate implements IDelegate {

	public void execute(IProject project, IProjectFacetVersion facetVersion, Object config, IProgressMonitor monitor) throws CoreException {
		
		monitor = Util.getNonNullMonitor(monitor);

		try {

			monitor.beginTask("", 1); //$NON-NLS-1$
			
			IProjectDescription description = project.getDescription();
			String[] prevNatures= description.getNatureIds();
			String[] newNatures= new String[prevNatures.length - 1];
			int i=0;
			for (int j = 0; j < prevNatures.length; j++) {
				String aNature = prevNatures[j];
				if(!aNature.equals(Constants.NATURE_ID))
					newNatures[i++]= aNature;
			}

			description.setNatureIds(newNatures);
			project.setDescription(description, IResource.FORCE, null);


		} finally {
			monitor.done();
		}
	}

}
