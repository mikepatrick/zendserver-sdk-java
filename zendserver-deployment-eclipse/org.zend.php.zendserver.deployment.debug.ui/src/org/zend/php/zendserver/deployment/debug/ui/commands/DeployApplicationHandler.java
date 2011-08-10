package org.zend.php.zendserver.deployment.debug.ui.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.zend.php.zendserver.deployment.debug.core.config.DeploymentHelper;
import org.zend.php.zendserver.deployment.debug.core.config.IDeploymentHelper;
import org.zend.php.zendserver.deployment.debug.core.config.LaunchUtils;
import org.zend.php.zendserver.deployment.debug.core.jobs.AbstractLaunchJob;
import org.zend.php.zendserver.deployment.debug.core.jobs.DeployLaunchJob;
import org.zend.php.zendserver.deployment.debug.core.jobs.UpdateLaunchJob;
import org.zend.php.zendserver.deployment.debug.ui.contributions.ApplicationContribution;
import org.zend.php.zendserver.deployment.debug.ui.wizards.DeploymentWizard;

public class DeployApplicationHandler extends AbstractDeploymentHandler {

	private AbstractLaunchJob job;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String mode = event.getParameter(ApplicationContribution.MODE);

		Object obj = event.getApplicationContext();
		IEvaluationContext ctx = null;
		if (obj instanceof IEvaluationContext) {
			ctx = (IEvaluationContext) obj;
		}

		IProject[] projects = null;
		String targetId = null;

		if (ctx != null) {
			projects = getProjects(ctx.getVariable(ApplicationContribution.PROJECT_NAME));
			targetId = (String) ctx.getVariable(ApplicationContribution.TARGET_ID);
		}
		if (projects == null) {
			projects = getProjects(event.getParameter(ApplicationContribution.PROJECT_NAME));
		}
		if (projects == null) {
			projects = new IProject[] { getProjectFromEditor() };
		}

		for (IProject project : projects) {
			execute(mode, project, targetId);
		}

		return null;
	}

	private void execute(final String mode, IProject project, String targetId) {
		ILaunchConfiguration config = LaunchUtils.findLaunchConfiguration(project, targetId);
		DeploymentHelper helper = null;
		if (config != null) {
			helper = DeploymentHelper.create(config);
		} else {
			helper = new DeploymentHelper();
			helper.setTargetId(targetId);
			helper.setOperationType(IDeploymentHelper.DEPLOY);
		}
		final DeploymentWizard wizard = new DeploymentWizard(project, helper);
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		WizardDialog dialog = new WizardDialog(shell, wizard);
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
			default:
				return;
			}
			job.setUser(true);
			job.schedule();
		}
	}

}
