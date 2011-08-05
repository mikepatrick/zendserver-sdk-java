package org.zend.php.zendserver.deployment.ui.targets;

import java.io.IOException;

import org.eclipse.swt.widgets.Composite;
import org.zend.php.zendserver.deployment.ui.actions.DetectTargetAction;
import org.zend.sdklib.SdkException;
import org.zend.sdklib.target.IZendTarget;

public class DetectLocal extends AbstractTargetDetailsComposite {

	@Override
	public Composite create(Composite parent) {
		return null;
	}

	@Override
	public void setDefaultTargetSettings(IZendTarget defaultTarget) {
		// empty
	}

	@Override
	protected String[] getData() {
		return null;
	}

	@Override
	protected IZendTarget createTarget(String[] data) throws SdkException,
			IOException {
		
		DetectTargetAction detectTargetAction = new DetectTargetAction();
		detectTargetAction.run();
		return detectTargetAction.getDetectedTarget();
	}

}
