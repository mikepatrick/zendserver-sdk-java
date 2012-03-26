package org.zend.php.common;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.ProvUIMessages;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.engine.IProvisioningPlan;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.p2.operations.ProfileModificationJob;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.planner.IPlanner;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.service.prefs.BackingStoreException;

public class RevertUtil {

	private static final String REVERT_TIMESTAMP = "revert_timestamp";
	private ProvisioningUI ui;
	
	public static void revertToPdt() {
		RevertUtil ru = new RevertUtil();
		
		IProfile snapshot = ru.figureOutLastPreStudioSnapshot();
		ru.revert(snapshot);
	}

	private IProfile figureOutLastPreStudioSnapshot() {
		long profileTimestamp = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID).getLong(REVERT_TIMESTAMP, 0);
		if (profileTimestamp == 0) {
			return null;
		}
		
		String profileId = getProvisioningUI().getProfileId();
		IProfileRegistry registry = ProvUI.getProfileRegistry(getSession());
		
		return ProvUI.getProfileRegistry(getProvisioningUI().getSession()).getProfile(profileId, profileTimestamp);
	}
	
	
	private void revert(final IProfile snapshot) {
		Job job = new UIJob("") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				doRevert(snapshot);
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}
	
	// inspired by org.eclipse.equinox.p2.ui.RevertProfilePage.revert()
	private boolean doRevert(final IProfile snapshot) {
		final String profileId = getProvisioningUI().getProfileId();
		
		if (snapshot == null)
			return false;
		final IProvisioningPlan[] plan = new IProvisioningPlan[1];
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				IProfile currentProfile;
				IProfileRegistry registry = ProvUI.getProfileRegistry(getSession());
				IPlanner planner = (IPlanner) getSession().getProvisioningAgent().getService(IPlanner.SERVICE_NAME);
				currentProfile = registry.getProfile(profileId);
				System.out.println("about to revert to "+snapshot.getTimestamp()+" from "+currentProfile.getTimestamp());
				plan[0] = planner.getDiffPlan(currentProfile, snapshot, monitor);
			}
		};
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, true, runnable);
		} catch (InvocationTargetException e) {
			ProvUI.handleException(e.getCause(), null, StatusManager.SHOW | StatusManager.LOG);
		} catch (InterruptedException e) {
			// nothing to report
		}
		// ignore dialog cancel, because Revert as a part of license mechanism, cannot be canceled. 

		boolean reverted = false;
		if (plan[0] != null) {
			if (plan[0].getStatus().isOK()) {
				// We use a default provisioning context (all repos) because we have no other
				// way currently to figure out which sites the user wants to contact
				ProfileModificationJob op = new ProfileModificationJob(ProvUIMessages.RevertDialog_RevertOperationLabel, getSession(), profileId, plan[0], new ProvisioningContext(getSession().getProvisioningAgent()));
				// we want to force a restart (not allow apply changes)
				op.setRestartPolicy(ProvisioningJob.RESTART_ONLY);
				getProvisioningUI().schedule(op, StatusManager.SHOW | StatusManager.LOG);
				reverted = true;
			} else if (plan[0].getStatus().getSeverity() != IStatus.CANCEL) {
				ProvUI.reportStatus(plan[0].getStatus(), StatusManager.LOG | StatusManager.SHOW);
				// This message has no effect in an installation dialog
				// setMessage(ProvUIMessages.ProfileModificationWizardPage_UnexpectedError, IMessageProvider.ERROR);
			}
		}
		return reverted;
	}

	private Shell getShell() {
		return Display.getDefault().getActiveShell();
	}

	private ProvisioningSession getSession() {
		return getProvisioningUI().getSession();
	}
	
	ProvisioningUI getProvisioningUI() {
		// if a UI has not been set then assume that the current default UI is the right thing
		if (ui == null)
			return ui = ProvisioningUI.getDefaultUI();
		return ui;
	}
	

	public boolean canRevert() {
		return figureOutLastPreStudioSnapshot() != null;
	}
	
	public void setRevertTimestamp() {
		String currentProfileId = getProvisioningUI().getProfileId();
		IProfileRegistry registry = ProvUI.getProfileRegistry(getSession());
		IProfile currentProfile = registry.getProfile(currentProfileId);
		
		long[] timestamps = registry.listProfileTimestamps(currentProfileId);
		
		Arrays.sort(timestamps);
		
		// why -2? we'll take last but one profile. because the last one is the current, which is going to be modified if user continues.
		long lastTimestamp = timestamps[timestamps.length - 2];
		
		IEclipsePreferences node = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		node.putLong(REVERT_TIMESTAMP, lastTimestamp);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}
}
