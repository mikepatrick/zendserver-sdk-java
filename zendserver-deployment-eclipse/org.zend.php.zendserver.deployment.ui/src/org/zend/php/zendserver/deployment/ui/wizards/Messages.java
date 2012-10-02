/*******************************************************************************
 * Copyright (c) 2011 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.php.zendserver.deployment.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.zend.php.zendserver.deployment.ui.wizards.messages"; //$NON-NLS-1$
	public static String exportWizard_Titile;
	public static String exportWizard_JobTitle;
	public static String exportPage_Description;
	public static String exportPage_Title;
	public static String exportPage_TableLabel;
	public static String exportPage_DestinationLabel;
	public static String exportPage_Browse;
	public static String exportPage_SelectAll;
	public static String exportPage_DeselectAll;
	public static String exportPage_DirectoryDialogMessage;
	public static String exportPage_DirectoryDialogTitle;
	public static String exportPage_TableError;
	public static String exportPage_DestinationError;
	public static String MySQLCredentialsDialog_DialogTitle;
	public static String NewDependencyWizard_0;
	public static String OpenShiftDomainPage_EnterNameMessage;
	public static String OpenShiftDomainPage_NameLabel;
	public static String OpenShiftDomainPage_PageDescription;
	public static String OpenShiftDomainPage_PageTitle;
	public static String OpenShiftTargetPage_AddMySQLLabel;
	public static String OpenShiftTargetPage_EnterNameMessage;
	public static String OpenShiftTargetPage_GearProfileLabel;
	public static String OpenShiftTargetPage_PageDescription;
	public static String OpenShiftTargetPage_PageTitle;
	public static String OpenShiftTargetPage_SameTargetErrorMessage;
	public static String OpenShiftTargetPage_TargetNameLabel;
	public static String OpenShiftTargetWizard_Description;
	public static String OpenShiftTargetWizard_Title;
	public static String OpenShiftTargetWizardDialog_CreateDomainJobTitle;
	public static String PackageExportPage_0;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
