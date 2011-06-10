/*******************************************************************************
 * Copyright (c) May 24, 2011 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.sdkcli.internal.commands;

import org.zend.sdkcli.internal.options.Option;
import org.zend.webapi.core.connection.data.ApplicationInfo;

/**
 * Deploys package to specified target.
 * 
 * @author Wojciech Galanciak, 2011
 * 
 */
public class RemoveApplicationCommand extends ApplicationAwareCommand {

	private static final String APPID = "id";
	
	@Option(opt = APPID, required = true, description = "The application id", argName="app-id")
	public String getApplicationId() {
		return getValue(APPID);
	}

	@Override
	public boolean doExecute() {
		
		ApplicationInfo info = getApplication().remove(getTargetId(), getApplicationId());
		
		if (info == null) {
			return false;
		}
		return true;
	}

}
