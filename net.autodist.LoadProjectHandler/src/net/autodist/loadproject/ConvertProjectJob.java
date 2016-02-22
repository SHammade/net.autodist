package net.autodist.loadproject;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 *  Workspace Job for Convert Project
 * @author Hammade, Retzlaff
 */
public class ConvertProjectJob extends WorkspaceJob{
	private ConvertProject convertProject;
	public ConvertProjectJob(ConvertProject convertProject) {
		super("Convert Project");
		this.convertProject = convertProject;		
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Convert started", 100);
		IStatus status = this.convertProject.perform(monitor);
		monitor.worked(10);
		monitor.done();
		return status;
	}
}
