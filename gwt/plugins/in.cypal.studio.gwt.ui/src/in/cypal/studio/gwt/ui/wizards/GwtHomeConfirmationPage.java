/*
 * Copyright 2006 Cypal Solutions (tools@cypal.in)
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

package in.cypal.studio.gwt.ui.wizards;

import in.cypal.studio.gwt.core.common.Preferences;
import in.cypal.studio.gwt.ui.common.Constants;
import in.cypal.studio.gwt.ui.common.Util;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * 
 * @author Prakash G.R.
 *
 */
public class GwtHomeConfirmationPage extends WizardPage {

	public GwtHomeConfirmationPage() {
		super("GwtHomeConfirmationPage");
		setTitle("GWT Home");
		setDescription("Set the install location of GWT");
	}

	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		
		Label locationLabel = new Label(composite, SWT.NONE);
		locationLabel.setText("Location:");
		
		final Text locationText = new Text(composite, SWT.BORDER);
		locationText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		locationText.setText(Preferences.getString(Constants.GWT_HOME_PREFERENCE));
		locationText.setEditable(false);
		
		Button setButton = new Button(composite, SWT.PUSH);
		setButton.setText("Set...");
		setButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getContainer().getShell(), "mainPreferencePage", null, null);
				dialog.open();
				locationText.setText(Preferences.getString(Constants.GWT_HOME_PREFERENCE));
				updateStatus();
			}
		});
		
		setControl(composite);
		
		if(!Util.isGwtHomeSet())
			setPageComplete(false);
	}

	protected void updateStatus() {
		
		if(Util.isGwtHomeSet()){
			setMessage(null);
			setPageComplete(true);
		}else {
			setErrorMessage("The GWT Home is not valid");
			setPageComplete(false);
		}

	}

}
