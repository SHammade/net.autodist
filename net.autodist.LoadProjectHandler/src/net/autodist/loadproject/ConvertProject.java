package net.autodist.loadproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

import net.autodist.nature.DistributedNature;


/**
 * Check and convert a project 
 * @author resh
 *
 */
public class ConvertProject  {
	private IProject project;
	private IPath newProjectPath;
	
	public ConvertProject(IProject project, IPath newProjectPath) {
		this.project = project;
		this.newProjectPath = newProjectPath;
	}

	public IStatus perform(IProgressMonitor monitor) {
		IProject project = null;
		try {
			IPath path = newProjectPath;
			this.project.copy(path, IResource.NONE, monitor);
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.lastSegment());
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Copy failed: The Project '"
					+ this.project.getFullPath() + "' couldn't copy to '" + newProjectPath + "'", e);				
		}
		try {
			if (project == null) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						"Convert failed: The Project '" + this.project.getFullPath() + "' couldn't found'");
			}
			if (!project.isOpen()) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						"Convert failed: The Project '" + this.project.getFullPath()
								+ "' is not open. Please right click on the project and call 'open project'");
			}
			if (!project.isNatureEnabled(JavaCore.NATURE_ID)) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						"Convert failed: The Project '" + this.project.getFullPath()
								+ "' is not a Java Project! Please convert it to a Java Project manually");
			}

			IProjectNature nature = new DistributedNature();
			nature.setProject(project);
			nature.configure();
			
			monitor.worked(10);
			
			IJavaProject javaProject = JavaCore.create(project);
			addClassPath(javaProject);
			project.refreshLocal(IResource.DEPTH_INFINITE,monitor);
			
			monitor.worked(10);
			
		} catch (Exception e) {

			try {
				project.delete(true, monitor);
				ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE,monitor);
			} catch (CoreException e1) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Convert failed: The Project '"
						+ this.project.getFullPath() + "' can't be refreshed.", e1);
			}
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Convert failed: The Project '"
					+ this.project.getFullPath() + "' can't converted. Please Report that error to the developer.", e);
		}
		return new Status(IStatus.OK, Activator.PLUGIN_ID, "Convert finished");
	}

	private static void addClassPath(IJavaProject javaProject) throws IOException, URISyntaxException, CoreException {
		IClasspathEntry[] entries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];

		System.arraycopy(entries, 0, newEntries, 0, entries.length);

		Bundle bundle = Platform.getBundle("net.autodist.LoadProjectHandler");
		URL fileURL = bundle.getEntry("lib/net.autodist.annotation.jar");
		URL resolvedFileURL = FileLocator.toFileURL(fileURL);
		URI resolvedURI = new URI(resolvedFileURL.getProtocol(), resolvedFileURL.getPath(), null);
		File file = new File(resolvedURI);

		IFolder newLibFolder = javaProject.getProject().getFolder("lib");
		if (!newLibFolder.exists())
			newLibFolder.create(true, true, null);

		IFile newLib = newLibFolder.getFile("net.autodist.annotation.jar");
		if (!newLib.exists()) {
			FileInputStream fileStream = new FileInputStream(file);
			newLib.create(fileStream, false, null);
		}
		IClasspathEntry junitEntry = JavaCore.newLibraryEntry(newLib.getFullPath(), null, null, false);
		newEntries[entries.length] = junitEntry;
		javaProject.setRawClasspath(newEntries, null);
	}
}
