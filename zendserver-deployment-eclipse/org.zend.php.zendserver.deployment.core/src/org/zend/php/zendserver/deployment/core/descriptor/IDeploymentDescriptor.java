package org.zend.php.zendserver.deployment.core.descriptor;

import java.util.List;

public interface IDeploymentDescriptor extends IModelContainer {
	
	String getName();

	void setName(String name);
	
	String getSummary();
	
	void setSummary(String summary);

	String getDescription();
	
	void setDescription(String descr);

	String getReleaseVersion();
	
	void setReleaseVersion(String ver);
	
	String getApiVersion();

	void setApiVersion(String ver);
	
	String getEulaLocation();
	
	void setEulaLocation(String value);

	String getIconLocation();
	
	void setIconLocation(String value);

	String getDocumentRoot();
	
	void setDocumentRoot(String folder);
	
	String getApplicationDir();
	
	void setApplicationDir(String value);

	String getScriptsRoot();

	void setScriptsRoot(String value);
	
	/**
	 * 
	 * @return URL or null
	 */
	String getHealthcheck();

	List<IParameter> getParameters();

	List<IVariable> getVariables();

	List<IPHPDependency> getPHPDependencies();
	
	List<IDirectiveDependency> getDirectiveDependencies();
	
	List<IExtensionDependency> getExtensionDependencies();
	
	List<IZendServerDependency> getZendServerDependencies();
	
	List<IZendFrameworkDependency> getZendFrameworkDependencies();
	
	List<IZendComponentDependency> getZendComponentDependencies();

	List<String> getPersistentResources();
}
