package net.autodist.loadproject;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.Wizard;

public class ConvertWizardDistributedProject extends Wizard {
	private ConvertWizardMainPage mainPage;
	private IProject project;

	public ConvertWizardDistributedProject(IProject project) {
		this.project = project;
		this.mainPage = new ConvertWizardMainPage(project);
		setWindowTitle("Convert Existing Projects");
	}

	@Override
	public void addPages() {
		addPage(mainPage);
	}
	
	@Override
	public boolean canFinish() {
		return mainPage.canFlipToNextPage();
	}
	
	@Override
	public boolean performFinish() {
		if(!canFinish())
			return false;
		
		ConvertProject convertProject = new ConvertProject(project, this.mainPage.getNewProjectPath());
		ConvertProjectJob job = new ConvertProjectJob(convertProject);
		job.setPriority(Job.LONG);
		job.setUser(true);
		job.schedule();	
		return true;
	}
}
