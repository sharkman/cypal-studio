package in.cypal.studio.gwt.core.nature;

import in.cypal.studio.gwt.core.common.Constants;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class GWTNature implements IProjectNature {

	private IProject project;

	public void configure() throws CoreException {
		
		IProjectDescription description = project.getDescription();
		ICommand[] oldBuilders = description.getBuildSpec();
		ICommand[] newBuilders = new ICommand[oldBuilders.length+1];
		System.arraycopy(oldBuilders, 0, newBuilders, 1, oldBuilders.length);
		newBuilders[0] = description.newCommand();
		newBuilders[0].setBuilderName(Constants.BUILDER_ID);
		description.setBuildSpec(newBuilders);

		project.setDescription(description, IResource.FORCE, null);


	}

	public void deconfigure() throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] oldBuilders = description.getBuildSpec();
		ICommand[] newBuilders = new ICommand[oldBuilders.length-1];
		int i=0;
		for (int j = 0; j < oldBuilders.length; j++) {
			ICommand aCommand = oldBuilders[j];
			if(!aCommand.getBuilderName().equals(Constants.BUILDER_ID))
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
