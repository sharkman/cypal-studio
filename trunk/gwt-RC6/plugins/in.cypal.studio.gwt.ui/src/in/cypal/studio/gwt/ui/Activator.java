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
package in.cypal.studio.gwt.ui;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Prakash G.R.
 * 
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "in.cypal.studio.gwt.ui";

	// The shared instance
	private static Activator plugin;

	public static final String FILE_ICON = "icons/file.gif"; //$NON-NLS-1$
	public static final String GWT_ICON = "icons/gwt_icon16.png"; //$NON-NLS-1$
	public static final String PARAMETERS_ICON = "icons/parameters_tab.gif"; //$NON-NLS-1$

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;

		ImageRegistry registry = getImageRegistry();
		registry.put(FILE_ICON, getImageDescriptor(FILE_ICON));
		registry.put(GWT_ICON, getImageDescriptor(GWT_ICON));
		registry.put(PARAMETERS_ICON, getImageDescriptor(PARAMETERS_ICON));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Logs the exception to the default logger of the plugin
	 * 
	 * @param e
	 *            Exception to be logged
	 */
	public static void logException(final Throwable e) {

		final ILog log = getDefault().getLog();
		final String msg = "Encountered an unexpected exception.";
		log.log(new Status(IStatus.ERROR, PLUGIN_ID, -1, msg, e));
	}

	public static void logInfo(String message) {

		final ILog log = getDefault().getLog();
		log.log(new Status(IStatus.INFO, PLUGIN_ID, IStatus.OK, message, null));
	}

	public static void logError(String message) {

		final ILog log = getDefault().getLog();
		log.log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, null));
	}

	public static void logWarning(String message) {

		final ILog log = getDefault().getLog();
		log.log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.OK, message, null));
	}

	public static Image getImage(String which) {
		return getDefault().getImageRegistry().get(which);
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * This would log the exception and then display an error dialog to the user
	 * 
	 * @param e
	 *            the exception to be handled
	 */
	public static void handleException(Exception e) {
		handleException(e, null, null, null);
	}

	/**
	 * This would log the exception and then display an error dialog to the user
	 * 
	 * @param e
	 *            the exception to be handled
	 * @param shell
	 *            the shell where the error dialog box has to be shown. Can be
	 *            <code>null</code>
	 * @param title
	 *            the title of the dialog box. Can be <code>null</code>
	 * @param message
	 *            the error message. If this is empty, then the exception
	 *            message will be displayed
	 */
	public static void handleException(final Exception e, final Shell shell, final String title, String message) {

		logException(e);

		if (message == null) {
			if (e.getMessage() == null || e.getMessage().length() == 0) {
				message = "See Error Log for more details";
			} else {
				message = e.getMessage();
			}
		}
		final String errorDetail = message;

		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			public void run() {
				MessageDialog.openError(shell, title, errorDetail);
			}
		});
	}

}
