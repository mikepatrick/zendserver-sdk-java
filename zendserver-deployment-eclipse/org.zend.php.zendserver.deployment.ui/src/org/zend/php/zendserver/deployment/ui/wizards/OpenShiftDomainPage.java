/*******************************************************************************
 * Copyright (c) 2011 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.php.zendserver.deployment.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.zend.sdklib.internal.target.OpenShiftTarget;

/**
 * Domain page.
 * 
 * @author Wojciech Galanciak, 2012
 * 
 */
public class OpenShiftDomainPage extends WizardPage {

	private Text domainNameText;
	private OpenShiftTarget target;

	protected OpenShiftDomainPage(OpenShiftTargetWizard wizard, OpenShiftTargetData data) {
		super(Messages.OpenShiftDomainPage_PageTitle);
		setDescription(Messages.OpenShiftDomainPage_EnterNameMessage);
		setTitle(Messages.OpenShiftDomainPage_PageDescription);
		this.target = new OpenShiftTarget(wizard.getUsername(),
				wizard.getPassword());
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 0;
		container.setLayout(layout);
		domainNameText = createLabelWithText(
				Messages.OpenShiftDomainPage_NameLabel, container);
		setControl(container);
		setPageComplete(validatePage());
	}

	public String getDomainName() {
		if (domainNameText != null) {
			return domainNameText.getText().trim();
		}
		return null;
	}

	public OpenShiftTarget getTarget() {
		return target;
	}

	private boolean validatePage() {
		setErrorMessage(null);
		if (domainNameText == null
				|| domainNameText.getText().trim().length() == 0) {
			setMessage(Messages.OpenShiftDomainPage_EnterNameMessage);
			return false;
		}
		setErrorMessage(null);
		setMessage(Messages.OpenShiftDomainPage_PageDescription);
		return true;
	}

	private Text createLabelWithText(String labelText, Composite container) {
		Composite parent = new Composite(container, SWT.NONE);
		parent.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		parent.setLayout(layout);
		Label label = new Label(parent, SWT.NONE);
		label.setText(labelText);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, true);
		gd.widthHint = 100;
		label.setLayoutData(gd);
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
		text.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				setPageComplete(validatePage());
			}
		});
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return text;
	}

}
