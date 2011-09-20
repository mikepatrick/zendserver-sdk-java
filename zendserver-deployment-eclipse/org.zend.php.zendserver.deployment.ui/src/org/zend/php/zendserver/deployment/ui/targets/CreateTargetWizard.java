package org.zend.php.zendserver.deployment.ui.targets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.zend.php.zendserver.deployment.ui.Messages;
import org.zend.sdklib.target.IZendTarget;

public class CreateTargetWizard extends Wizard {

	private SelectTargetTypePage typePage;
	private TargetDetailsPage detailsPage;
	private NewTargetWizardDialog dialog;
	private String type;
	private IZendTarget defaultTarget;
	
	private static class NewTargetWizardDialog extends WizardDialog {

		public NewTargetWizardDialog(Shell parentShell, IWizard newWizard) {
			super(parentShell, newWizard);
		}
		
		/* 
		 * make this method public, so that we can close wizard when user double-clicks target that doesn't require
		 * configuration.
		 */
		@Override
		public void finishPressed() {
			super.finishPressed();
		}
		
		@Override
		public void run(final boolean fork, final boolean cancelable,
				final IRunnableWithProgress runnable)
				throws InvocationTargetException, InterruptedException {
			Control currentFocus = getShell().getDisplay().getFocusControl();
			getButton(IDialogConstants.CANCEL_ID).setFocus();
			super.run(fork, cancelable, runnable);
			currentFocus.setFocus();
		}
	}
	
	public CreateTargetWizard() {
		super();
		setWindowTitle(Messages.AddTargetAction_AddTarget);
		setNeedsProgressMonitor(true);
	}
	
	public void addPages() {
		Contribution[] elements = NewTargetContributionsFactory.getElements();
		
		detailsPage = new TargetDetailsPage(elements);
		if (defaultTarget != null) {
			detailsPage.setDefaultTargetSettings(defaultTarget);
		}
		
		if (type != null) {
			detailsPage.setType(type);
		} else {
			typePage = new SelectTargetTypePage(elements);
			typePage.getSelectTargetType().addPropertyChangeListener(SelectTargetType.PROP_TYPE, new PropertyChangeListener() {
				
				public void propertyChange(PropertyChangeEvent evt) {
					detailsPage.setType(typePage.getSelectTargetType().getType());
					dialog.updateButtons();
				}
			});
			typePage.getSelectTargetType().addPropertyChangeListener(SelectTargetType.PROP_DOUBLECLICK, new PropertyChangeListener() {
				
				public void propertyChange(PropertyChangeEvent evt) {
					if (!detailsPage.hasPage(typePage.getSelectTargetType().getType())) {
						dialog.finishPressed();
					} else {
						dialog.showPage(detailsPage);
					}
				}
			});
			addPage(typePage);
		}
		addPage(detailsPage);
	}
	
	@Override
	public boolean performFinish() {
		IZendTarget target = detailsPage.getTarget();
		if(target != null) {
			return true;
		}
		
		try {
			detailsPage.validate();
		} catch (InvocationTargetException e) {
			// errored
			return false;
		} catch (InterruptedException e) {
			// cancelled
			return false;
		}
		
		if (!detailsPage.isPageComplete()) {
			return false;
		}
		
		return detailsPage.getTarget() != null;
	}
	
	public WizardDialog createDialog(Shell parentShell) {
		dialog = new NewTargetWizardDialog(parentShell, this);
		dialog.setTitle(Messages.AddTargetAction_AddTarget);
		
		return dialog;
	}

	public IZendTarget getTarget() {
		return detailsPage.getTarget();
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Sets default values for the fields.
	 * It also disables idField, because usually we don't want to change id.
	 * 
	 * @param targetComposite
	 */
	public void setDefaultTarget(IZendTarget target) {
		this.defaultTarget = target;
		if (detailsPage != null) {
			detailsPage.setDefaultTargetSettings(target);
		}
	}
	
	@Override
	public boolean canFinish() {
		return detailsPage.isPageComplete();
	}

}
