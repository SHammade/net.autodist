package net.autodist.distributeprojecthandler.handlers;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class PackageNameDialog extends TitleAreaDialog {
	private Combo comboPackageName;
	private ArrayList<String> packageNames;
	private String packageName;
	
	public PackageNameDialog(Shell parentShell, ArrayList<String> packageNames) {
		super(parentShell);
		this.packageNames = packageNames;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Enter Packagename");
		setMessage("Please enter a Packagename, in which the Classes will be generated.", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		create(container);

		return area;
	}

	private void create(Composite container) {
		Label lbtFirstName = new Label(container, SWT.NONE);
		lbtFirstName.setText("Package Name");

		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;

		comboPackageName = new Combo(container, SWT.NONE);
		comboPackageName.setLayoutData(gd);
		comboPackageName.setItems(this.packageNames.toArray(new String[this.packageNames.size()]));
		comboPackageName.select(this.packageNames.size() - 1);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private void saveInput() {
		int index = this.comboPackageName.getSelectionIndex();
		if (index != -1)
			packageName = this.packageNames.get(index);
		else
			packageName = this.comboPackageName.getText();
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	public String getPackageName() {
		return packageName;
	}
}
