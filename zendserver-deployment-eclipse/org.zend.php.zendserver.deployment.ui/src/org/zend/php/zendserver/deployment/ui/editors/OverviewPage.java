package org.zend.php.zendserver.deployment.ui.editors;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.menus.IMenuService;
import org.zend.php.zendserver.deployment.core.descriptor.DeploymentDescriptorPackage;
import org.zend.php.zendserver.deployment.core.descriptor.IDeploymentDescriptor;
import org.zend.php.zendserver.deployment.core.descriptor.IDescriptorContainer;
import org.zend.php.zendserver.deployment.ui.Messages;
import org.zend.php.zendserver.deployment.ui.actions.ExportApplicationAction;

public class OverviewPage extends DescriptorEditorPage {

	private static final String LINK_EXPORT = "export"; //$NON-NLS-1$

	private TextField name;
	private TextField summary;
	private TextField description;
	private TextField releaseVersion;
	private TextField apiVersion;
	private TextField license;
	private TextField icon;
	private TextField docRoot;
	private TextField appDir;

	private ResourceListSection persistent;

	private IDescriptorContainer fModel;

	public OverviewPage(DeploymentDescriptorEditor editor, IDescriptorContainer model) {
		super(editor, "overview", Messages.OverviewPage_Overview); //$NON-NLS-1$
		this.fModel = model;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);

		ScrolledForm form = managedForm.getForm();
		form.getBody().setLayout(
				FormLayoutFactory.createFormTableWrapLayout(true, 2));
		
		final FormToolkit toolkit = managedForm.getToolkit();
		final Composite body = managedForm.getForm().getBody();
		
		Composite left = toolkit.createComposite(body);
		left.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
		left.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		
		createGeneralInformationSection(managedForm, left);
		createPersistentResourcesSection(managedForm, left);

		Composite right = toolkit.createComposite(body);
		right.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
		right.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		
		createTestingSection(managedForm, right);
		createExportingSection(managedForm, right);

		form.reflow(true);
		
		showMarkers();
	}

	private void createPersistentResourcesSection(IManagedForm managedForm, Composite left) {
		persistent = new ResourceListSection(editor, managedForm,
				Messages.OverviewPage_PersistentResources,
				Messages.OverviewPage_PersistentResourcesDescription, left) {

			@Override
			public Object[] getElements(Object input) {
				List<String> list = editor.getModel().getPersistentResources();
				return list.toArray();
			}

			@Override
			protected void addPath() {
				Shell shell = editor.getSite().getShell();
				IProject root = editor.getProject();
				String[] newPaths = OpenFileDialog.openAny(shell, root,
						Messages.OverviewPage_AddPath,
						Messages.OverviewPage_SelectPath, 
						Messages.OverviewPage_2, null);
				if (newPaths == null) {
					return;
				}

				for (int i = 0; i < newPaths.length; i++) {
					editor.getModel().getPersistentResources().add(newPaths[i]);
				}
			}

			@Override
			protected void editPath(Object element) {
				String currPath = (String) element;

				Shell shell = editor.getSite().getShell();
				IProject root = editor.getProject();
				String newPath = OpenFileDialog.open(shell, root,
						Messages.OverviewPage_ChangePath,
						Messages.OverviewPage_SelectPath, currPath.toString());
				if (newPath == null) {
					return;
				}
				editor.getModel().getPersistentResources().remove(currPath);
				editor.getModel().getPersistentResources().add(newPath);
			}

			@Override
			protected void removePath(Object element) {
				String path = (String) element;
				editor.getModel().getPersistentResources().remove(path);
			}
		};

	}

	private void createExportingSection(IManagedForm managedForm, Composite body) {
		FormToolkit toolkit = managedForm.getToolkit();
		Section section = createStaticSection(toolkit, body,
				Messages.OverviewPage_Exporting);
		Composite container = createStaticSectionClient(toolkit, section);
		createClient(container, Messages.OverviewPage_PackageAndExport,
				toolkit, new HyperlinkAdapter() {
					public void linkActivated(HyperlinkEvent e) {
						handleLinkClick(e.data);
					}
				});
		section.setClient(container);
	}

	/**
	 * @param toolkit
	 * @param parent
	 * @return
	 */
	protected Composite createStaticSectionClient(FormToolkit toolkit,
			Composite parent) {
		Composite container = toolkit.createComposite(parent, SWT.NONE);
		container.setLayout(FormLayoutFactory
				.createSectionClientTableWrapLayout(false, 1));
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		container.setLayoutData(data);
		return container;
	}

	protected final Section createStaticSection(FormToolkit toolkit,
			Composite parent, String text) {
		Section section = toolkit.createSection(parent,
				ExpandableComposite.TITLE_BAR);
		section.clientVerticalSpacing = FormLayoutFactory.SECTION_HEADER_VERTICAL_SPACING;
		section.setText(text);
		section.setLayout(FormLayoutFactory
				.createClearTableWrapLayout(false, 1));
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		section.setLayoutData(data);
		return section;
	}

	protected final FormText createClient(Composite section, String content,
			FormToolkit toolkit, IHyperlinkListener hyperLink) {
		FormText text = toolkit.createFormText(section, true);
		try {
			text.setText(content, true, false);
		} catch (SWTException e) {
			text.setText(e.getMessage(), false, false);
		}
		text.addHyperlinkListener(hyperLink);
		return text;
	}

	private void createTestingSection(IManagedForm managedForm, Composite body) {
		FormToolkit toolkit = managedForm.getToolkit();

		Section section = toolkit.createSection(body,
				Section.DESCRIPTION | Section.TITLE_BAR | Section.EXPANDED);
		section.setText(Messages.OverviewPage_Testing);
		section.setDescription(Messages.OverviewPage_TestingDescr);
		final Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);
		sectionClient.setLayout(new GridLayout(1, false));

		ContributionManager contributionManager = new ContributionManager() {
			
			public void update(boolean force) {
				updateTestingActions(sectionClient, getItems());
			}
		};
		contributionManager.add(new GroupMarker("testing")); //$NON-NLS-1$
		IMenuService service = (IMenuService) getSite().getService(IMenuService.class);
		service.populateContributionManager(contributionManager, DeploymentDescriptorEditor.TOOLBAR_LOCATION_URI);
		contributionManager.update(false);
	}

	protected void updateTestingActions(Composite parent, IContributionItem[] items) {
		for (IContributionItem item : items) {
			item.fill(parent);
		}
	}

	private void createGeneralInformationSection(IManagedForm managedForm, Composite body) {
		IDeploymentDescriptor descr = editor.getModel();

		name = addField(new TextField(descr,
				DeploymentDescriptorPackage.PKG_NAME,
				Messages.OverviewPage_Name));
		summary = addField(new TextField(descr,
				DeploymentDescriptorPackage.SUMMARY,
				Messages.OverviewPage_Summary));
		description = addField(new TextField(descr,
				DeploymentDescriptorPackage.PKG_DESCRIPTION,
				Messages.OverviewPage_Description, SWT.MULTI|SWT.WRAP|SWT.V_SCROLL|SWT.RESIZE, false));
		releaseVersion = addField(new TextField(descr,
				DeploymentDescriptorPackage.VERSION_RELEASE,
				Messages.OverviewPage_0));
		apiVersion = addField(new TextField(descr,
				DeploymentDescriptorPackage.VERSION_API,
				Messages.OverviewPage_1));
		license = addField(new FileField(fModel,
				DeploymentDescriptorPackage.EULA,
				"License", editor.getProject())); //$NON-NLS-1$
		icon = addField(new FileField(fModel, DeploymentDescriptorPackage.ICON,
				Messages.OverviewPage_Icon, editor.getProject()));
		docRoot = addField(new FolderField(fModel,
				DeploymentDescriptorPackage.DOCROOT,
				Messages.OverviewPage_Docroot, editor.getProject()));
		appDir = addField(new TextField(descr,
				DeploymentDescriptorPackage.APPDIR,
				Messages.OverviewPage_Appdir));

		FormToolkit toolkit = managedForm.getToolkit();
		Section section = toolkit.createSection(body,
				Section.DESCRIPTION | Section.TITLE_BAR | Section.EXPANDED);
		section.setText(Messages.OverviewPage_GeneralInfo);
		section.setDescription(Messages.OverviewPage_GeneralInfoDescr);
		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);
		sectionClient.setLayout(new GridLayout(3, false));

		TableWrapData td = new TableWrapData();
		td.grabHorizontal = true;
		td.rowspan = 2;
		section.setLayoutData(td);

		name.create(sectionClient, toolkit);
		summary.create(sectionClient, toolkit);
		description.create(sectionClient, toolkit);
		((GridData)description.getText().getLayoutData()).heightHint = 50;
		
		releaseVersion.create(sectionClient, toolkit);
		apiVersion.create(sectionClient, toolkit);

		appDir.create(sectionClient, toolkit);
		docRoot.create(sectionClient, toolkit);
		
		license.create(sectionClient, toolkit);

		icon.create(sectionClient, toolkit);
	}

	protected void handleLinkClick(Object href) {
		if (LINK_EXPORT.equals(href)) {
			new ExportApplicationAction(editor.getProject()).run();
		} else if (href instanceof String) {
			editor.setActivePage((String) href);
		}
	}

	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		if (active) {
			refresh();
		}
	}

	@Override
	public void setFocus() {
		name.setFocus();
	}

	public void refresh() {
		name.refresh();
		summary.refresh();
		description.refresh();
		releaseVersion.refresh();
		apiVersion.refresh();
		license.refresh();
		icon.refresh();
		docRoot.refresh();
		appDir.refresh();
		persistent.refresh();
	}
}
