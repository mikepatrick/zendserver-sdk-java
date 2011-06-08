/*******************************************************************************
 * Copyright (c) May 26, 2011 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.sdkcli.internal.commands;

import java.io.File;

import org.zend.sdkcli.internal.options.Option;
import org.zend.sdklib.application.PackageBuilder;

/**
 * Create deployment package.
 * 
 * @author Wojciech Galanciak, 2011
 * 
 */
public class CreatePackageCommand extends TargetAwareCommand {

	private static final String PATH = "p";
	private static final String DESTINATION = "d";

	@Option(opt = PATH, required = true, description = "The path to project", argName = "path")
	public String getPath() {
		return getValue(PATH);
	}

	@Option(opt = DESTINATION, required = false, description = "The location where package will be created", argName = "path")
	public String getDestination() {
		return getValue(DESTINATION);
	}

	@Override
	public boolean doExecute() {

		String path = getPath();
		File project = new File(path);
		if (!project.exists() || !project.isDirectory()) {
			getLogger()
					.error("Provided path is not a path to a vaild project.");
			return false;
		}
		PackageBuilder builder = new PackageBuilder(project);
		File result = null;
		String destination = getDestination();
		if (destination == null) {
			result = builder.createDeploymentPackage();
		} else {
			result = builder.createDeploymentPackage(destination);
		}
		if (result != null) {
			return true;
		}
		return false;
	}

}
