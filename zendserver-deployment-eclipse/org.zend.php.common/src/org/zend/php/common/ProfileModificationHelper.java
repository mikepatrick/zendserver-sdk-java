package org.zend.php.common;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
import org.eclipse.equinox.internal.p2.ui.ElementQueryDescriptor;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.ProvUIActivator;
import org.eclipse.equinox.internal.p2.ui.ProvUIMessages;
import org.eclipse.equinox.internal.p2.ui.dialogs.ApplyProfileChangesDialog;
import org.eclipse.equinox.internal.p2.ui.model.InstalledIUElement;
import org.eclipse.equinox.internal.p2.ui.model.ProfileElement;
import org.eclipse.equinox.internal.p2.ui.query.InstalledIUElementWrapper;
import org.eclipse.equinox.internal.provisional.configurator.Configurator;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.RepositoryTracker;
import org.eclipse.equinox.p2.query.Collector;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.p2.ui.Policy;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

public class ProfileModificationHelper {

	private static final String LISTENERS_EXT_POINT = "org.zend.php.common.profileModificationListeners";

	static List<IInstallableUnit> toInstall;
	static List<IInstallableUnit> toUninstall;

	private static List<IProfileModificationListener> listeners;

	public static interface Callback {
		void runCallback();
	}
	public static IStatus modify(IProgressMonitor monitor,
			final List<CatalogItem> toAdd, final List<CatalogItem> toRemove,
			final int restartPolicy) {
		IStatus status = modify(monitor, getDescriptorIds(toAdd),
				getDescriptorIds(toRemove), restartPolicy);
		List<IProfileModificationListener> listeners = getModificationListeners();
		for (IProfileModificationListener listener : listeners) {
			listener.profileChanged(getIds(toAdd), getIds(toRemove), status);
		}
		return status;
	}

	public static boolean isFeatureInstalled(Object pluginid) {
		ProvisioningUI pui = ProvisioningUI.getDefaultUI();

		ProfileElement element = new ProfileElement(null, pui.getProfileId());

		IProfileRegistry pregistry = ProvUI.getProfileRegistry(pui.getSession());
		if (pregistry == null) { // for developers
			return true;
		}
		
		IProfile profile = pregistry.getProfile(pui.getProfileId());

		if (profile == null) { // for developers
			return true;
		}

		ElementQueryDescriptor queryDescriptor = new ElementQueryDescriptor(
				profile, QueryUtil.createCompoundQuery(pui.getPolicy()
						.getVisibleInstalledIUQuery(), QueryUtil
						.createIUPatchQuery(), false),
				new Collector<IInstallableUnit>(),
				new InstalledIUElementWrapper(profile, element));

		if (queryDescriptor != null) {
			Collection<?> results = queryDescriptor
					.performQuery(new NullProgressMonitor());
			for (Object item : results) {
				if (item instanceof InstalledIUElement) {
					if (((InstalledIUElement) item).getIU().getId()
							.equals(pluginid)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * @param toAdd
	 *            - ids of InstallableUnit
	 * @param toRemove
	 *            - ids of InstallableUnit
	 * @param restartPolicy
	 */
	public static IStatus modify(IProgressMonitor monitor,
			final Set<String> toAdd, final Set<String> toRemove,
			final int restartPolicy) {
		try {
			if ((toAdd == null || toAdd.isEmpty())
					&& (toRemove == null || toRemove.isEmpty())) {
				return Status.CANCEL_STATUS;
			}
			ProvisioningSession session = ProvisioningUI.getDefaultUI()
					.getSession();
			if (null != toAdd && !toAdd.isEmpty()) {
				toInstall = queryRepoInstallableUnits(toAdd);
			}
			if (null != toRemove && !toRemove.isEmpty()) {
				toUninstall = queryProfileInstallableUnits(toRemove);
			}
			ZendProfileChangeOperation op = new ZendProfileChangeOperation(
					session, toInstall, toUninstall);
			IStatus result = op.resolveModal(monitor);
			if (result.getSeverity() < IStatus.ERROR) {
				if (result.isMultiStatus()) {
					StatusManager.getManager().handle(result,
							StatusManager.SHOW);
				}
				result = op.getProvisioningJob(monitor).runModal(monitor);
			}
			if (result.getSeverity() < IStatus.ERROR) {
				requestRestart(restartPolicy);
			}
			return result;

		} catch (URISyntaxException e) {
			return handleException(e);
		} catch (MalformedURLException e) {
			return handleException(e);
		} catch (ProvisionException e) {
			return handleException(e);
		}
	}

	static void applyProfileChanges() {
		Configurator configurator = (Configurator) ServiceHelper.getService(
				ProvUIActivator.getContext(), Configurator.class.getName());
		try {
			configurator.applyConfiguration();
		} catch (IOException e) {
			ProvUI.handleException(e,
					ProvUIMessages.ProvUI_ErrorDuringApplyConfig,
					StatusManager.LOG | StatusManager.BLOCK);
		} catch (IllegalStateException e) {
			IStatus illegalApplyStatus = new Status(
					IStatus.WARNING,
					ProvUIActivator.PLUGIN_ID,
					0,
					ProvUIMessages.ProvisioningOperationRunner_CannotApplyChanges,
					e);
			ProvUI.reportStatus(illegalApplyStatus, StatusManager.LOG
					| StatusManager.BLOCK);
		}
	}

	public static void requestRestart(final int restartPolicy,final Callback callback) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				boolean restart=false;
				if (restartPolicy == Policy.RESTART_POLICY_FORCE) {
					PlatformUI.getWorkbench().restart();
					restart=true;
				}else if (restartPolicy == Policy.RESTART_POLICY_FORCE_APPLY) {
					applyProfileChanges();
					return;
				}else if (PlatformUI.getWorkbench().isClosing()){
					restart=true;
					return;
				}else{
					int retCode = ApplyProfileChangesDialog.promptForRestart(
							ProvUI.getDefaultParentShell(),
							restartPolicy == Policy.RESTART_POLICY_PROMPT);
					if (retCode == ApplyProfileChangesDialog.PROFILE_APPLYCHANGES) {
						applyProfileChanges();
					} else if (retCode == ApplyProfileChangesDialog.PROFILE_RESTART) {
						restart=true;
						PlatformUI.getWorkbench().restart();
					}
				}
				if (!restart && callback != null) {
					callback.runCallback();
				}
			}
		});
	}

	public static void requestRestart(final int restartPolicy) {
		requestRestart(restartPolicy, null);
	}

	private static IStatus handleException(Exception e) {
		Activator.log(e);
		return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
	}

	static public Set<String> getDescriptorIds(List<CatalogItem> toAddItems) {
		Set<String> installableUnits = new HashSet<String>();
		for (Object connector : toAddItems) {
			if (connector instanceof CatalogItem) {
				installableUnits.addAll(((CatalogItem) connector)
						.getInstallableUnits());
			}
		}
		return installableUnits;
	}

	static private IMetadataRepository loadRepository()
			throws MalformedURLException, URISyntaxException,
			ProvisionException {
		ProvisioningSession session = ProvisioningUI.getDefaultUI()
				.getSession();
		URI repo = getDefaultRepository();
		IMetadataRepositoryManager manager = (IMetadataRepositoryManager) session
				.getProvisioningAgent().getService(
						IMetadataRepositoryManager.SERVICE_NAME);
		return manager.loadRepository(repo, null);
	}

	static public List<IInstallableUnit> queryRepoInstallableUnits(
			Set<String> installableUnitIds) throws URISyntaxException,
			MalformedURLException, ProvisionException {
		IMetadataRepository repository = loadRepository();
		IQueryResult<IInstallableUnit> result = repository.query(
				QueryUtil.createIUGroupQuery(), null);
		return queryForIUs(installableUnitIds, result);
	}

	static public List<IInstallableUnit> queryProfileInstallableUnits(
			Set<String> installableUnitIds) throws URISyntaxException,
			MalformedURLException, ProvisionException {
		IProfile profile = ProvUI.getProfileRegistry(
				ProvisioningUI.getDefaultUI().getSession()).getProfile(
				IProfileRegistry.SELF);
		IQueryResult<IInstallableUnit> result = profile.query(
				QueryUtil.createIUGroupQuery(), null);
		return queryForIUs(installableUnitIds, result);
	}

	private static List<IInstallableUnit> queryForIUs(
			Set<String> installableUnitIds,
			IQueryResult<IInstallableUnit> result) {
		final List<IInstallableUnit> installableUnits = new ArrayList<IInstallableUnit>();
		for (Iterator<IInstallableUnit> iter = result.iterator(); iter
				.hasNext();) {
			IInstallableUnit iu = iter.next();
			String id = iu.getId();
			if (installableUnitIds.contains(id))
				installableUnits.add(iu);
		}
		return installableUnits;
	}

	public static URI getDefaultRepository() {
		URI location = null;
		RepositoryTracker repositoryTracker = ProvisioningUI.getDefaultUI()
				.getRepositoryTracker();
		ProvisioningSession session = ProvisioningUI.getDefaultUI()
				.getSession();
		if (repositoryTracker == null || session == null) {
			Activator
					.logErrorMessage("RepositoryTracker or ProvisioningSession was null.");
			return null;
		}
		try {
			String siteUrl = System.getProperty("com.zend.php.customization.site.url");
			if (siteUrl == null) {
				siteUrl = System.getProperty("org.zend.php.customization.site.url");
			}
			if (siteUrl != null && !siteUrl.isEmpty()) {
				location = new URI(siteUrl);
				repositoryTracker.addRepository(location, null, session);
			} else {
				URI[] knownRepositories = repositoryTracker
						.getKnownRepositories(session);
				for (URI repo : knownRepositories) {
					// TODO how to recognize Zend Extra Features Update Site???
					if (repo.toString().contains("extra")) {
						location = repo;
						// repositoryTracker.refreshRepositories(
						// new URI[] { repo }, session, null);
						break;
					}
				}
			}
		} catch (URISyntaxException e) {
			Activator.log(e);
		}

		return location;
	}

	private static List<IProfileModificationListener> getModificationListeners() {
		if (listeners == null) {
			List<IProfileModificationListener> result = new ArrayList<IProfileModificationListener>();
			IConfigurationElement[] elements = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(LISTENERS_EXT_POINT);
			for (IConfigurationElement element : elements) {
				if ("modificationListener".equals(element.getName())) { //$NON-NLS-1$
					try {
						Object listener = element
								.createExecutableExtension("class"); //$NON-NLS-1$
						if (listener instanceof IProfileModificationListener) {
							result.add((IProfileModificationListener) listener);
						}
					} catch (CoreException e) {
						Activator.log(e);
					}
				}
			}
			listeners = result;
		}
		return listeners;
	}

	private static List<String> getIds(List<CatalogItem> items) {
		List<String> result = new ArrayList<String>();
		for (CatalogItem item : items) {
			result.add(item.getId());
		}
		return result;
	}

}
