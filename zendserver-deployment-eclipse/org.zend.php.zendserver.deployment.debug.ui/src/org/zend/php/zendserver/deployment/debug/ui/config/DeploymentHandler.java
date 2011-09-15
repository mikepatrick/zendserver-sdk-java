package org.zend.php.zendserver.deployment.debug.ui.config;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.zend.php.zendserver.deployment.core.descriptor.DescriptorContainerManager;
import org.zend.php.zendserver.deployment.core.descriptor.IDescriptorContainer;
import org.zend.php.zendserver.deployment.core.descriptor.IParameter;
import org.zend.php.zendserver.deployment.core.targets.TargetsManagerService;
import org.zend.php.zendserver.deployment.debug.core.config.DeploymentHelper;
import org.zend.php.zendserver.deployment.debug.core.config.IDeploymentHelper;
import org.zend.php.zendserver.deployment.debug.core.config.LaunchUtils;
import org.zend.php.zendserver.deployment.debug.core.jobs.AbstractLaunchJob;
import org.zend.php.zendserver.deployment.debug.core.jobs.DeployLaunchJob;
import org.zend.php.zendserver.deployment.debug.core.jobs.DeploymentLaunchJob;
import org.zend.php.zendserver.deployment.debug.core.jobs.ExisitngAppIdJob;
import org.zend.php.zendserver.deployment.debug.core.jobs.UpdateLaunchJob;
import org.zend.php.zendserver.deployment.debug.core.tunnel.ZendDevCloudTunnelManager;
import org.zend.php.zendserver.deployment.debug.ui.Activator;
import org.zend.php.zendserver.deployment.debug.ui.Messages;
import org.zend.php.zendserver.deployment.debug.ui.listeners.DeployJobChangeListener;
import org.zend.php.zendserver.deployment.debug.ui.wizards.DeploymentWizard;
import org.zend.sdklib.target.IZendTarget;
import org.zend.webapi.core.connection.response.ResponseCode;

public class DeploymentHandler {

	public static final int OK = 0;
	public static final int CANCEL = -1;

	private AbstractLaunchJob job;

	private boolean dialogResult;
	private boolean cancelled;

	private DeployJobChangeListener listener;
	
	private ILaunchConfiguration config;
	private String mode;

	public DeploymentHandler() {
		this(null);
	}

	public DeploymentHandler(ILaunchConfiguration config) {
		super();
		this.config = config;
	}

	public int executeDeployment(String executionMode) {
		mode = executionMode;
		job = null;
		listener = new DeployJobChangeListener(config);
		try {
			if (LaunchUtils.getConfigurationType() == config.getType()) {
				IDeploymentHelper helper = DeploymentHelper.create(config);
				final IProject project = LaunchUtils.getProjectFromFilename(config);
				switch (helper.getOperationType()) {
				case IDeploymentHelper.DEPLOY:
					if (helper.getTargetId().isEmpty()) {
						IDeploymentHelper defaultHelper = LaunchUtils.createDefaultHelper(project);
						if (defaultHelper != null) {
							try {
								helper = defaultHelper;
								LaunchUtils.updateLaunchConfiguration(project, defaultHelper,
										config.getWorkingCopy());
							} catch (CoreException e) {
								Activator.log(e);
							}
						}
					}
					if (!helper.getTargetId().isEmpty() && !hasEmptyParameters(project, helper)) {
						job = new DeployLaunchJob(helper, project);
					} else {
						setDefaultTarget(helper, project);
						if (helper.getTargetId() != null
								|| !helper.getTargetId().isEmpty()) {
							helper.setProjectName(project.getName());
						}
						doOpenDeploymentWizard(helper, project);
					}
					if (job == null) {
						return CANCEL;
					}
					break;
				case IDeploymentHelper.UPDATE:
					if (hasEmptyParameters(project, helper)) {
						setDefaultTarget(helper, project);
						if (helper.getTargetId() != null
								|| !helper.getTargetId().isEmpty()) {
							helper.setProjectName(project.getName());
						}
						doOpenDeploymentWizard(helper, project);
					} else {
						job = new UpdateLaunchJob(helper, project);
					}
					break;
				case IDeploymentHelper.AUTO_DEPLOY:
					job = LaunchUtils.getAutoDeployJob();
					if (job == null) {
						return CANCEL;
					}
					job.setHelper(helper);
					job.setProject(project);
					job.addJobChangeListener(new JobChangeAdapter() {
						@Override
						public void done(IJobChangeEvent event) {
							if (event.getResult().getSeverity() == IStatus.CANCEL) {
								cancelled = true;
							}
						}
					});
					break;
				case IDeploymentHelper.NO_ACTION:
					updateLaunchConfiguration(helper, config, project);
					return OK;
				default:
					return CANCEL;
				}
				job.addJobChangeListener(listener);
				job.setUser(true);
				job.schedule();
				job.join();
				return verifyJobResult(job.getHelper(), project);
			}
		} catch (CoreException e) {
			Activator.log(e);
		} catch (InterruptedException e) {
			Activator.log(e);
		}
		return OK;
	}

	public int openNoConfigDeploymentWizard(IDeploymentHelper helper, IProject project) {
		job = null;
		try {
			doOpenDeploymentWizard(helper, project);
			if (job == null) {
				return OK;
			}
			job.setUser(true);
			job.schedule();
			job.join();
			return verifyJobResult(job.getHelper(), project);
		} catch (InterruptedException e) {
			Activator.log(e);
		}
		return OK;
	}

	public int openDeploymentWizard() {
		job = null;
		listener = new DeployJobChangeListener(config);
		try {
			if (LaunchUtils.getConfigurationType() == config.getType()) {
				IDeploymentHelper helper = DeploymentHelper.create(config);
				final IProject project = LaunchUtils.getProjectFromFilename(config);
				doOpenDeploymentWizard(helper, project);
				if (job == null) {
					return OK;
				}
				job.addJobChangeListener(listener);
				job.setUser(true);
				job.schedule();
				job.join();
				return verifyJobResult(job.getHelper(), project);
			}
		} catch (CoreException e) {
			Activator.log(e);
		} catch (InterruptedException e) {
			Activator.log(e);
		}
		return OK;
	}

	private void setDefaultTarget(IDeploymentHelper helper,
			final IProject project) {
		String targetId = helper.getTargetId();
		if (targetId == null || targetId.isEmpty()) {
			IZendTarget defaultTarget = LaunchUtils.getDefaultTarget(project);
			if (defaultTarget != null) {
				helper.setTargetId(defaultTarget.getId());
				helper.setTargetHost(defaultTarget.getHost().toString());
			}
		}
	}

	private boolean hasEmptyParameters(IProject project, IDeploymentHelper helper) {
		IResource descriptor = project.findMember(DescriptorContainerManager.DESCRIPTOR_PATH);
		IDescriptorContainer model = DescriptorContainerManager.getService()
				.openDescriptorContainer((IFile) descriptor);
		List<IParameter> definedParams = model.getDescriptorModel().getParameters();
		if (definedParams == null || definedParams.size() == 0) {
			return false;
		}
		Map<String, String> params = helper.getUserParams();
		if (params != null && params.size() > 0) {
			for (IParameter parameter : definedParams) {
				String value = (String) params.get(parameter.getId());
				if (parameter.isRequired() && (value == null || value.isEmpty())) {
					return true;
				}
			}
		} else {
			return true;
		}
		return false;
	}

	private boolean isCancelled() {
		if (listener != null) {
			return listener.isCancelled() || cancelled;
		}
		return cancelled;
	}

	private int verifyJobResult(final IDeploymentHelper helper, IProject project)
			throws InterruptedException {
		if (isCancelled()) {
			return CANCEL;
		}
		if (job instanceof DeploymentLaunchJob) {
			DeploymentLaunchJob deploymentJob = (DeploymentLaunchJob) job;
			ResponseCode code = deploymentJob.getResponseCode();
			if (code == null) {
				String targetHost = helper.getTargetHost();
				if (helper.getOperationType() == IDeploymentHelper.DEPLOY
						&& LaunchUtils.isAutoDeployAvailable()
						&& targetHost
								.contains(ZendDevCloudTunnelManager.DEVPASS_HOST)) {
					job = getAutoDeployJob(helper, project);
				}
				return checkSSHTunnel(helper);
			}
			switch (deploymentJob.getResponseCode()) {
			case BASE_URL_CONFLICT:
				String targetHost = helper.getTargetHost();
				if (LaunchUtils.isAutoDeployAvailable()
						&& targetHost
								.contains(ZendDevCloudTunnelManager.DEVPASS_HOST)) {
					job = new ExisitngAppIdJob(helper, project);
					job.setUser(true);
					job.schedule();
					job.join();
					job = getAutoDeployJob(job.getHelper(), project);
					if (job != null) {
						return checkSSHTunnel(helper);
					}
				}
				return handleBaseUrlConflict(helper, project);
			case APPLICATION_CONFLICT:
				return handleApplicationConflict(helper, project);
			default:
				break;
			}
		}
		return checkSSHTunnel(helper);
	}

	private AbstractLaunchJob getAutoDeployJob(IDeploymentHelper helper,
			IProject project)
			throws InterruptedException {
		AbstractLaunchJob job = LaunchUtils.getAutoDeployJob();
		if (job == null) {
			// if auto deploy is not available then just leave
			// configuration without changes
			return null;
		}
		helper.setOperationType(IDeploymentHelper.AUTO_DEPLOY);
		job.setHelper(helper);
		job.setProject(project);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (event.getResult().getSeverity() == IStatus.CANCEL) {
					cancelled = true;
				}
			}
		});
		if (listener != null) {
			job.addJobChangeListener(listener);
		}
		job.setUser(true);
		job.schedule();
		job.join();
		return job;
	}

	private int checkSSHTunnel(IDeploymentHelper helper) {
		String targetHost = helper.getTargetHost();
		if (mode != null && mode.equals(ILaunchManager.DEBUG_MODE)
				&& targetHost.contains(ZendDevCloudTunnelManager.DEVPASS_HOST)) {
			IZendTarget target = TargetsManagerService.INSTANCE.getTargetManager().getTargetById(
					helper.getTargetId());
			try {
				if (!ZendDevCloudTunnelManager.getManager().connect(target)) {
					throw new IllegalStateException(
							Messages.DeploymentHandler_sshTunnelErrorMessage);
				}
			} catch (final Exception e) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

					public void run() {
						Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getShell();
						MessageDialog.openError(shell,
								Messages.DeploymentHandler_sshTunnelErrorTitle, e.getMessage());
					}
				});
				return CANCEL;
			}
		}
		return OK;
	}

	private int handleApplicationConflict(IDeploymentHelper helper, IProject project)
			throws InterruptedException {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			public void run() {
				MessageDialog dialog = getApplicationConflictDialog();
				if (dialog.open() != 0) {
					dialogResult = true;
				}
			}
		});
		if (!dialogResult) {
			doOpenDeploymentWizard(helper, project);
			if (job == null) {
				return CANCEL;
			}
			if (listener != null) {
				job.addJobChangeListener(listener);
			}
			job.setUser(true);
			job.schedule();
			job.join();
			return verifyJobResult(job.getHelper(), project);
		} else {
			dialogResult = false;
			return CANCEL;
		}
	}

	private int handleBaseUrlConflict(IDeploymentHelper helper, IProject project)
			throws InterruptedException {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			public void run() {
				MessageDialog dialog = getUpdateExistingApplicationDialog();
				if (dialog.open() == 0) {
					dialogResult = true;
				}
			}
		});
		if (!dialogResult) {
			doOpenDeploymentWizard(helper, project);
			if (job == null) {
				return CANCEL;
			}
			if (listener != null) {
				job.addJobChangeListener(listener);
			}
			job.setUser(true);
			job.schedule();
			job.join();
			return verifyJobResult(job.getHelper(), project);
		} else {
			dialogResult = false;
			job = new ExisitngAppIdJob(helper, project);
			job.setUser(true);
			job.schedule();
			job.join();
			job = new UpdateLaunchJob(job.getHelper(), project);
			if (listener != null) {
				job.addJobChangeListener(listener);
			}
			job.setUser(true);
			job.schedule();
			job.join();
			return OK;
		}
	}

	private void doOpenDeploymentWizard(final IDeploymentHelper helper, final IProject project) {
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				DeploymentWizard wizard = new DeploymentWizard(project, helper, config != null);
				Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.setPageSize(550, 350);
				dialog.create();
				if (dialog.open() == Window.OK) {
					IDeploymentHelper updatedHelper = wizard.getHelper();
					switch (updatedHelper.getOperationType()) {
					case IDeploymentHelper.DEPLOY:
						job = new DeployLaunchJob(updatedHelper, project);
						break;
					case IDeploymentHelper.UPDATE:
						job = new UpdateLaunchJob(updatedHelper, project);
						break;
					case IDeploymentHelper.AUTO_DEPLOY:
						job = LaunchUtils.getAutoDeployJob();
						if (job == null) {
							break;
						}
						job.setHelper(updatedHelper);
						job.setProject(project);
						job.addJobChangeListener(new JobChangeAdapter() {
							@Override
							public void done(IJobChangeEvent event) {
								if (event.getResult().getSeverity() == IStatus.CANCEL) {
									cancelled = true;
								}
							}
						});
						break;
					case IDeploymentHelper.NO_ACTION:
						try {
							if (config != null) {
								updateLaunchConfiguration(updatedHelper, config, project);
							}
						} catch (CoreException e) {
							Activator.log(e);
						}
						return;
					}
				} else {
					cancelled = true;
					job = null;
				}
			}
		});
	}

	private void updateLaunchConfiguration(IDeploymentHelper helper,
			final ILaunchConfiguration config, final IProject project) throws CoreException {
		ILaunchConfigurationWorkingCopy wc = null;
		if (config instanceof ILaunchConfigurationWorkingCopy) {
			wc = (ILaunchConfigurationWorkingCopy) config;
		} else {
			wc = config.getWorkingCopy();
		}
		LaunchUtils.updateLaunchConfiguration(project, helper, wc);
		wc.doSave();
	}

	private MessageDialog getUpdateExistingApplicationDialog() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		return new MessageDialog(shell, Messages.updateExistingApplicationDialog_Title, null,
				Messages.updateExistingApplicationDialog_Message, MessageDialog.QUESTION,
				new String[] { Messages.updateExistingApplicationDialog_yesButton,
						Messages.updateExistingApplicationDialog_noButton }, 1);
	}

	private MessageDialog getApplicationConflictDialog() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		return new MessageDialog(shell, Messages.applicationConflictDialog_Title, null,
				Messages.applicationConflictDialog_Message, MessageDialog.QUESTION, new String[] {
						Messages.updateExistingApplicationDialog_yesButton,
						Messages.updateExistingApplicationDialog_noButton }, 0);
	}

}
