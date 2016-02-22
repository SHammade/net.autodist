package net.autodist.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.launching.*;
import org.osgi.framework.Bundle;

import net.autodist.configuration.ConfigurationConstants;
import net.autodist.thriftgenexecuter.ProcessThriftGenExecuter;
import net.autodist.thriftgenexecuter.ThriftGenExecuter;

/**
 * The class LibUtils is used to add dependencies to a project
 * @author Hammade, Retzlaff
 *
 */
public class LibUtils {
	
	/**
	 * Add the SLF4 Library to the project {@link javaProject}
	 * @param javaProject
	 * @throws CoreException
	 * @throws IOException
	 */
	public static void addSLF4JLibrary(IJavaProject javaProject) throws CoreException, IOException{
		IFolder libDirectory = JDTUtils.getLibDirectory(javaProject.getProject());
		IFile iSlf4jFile = libDirectory.getFile(ConfigurationConstants.SLF4J_LIB_FILE);
		File slf4jFile = iSlf4jFile.getRawLocation().makeAbsolute().toFile();
		download(ConfigurationConstants.SLF4J_LIB_URL, slf4jFile);
		libDirectory.refreshLocal(1, null);
	}
	
	/**
	 * Add the AspectJ Library to the project {@link javaProject}
	 * @param javaProject
	 * @throws CoreException
	 * @throws IOException
	 */
	public static void addAspectJLibrary(IJavaProject javaProject) throws CoreException, IOException{
		IFolder libDirectory = JDTUtils.getLibDirectory(javaProject.getProject());
		IFile iAspectJFile = libDirectory.getFile(ConfigurationConstants.ASPECTJ_LIB_FILE);
		File aspectJFile = iAspectJFile.getRawLocation().makeAbsolute().toFile();
		download(ConfigurationConstants.ASPECTJ_LIB_URL, aspectJFile);		
		libDirectory.refreshLocal(1, null);
	}
	
	/**
	 * Add the Thrift Library to the project {@link javaProject}
	 * @param javaProject
	 * @throws Exception
	 */
	public static void addThriftLibrary(IJavaProject javaProject, String rawThriftPath) throws Exception
	{
		File thriftBinaryPath = new File(rawThriftPath);
		ThriftGenExecuter thriftGenExecuter = new ProcessThriftGenExecuter();
		String thriftVersion = thriftGenExecuter.getVersion(thriftBinaryPath);
		IFolder libDirectory = JDTUtils.getLibDirectory(javaProject.getProject());
		IFile iThriftLibFile = libDirectory.getFile(ConfigurationConstants.THRIFT_LIB_FILE.replaceAll("VERSION", thriftVersion));
		File thriftLibFile = iThriftLibFile.getRawLocation().makeAbsolute().toFile();		
		download(ConfigurationConstants.THRIFT_LIB_URL.replaceAll("VERSION", thriftVersion), thriftLibFile);
		libDirectory.refreshLocal(1, null);
	}	
	
	/**
	 * Add the System Library to the  project {@link javaProject}
	 * @param javaProject
	 * @throws JavaModelException
	 */
	public static void addSystemLibrary(IJavaProject javaProject) throws JavaModelException
	{
		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>(Arrays.asList(javaProject.getRawClasspath()));
		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
		LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
		for (LibraryLocation element : locations) {
			entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
		}
		javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
	}
	
	/**
	 * Add all libraries from folder lib to the  project {@link javaProject}
	 * @param javaProject
	 * @throws CoreException
	 * @throws IOException
	 */
	public static void addLibraryFolderToClasspath(IJavaProject javaProject) throws CoreException, IOException
	{
		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>(Arrays.asList(javaProject.getRawClasspath()));
		
		Bundle bundle = Platform.getBundle(ConfigurationConstants.ASPECTJ_BUNDLE_NAME);
		if(bundle != null)
		{
			URL url = FileLocator.resolve(bundle.getEntry("/"));
			String path = url.getPath();
			if (path.startsWith("file:"))
				path = path.substring("file:".length());
			if (path.endsWith("!/"))
				path = path.substring(0, path.length() - 2);
			File file = new File(path);
			if (file.isDirectory()) {
				file = new File(file, "bin");
			}
			String absolute = file.getAbsolutePath();			
			IPath replacement = new Path(absolute);
			IClasspathEntry newEntry = JavaCore.newLibraryEntry(replacement, null, null);
			if(!entries.contains(newEntry))
				entries.add(newEntry);
		}
		
		IFolder libDirectory = JDTUtils.getLibDirectory(javaProject.getProject());
		for (IResource resource : libDirectory.members()) {
			if (resource instanceof IFile) {
				IFile iFile = (IFile) resource;
				if (iFile.getFileExtension().equals("jar")) {
					IClasspathEntry entry = JavaCore.newLibraryEntry(iFile.getFullPath(), null, null, false);
					if(!entries.contains(entry))
						entries.add(entry);
				}
			}
		}
		javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
	}
	
	/**
	 * Add a path to the classpath of the  project {@link javaProject}
	 * @param javaProject
	 * @param path
	 * @throws JavaModelException
	 */
	public static void addPathToClasspath(IJavaProject javaProject, IPath path) throws JavaModelException
	{
		IClasspathEntry entry = JavaCore.newSourceEntry(path); 
	    IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
	    IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
	    System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
	    newEntries[oldEntries.length] = entry;
	    javaProject.setRawClasspath(newEntries, null);
	}
	
	/**
	 * Download the URL {@link url} to File {@link file}
	 * @param url
	 * @param file
	 * @throws IOException
	 */
	private static void download(String url, File file) throws IOException {
		URL website = new URL(url);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(file);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}
}
