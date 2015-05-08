/*******************************************************************************
 * Copyright (c) 2014 Zend Technologies.
 * All rights reserved. This program and the accompanying materials
 * are the copyright of Zend Technologies and is protected under
 * copyright laws of the United States.
 * You must not copy, adapt or redistribute this document for 
 * any use.
 *******************************************************************************/
package org.zend.php.zendserver.deployment.ui.servers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.php.internal.server.core.Server;
import org.eclipse.php.internal.server.core.manager.ServersManager;
import org.eclipse.php.internal.ui.wizards.IControlHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.statushandlers.StatusManager;
import org.zend.php.server.core.utils.ServerUtils;
import org.zend.php.server.ui.fragments.AbstractCompositeFragment;
import org.zend.php.zendserver.deployment.core.targets.EclipseSSH2Settings;
import org.zend.php.zendserver.deployment.core.targets.TargetsManagerService;
import org.zend.php.zendserver.deployment.core.tunnel.SSHTunnelConfiguration;
import org.zend.php.zendserver.deployment.ui.Activator;
import org.zend.sdklib.internal.target.ZendDevCloud;
import org.zend.sdklib.internal.target.ZendTarget;
import org.zend.sdklib.manager.TargetsManager;
import org.zend.sdklib.target.IZendTarget;

/**
 * Abstract composite for Phpcloud and OpenShift fragments.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
@SuppressWarnings("restriction")
public abstract class AbstractCloudCompositeFragment extends
		AbstractCompositeFragment {

	private int lastPort = -1;

	protected AbstractCloudCompositeFragment(Composite parent,
			IControlHandler handler, boolean isForEditing, String title,
			String description) {
		super(parent, handler, isForEditing, title, title, description);
	}

	@Override
	public boolean performOk() {
		return isComplete();
	}

	protected abstract void detectServers(IProgressMonitor monitor);

	/**
	 * Set up SSH settings for specified server base on target type.
	 * 
	 * @param server
	 * @param target
	 */
	protected abstract void setupSSHConfiguration(Server server,
			IZendTarget target);

	/**
	 * @return prefix of a new private key name
	 */
	protected abstract String getGeneratedKeyName();

	protected abstract void updateData();

	/**
	 * Generate new private RSA key.
	 * 
	 * @return full path to generated key
	 */
	protected String generateKey() {
		String sshHome = EclipseSSH2Settings.getSSHHome();
		String file;
		if (sshHome != null) {
			File tmpFile = new File(sshHome, getGeneratedKeyName());
			int i = 1;
			while (tmpFile.exists()) {
				tmpFile = new File(sshHome, getGeneratedKeyName() + i);
				i++;
			}

			file = tmpFile.getAbsolutePath();

			boolean confirm = MessageDialog
					.openConfirm(
							getShell(),
							Messages.AbstarctCloudCompositeFragment_GenerateKeyTitle,
							Messages.bind(
									Messages.AbstarctCloudCompositeFragment_GenerateKeyMessage,
									file));
			if (!confirm) {
				return null;
			}
		} else {
			FileDialog d = new FileDialog(getShell(), SWT.SAVE);
			file = d.open();
			if (file == null) {
				return null;
			}
		}
		try {
			EclipseSSH2Settings.createPrivateKey(ZendDevCloud.KEY_TYPE, file);
			return file;
		} catch (CoreException e) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							e.getMessage(), e), StatusManager.SHOW);
		}
		return null;
	}

	/**
	 * Create copy of specified temporary target.
	 * 
	 * @param temporaryTarget
	 *            temporary target
	 * @return target copy without temporary flag
	 */
	protected IZendTarget copy(ZendTarget temporaryTarget) {
		ZendTarget target = new ZendTarget(temporaryTarget.getId(),
				temporaryTarget.getHost(),
				temporaryTarget.getDefaultServerURL(),
				temporaryTarget.getKey(), temporaryTarget.getSecretKey());
		String[] keys = temporaryTarget.getPropertiesKeys();
		for (String key : keys) {
			target.addProperty(key, temporaryTarget.getProperty(key));
		}
		return target;
	}

	/**
	 * Check if there is already a server with specified host name.
	 * 
	 * @param host
	 * @return {@link Server} instance which has specified host or
	 *         <code>null</code> if such server does not exist
	 */
	protected Server getExistingServer(String host) {
		Server[] servers = ServersManager.getServers();
		for (Server server : servers) {
			if (server.getHost().equals(host)) {
				return server;
			}
		}
		return null;
	}

	/**
	 * Check if there is already a target with specified host URL
	 * 
	 * @param target
	 * @return {@link IZendTarget} instance which has specified URL or
	 *         <code>null</code> if such target does not exist
	 */
	protected IZendTarget findExistingTarget(IZendTarget target) {
		TargetsManager manager = TargetsManagerService.INSTANCE
				.getTargetManager();
		IZendTarget[] targets = manager.getTargets();
		for (IZendTarget t : targets) {
			if (TargetsManager.isPhpcloud(t)
					&& t.getHost().getHost().equals(target.getHost().getHost())) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Generate new port for database local port forwarding which is not in
	 * conflict of any existing SSH tunnel configuration (including any server
	 * created during cloud servers detection which has not been added yet).
	 * 
	 * @return port for local port forwarding for database connection
	 */
	protected int getNewDatabasePort() {
		int port = SSHTunnelConfiguration.getNewDatabasePort();
		if (lastPort == -1) {
			lastPort = port;
		}
		if (lastPort == port) {
			port++;
			lastPort = port;
		}
		return port;
	}

	/**
	 * Remove existing targets from list of cloud target detected on the
	 * account. For existing targets it updates private key path.
	 * 
	 * @param targets
	 *            detected cloud targets
	 * @param manager
	 *            {@link TargetsManager} instance
	 * @return list of new targets
	 */
	protected IZendTarget[] removeExistingTargets(IZendTarget[] targets,
			TargetsManager manager) {
		List<IZendTarget> result = new ArrayList<IZendTarget>();
		IZendTarget[] existingTargets = manager.getTargets();
		for (IZendTarget target : targets) {
			boolean unique = true;
			for (IZendTarget existingTarget : existingTargets) {
				if (ServerUtils.getServer(existingTarget) != null
						&& existingTarget.getDefaultServerURL().equals(
								target.getDefaultServerURL())) {
					unique = false;
					// only update private key path and then skip it
					String privateKey = target
							.getProperty(ZendDevCloud.SSH_PRIVATE_KEY_PATH);
					ZendTarget toUpdate = (ZendTarget) existingTarget;
					toUpdate.addProperty(ZendDevCloud.SSH_PRIVATE_KEY_PATH,
							privateKey);
					manager.updateTarget(existingTarget, true);
					Server server = ServerUtils.getServer(existingTarget);
					if (server != null) {
						SSHTunnelConfiguration config = SSHTunnelConfiguration
								.read(server);
						config.setPrivateKey(privateKey);
						config.store(server);
					}
					break;
				}
			}
			if (unique) {
				result.add(target);
			}
		}
		return result.toArray(new IZendTarget[result.size()]);
	}

	protected void showWarningMessage(final String title, final String message) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openWarning(getShell(), title, message);
			}
		});
	}

}