package net.autodist.utils;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.text.edits.TextEdit;
import org.osgi.framework.Bundle;

import net.autodist.configuration.ConfigurationConstants;

/**
 * The class JDTUtils is used as a helper class for the JDT API
 * @author Hammade, Retzlaff
 *
 */
public class JDTUtils {
	
	/**
	 * Gives an Array of Java files in the project
	 * @param project project in which the Java files are searched
	 * @return Gives an Array of Java files
	 * @throws CoreException
	 */
	public static File[] getJavaFiles(IProject project) throws CoreException {
		ArrayList<IFile> ifiles = new ArrayList<>();
		findFilesRecursively(project, ifiles, "java$");
		ArrayList<File> files = new ArrayList<>();
		for (IFile f : ifiles)
			files.add(f.getRawLocation().makeAbsolute().toFile());
		return files.toArray(new File[files.size()]);
	}

	/**
	 * Search for files with a given {@link pattern} in container {@link container} and add the files in the ArrayList {@link list} 
	 * @param container Location to search
	 * @param list Add files to these list
	 * @param pattern Regex {@link String.matches} for file search
	 * @throws CoreException
	 */
	public static void findFilesRecursively(IContainer container, ArrayList<IFile> list, String pattern)
			throws CoreException {
		for (IResource r : container.members()) {
			if (r instanceof IContainer) {
				findFilesRecursively((IContainer) r, list, pattern);
			} else if (r instanceof IFile && r.getFileExtension().matches(pattern)) {
				list.add((IFile) r);
			}
		}
		return;
	}

	/**
	 * Delete directory {@link ConfigurationConstants.TEMP_FOLDER} in {@link project}
	 * @param project Project, in which the directory {@link ConfigurationConstants.TEMP_FOLDER} should be deleted
	 * @throws CoreException
	 */
	public static void deleteTempDirectory(IProject project) throws CoreException {
		IFolder folder = project.getFolder(ConfigurationConstants.TEMP_FOLDER);
		if (folder.exists()) {
			folder.delete(true, null);
		}
		project.refreshLocal(IResource.DEPTH_INFINITE,null);
	}

	/**
	 * Get directory {@link ConfigurationConstants.TEMP_FOLDER} in {@link project}
	 * @param project  Project, in which the directory {@link ConfigurationConstants.TEMP_FOLDER} should return
	 * @return {@link ConfigurationConstants.TEMP_FOLDER}
	 * @throws CoreException
	 */
	public static IFolder getTempDirectory(IProject project) throws CoreException {
		IFolder folder = project.getFolder(ConfigurationConstants.TEMP_FOLDER);
		if (!folder.exists()) {
			folder.create(IResource.NONE, true, null);
			folder.setHidden(true);
		}
		project.refreshLocal(IResource.DEPTH_INFINITE,null);
		return folder;
	}
	
	/**
	 * Get directory {@link ConfigurationConstants.LIB_FOLDER} in {@link project}
	 * @param project  Project, in which the directory {@link ConfigurationConstants.LIB_FOLDER} should return
	 * @return {@link ConfigurationConstants.LIB_FOLDER}
	 * @throws CoreException
	 */
	public static IFolder getLibDirectory(IProject project) throws CoreException {
		IFolder folder = project.getFolder(ConfigurationConstants.LIB_FOLDER);
		if (!folder.exists()) {
			folder.create(IResource.NONE, true, null);
		}
		project.refreshLocal(IResource.DEPTH_INFINITE,null);
		return folder;
	}

	/**
	 * Read a file in a given {@link Bundle}
	 * @param file full filename
	 * @param bundleName Name of the bundle 
	 * @return Content of the file
	 * @throws IOException
	 */
	public static String readFile(String file, String bundleName) throws IOException {
		Bundle bundle = Platform.getBundle(bundleName);
		URL fileURL = bundle.getEntry(file);
		InputStream inputStream = fileURL.openConnection().getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer sb = new StringBuffer();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine);
			sb.append('\n');
		}
		in.close();
		return sb.toString();
	}

	/**
	 * Add the nature {@link id} to project {@link project}
	 * @param project
	 * @param id
	 * @throws CoreException
	 */
	public static void addNature(IProject project, String id) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] oldNatures = description.getNatureIds();
		String[] newNatures = new String[oldNatures.length + 1];
		System.arraycopy(oldNatures, 0, newNatures, 0, oldNatures.length);
		newNatures[oldNatures.length] = id;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}

	/**
	 * Add the Build Spec with the name {@link name} to project {@link project}
	 * @param project
	 * @param builderName
	 * @throws CoreException
	 */
	public static void addBuildSpec(IProject project, String builderName) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] oldCommand = description.getBuildSpec();
		ICommand[] newCommand = new ICommand[oldCommand.length + 1];
		System.arraycopy(oldCommand, 0, newCommand, 0, oldCommand.length);
		ICommand command = description.newCommand();
		command.setBuilderName(builderName);
		newCommand[oldCommand.length] = command;
		description.setBuildSpec(newCommand);
		project.setDescription(description, null);
	}

	/**
	 * Format a given ICompilationUnit and write it back
	 * @param unit
	 * @param monitor
	 * @throws JavaModelException
	 */
	public static void formatUnitSourceCode(ICompilationUnit unit, IProgressMonitor monitor) throws JavaModelException {
		CodeFormatter formatter = ToolFactory.createCodeFormatter(null);
		ISourceRange range = unit.getSourceRange();
		TextEdit formatEdit = formatter.format(CodeFormatter.K_COMPILATION_UNIT, unit.getSource(), range.getOffset(),
				range.getLength(), 0, null);
		if (formatEdit != null && formatEdit.hasChildren()) {
			unit.applyTextEdit(formatEdit, monitor);
		}
	}

	/**
	 * Copy the project {@link project} to {@link path}
	 * @param selectedProject
	 * @param path
	 * @return
	 * @throws CoreException
	 */
	public static IProject copyProject(IProject selectedProject, IPath path) throws CoreException {
		selectedProject.copy(path, IResource.NONE, null);
		IProject clientProject = ResourcesPlugin.getWorkspace().getRoot().getProject(path.lastSegment());
		clientProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		return clientProject;
	}

	/**
	 * Create a new project with name {@link projectName}, delete it if already exist! 
	 * @param projectName
	 * @return 
	 * @throws CoreException
	 */
	public static IProject createProject(String projectName) throws CoreException {
		if (projectName == null || projectName.isEmpty())
			throw new IllegalArgumentException("projectName is null or empty!");
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = workspaceRoot.getProject(projectName);

		if (project.exists()) {
			if (project.isOpen())
			{
				project.close(null);
			}
			project.delete(true, true, null);			
		}
		project = workspaceRoot.getProject(projectName);
		project.create(null);
		project.refreshLocal(IResource.DEPTH_INFINITE,null);
		if (!project.isOpen())
			project.open(null);
		project.refreshLocal(IResource.DEPTH_INFINITE,null);
		return project;
	}

	/**
	 * Create a package with name {@link packageName} in project {@link javaProject}
	 * @param javaProject
	 * @param packageName
	 * @return 
	 * @throws CoreException
	 */
	public static IPackageFragment createPackage(IJavaProject javaProject, String packageName) throws CoreException {
		if (javaProject == null || packageName == null)
			throw new NullPointerException();
		IFolder sourceFolder = javaProject.getProject().getFolder(ConfigurationConstants.SRC_FOLDER);
		if (!sourceFolder.exists())
			throw new IllegalArgumentException("no source folder found!");
		return javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment(packageName, false, null);
	}

	/**
	 * Array of supported primitiv types
	 */
	private static final PrimitiveType.Code[] PRIMITIVIES = { PrimitiveType.BYTE, PrimitiveType.SHORT,
			PrimitiveType.CHAR, PrimitiveType.INT, PrimitiveType.LONG, PrimitiveType.FLOAT, PrimitiveType.DOUBLE,
			PrimitiveType.BOOLEAN, PrimitiveType.VOID };

	public static Type getType(String type, AST ast) {
		PrimitiveType.Code variableType = null;
		for (PrimitiveType.Code code : PRIMITIVIES) {
			if (code.toString().equals(type)) {
				variableType = code;
			}
		}
		if (variableType != null)
			return ast.newPrimitiveType(variableType);
		if(type.contains("."))
		{
			int lastDot = type.lastIndexOf('.');
			String baseQualifiedName = type.substring(0, lastDot);
			String baseName = type.substring(lastDot + 1);
			Type baseType = ast.newQualifiedType(
					ast.newSimpleType(ast.newName(baseQualifiedName)),
					ast.newSimpleName(baseName));
			return baseType;
		}
		return ast.newSimpleType(ast.newSimpleName(type));
	}

	/**
	 * Create a Field in AST {@link ast} with name {@link name} and default value {@link value}
	 * @param ast
	 * @param name
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static FieldDeclaration getFieldDeclaration(AST ast, String name, Object value) {
		Expression initializerExpression = null;
		if (value instanceof Integer) {
			Integer i = (Integer) value;
			initializerExpression = ast.newNumberLiteral(i.toString());
		} else if (value instanceof String) {
			String s = (String) value;
			StringLiteral literal = ast.newStringLiteral();
			literal.setLiteralValue(s);
			initializerExpression = literal;
		}
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName(name));
		fragment.setInitializer(initializerExpression);
		FieldDeclaration newFieldDeclaration = ast.newFieldDeclaration(fragment);
		if (value instanceof Integer)
			newFieldDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));
		else if (value instanceof String)
			newFieldDeclaration.setType(ast.newSimpleType(ast.newSimpleName("String")));
		newFieldDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
		newFieldDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
		newFieldDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
		return newFieldDeclaration;
	}
}
