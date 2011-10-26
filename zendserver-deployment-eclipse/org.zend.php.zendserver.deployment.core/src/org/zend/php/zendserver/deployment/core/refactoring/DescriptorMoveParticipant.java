package org.zend.php.zendserver.deployment.core.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.zend.php.zendserver.deployment.core.descriptor.DescriptorContainerManager;
import org.zend.php.zendserver.deployment.core.descriptor.IDeploymentDescriptor;
import org.zend.php.zendserver.deployment.core.descriptor.IDescriptorContainer;

public class DescriptorMoveParticipant extends MoveParticipant {
	
	private IResource[] affectedResources;

	@Override
	protected boolean initialize(Object element) {
		if (element instanceof List) {
			List list = (List) element;
			List<IResource> result = new ArrayList<IResource>(list.size());
			for (Object item : list) {
				if (item instanceof IResource) {
					result.add((IResource) item);
				}
			}
			affectedResources = result.toArray(new IResource[result.size()]);
		}
		if (element instanceof IResource) {
			affectedResources = new IResource[] { (IResource) element };
		}
		return true;
	}

	@Override
	public String getName() {
		return "Application Deployment";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		MoveArguments args = getArguments();
		Object newPath = args.getDestination();
		if (! (newPath instanceof IContainer)) {
			return null;
		}
		
		IContainer newParent = (IContainer) newPath;
		
		IDescriptorContainer container = DescriptorContainerManager.getService().openDescriptorContainer(newParent.getProject());
		if (! container.getFile().exists()) {
			return null;
		}
		IDeploymentDescriptor descriptor = container.getDescriptorModel();
		DeploymentRefactoring r = new DeploymentRefactoring("move"); //$NON-NLS-1$
		
		boolean hasChanged = false;
		for (IResource affectedResource : affectedResources) {
			// move from one project to another - make no changes, leave it for validator to detect error
			if (newParent.getProject().equals(affectedResource.getProject())) {
				IPath projectRelativePath = affectedResource.getProjectRelativePath();
				String oldFullPath = projectRelativePath.toString();
				String newFullPath = newParent.getProjectRelativePath().append(affectedResource.getName()).toString();
				
				hasChanged |= r.updatePathInDescriptor(oldFullPath, newFullPath, descriptor);
			}
		}
		
		if (! hasChanged) {
			return null;
		}
		
		TextFileChange change = r.createDescriptorTextChange(container);
		
		return change;
	}

}
