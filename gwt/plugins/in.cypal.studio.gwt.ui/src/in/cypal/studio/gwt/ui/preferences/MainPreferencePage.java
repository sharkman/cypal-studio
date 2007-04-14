package in.cypal.studio.gwt.ui.preferences;

import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.ui.Activator;
import in.cypal.studio.gwt.ui.common.Util;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class MainPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{

	static final IPropertyChangeListener changeListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			if(event.getProperty().equals(Constants.GWT_HOME_PREFERENCE)){
				IPath newGwtHome = new Path((String)event.getNewValue());
				try {
					JavaCore.setClasspathVariable(Constants.GWT_HOME_CPE, newGwtHome, new NullProgressMonitor());
					ResourcesPlugin.getWorkspace().getPathVariableManager().setValue(Constants.GWT_HOME_PATH, newGwtHome);
				} catch (Exception e) {
					Activator.logException(e);
				}
			}
		}
	};
		
	
	public MainPreferencePage() {
		super(GRID);
		setDescription("Options for GWT Pro");
		setPreferenceStore(Util.getPreferenceStore());
		
	}

	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(Constants.GWT_HOME_PREFERENCE, "GWT Home:", getFieldEditorParent()));
		addField(new BooleanFieldEditor(Constants.UPDATE_ASYNC_PREFERENCE, "Manually manage Async files", getFieldEditorParent()));
		addField(new BooleanFieldEditor(Constants.COMPILE_AT_FULLBUILD_PREFERENCE, "Invoke GWT Compiler on Clean &Build", getFieldEditorParent()));
		addField(new BooleanFieldEditor(Constants.COMPILE_AT_PUBLISH_PREFERENCE, "Invoke GWT Compiler when publishing to an &external server", getFieldEditorParent())); 
	}

	public void init(IWorkbench workbench) {
		getPreferenceStore().addPropertyChangeListener(changeListener);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		getPreferenceStore().removePropertyChangeListener(changeListener);
		
	}
	
	
}
