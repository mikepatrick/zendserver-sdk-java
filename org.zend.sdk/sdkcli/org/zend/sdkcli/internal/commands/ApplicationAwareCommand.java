/*******************************************************************************
 * Copyright (c) May 25, 2011 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.sdkcli.internal.commands;

import org.zend.sdklib.ZendApplication;

/**
 * Base class for all command lines that need access to zend application.
 * 
 * @author Wojciech Galanciak, 2011
 * 
 */
public abstract class ApplicationAwareCommand extends AbstractCommand {

	private final ZendApplication application;

	public ApplicationAwareCommand() {
		application = new ZendApplication();
	}

	public ZendApplication getApplication() {
		return application;
	}

}
