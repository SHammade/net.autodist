package net.autodist.distributeprojecthandler.handlers;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public class DistributeProjectJob extends WorkspaceJob {
	private DistributeProject distributeProject;
	public DistributeProjectJob(DistributeProject distributeProject) {
		super("Distribute Project");
		this.distributeProject = distributeProject;		
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Convert started", 100);
		IStatus status = this.distributeProject.perform(monitor);
		monitor.worked(10);
		monitor.done();
		return status;
	}
}
