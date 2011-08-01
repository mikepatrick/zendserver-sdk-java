package org.zend.php.zendserver.deployment.debug.ui.config;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.zend.php.zendserver.deployment.core.debugger.DeploymentAttributes;
import org.zend.php.zendserver.deployment.debug.core.config.DeploymentHelper;
import org.zend.php.zendserver.deployment.debug.core.config.IDeploymentHelper;
import org.zend.php.zendserver.deployment.debug.core.config.LaunchUtils;
import org.zend.php.zendserver.deployment.debug.ui.Activator;
import org.zend.php.zendserver.deployment.debug.ui.Messages;
import org.zend.php.zendserver.deployment.debug.ui.dialogs.DeploymentConfigurationBlock;
import org.zend.php.zendserver.deployment.debug.ui.dialogs.IStatusChangeListener;

public class DeploymentLaunchConfigurationTab extends AbstractLaunchConfigurationTab implements
		IStatusChangeListener {

	private DeploymentConfigurationBlock block;
	private IProject project;

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		block = new DeploymentConfigurationBlock(this);
		block.createContents(composite);
		setDeploymentPageEnablement(false);
		setControl(composite);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			project = getProject(configuration);
			if (project == null) {
				project = LaunchUtils.getProjectFromFilename(configuration);
				setDeploymentPageEnablement(true);
			}
			if (project != null) {
				block.createParametersGroup(project);
				IDeploymentHelper helper = DeploymentHelper.create(configuration);
				block.initializeFields(helper);
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy wc) {
		if (project != null) {
			URL baseURL = block.getBaseURL();
			IDeploymentHelper helper = new DeploymentHelper();
			helper.setBasePath(baseURL.getPath());
			helper.setTargetId(block.getTarget().getId());
			helper.setProjectName(project.getName());
			helper.setUserParams(block.getParameters());
			helper.setAppName(block.getUserAppName());
			helper.setIgnoreFailures(block.isIgnoreFailures());
			helper.setDefaultServer(block.isDefaultServer());
			helper.setVirtualHost(baseURL.getHost());
			try {
				LaunchUtils.updateLaunchConfiguration(project, helper, wc);
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
	}

	public String getName() {
		return Messages.deploymentTab_Title;
	}

	public void statusChanged(IStatus status) {
		setMessages(status);
		updateLaunchConfigurationDialog();
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		IStatus status = block.validatePage();
		setMessages(status);
		if (status.getSeverity() == IStatus.OK) {
			return super.isValid(launchConfig);
		} else {
			return false;
		}
	}

	protected void updateLaunchConfigurationDialog() {
		if (getLaunchConfigurationDialog() != null) {
			getLaunchConfigurationDialog().updateButtons();
			getLaunchConfigurationDialog().updateMessage();
		}
	}

	private void setMessages(IStatus status) {
		if (status.getSeverity() == IStatus.OK) {
			setMessage(status.getMessage());
			setErrorMessage(null);
		} else {
			setErrorMessage(status.getMessage());
		}
	}

	private IProject getProject(ILaunchConfiguration config) throws CoreException {
		String projectName = config.getAttribute(DeploymentAttributes.PROJECT_NAME.getName(), "");
		if (!projectName.isEmpty()) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource resource = root.findMember(projectName);
			if (resource != null) {
				return resource.getProject();
			}
		}
		return null;
	}

	private void setDeploymentPageEnablement(boolean value) {
		block.setDeployComboEnabled(value);
		block.setDefaultServerEnabled(value);
		block.setBaseURLEnabled(value);
		block.setUserAppNameEnabled(value);
	}

}
