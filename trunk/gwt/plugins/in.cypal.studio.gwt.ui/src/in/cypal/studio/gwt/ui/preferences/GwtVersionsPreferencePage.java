package in.cypal.studio.gwt.ui.preferences;

import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.core.common.GwtRuntime;
import in.cypal.studio.gwt.ui.Activator;
import in.cypal.studio.gwt.ui.common.GwtLabelProvider;
import in.cypal.studio.gwt.ui.common.Util;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GwtVersionsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private CheckboxTableViewer tableViewer;
	private Button addButton;
	private Button deleteButton;
	private IWorkbench workbench;
	List<GwtRuntime> runtimes;

	public GwtVersionsPreferencePage() {
		setPreferenceStore(Util.getPreferenceStore());
		setDescription("Add or Remove available GWT versions. The checked version is set as the default one.");
		runtimes = new ArrayList<GwtRuntime>();
		runtimes.addAll(Arrays.asList(Util.getRuntimes()));
	}

	public void init(IWorkbench workbench) {
		this.workbench = workbench;
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label label = new Label(composite, SWT.NONE);
		label.setText("GWT Runtimes:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));

		tableViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.SINGLE);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new GwtLabelProvider());
		tableViewer.setInput(runtimes);
		GridData layoutData = new GridData(SWT.FILL, SWT.NONE, true, false, 1, 4);
		layoutData.heightHint = 100;
		tableViewer.getTable().setLayoutData(layoutData);

		addButton = new Button(composite, SWT.PUSH);
		addButton.setText("Add...");
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));

		deleteButton = new Button(composite, SWT.PUSH);
		deleteButton.setText("Delete");
		deleteButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		deleteButton.setEnabled(false);

		addListeners();
		updateTable(null);

		return composite;
	}

	private void addListeners() {
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				boolean isEmptySelection = event.getSelection().isEmpty();
				deleteButton.setEnabled(!isEmptySelection);
			}
		});

		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					GwtRuntime defaultRuntime = (GwtRuntime) event.getElement();
					defaultRuntime.setWorkspaceDefault(true);
					updateTable(defaultRuntime);
				}

			}
		});

		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doAdd();
			}
		});

		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				runtimes.remove(selection.getFirstElement());
				updateTable(null);
			}
		});
	}

	protected void doAdd() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage("Browse for the GWT installation:");
		String location = dialog.open();
		if (location != null) {
			File file = new File(location + File.separatorChar + Util.getGwtDevLibJarName());
			if (!file.exists()) {
				MessageDialog.openError(getShell(), "Invalid directory", "The directory doesn't contain '" + Util.getGwtDevLibJarName() + "' file.");
			} else {
				try {
					URLClassLoader classLoader = new URLClassLoader(new URL[] { file.toURI().toURL() }, this.getClass().getClassLoader());
					@SuppressWarnings("unchecked")
					Class aboutClass = classLoader.loadClass(Constants.GWT_ABOUT_CLASS);
					Field versionField = aboutClass.getDeclaredField("GWT_VERSION");
					String version = (String) versionField.get(aboutClass);
					GwtRuntime gwtRuntime = new GwtRuntime();
					gwtRuntime.setName(version);
					gwtRuntime.setLocation(location);
					runtimes.add(gwtRuntime);
					updateTable(null);
				} catch (MalformedURLException e) {
					Activator.logException(e);
					MessageDialog.openError(getShell(), "Error Occured", "Could not determine the GWT version.");
				} catch (Exception e) {
					Activator.logException(e);
					MessageDialog.openError(getShell(), "Error Occured", "The '" + Util.getGwtDevLibJarName() + "' file seems to be corrupted");
				}
			}
		}
	}

	private void updateTable(GwtRuntime defaultRuntime) {
		
		if (defaultRuntime == null) {

			// first search in the list of runtimes
			for (GwtRuntime gwtRuntime : runtimes) {
				if (gwtRuntime.isWorkspaceDefault()) {
					defaultRuntime = gwtRuntime;
					break;
				}
			}

			// if nothing set, then set the first one
			if (defaultRuntime == null && runtimes.size() > 0) {
				defaultRuntime = runtimes.get(0);
				defaultRuntime.setWorkspaceDefault(true);
			}

		}

		tableViewer.refresh();

		if (defaultRuntime != null) {
			// unset others
			for (GwtRuntime gwtRuntime : runtimes) {
				if (gwtRuntime != defaultRuntime)
					gwtRuntime.setWorkspaceDefault(false);
			}
			tableViewer.setCheckedElements(new Object[] { defaultRuntime });
		}

	}

	@Override
	public boolean performOk() {
		Util.setRuntimes(runtimes.toArray(new GwtRuntime[runtimes.size()]));
		return true;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
	}

}
