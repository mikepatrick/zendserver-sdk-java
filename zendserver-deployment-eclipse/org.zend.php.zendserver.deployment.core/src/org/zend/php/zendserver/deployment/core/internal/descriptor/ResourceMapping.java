package org.zend.php.zendserver.deployment.core.internal.descriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.zend.php.zendserver.deployment.core.descriptor.IMapping;
import org.zend.php.zendserver.deployment.core.descriptor.IResourceMapping;


public class ResourceMapping implements IResourceMapping {

	private Map<IPath, IMapping[]> mappingRules = new HashMap<IPath, IMapping[]>();
	private List<IPath> exclusions = new ArrayList<IPath>();

	public Map<IPath, IMapping[]> getMappingRules() {
		return mappingRules;
	}

	public List<IPath> getExclusions() {
		return exclusions;
	}

	void setMappingRules(Map<IPath, IMapping[]> mappingRules) {
		this.mappingRules = mappingRules;
	}

	void setExclusions(List<IPath> exclusions) {
		this.exclusions = exclusions;
	}

}
