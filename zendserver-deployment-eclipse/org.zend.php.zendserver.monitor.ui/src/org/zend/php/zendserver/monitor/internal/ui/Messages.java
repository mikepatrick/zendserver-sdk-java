/*******************************************************************************
 * Copyright (c) 2012 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.php.zendserver.monitor.internal.ui;

import org.eclipse.osgi.util.NLS;

/**
 * User interface messages.
 * 
 * @author Wojciech Galanciak, 2012
 * 
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.zend.php.zendserver.monitor.internal.ui.messages"; //$NON-NLS-1$
	public static String EventBody_2;
	public static String EventBody_ApplicationLabel;
	public static String EventBody_CodetraceJobErrorMessage;
	public static String EventBody_CodetraceJobTitle;
	public static String EventBody_CodetraceLink;
	public static String EventBody_DoubleClickLabel;
	public static String EventBody_SourceLink;
	public static String EventDetailsDialog_ActionsLabel;
	public static String EventDetailsDialog_6;
	public static String EventDetailsDialog_Description;
	public static String EventDetailsDialog_DescriptionLabel;
	public static String EventDetailsDialog_JumpTooltip;
	public static String EventDetailsDialog_SourceLabel;
	public static String EventDetailsDialog_Title;
	public static String EventDetailsDialog_TraceTooltip;
	public static String EventDetailsDialog_TypeLabel;
	public static String OpenInEditorJob_0;
	public static String OpenInEditorJob_UnavailableMessage;
	public static String OpenInEditorJob_UnavailableTitle;
	public static String RequestGeneratorJob_MessageBody;
	public static String RequestGeneratorJob_MessageTitle;
	public static String RequestGeneratorJob_NotificationTitle;
	public static String RequestGeneratorJob_RepeatFailedMessage;
	public static String RequestGeneratorJob_RepeatTaskTitle;
	public static String ServerMonitoringPropertyPage_DelayLabel;
	public static String ServerMonitoringPropertyPage_DelayTooltip;
	public static String ServerMonitoringPropertyPage_Description;
	public static String ServerMonitoringPropertyPage_EnableCheckboxLabel;
	public static String ServerMonitoringPropertyPage_EnableJobTitle;
	public static String ServerMonitoringPropertyPage_HideLabel;
	public static String ServerMonitoringPropertyPage_InvalidDelayMessage;
	public static String ServerMonitoringPropertyPage_PreferencePageDescription;
	public static String TargetsMonitoringPreferencePage_PreferencePageDescription;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
