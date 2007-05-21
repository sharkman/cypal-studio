package in.cypal.studio.gwt.core.common;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;


public class DefaultPreferencesInitializer extends AbstractPreferenceInitializer{

	public void initializeDefaultPreferences() {
	
		IEclipsePreferences node = new DefaultScope().getNode(Constants.PLUGIN_ID);
		node.put(Constants.GWT_HOME_PREFERENCE, "C:\\Program Files\\gwt"); //$NON-NLS-1$
		node.put(Constants.GWT_HOME_PREFERENCE, "C:\\Program Files\\gwt"); //$NON-NLS-1$
		node.put(Constants.COMPILE_AT_FULLBUILD_PREFERENCE, Boolean.toString(true));
		node.put(Constants.COMPILE_AT_PUBLISH_PREFERENCE, Boolean.toString(false));
		node.put(Constants.UPDATE_ASYNC_PREFERENCE, Boolean.toString(false));
		
	}

}
