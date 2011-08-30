/*******************************************************************************
 * Copyright (c) 2011 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.php.zendserver.deployment.debug.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.zend.php.zendserver.deployment.debug.ui.messages"; //$NON-NLS-1$

	public static String deploymentWizard_LaunchTitle;
	public static String deploymentWizard_DeployTitle;
	public static String deploymentWizard_Message;

	public static String runContribution_LaunchingPHPApp;
	public static String debugContribution_LaunchingPHPApp;
	public static String deployContribution_DeployPHPApp;

	public static String deploymentTab_Title;

	public static String configurationPage_Description;
	public static String configurationPage_Name;
	public static String configurationPage_Title;
	public static String configurationPage_AddTarget;
	public static String configurationPage_DeployTo;
	public static String configurationPage_DeployToTooltip;
	public static String configurationPage_baseURL;
	public static String configurationPage_appUserName;
	public static String configurationPage_appUserNameTooltip;
	public static String configurationPage_ignoreFailures;
	public static String configurationPage_ignoreFailuresTooltip;
	public static String configurationPage_ValidationError_TargetLocation;
	public static String configurationPage_ValidationError_BaseUrl;

	public static String parametersPage_Description;
	public static String parametersPage_Name;
	public static String parametersPage_Title;
	public static String parametersPage_applicationParams;
	public static String parametersPage_ValidationError_ParamRequired;

	public static String updateExistingApplicationDialog_Title;
	public static String updateExistingApplicationDialog_Message;
	public static String updateExistingApplicationDialog_yesButton;
	public static String updateExistingApplicationDialog_noButton;

	public static String advancedSection_Label;
	public static String advancedSection_Deploy;
	public static String advancedSection_Update;
	public static String advancedSection_AutoDeploy;
	public static String advancedSection_NoAction;
	public static String advancedSection_autoDeployComboLabel;
	public static String advancedSection_autoDeployComboTooltip;
	public static String advancedSection_Title;
	public static String advancedSection_updateComboLabel;
	public static String advancedSection_updateComboTooltip;

	public static String applicationConflictDialog_Message;

	public static String applicationConflictDialog_Title;

	public static String DeploymentParameters_Title;

	public static String DevCloudTunnelHandler_10;
	public static String DevCloudTunnelHandler_7;
	public static String DevCloudTunnelHandler_8;

	public static String EmailValidator_InvalidEmail;

	public static String ExportParametersWizard_ExportError_Message;

	public static String LaunchApplicationHandler_0;

	public static String LaunchApplicationHandler_1;
	public static String NumberValidator_NotANumber;
	public static String PasswordValidator_InvalidPassword;

	public static String ParametersBlock_ExportButton;
	public static String ParametersBlock_ImportButton;
	public static String ParametersBlock_ImportDialogDescription;
	public static String ParametersBlock_ExportDialogDescription;
	public static String ParametersBlock_ImportDialogTitle;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
