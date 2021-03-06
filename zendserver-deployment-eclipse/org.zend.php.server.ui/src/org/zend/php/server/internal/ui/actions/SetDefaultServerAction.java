/*******************************************************************************
 * Copyright (c) 2014 Zend Technologies Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Zend Technologies Ltd. - initial API and implementation
 *******************************************************************************/
package org.zend.php.server.internal.ui.actions;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.php.internal.server.core.Server;
import org.eclipse.php.internal.server.core.manager.ServersManager;
import org.zend.php.server.internal.ui.Messages;
import org.zend.php.server.ui.ServersUI;

/**
 * Action responsible for setting selected server as a default workspace PHP
 * server.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
@SuppressWarnings("restriction")
public class SetDefaultServerAction extends AbstractServerAction implements
		ISelectionChangedListener {

	private boolean isEnabled;

	public SetDefaultServerAction(ISelectionProvider provider) {
		super(Messages.SetDefaultServerAction_Name, ServersUI
				.getImageDescriptor(ServersUI.SET_DEFAULT_ICON), provider);
		provider.addSelectionChangedListener(this);
	}

	@Override
	public void run() {
		List<Server> toEdit = getSelection();
		if (toEdit.size() == 1) {
			ServersManager.setDefaultServer(null, toEdit.get(0));
			ServersManager.save();
		}
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		IStructuredSelection selectedServers = (IStructuredSelection) selection;
		boolean newVal = selectedServers.size() == 1
				&& !ServersManager.getDefaultServer(null).equals(
						selectedServers.getFirstElement());
		if (isEnabled != newVal) {
			isEnabled = newVal;
			firePropertyChange(ENABLED, !isEnabled, isEnabled);
		}
	}

}
