package net.autodist.clientaspectgenerator;

import org.eclipse.core.resources.IProject;

public interface ClientAspectGenerator {
	void generateClient(IProject selectedProject, String rawThriftPath) throws Exception;
}
