/*******************************************************************************
 * Copyright (c) 2013 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.php.library.internal.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.zend.php.library.core.deploy.LibraryDeployData;
import org.zend.php.library.internal.ui.LibraryUI;

/**
 * @author Wojciech Galanciak, 2013
 *
 */
public abstract class AbstractLibraryWizard extends Wizard {

	private LibraryDeployData data;
	
	public AbstractLibraryWizard() {
		setDialogSettings(LibraryUI.getDefault().getDialogSettings());
	}
	
	public LibraryDeployData getData() {
		return data;
	}
	
	protected void setData(LibraryDeployData data) {
		this.data = data;
	}
}
