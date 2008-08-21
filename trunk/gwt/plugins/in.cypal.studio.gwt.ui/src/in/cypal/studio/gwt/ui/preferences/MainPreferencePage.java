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
package in.cypal.studio.gwt.ui.preferences;

import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.ui.Activator;
import in.cypal.studio.gwt.ui.common.Util;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Prakash G.R.
 * 
 */
public class MainPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	static final IPropertyChangeListener changeListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(Constants.GWT_HOME_PREFERENCE)) {
				IPath newGwtHome = new Path((String) event.getNewValue());
				try {
					JavaCore.setClasspathVariable(Constants.GWT_HOME_CPE, newGwtHome, new NullProgressMonitor());
					ResourcesPlugin.getWorkspace().getPathVariableManager().setValue(Constants.GWT_HOME_PATH, newGwtHome);
				} catch (Exception e) {
					Activator.logException(e);
				}
			}

			if (event.getProperty().equals(Constants.GWT_OUTPUT_PREFERENCE)) {
				MessageDialog.openInformation(null, "Cypal Studio for GWT", "Compiler output location is saved and will be reflected during next GWT Compilation");
			}

		}
	};

	public MainPreferencePage() {
		super(GRID);
		setDescription("Options for Cypal Studio for GWT");
		setPreferenceStore(Util.getPreferenceStore());

	}

	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(Constants.GWT_HOME_PREFERENCE, "GWT &Home:", getFieldEditorParent()));
		addField(new BooleanFieldEditor(Constants.UPDATE_ASYNC_PREFERENCE, "Manually manage Async files", getFieldEditorParent()));
		addField(new BooleanFieldEditor(Constants.DELETE_SERVICE_PREFERENCE, "When deleting a RemoteService, delete associated Async, Impl files and remove entries from gwt.xml and web.xml", getFieldEditorParent()));
		addField(new StringFieldEditor(Constants.DEFAULT_VM_OPTION_PREFERENCE, "Default &VM Options:", getFieldEditorParent()));

		Group group = new Group(getFieldEditorParent(), SWT.NONE);
		group.setText("GWT Compiler Options:");
		group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 3, 1));
		addField(new StringFieldEditor(Constants.GWT_OUTPUT_PREFERENCE, "&Output Folder:", group));
		addField(new BooleanFieldEditor(Constants.COMPILE_AT_FULLBUILD_PREFERENCE, "Invoke on Clean &Build", group));
		addField(new BooleanFieldEditor(Constants.COMPILE_AT_PUBLISH_PREFERENCE, "Invoke when publishing to an &external server", group));

	}

	public void init(IWorkbench workbench) {
		getPreferenceStore().addPropertyChangeListener(changeListener);
	}

	public void dispose() {
		super.dispose();
		getPreferenceStore().removePropertyChangeListener(changeListener);

	}

}
