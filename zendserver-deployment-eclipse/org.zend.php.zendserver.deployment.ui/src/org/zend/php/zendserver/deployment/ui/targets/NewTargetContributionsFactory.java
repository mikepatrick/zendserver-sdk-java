package org.zend.php.zendserver.deployment.ui.targets;

import org.zend.php.zendserver.deployment.ui.Activator;
import org.zend.php.zendserver.deployment.ui.Messages;

public class NewTargetContributionsFactory {
	
	public static Contribution[] getElements() {
		return new Contribution[] {
				new Contribution(Messages.SelectTargetTypePage_ZendServer, Activator.IMAGE_ZEND, ZendTargetDetailsComposite.class),
				new Contribution(Messages.SelectTargetTypePage_DevCloud, Activator.IMAGE_CLOUD, DevCloudDetailsComposite.class),
				new Contribution("Detect Local", Activator.IMAGE_DETECT, DetectLocal.class),
		};
	}

}
