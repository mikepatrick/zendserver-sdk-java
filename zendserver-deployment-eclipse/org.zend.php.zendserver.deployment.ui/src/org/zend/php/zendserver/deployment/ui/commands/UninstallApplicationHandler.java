package org.zend.php.zendserver.deployment.ui.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.zend.php.zendserver.deployment.core.debugger.DeploymentAttributes;
import org.zend.php.zendserver.deployment.core.debugger.PHPLaunchConfigs;
import org.zend.php.zendserver.deployment.core.targets.PhpcloudContainerListener;
import org.zend.php.zendserver.deployment.ui.Activator;
import org.zend.php.zendserver.deployment.ui.Messages;
import org.zend.php.zendserver.monitor.core.MonitorManager;
import org.zend.sdklib.application.ZendApplication;
import org.zend.sdklib.internal.target.ZendDevCloud;
import org.zend.webapi.core.WebApiClient;
import org.zend.webapi.core.service.IRequestListener;

public class UninstallApplicationHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		EvaluationContext ctx = (EvaluationContext) event.getApplicationContext();
		
		Object element = ctx.getDefaultVariable();
		if (element instanceof List) {
			List<?> list = (List<?>) element;
			for (Object o : list) {
				uninstall(o);
			}
		}
		
		return null;
	}
	
	private void uninstall(Object element) {
		if (element instanceof ILaunchConfiguration) {
			final ILaunchConfiguration cfg = (ILaunchConfiguration) element;
			Job job = new Job(Messages.UninstallApplicationHandler_0) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					IRequestListener preListener = null;
					try {
						int appId = cfg.getAttribute(DeploymentAttributes.APP_ID.getName(), -1);
						String targetId = cfg.getAttribute(DeploymentAttributes.TARGET_ID.getName(), (String) null);
						String targetHost = cfg.getAttribute(DeploymentAttributes.TARGET_HOST.getName(), (String) null);
						if (targetHost != null
								&& targetHost.contains(ZendDevCloud.DEVPASS_HOST)) {
							preListener = new PhpcloudContainerListener(targetId);
							WebApiClient.registerPreRequestListener(preListener);
						}
						ZendApplication za = new ZendApplication();
						za.remove(targetId, Integer.toString(appId));
						PHPLaunchConfigs.preLaunchConfigurationRemoval(cfg);
						String projectName = cfg.getAttribute(
								DeploymentAttributes.PROJECT_NAME.getName(), (String) null);
						cfg.delete();
						if (targetId != null && projectName != null) {
							MonitorManager.remove(targetId, projectName);
						}
					} catch (CoreException e) {
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
					} finally {
						if (preListener != null) {
							WebApiClient.unregisterPreRequestListener(preListener);
						}
					}
					return Status.OK_STATUS;
				}
				
			};
			job.setUser(true);
			job.schedule();
			
		}
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		EvaluationContext ctx = (EvaluationContext) evaluationContext;
		Object obj = ctx.getDefaultVariable();
		boolean enabled = false;
		if (obj instanceof List) {
			List<?> list = (List<?>) obj;
			if (list.size() > 0) {
				enabled = true;
				for (Object o : list) {
					if (! (o instanceof ILaunchConfiguration)) {
						enabled = false;
					}
				}
			}
		}
		setBaseEnabled(enabled);
	}
	
}
