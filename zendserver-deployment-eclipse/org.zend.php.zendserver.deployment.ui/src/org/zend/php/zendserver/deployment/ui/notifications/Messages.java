package org.zend.php.zendserver.deployment.ui.notifications;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.zend.php.zendserver.deployment.ui.notifications.messages"; //$NON-NLS-1$
	public static String AddingLocalZendServerNotification_MoreAboutZendServer_LinkText;
	public static String LocalZendServerDetectedNotification_AddServer_Text;
	public static String LocalZendServerDetectedNotification_DoNotNotifyAgain_Text;
	public static String LocalZendServerDetectedNotification_LocalZendServerFound_Label;
	public static String LocalZendServerDetectedNotification_LocalZendServerFound_Message;
	public static String LocalZendServerDetectionDisabledNotification_DetectionDisabled_Label;
	public static String LocalZendServerDetectionDisabledNotification_DetectionDisabled_Message;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
