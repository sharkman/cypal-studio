package in.cypal.studio.gwt.ui.common;

import in.cypal.studio.gwt.core.common.Constants;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class Util {

	private final static IPreferenceStore preferenceStore = new ScopedPreferenceStore(new InstanceScope(), Constants.PLUGIN_ID);
	
	public static IPreferenceStore getPreferenceStore() {
		return preferenceStore;
	}
	
	public static IProgressMonitor getNonNullMonitor(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void writeFile(String string, IFile moduleHtml,
			HashMap templateVars) {
		// TODO Auto-generated method stub
		
	}

}
