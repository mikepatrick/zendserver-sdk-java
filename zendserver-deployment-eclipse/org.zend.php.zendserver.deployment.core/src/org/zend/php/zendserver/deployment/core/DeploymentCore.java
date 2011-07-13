package org.zend.php.zendserver.deployment.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.framework.BundleContext;
import org.zend.php.zendserver.deployment.core.sdk.SdkManager;

public class DeploymentCore extends Plugin {

	public static final String PLUGIN_ID = "org.zend.php.zendserver.deployment.core"; //$NON-NLS-1$

	private static final int INTERNAL_ERROR = 0;

	private static BundleContext context;

	private static DeploymentCore plugin;

	private SdkManager sdkManager;

	public DeploymentCore() {
		super();
		plugin = this;
	}

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		DeploymentCore.context = bundleContext;

		sdkManager = new SdkManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		DeploymentCore.context = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static DeploymentCore getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR,
				"ADP internal error", e)); //$NON-NLS-1$
	}

	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR, message, null));
	}

	public static IEclipsePreferences getPreferenceScope() {
		return DefaultScope.INSTANCE.getNode(DeploymentCore.PLUGIN_ID);
	}

	public void debug(Object message) {
		logErrorMessage(message.toString());
	}

	public void info(Object message) {
		logErrorMessage(message.toString());
	}

	public void warning(Object message) {
		logErrorMessage(message.toString());
	}

	public void error(Object message) {
		logErrorMessage(message.toString());
	}
}
