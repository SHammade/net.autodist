package net.autodist.loadproject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class ConvertWizardMainPage extends WizardPage {
	private IProject project;

	private Text txtProjectName;
	private Text txtNewProjectName;
	private Label lblNewLabel;

	public ConvertWizardMainPage(IProject project) {
		super("Convert Existing Project");
		this.project = project;
		setDescription("Convert an Java Project to a Distributed System Project");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		GridLayout gl_container = new GridLayout(2, false);
		gl_container.verticalSpacing = 3;
		container.setLayout(gl_container);
		
				Label lblProjectLabel = new Label(container, SWT.NONE);
				lblProjectLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblProjectLabel.setText("Project");
		
				txtProjectName = new Text(container, SWT.BORDER);
				txtProjectName.setEditable(false);
				txtProjectName.setText(this.project.getFullPath().toPortableString());
				txtProjectName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setText("New Project");
		
		txtNewProjectName = new Text(container, SWT.BORDER);
		GridData gd_txtNewProjectName = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_txtNewProjectName.widthHint = 436;
		txtNewProjectName.setLayoutData(gd_txtNewProjectName);
		txtNewProjectName.setText(getProjectName(this.project.getFullPath().toPortableString().concat("Distributed"), 1));
		txtNewProjectName.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if(!verifyProjectName(txtNewProjectName.getText()))
					txtNewProjectName.setFocus();
			}	
			
			@Override
			public void focusGained(FocusEvent e) {
			}
		});
	}

	private boolean verifyProjectName(String name)
	{
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        if (name.equals("")) {
            setErrorMessage(null);
            setMessage("Project name must be specified");
            return false;
        }
        IStatus nameStatus = workspace.validateName(name, IResource.PROJECT);
        if (!nameStatus.isOK()) {
            setErrorMessage(nameStatus.getMessage());
            return false;
        }
        if (existProject(name)) {
			setErrorMessage(String.format("The Project '%s' exist already! Please choose another name", name));
			return false;
		}
        setErrorMessage(null);
        return true;
	}

	@Override
	public boolean canFlipToNextPage() {
		if (getErrorMessage() != null) return false;
		String newProjectName = txtNewProjectName.getText();
		
		if(!verifyProjectName(newProjectName))
			return false;
		
		return true;
	}

	private boolean existProject(String projectName) {		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = workspaceRoot.getProject(projectName);
		return project.exists();
	}

	private String getProjectName(String projectName, int number) {
		if (existProject(projectName)) {
			projectName = getProjectName(projectName.concat("-").concat(Integer.toString(number)), number + 1);
		}
		if(projectName.charAt(0) == '/')
			projectName = projectName.substring(1);
		return projectName;
	}

	public IPath getNewProjectPath() {
		return new Path(this.txtNewProjectName.getText());
	}
}
