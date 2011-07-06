package org.zend.php.zendserver.deployment.ui.editors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;
import org.zend.php.zendserver.deployment.ui.Activator;


public class OpenFileDialog {

	protected static final IStatus OK = new Status(IStatus.OK, Activator.PLUGIN_ID, ""); //$NON-NLS-1$

	private ElementTreeSelectionDialog dialog;
	
	public OpenFileDialog(Shell shell, IContainer root, String title, String description, String initialPath) {
		ILabelProvider lp = new WorkbenchLabelProvider();
		ITreeContentProvider cp = new WorkbenchContentProvider();
		dialog = new ElementTreeSelectionDialog(shell, lp, cp);
		
		IResource initialElement = null;
		if (initialPath != null) {
			initialElement = root.findMember(
					new Path(initialPath));
			if (initialElement == null || !initialElement.exists()) {
				initialElement = null;
			}
		}

		dialog.setTitle(title);
		dialog.setMessage(description);
		dialog.setInput(root);
		
		ISelectionStatusValidator validator = new ISelectionStatusValidator() {

			public IStatus validate(Object[] selection) {
				return OK;
			}
			
		};
		dialog.setValidator(validator);
		dialog.setInitialSelection(initialElement);
		
		dialog.setComparator(new ResourceComparator(
				ResourceComparator.NAME));
		dialog.setHelpAvailable(false);

	}
	
	public String openFile() {
		String[] out = open(false, new Class[] {IResource.class}, IFile.class);
		return out == null ? null : out[0];
	}
	
	public String openFolder() {
		String[] out = open(false, new Class[] {IContainer.class}, IFolder.class);
		return out == null ? null : out[0];
	}
	
	public String open() {
		String[] out = open(false, new Class[] {IResource.class}, IResource.class);
		return out == null ? null : out[0];
	}
	
	public String[] openMany() {
		return open(true, new Class[] {IResource.class}, IResource.class);
	}
	
	public static String openFile(Shell shell, IContainer root, String title, String description, String initialPath) {
		OpenFileDialog dialog = new OpenFileDialog(shell, root, title, description, initialPath);
		return dialog.openFile();
	}
	
	public static String openFolder(Shell shell, IContainer root, String title, String description, String initialPath) {
		OpenFileDialog dialog = new OpenFileDialog(shell, root, title, description, initialPath);
		return dialog.openFolder();
	}
	
	public static String open(Shell shell, IContainer root, String title, String description, String initialPath) {
		OpenFileDialog dialog = new OpenFileDialog(shell, root, title, description, initialPath);
		return dialog.open();
	}
	
	public static String[] openMany(Shell shell, IContainer root, String title, String description, String initialPath) {
		OpenFileDialog dialog = new OpenFileDialog(shell, root, title, description, initialPath);
		return dialog.openMany();
	}
	
	private String[] open(boolean allowMultiple, final Class[] visibleTypes, final Class selectable) {

		ViewerFilter filter = new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				for (int i = 0; i < visibleTypes.length; i++) {
					if (visibleTypes[i].isAssignableFrom(element.getClass())) {
						return true;
					}
				}
				return false;
			}
			
		};
		dialog.addFilter(filter);
		
		dialog.setAllowMultiple(allowMultiple);

		if (dialog.open() == Window.OK) {
			Object[] objects = dialog.getResult();
			if (objects.length == 0) {
				return null;
			}
			String[] paths = new String[objects.length];
			for (int i = 0; i < objects.length; i++) {
				IResource file = (IResource) objects[i];
				paths[i] = file.getProjectRelativePath().toString();
			}
			return paths;
		}
		
		return null;
	}

}
