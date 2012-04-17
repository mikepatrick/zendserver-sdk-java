/*******************************************************************************
 * Copyright (c) 2012 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.php.zendserver.deployment.ui.commands;

import org.zend.php.zendserver.monitor.core.MonitorManager;
import org.zend.sdklib.target.IZendTarget;

/**
 * Command handler responsible for disabling event monitoring for selected
 * target.
 * 
 * @author Wojciech Galanciak, 2012
 * 
 */
public class DisableTargetMonitorHandler extends AbstractMonitoringHandler {

	protected void enableMonitoring(Object element) {
		if (element instanceof IZendTarget) {
			IZendTarget target = (IZendTarget) element;
			MonitorManager.setTargetEnabled(target.getId(), false);
			MonitorManager.removeTargetMonitor(target.getId());
		}
	}

}
