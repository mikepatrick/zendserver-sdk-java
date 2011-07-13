package org.zend.php.zendserver.deployment.core.sdk;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.osgi.framework.BundleException;
import org.zend.php.zendserver.deployment.core.DeploymentCore;



public class SdkManager implements IPreferenceChangeListener {

	public static final String SDK_PATH = "sdk_path"; //$NON-NLS-1$

	private Sdk currentSdk;
	
	public SdkManager() throws BundleException {
		String path = DeploymentCore.getPreferenceScope().get(SDK_PATH, getDefaultSdkPath());
		currentSdk = new Sdk(path);
		currentSdk.install();
	}
	
	public static String getDefaultSdkPath() {
		URL url = FileLocator.find(DeploymentCore.getContext().getBundle(), new Path("/sdk"), null);
		URL fileUrl = null;
		try {
			fileUrl = FileLocator.toFileURL(url);
		} catch (IOException e) {
			DeploymentCore.log(e);
		}
		
		return fileUrl == null ? null : fileUrl.getPath();
	}
	
	private void updateSdk(String path) throws CoreException {
		Sdk newSdk = new Sdk(path);
		String error = newSdk.validate();
		if (error != null) {
			throw new CoreException(new Status(IStatus.ERROR, DeploymentCore.PLUGIN_ID, error));
		}
		
		if (currentSdk != null) {
			try {
				currentSdk.uninstall();
			} catch (BundleException e) {
				throw new CoreException(new Status(IStatus.ERROR, DeploymentCore.PLUGIN_ID, "An error occured during uninstalling current SDK: "+e.getMessage(), e));
			}
		}
		
		try {
			newSdk.install();
			currentSdk = newSdk;
		} catch (BundleException e) {
			throw new CoreException(new Status(IStatus.ERROR, DeploymentCore.PLUGIN_ID, "An error occured during installing new SDK: "+e.getMessage(), e));
		}
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		if (SDK_PATH.equals(event.getKey())) {
			try {
				updateSdk((String) event.getNewValue());
			} catch (CoreException e) {
				DeploymentCore.log(e); // TODO handle showing errors
			}
		}
		
	}	
}
