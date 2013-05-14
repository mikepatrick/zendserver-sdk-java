/*******************************************************************************
 * Copyright (c) 2013 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.php.library.core.deploy;

import org.zend.php.library.internal.core.LibraryCore;

/**
 * List of attributes related to library deployment.
 * 
 * @author Wojciech Galanciak, 2013
 * 
 */
public enum LibraryDeploymentAttributes {

	ADD_LIBRARY("addLibrary"), //$NON-NLS-1$

	TARGET_ID("targetId"), //$NON-NLS-1$

	WARN_UPDATE("warnUpdate"); //$NON-NLS-1$

	private static final String PREFIX = LibraryCore.PLUGIN_ID + '.';

	private String name;

	private LibraryDeploymentAttributes(String name) {
		this.name = PREFIX + name;
	}

	public String getName() {
		return name;
	}

}