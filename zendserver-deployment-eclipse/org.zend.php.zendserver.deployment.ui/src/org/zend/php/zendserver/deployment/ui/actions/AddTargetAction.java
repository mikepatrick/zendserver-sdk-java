package org.zend.php.zendserver.deployment.ui.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
import org.zend.php.zendserver.deployment.core.targets.TargetsManagerService;
import org.zend.php.zendserver.deployment.ui.Activator;
import org.zend.php.zendserver.deployment.ui.Messages;
import org.zend.php.zendserver.deployment.ui.targets.CreateTargetWizard;
import org.zend.sdklib.manager.TargetException;
import org.zend.sdklib.manager.TargetsManager;
import org.zend.sdklib.target.IZendTarget;

/**
 * Adds new Deployment Target via TargetDetailsDialog.
 *
 */
public class AddTargetAction extends Action {
	
	private IZendTarget addedTarget;


	public AddTargetAction() {
		super(Messages.AddTargetAction_AddNewTarget, Activator.getImageDescriptor(Activator.IMAGE_ADD_TARGET));
	}

	
	@Override
	public void run() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		
		CreateTargetWizard wizard = new CreateTargetWizard();
		WizardDialog dialog = wizard.createDialog(window.getShell());
		
		if (dialog.open() != Window.OK) {
			return; // canceled by user
		}
		
		IZendTarget[] newTarget = wizard.getTarget();
		
		TargetsManager tm = TargetsManagerService.INSTANCE.getTargetManager();
		try {
			for (IZendTarget t : newTarget) {
				tm.add(t);
				addedTarget = t;
			}
		} catch (TargetException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e), StatusManager.SHOW);
		}
	}

	/**
	 * @return Created target or null if target was not created.  
	 */
	public IZendTarget getTarget() {
		return addedTarget;
	}
}