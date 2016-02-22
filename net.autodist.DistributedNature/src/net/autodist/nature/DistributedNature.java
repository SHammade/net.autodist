package net.autodist.nature;

import java.util.ArrayList;
import java.util.Arrays;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

/**
 * Nature for AutoDist projects
 * @author Hammade, Retzlaff
 */
public class DistributedNature implements IProjectNature {
	public static final String NATURE_ID = "net.autodist.nature.DistributedNature";

	private IProject project;

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}
	
	@Override
	public void configure() throws CoreException {
		IProjectDescription description = this.project.getDescription();
		ArrayList<String> naturesList = new ArrayList<>(Arrays.asList(description.getNatureIds()));
		if(naturesList.contains(NATURE_ID))
			return;
		naturesList.add(NATURE_ID);
		description.setNatureIds(naturesList.toArray(new String[naturesList.size()]));
		this.project.setDescription(description, null);
	}

	@Override
	public void deconfigure() throws CoreException {
		IProjectDescription description = this.project.getDescription();
		ArrayList<String> naturesList = new ArrayList<>(Arrays.asList(description.getNatureIds()));
		if(naturesList.contains(NATURE_ID))
			naturesList.remove(NATURE_ID);
		description.setNatureIds(naturesList.toArray(new String[naturesList.size()]));
		this.project.setDescription(description, null);
	}
}
