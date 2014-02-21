package org.zend.php.zendserver.deployment.ui.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.zend.php.zendserver.deployment.ui.preferences.messages"; //$NON-NLS-1$
	public static String OpenShiftPreferencesPage_0;
	public static String OpenShiftPreferencesPage_1;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
