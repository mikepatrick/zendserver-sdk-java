/*******************************************************************************
 * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.sdkcli.internal.commands;

import org.zend.sdkcli.internal.options.Option;
import org.zend.sdklib.target.IZendTarget;
import org.zend.webapi.core.WebApiException;

public class ListTargetsCommand extends TargetAwareCommand {

	private static final String STATUS = "s";

	@Option(opt = STATUS, required = false, description = "show status line for targets")
	public boolean isStatus() {
		return hasOption(STATUS);
	}
	
	@Override
	public boolean doExecute() {
		final IZendTarget[] list = getTargetManager().getTargets();
		if (list.length == 0) {
			commandLine.getLog().info("No Available Zend Targets.");
			return true;
		}

		commandLine.getLog().info("Available Zend Targets:");
		for (IZendTarget target : list) {
			commandLine.getLog().info("id: " + target.getId());
			commandLine.getLog().info("\tHost: " + target.getHost());
			commandLine.getLog().info("\tDefault Server URL: " + target.getDefaultServerURL());
			commandLine.getLog().info("\tKey: " + target.getKey());

			if (isStatus()) {
				boolean connect = false;
				try {
					connect = target.connect();
				} catch (WebApiException e) {
					connect = false;
				}
				commandLine.getLog()
						.error("\tStatus: "
								+ (connect ? "connected" : "disconnected"));
			}

		}

		return true;
	}
}
