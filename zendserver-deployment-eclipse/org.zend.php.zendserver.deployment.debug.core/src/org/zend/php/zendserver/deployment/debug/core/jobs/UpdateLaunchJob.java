package org.zend.php.zendserver.deployment.debug.core.jobs;

import org.eclipse.core.resources.IProject;
import org.zend.php.zendserver.deployment.core.debugger.IDeploymentHelper;
import org.zend.php.zendserver.deployment.debug.core.Messages;
import org.zend.sdklib.application.ZendApplication;
import org.zend.webapi.core.connection.data.ApplicationInfo;

public class UpdateLaunchJob extends DeploymentLaunchJob {

	public UpdateLaunchJob(IDeploymentHelper helper, IProject project) {
		super(Messages.updateJob_Title, helper, project);
	}

	@Override
	protected ApplicationInfo performOperation(ZendApplication app, String projectPath) {
		String appId = String.valueOf(helper.getAppId());
		return app.update(project.getLocation().toString(), helper.getTargetId(), appId,
				helper.getUserParams(), helper.isIgnoreFailures());
	}

}
