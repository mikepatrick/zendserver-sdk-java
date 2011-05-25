/*******************************************************************************
 * Copyright (c) May 24, 2011 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.sdkcli.internal.commands;

import java.util.List;

import org.zend.sdkcli.ParseError;
import org.zend.webapi.core.connection.data.ApplicationInfo;
import org.zend.webapi.core.connection.data.ApplicationServer;
import org.zend.webapi.core.connection.data.ApplicationsList;
import org.zend.webapi.core.connection.data.DeployedVersion;
import org.zend.webapi.core.connection.data.MessageList;

/**
 * List application statuses.
 * 
 * @author Wojciech Galanciak, 2011
 * 
 */
public class ListApplicationsCommand extends ApplicationAwareCommand {

	private static final String ID = "t";
	private static final String APP_ID = "appId";

	public ListApplicationsCommand(CommandLine commandLine) throws ParseError {
		super(commandLine);
	}

	@Override
	public boolean execute() {
		ApplicationsList appList = getApplication().getStatus(getValue(ID),
				getValues(APP_ID));
		if (appList == null) {
			return false;
		}
		List<ApplicationInfo> infos = appList.getApplicationsInfo();
		for (ApplicationInfo info : infos) {
			getLogger().info("Id:                     " + info.getId());
			getLogger().info("Application Name:       " + info.getAppName());
			getLogger()
					.info("User Application Name:  " + info.getUserAppName());
			getLogger().info("Base URL:               " + info.getBaseUrl());
			getLogger().info(
					"Installed Location:     " + info.getInstalledLocation());
			getLogger().info(
					"Status:                 " + info.getStatus().getName());
			getLogger().info("Servers:");
			List<ApplicationServer> servers = info.getServers()
					.getApplicationServers();
			if (servers != null) {
				for (ApplicationServer server : servers) {
					getLogger().info("\tid:               " + server.getId());
					getLogger().info(
							"\tDeployed Version: "
									+ server.getDeployedVersion());
					getLogger().info(
							"\tStatus:           "
									+ server.getStatus().getName());
				}
			}
			List<DeployedVersion> versions = info.getDeployedVersions()
					.getDeployedVersions();
			if (versions != null) {
				getLogger().info("Deployed Versions:");
				for (DeployedVersion version : versions) {
					getLogger().info("\t" + version.getVersion());
				}
			}
			MessageList messages = info.getMessageList();
			if (messages != null) {
				List<String> errors = messages.getError();
				if (errors != null) {
					getLogger().info("Errors:");
					for (String error : errors) {
						getLogger().info("\t" + error);
					}
				}
				List<String> warnings = messages.getWarning();
				if (warnings != null) {
					getLogger().info("Warnings:");
					for (String warning : warnings) {
						getLogger().info("\t" + warning);
					}
				}
				List<String> mInfos = messages.getInfo();
				if (mInfos != null) {
					getLogger().info("Info:");
					for (String mInfo : mInfos) {
						getLogger().info("\t" + mInfo);
					}
				}
			}
		}
		return true;
	}

	@Override
	protected void setupOptions() {
		// application ID(s)
		addArgumentsOption(APP_ID, false, "one or more application IDs");
		// target name
		addArgumentOption(ID, true, "use given target name");
	}
}
