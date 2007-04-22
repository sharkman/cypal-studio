package in.cypal.studio.gwt.ui.common;

import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.ui.Activator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class Util extends in.cypal.studio.gwt.core.common.Util{

	public static final IStatus okStatus = Status.OK_STATUS;
	public static final IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Error", null);//$NON-NLS-1$
	public static IStatus getErrorStatus(String errorMessage) {
		return new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, errorMessage, null);
	}
	

	private final static IPreferenceStore preferenceStore = new ScopedPreferenceStore(new InstanceScope(), Constants.PLUGIN_ID);
	public static final String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$

	public static IPreferenceStore getPreferenceStore() {
		return preferenceStore;
	}
	
	public static IProgressMonitor getNonNullMonitor(IProgressMonitor monitor) {
		if(monitor == null)
			monitor = new NullProgressMonitor();
		return monitor;
	}

	public static void writeFile(String templateResource, IFile output, Map templateVars) throws IOException, CoreException {
		
		String contents = Util.getResourceContents(templateResource);
	
		for (Iterator i = templateVars.keySet().iterator(); i.hasNext();) {
			String aKey = (String) i.next();
			
			String value = ((String)templateVars.get(aKey)).replaceAll("\\\\", "\\\\\\\\");//$NON-NLS-1$ //$NON-NLS-2$ 
			contents = contents.replaceAll(aKey, value);
		}
	
		if(output.exists())
			output.setContents(new StringBufferInputStream(contents), true, false, null);
		else
			output.create(new StringBufferInputStream(contents), true, null);
	}

	public static String getResourceContents(String resourceName) throws IOException {
	
		InputStream inputStream = Util.class.getResourceAsStream(resourceName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	
		StringBuilder contents = new StringBuilder(5000);
		while(reader.ready()) {
			contents.append(reader.readLine()).append(Util.lineSeparator);
		}
		return contents.toString();
	}

}
