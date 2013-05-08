package org.zend.php.zendserver.deployment.debug.ui.contributions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.zend.php.zendserver.deployment.debug.ui.Activator;
import org.zend.php.zendserver.deployment.ui.contributions.ITestingSectionContribution;

public abstract class TestingSectionContribution implements ITestingSectionContribution {
	
	private String label;
	private String command;
	private String mode;
	private String image;

	public TestingSectionContribution(String command, String mode,
			String label, String image) {
		super();
		this.label = label;
		this.command = command;
		this.mode = mode;
		this.image = image;
	}

	public String getLabel() {
		return label;
	}

	public ImageDescriptor getIcon() {
		return Activator.getImageDescriptor(image);
	}

	public String getCommand() {
		return command;
	}

	public String getMode() {
		return mode;
	}

}
