/*
 * Copyright 2009 Cypal Solutions (tools@cypal.in)
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
package in.cypal.studio.gwt.core.common;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class GwtRuntimeClasspathContainerInitializer extends ClasspathContainerInitializer {

	public GwtRuntimeClasspathContainerInitializer() {
		super();
	}
	@Override
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project }, new IClasspathContainer[] { new GwtLibraryContainer(containerPath) }, null);
	}

	private static class GwtLibraryContainer implements IClasspathContainer {

		private final IClasspathEntry[] entries;
		private final IPath path;

		public GwtLibraryContainer(IPath path) {
			this.path = path;
			entries = createEntries();
		}

		private IClasspathEntry[] createEntries() {

			IClasspathEntry[] entries = new IClasspathEntry[0];

			String lastSegment = path.lastSegment();
			boolean useWorkspaceDefault = lastSegment.equals(Constants.GWT_RUNTIME_WORKSPACE_DEFAULT);
			for (GwtRuntime gwtRuntime : Util.getRuntimes()) {
				if ((useWorkspaceDefault && gwtRuntime.isWorkspaceDefault()) || gwtRuntime.getName().equals(lastSegment)) {
					entries = new IClasspathEntry[1];
					Path location = new Path(gwtRuntime.getLocation());
					// entries[0] =
					// JavaCore.newLibraryEntry(location.append(Util.getGwtServletJarName()),
					// null, null);
					entries[0] = JavaCore.newLibraryEntry(location.append(Util.getGwtUserLibJarName()), null, null);
					// entries[1] =
					// JavaCore.newLibraryEntry(location.append(Util.getGwtDevLibJarName()),
					// null, null);
					break;
				}
			}
			return entries;
		}

		public IClasspathEntry[] getClasspathEntries() {
			return entries;
		}

		public String getDescription() {
			return "GWT";
		}

		public int getKind() {
			return IClasspathContainer.K_APPLICATION;
		}

		public IPath getPath() {
			return path;
		}

	}

}
