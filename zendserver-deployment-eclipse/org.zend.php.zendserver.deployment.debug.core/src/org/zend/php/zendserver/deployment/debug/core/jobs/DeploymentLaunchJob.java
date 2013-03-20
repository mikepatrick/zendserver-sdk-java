package org.zend.php.zendserver.deployment.debug.core.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.zend.php.zendserver.deployment.core.debugger.IDeploymentHelper;
import org.zend.php.zendserver.deployment.core.sdk.EclipseMappingModelLoader;
import org.zend.php.zendserver.deployment.core.sdk.EclipseVariableResolver;
import org.zend.php.zendserver.deployment.core.sdk.SdkStatus;
import org.zend.php.zendserver.deployment.core.sdk.StatusChangeListener;
import org.zend.php.zendserver.deployment.core.targets.PhpcloudContainerListener;
import org.zend.php.zendserver.deployment.debug.core.Activator;
import org.zend.php.zendserver.deployment.debug.core.Messages;
import org.zend.sdklib.application.ZendApplication;
import org.zend.sdklib.manager.TargetsManager;
import org.zend.webapi.core.WebApiClient;
import org.zend.webapi.core.connection.data.ApplicationInfo;
import org.zend.webapi.core.connection.data.ApplicationsList;
import org.zend.webapi.core.connection.data.values.ApplicationStatus;
import org.zend.webapi.core.connection.response.ResponseCode;
import org.zend.webapi.core.service.IRequestListener;
import org.zend.webapi.internal.core.connection.exception.UnexpectedResponseCode;
import org.zend.webapi.internal.core.connection.exception.WebApiCommunicationError;

public abstract class DeploymentLaunchJob extends AbstractLaunchJob {

	private ResponseCode responseCode;

	protected DeploymentLaunchJob(String name, IDeploymentHelper helper,
			String projectPath) {
		super(name, helper, projectPath);
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		StatusChangeListener listener = new StatusChangeListener(monitor);
		ZendApplication app = new ZendApplication(new EclipseMappingModelLoader());
		app.addStatusChangeListener(listener);
		app.setVariableResolver(new EclipseVariableResolver());
		IRequestListener preListener = new PhpcloudContainerListener(
				helper.getTargetId());
		if (TargetsManager.isPhpcloud(helper.getTargetHost())) {
			WebApiClient.registerPreRequestListener(preListener);
		}
		try {
			ApplicationInfo info = performOperation(app, projectPath);
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			if (info != null && info.getStatus() == ApplicationStatus.STAGING) {
				helper.setAppId(info.getId());
				return monitorApplicationStatus(listener, helper.getTargetId(),
						info.getId(), app, monitor);
			}
			Throwable exception = listener.getStatus().getThrowable();
			if (exception instanceof UnexpectedResponseCode) {
				UnexpectedResponseCode codeException = (UnexpectedResponseCode) exception;
				responseCode = codeException.getResponseCode();
				switch (responseCode) {
				case BASE_URL_CONFLICT:
				case APPLICATION_CONFLICT:
					return Status.OK_STATUS;
				case INVALID_PARAMETER:
					String message = codeException.getMessage();
					if (message != null) {
						message = message.trim();
						if (message
								.startsWith("Invalid userAppName parameter: Application name") //$NON-NLS-1$
								&& message.endsWith("already exists")) { //$NON-NLS-1$
							return new Status(IStatus.INFO,
									Activator.PLUGIN_ID, message);
						}
					}
				default:
					break;
				}
			} else if (exception instanceof WebApiCommunicationError) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						Messages.DeploymentLaunchJob_ConnectionRefusedMessage);
			}
			return new SdkStatus(listener.getStatus());
		} finally {
			WebApiClient.unregisterPreRequestListener(preListener);
		}
	}

	public ResponseCode getResponseCode() {
		return responseCode;
	}

	protected abstract ApplicationInfo performOperation(ZendApplication app, String projectPath);

	private IStatus monitorApplicationStatus(StatusChangeListener listener, String targetId,
			int id, ZendApplication application, IProgressMonitor monitor) {
		monitor.beginTask(Messages.statusJob_Title, IProgressMonitor.UNKNOWN);
		ApplicationStatus result = null;
		while (result != ApplicationStatus.DEPLOYED) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			ApplicationsList info = application.getStatus(targetId, String.valueOf(id));
			if (info != null && info.getApplicationsInfo() != null) {
				result = info.getApplicationsInfo().get(0).getStatus();
				helper.setInstalledLocation(info.getApplicationsInfo().get(0)
						.getInstalledLocation());
				if (isErrorStatus(result)) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							"Error on the Zend Server during application deployment: " //$NON-NLS-1$
									+ result.getName()
									+ "\nTo get more details, see Zend Server log file."); //$NON-NLS-1$
				}
				monitor.subTask(getStateLabel(result.getName()));
			}
		}
		monitor.done();
		return new SdkStatus(listener.getStatus());
	}

	private String getStateLabel(String name) {
		String firstLetter = name.substring(0, 1);
		firstLetter = firstLetter.toUpperCase();
		name = firstLetter + name.substring(1) + "..."; //$NON-NLS-1$
		return name;
	}

	private boolean isErrorStatus(ApplicationStatus result) {
		switch (result) {
		case ACTIVATE_ERROR:
		case DEACTIVATE_ERROR:
		case STAGE_ERROR:
		case UNSTAGE_ERROR:
		case UPLOAD_ERROR:
		case UNKNOWN:
			return true;
		default:
			return false;
		}
	}

}
