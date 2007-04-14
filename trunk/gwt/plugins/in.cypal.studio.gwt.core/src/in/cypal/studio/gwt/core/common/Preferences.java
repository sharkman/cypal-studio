package in.cypal.studio.gwt.core.common;

import in.cypal.studio.gwt.core.Activator;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

public final class Preferences {
	
	static final IPreferencesService preferencesService = Platform.getPreferencesService();
	static final IScopeContext[] contexts = new IScopeContext[] {new InstanceScope(), new DefaultScope()};

	public static String getString(String key, String defaultValue) {
		return preferencesService.getString(Activator.PLUGIN_ID, key, defaultValue, contexts);
	}
	
	public static boolean getBoolean(String key, boolean defaultValue) {
		return preferencesService.getBoolean(Activator.PLUGIN_ID, key, defaultValue, contexts);
	}

	public static String getString(String key) {
		return getString(key, "");
	}
	
	public static boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

}