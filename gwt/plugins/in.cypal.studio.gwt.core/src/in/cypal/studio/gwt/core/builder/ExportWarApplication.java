/*
 * Copyright 2007 - 2008 Cypal Solutions (tools@cypal.in)
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentExportDataModelProperties;
import org.eclipse.jst.j2ee.internal.web.archive.operations.WebComponentExportDataModelProvider;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

/**
 * 
 * @author Prakash G.R.
 * 
 */
public class ExportWarApplication implements IApplication {

	private Integer status;
	private String destFile;
	private String projectName;

	public Object start(IApplicationContext context) throws Exception {

		String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		processArgs(args);

		if (status == IApplication.EXIT_OK) {

			System.out.println("Building project '" + projectName + "'...");
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			project.build(IncrementalProjectBuilder.FULL_BUILD, null);

			System.out.println("Exporting to WAR...");
			IDataModel dataModel = DataModelFactory.createDataModel(new WebComponentExportDataModelProvider());
			dataModel.setProperty(IJ2EEComponentExportDataModelProperties.PROJECT_NAME, projectName);
			dataModel.setProperty(IJ2EEComponentExportDataModelProperties.ARCHIVE_DESTINATION, destFile);

			dataModel.getDefaultOperation().execute(null, null);
			System.out.println("Done.");
		}
		return status;
	}

	/**
	 * 
	 * @param args
	 * 
	 */
	private void processArgs(String[] args) {

		status = IApplication.EXIT_OK;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-project")) {
				if (i == args.length) {
					System.err.println("Project name not specified");
					status = new Integer(400);
					break;
				}
				projectName = args[++i];
			} else if (args[i].equals("-dest")) {
				if (i == args.length) {
					System.err.println("Destination file not specified");
					status = new Integer(401);
					break;
				}
				destFile = args[++i];
			} else {
				System.out.println("Unknown command line option '" + args[i] + "' ignored.");
			}
		}

		if (projectName == null) {
			System.err.println("Project name not specified");
			status = new Integer(400);
		}

		if (destFile == null) {
			System.err.println("Destination file not specified");
			status = new Integer(401);
		}
	}

	public void stop() {
		;// do nothing
	}

}
