package org.zend.php.zendserver.deployment.core.descriptor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.zend.sdklib.mapping.IResourceMapping;

/**
 * Contains descriptor model. Container can be a file, URL, zip entry, or
 * whatever. Container provides read-only and read-write access to model
 * 
 */
public interface IDescriptorContainer {

	/**
	 * Descriptor model
	 */
	IDeploymentDescriptor getDescriptorModel();
	
	/**
	 * Mappings specification
	 */
	IResourceMapping getResourceMapping();
	
	/**
	 * @deprecated use getFileInstead
	 */
	IProject getProject();

	/**
	 * File containing descriptor, file may not exist yet  
	 * @return
	 */
	IFile getFile();

	void save();

	void connect(IDocument document);

}
