package net.autodist.servergenerator;

import org.eclipse.core.resources.IProject;

public interface ServerGenerator {
	
	/**
	 * Generate the server projects
	 * @param selectedProject project, from which the generated sources are copied
	 * @param rawThriftPath Thrift binary path
	 * @throws Exception
	 */
	public void generate(IProject selectedProject, String rawThriftPath) throws Exception;
}
