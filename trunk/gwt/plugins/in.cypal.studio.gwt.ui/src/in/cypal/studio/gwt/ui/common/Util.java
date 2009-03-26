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
package in.cypal.studio.gwt.ui.common;

import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.core.common.GwtRuntime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * @author Prakash G.R.
 * 
 */
public class Util extends in.cypal.studio.gwt.core.common.Util {

	private final static IPreferenceStore preferenceStore = new ScopedPreferenceStore(new InstanceScope(), Constants.PLUGIN_ID);
	public static final String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$

	public static IPreferenceStore getPreferenceStore() {
		return preferenceStore;
	}

	public static IProgressMonitor getNonNullMonitor(IProgressMonitor monitor) {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		return monitor;
	}

	public static void writeFile(String templateResource, IFile output, Map templateVars) throws IOException, CoreException {

		String contents = Util.getResourceContents(templateResource);

		for (Iterator i = templateVars.keySet().iterator(); i.hasNext();) {
			String aKey = (String) i.next();

			String value = ((String) templateVars.get(aKey)).replaceAll("\\\\", "\\\\\\\\");//$NON-NLS-1$ //$NON-NLS-2$ 
			contents = contents.replaceAll(aKey, value);
		}

		if (output.exists())
			output.setContents(new StringBufferInputStream(contents), true, false, null);
		else
			output.create(new StringBufferInputStream(contents), true, null);
	}

	public static String getResourceContents(String resourceName) throws IOException {

		InputStream inputStream = Util.class.getResourceAsStream(resourceName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		StringBuilder contents = new StringBuilder(5000);
		while (reader.ready()) {
			contents.append(reader.readLine()).append(Util.lineSeparator);
		}
		return contents.toString();
	}

	public static void setRuntimes(GwtRuntime[] runtimes) {
		preferenceStore.setValue(Constants.GWT_RUNTIME_COUNT, runtimes.length);
		for (int i = 0; i < runtimes.length; i++) {
			preferenceStore.setValue(Constants.GWT_RUNTIME_NAME + i, runtimes[i].getName());
			preferenceStore.setValue(Constants.GWT_RUNTIME_LOCATION + i, runtimes[i].getLocation());
			preferenceStore.setValue(Constants.GWT_RUNTIME_DEFAULT + i, runtimes[i].isWorkspaceDefault());
		}
	}
}
