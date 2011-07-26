package org.zend.php.zendserver.deployment.core.debugger;

public enum DeploymentAttributes {

	BASE_PATH("basePath"), //$NON-NLS-1$
	
	TARGET_ID("targetId"), //$NON-NLS-1$
	
	APP_ID("appId"), //$NON-NLS-1$
	
	PROJECT_NAME("projectName"), //$NON-NLS-1$
	
	PARAMETERS("userParams"), //$NON-NLS-1$
	
	APPLICATION_NAME("applicationName"), //$NON-NLS-1$
	
	IGNORE_FAILURES("ignoreFailures"), //$NON-NLS-1$
	
	DEFAULT_SERVER("defaultServer"), //$NON-NLS-1$
	
	VIRTUAL_HOST("virtualHost"); //$NON-NLS-1$
	
	private String name;
	
	private DeploymentAttributes(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
