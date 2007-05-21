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
