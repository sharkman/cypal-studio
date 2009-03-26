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

package in.cypal.studio.gwt.ui.common;

import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.core.common.GwtRuntime;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * @author Prakash G.R.
 * 
 */

public class GwtRuntimeComposite {

	private final Button workspaceDefaultButton;
	private final Button projectSpecificButton;
	private final Combo versionCombo;
	private final Link configureVersionsLink;
	private String version;
	private IStatus status;
	private final IStatusListener statusListener;

	public GwtRuntimeComposite(Composite composite) {
		this(composite, null);
	}

	public GwtRuntimeComposite(Composite composite, IStatusListener statusListener) {

		this.statusListener = statusListener;

		Group group = new Group(composite, SWT.None);
		group.setText("GWT Version");
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));

		projectSpecificButton = new Button(group, SWT.RADIO);
		projectSpecificButton.setText("Use a project specific version:");

		versionCombo = new Combo(group, SWT.READ_ONLY);
		versionCombo.setEnabled(false);

		workspaceDefaultButton = new Button(group, SWT.RADIO);
		workspaceDefaultButton.setText("Use the default version:");
		workspaceDefaultButton.setSelection(true);

		configureVersionsLink = new Link(group, SWT.NONE);
		configureVersionsLink.setText("<a>Configure versions...</a>");
		configureVersionsLink.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false));

		initData();
		addListeners();
	}

	private void initData() {

		version = Constants.GWT_RUNTIME_WORKSPACE_DEFAULT;

		GwtRuntime[] runtimes = Util.getRuntimes();
		for (int i = 0; i < runtimes.length; i++) {
			versionCombo.add(runtimes[i].getName());
			if (runtimes[i].isWorkspaceDefault())
				versionCombo.select(i);
		}

	}

	private void addListeners() {
		versionCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		SelectionAdapter selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		};

		projectSpecificButton.addSelectionListener(selectionListener);
		workspaceDefaultButton.addSelectionListener(selectionListener);

		configureVersionsLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(new SameShellProvider(versionCombo).getShell(), "gwtVersionsPreferencePage", new String[] { "gwtVersionsPreferencePage", "mainPreferencePage" }, null);
				if (dialog.open() == Window.OK) {
					versionCombo.removeAll();
					initData();
					update();
				}

			}
		});
	}

	private void update() {
		boolean useDefault = workspaceDefaultButton.getSelection();
		versionCombo.setEnabled(!useDefault);
		version = useDefault ? Constants.GWT_RUNTIME_WORKSPACE_DEFAULT : versionCombo.getText();
		if (version.length() == 0)
			status = Util.getErrorStatus("No GWT Runtimes have been configured");
		else
			status = Status.OK_STATUS;
		
		if(statusListener != null)
			statusListener.updateStatus(status);
	}

	public void setVersion(String version) {
		this.version = version;
		if (version.equals(Constants.GWT_RUNTIME_WORKSPACE_DEFAULT)) {
			workspaceDefaultButton.setSelection(true);
			projectSpecificButton.setSelection(false);
			versionCombo.setEnabled(false);
		} else {
			workspaceDefaultButton.setSelection(false);
			projectSpecificButton.setSelection(true);
			versionCombo.setEnabled(true);
			versionCombo.setText(version);
		}
	}

	public String getVersion() {
		return version;
	}

	public IStatus getStatus() {
		return status;
	}

	public IClasspathEntry getClasspathEntry() {
		IPath containerPath = new Path(Constants.GWT_RUNTIME_ID).append(version);
		return JavaCore.newContainerEntry(containerPath);
	}

	public interface IStatusListener {
		public void updateStatus(IStatus newStatus);
	}
}
