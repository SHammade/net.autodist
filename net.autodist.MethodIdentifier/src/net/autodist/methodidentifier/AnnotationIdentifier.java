package net.autodist.methodidentifier;

import org.eclipse.core.resources.IProject;

public interface AnnotationIdentifier {
	public void analyze(IProject selectedProject) throws Exception;
}
