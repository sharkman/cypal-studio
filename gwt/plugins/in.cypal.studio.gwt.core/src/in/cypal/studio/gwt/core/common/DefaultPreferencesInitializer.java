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
package in.cypal.studio.gwt.core.common;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * @author Prakash G.R.
 * 
 */
public class DefaultPreferencesInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {

		IEclipsePreferences node = new DefaultScope().getNode(Constants.PLUGIN_ID);
		node.put(Constants.GWT_HOME_PREFERENCE, "C:\\Program Files\\gwt"); //$NON-NLS-1$
		node.put(Constants.GWT_OUTPUT_PREFERENCE, "build/gwtOutput"); //$NON-NLS-1$
		node.put(Constants.COMPILE_AT_FULLBUILD_PREFERENCE, Boolean.toString(true));
		node.put(Constants.COMPILE_AT_PUBLISH_PREFERENCE, Boolean.toString(false));
		node.put(Constants.UPDATE_ASYNC_PREFERENCE, Boolean.toString(false));

	}

}
