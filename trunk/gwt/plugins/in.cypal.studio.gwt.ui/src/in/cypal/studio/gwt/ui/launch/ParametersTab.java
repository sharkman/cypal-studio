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

package in.cypal.studio.gwt.ui.launch;

import in.cypal.studio.gwt.ui.Activator;
import in.cypal.studio.gwt.ui.common.Constants;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Prakash G.R.
 * 
 */
public class ParametersTab extends AbstractLaunchConfigurationTab {

	// server
	Button useDefaultUrl;
	Label urlLabel;
	Text url;
	Button useEmbedddedServer;
	Button headless;
	Text port;
	Label portLabel;

	// log level
	Button logError;
	Button logWarn;
	Button logInfo;
	Button logTrace;
	Button logDebug;
	Button logSpam;

	// Button logAll;

	// style
	Button stylePretty;
	Button styleDetail;
	Button styleObfuscated;

	class Listener implements ModifyListener, SelectionListener {

		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			updateLaunchConfigurationDialog();
		}

		public void widgetSelected(SelectionEvent e) {
			if (e.getSource() == useEmbedddedServer) {
				port.setEnabled(useEmbedddedServer.getSelection());
				portLabel.setEnabled(useEmbedddedServer.getSelection());
			} else if (e.getSource() == useDefaultUrl) {
				url.setEnabled(!useDefaultUrl.getSelection());
				urlLabel.setEnabled(!useDefaultUrl.getSelection());
			}
			updateLaunchConfigurationDialog();
		}
	}

	Listener listener = new Listener();

	private Text vmOptions;

	private Text whitelistText;

	private Text blacklistText;

	public void createControl(Composite parent) {

		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		composite.setFont(font);

		createServerOptions(composite, font);
		createVerticalSpacer(composite, 1);
		createStyleOptions(composite, font);
		createVerticalSpacer(composite, 1);
		createLogLevelOptions(composite, font);
		createVMOptions(composite, font);

		setControl(composite);

	}

	/**
	 * @param composite
	 * @param font
	 */
	private void createVMOptions(Composite parent, Font font) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(composite, SWT.NONE);
		label.setText("VM Options:");

		vmOptions = new Text(composite, SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.NONE, true, false);
		data.grabExcessHorizontalSpace = true;
		vmOptions.setLayoutData(data);
		vmOptions.addModifyListener(listener);
	}

	private void createStyleOptions(Composite parent, Font font) {

		Group group = new Group(parent, SWT.NONE);
		group.setText("&Generated Javascript Style:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		group.setLayout(layout);
		group.setFont(font);

		stylePretty = new Button(group, SWT.RADIO);
		stylePretty.setText("Pretty"); //$NON-NLS-1$
		stylePretty.addSelectionListener(listener);

		styleDetail = new Button(group, SWT.RADIO);
		styleDetail.setText("Detail"); //$NON-NLS-1$
		styleDetail.addSelectionListener(listener);

		styleObfuscated = new Button(group, SWT.RADIO);
		styleObfuscated.setText("Obfuscated"); //$NON-NLS-1$
		styleObfuscated.addSelectionListener(listener);
	}

	private void createLogLevelOptions(Composite parent, Font font) {

		Group group = new Group(parent, SWT.NONE);
		group.setText("&Shell Options:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		group.setLayout(layout);
		group.setFont(font);

		logError = new Button(group, SWT.RADIO);
		logError.setText("Error          "); //$NON-NLS-1$
		logError.addSelectionListener(listener);

		logWarn = new Button(group, SWT.RADIO);
		logWarn.setText("Warn            "); //$NON-NLS-1$
		logWarn.addSelectionListener(listener);

		logInfo = new Button(group, SWT.RADIO);
		logInfo.setText("Info            "); //$NON-NLS-1$
		logInfo.addSelectionListener(listener);

		logTrace = new Button(group, SWT.RADIO);
		logTrace.setText("Trace          "); //$NON-NLS-1$
		logTrace.addSelectionListener(listener);

		logDebug = new Button(group, SWT.RADIO);
		logDebug.setText("Debug          "); //$NON-NLS-1$
		logDebug.addSelectionListener(listener);

		logSpam = new Button(group, SWT.RADIO);
		logSpam.setText("Spam            "); //$NON-NLS-1$
		logSpam.addSelectionListener(listener);

		headless = new Button(group, SWT.CHECK);
		headless.setText("Show shell window");
		headless.addSelectionListener(listener);
		headless.setEnabled(false);
	}

	private void createServerOptions(Composite parent, Font font) {

		Group group = new Group(parent, SWT.NONE);
		group.setText("Server Options:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setFont(font);

		useDefaultUrl = new Button(group, SWT.CHECK);
		useDefaultUrl.setText("Use &default URL for module"); //$NON-NLS-1$
		useDefaultUrl.addSelectionListener(listener);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		useDefaultUrl.setLayoutData(gridData);

		// new Label(group, SWT.NONE);

		urlLabel = new Label(group, SWT.NONE);
		urlLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		urlLabel.setText("&URL:"); //$NON-NLS-1$

		url = new Text(group, SWT.BORDER);
		url.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		url.setText(""); //$NON-NLS-1$

		useEmbedddedServer = new Button(group, SWT.CHECK);
		useEmbedddedServer.setText("Use &Embedded Tomcat Server");
		useEmbedddedServer.addSelectionListener(listener);
		useEmbedddedServer.setLayoutData(gridData);

		portLabel = new Label(group, SWT.NONE);
		portLabel.setText("&Port:");
		portLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));

		port = new Text(group, SWT.BORDER);
		port.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		port.addModifyListener(listener);

		Label whitelistLabel = new Label(group, SWT.NONE);
		whitelistLabel.setText("Whitelist URLs:");

		whitelistText = new Text(group, SWT.BORDER);
		whitelistText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		whitelistText.addFocusListener(getFocusListener());

		Label blacklistLabel = new Label(group, SWT.NONE);
		blacklistLabel.setText("Blacklist URLs:");

		blacklistText = new Text(group, SWT.BORDER);
		blacklistText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		blacklistText.addFocusListener(getFocusListener());

	}

	private FocusListener getFocusListener() {
		return new FocusAdapter() {

			public void focusGained(FocusEvent e) {
				Text widget = (Text) e.widget;
				if (widget.getText().equals(Constants.COMMA_SEPARATED_MESSAGE))
					widget.setText("");
			}

			public void focusLost(FocusEvent e) {
				Text widget = (Text) e.widget;
				if (widget.getText().trim().equals(""))
					widget.setText(Constants.COMMA_SEPARATED_MESSAGE);
			}
		};
	}

	public String getName() {
		return "Parameters";
	}

	public void initializeFrom(ILaunchConfiguration configuration) {

		try {

			useDefaultUrl.setSelection(configuration.getAttribute(Constants.LAUNCH_ATTR_USE_DEFAULT_URL, true));
			url.setText(configuration.getAttribute(Constants.LAUNCH_ATTR_URL, "")); //$NON-NLS-1$
			url.setEnabled(!useDefaultUrl.getSelection());
			useEmbedddedServer.setSelection(configuration.getAttribute(Constants.LAUNCH_ATTR_USE_EMBEDDED_SERVER, true));
			headless.setSelection(configuration.getAttribute(Constants.LAUNCH_ATTR_HEADLESS, false));
			port.setText(configuration.getAttribute(Constants.LAUNCH_ATTR_PORT, "8888")); //$NON-NLS-1$
			port.setEnabled(useEmbedddedServer.getSelection());
			vmOptions.setText(configuration.getAttribute(Constants.LAUNCH_ATTR_VMOPTIONS, ""));
			whitelistText.setText(configuration.getAttribute(Constants.LAUNCH_ATTR_WHITELIST, Constants.COMMA_SEPARATED_MESSAGE));
			blacklistText.setText(configuration.getAttribute(Constants.LAUNCH_ATTR_BLACKLIST, Constants.COMMA_SEPARATED_MESSAGE));
			int style = configuration.getAttribute(Constants.LAUNCH_ATTR_STYLE, 0);
			switch (style) {
			case 0:
				stylePretty.setSelection(true);
				break;
			case 1:
				styleDetail.setSelection(true);
				break;
			default:
				styleObfuscated.setSelection(true);
				break;
			}
			int logLevel = configuration.getAttribute(Constants.LAUNCH_ATTR_LOGLEVEL, 2);
			switch (logLevel) {
			case 0:
				logError.setSelection(true);
				break;
			case 1:
				logWarn.setSelection(true);
				break;
			case 2:
				logInfo.setSelection(true);
				break;
			case 3:
				logTrace.setSelection(true);
				break;
			case 4:
				logDebug.setSelection(true);
				break;
			default:
				logSpam.setSelection(true);
				break;
			}
			setDirty(false);

		} catch (Exception e) {
			Activator.logException(e);
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		int style;
		if (stylePretty.getSelection())
			style = 0;
		else if (styleDetail.getSelection())
			style = 1;
		else
			style = 2; // obfus

		int logLevel;

		if (logError.getSelection())
			logLevel = 0;
		else if (logWarn.getSelection())
			logLevel = 1;
		else if (logInfo.getSelection())
			logLevel = 2;
		else if (logTrace.getSelection())
			logLevel = 3;
		else if (logDebug.getSelection())
			logLevel = 4;
		else
			logLevel = 5; // spam - we ignore 'all'

		configuration.setAttribute(Constants.LAUNCH_ATTR_USE_DEFAULT_URL, useDefaultUrl.getSelection());
		configuration.setAttribute(Constants.LAUNCH_ATTR_URL, url.getText().trim());
		configuration.setAttribute(Constants.LAUNCH_ATTR_USE_EMBEDDED_SERVER, useEmbedddedServer.getSelection());
		configuration.setAttribute(Constants.LAUNCH_ATTR_WHITELIST, whitelistText.getText());
		configuration.setAttribute(Constants.LAUNCH_ATTR_BLACKLIST, blacklistText.getText());
		configuration.setAttribute(Constants.LAUNCH_ATTR_HEADLESS, headless.getSelection());
		configuration.setAttribute(Constants.LAUNCH_ATTR_LOGLEVEL, logLevel);
		configuration.setAttribute(Constants.LAUNCH_ATTR_PORT, port.getText().trim());
		configuration.setAttribute(Constants.LAUNCH_ATTR_STYLE, style);
		configuration.setAttribute(Constants.LAUNCH_ATTR_VMOPTIONS, vmOptions.getText().trim());

	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

		configuration.setAttribute(Constants.LAUNCH_ATTR_USE_DEFAULT_URL, true);
		configuration.setAttribute(Constants.LAUNCH_ATTR_URL, ""); //$NON-NLS-1$
		configuration.setAttribute(Constants.LAUNCH_ATTR_USE_EMBEDDED_SERVER, true);
		configuration.setAttribute(Constants.LAUNCH_ATTR_WHITELIST, Constants.COMMA_SEPARATED_MESSAGE);
		configuration.setAttribute(Constants.LAUNCH_ATTR_BLACKLIST, Constants.COMMA_SEPARATED_MESSAGE);
		configuration.setAttribute(Constants.LAUNCH_ATTR_HEADLESS, true);
		configuration.setAttribute(Constants.LAUNCH_ATTR_LOGLEVEL, 2);
		configuration.setAttribute(Constants.LAUNCH_ATTR_PORT, "8888"); //$NON-NLS-1$
		configuration.setAttribute(Constants.LAUNCH_ATTR_STYLE, 1);
		configuration.setAttribute(Constants.LAUNCH_ATTR_VMOPTIONS, "");
	}

	public boolean isValid(ILaunchConfiguration launchConfig) {

		setMessage(null);
		setErrorMessage(null);

		boolean isValid = true;
		String portText = port.getText().trim();
		try {
			Integer.parseInt(portText);
		} catch (NumberFormatException e) {
			setErrorMessage("Invalid port number");
			isValid = false;
		}

		return isValid;
	}

	public Image getImage() {
		return Activator.getImage("icons/parameters_tab.gif");
	}

}
