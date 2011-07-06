package org.zend.php.zendserver.deployment.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.zend.php.zendserver.deployment.ui.Activator;
import org.zend.php.zendserver.deployment.ui.wizards.PackageExportWizard;

public class ExportApplicationAction extends Action {

	/**
	 * The wizard dialog width
	 */
	private static final int SIZING_WIZARD_WIDTH = 500;

	/**
	 * The wizard dialog height
	 */
	private static final int SIZING_WIZARD_HEIGHT = 500;

	private IProject project;

	public ExportApplicationAction() {
		setText("Export");
		setToolTipText("Export application package");
		setImageDescriptor(Activator
				.getImageDescriptor(Activator.IMAGE_EXPORT_APPLICATION));
	}

	public ExportApplicationAction(IProject project) {
		this.project = project;
	}

	@Override
	public void run() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();

		PackageExportWizard wizard = new PackageExportWizard();
		wizard.setProject(project);
		wizard.setWindowTitle("Export application");
		WizardDialog dialog = createDialog(window.getShell(), wizard);
		dialog.open();
	}

	/**
	 * Utility method to create typical wizard dialog for
	 * ZendStudioFirstStartWizard
	 * 
	 * @param parent
	 * @param wizard
	 * @return
	 */
	public static WizardDialog createDialog(Shell parent,
			PackageExportWizard wizard) {
		WizardDialog dialog = new WizardDialog(parent, wizard);
		dialog.setHelpAvailable(false);
		dialog.create();
		dialog.getShell().setSize(SIZING_WIZARD_WIDTH, SIZING_WIZARD_HEIGHT);

		// Move the dialog to the center of the top level shell.
		Rectangle shellBounds = parent.getBounds();
		Point dialogSize = dialog.getShell().getSize();

		dialog.getShell().setLocation(
				shellBounds.x + (shellBounds.width - dialogSize.x) / 2,
				shellBounds.y + (shellBounds.height - dialogSize.y) / 2);

		// PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
		// IWorkbenchHelpContextIds.NEW_WIZARD);

		return dialog;
	}

}
