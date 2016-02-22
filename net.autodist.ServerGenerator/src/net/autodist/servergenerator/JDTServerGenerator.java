package net.autodist.servergenerator;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.osgi.framework.Bundle;

import net.autodist.DAO.Method;
import net.autodist.DAO.Parameter;
import net.autodist.DAO.Project;
import net.autodist.DAO.database.DbConnection;
import net.autodist.DAO.database.DbReader;
import net.autodist.DAO.database.views.ServerMethod;
import net.autodist.configuration.*;
import net.autodist.utils.JDTUtils;
import net.autodist.utils.LibUtils;


/**
 * The class JDTServerGenerator generate the server source code 
 * @author Hammade, Retzlaff
 *
 */
public class JDTServerGenerator implements ServerGenerator {

	// Project that the user has selected
	private IProject selectedProject;
	
	// Project data
	private Project project;
	
	private DbConnection dbConnection;
	
	// server-port combination, each entry will be one server project 
	private ArrayList<ServerMethod> serverMethods;
	
	// uniq method names
	private HashMap<Integer, String> thriftMethodName;
	
	@Override
	public void generate(IProject selectedProject, String rawThriftPath) throws Exception {
		this.selectedProject = selectedProject;
		this.loadDatabase();
				
		for (ServerMethod serverMethod : this.serverMethods) {				
		
			// create project
			IProject iProject = JDTUtils.createProject(serverMethod.getServiceName());
			
			// add Java Nature
			JDTUtils.addNature(iProject, JavaCore.NATURE_ID);
			IJavaProject javaProject = createJavaProject(iProject, rawThriftPath);
			
			// create package
			IPackageFragment packageFragment = JDTUtils.createPackage(javaProject, this.project.getPackageName());
			
			// copy thrift class to package
			copyThriftClass(packageFragment, this.project.getPackageName(), serverMethod.getServiceName());
			
			// generate server main class
			createServerClass(packageFragment, this.project.getPackageName(), serverMethod.getServer(),
					serverMethod.getPort(), serverMethod.getServiceName());
			
			// generate class with the thrift method implementation 
			createThriftServerClass(packageFragment, this.project.getPackageName(), serverMethod.getServiceName(),
					serverMethod.getMethodIds());
		}
	}
	
	/**
	 * load the Database and read the content
	 * @throws ClassNotFoundException 
	 */
	private void loadDatabase() throws CoreException, SQLException, ClassNotFoundException {
		IFile iDatabaseTempFile = JDTUtils.getTempDirectory(this.selectedProject).getFile(ConfigurationConstants.DATABASE_FILE);
		File databaseTempFile = iDatabaseTempFile.getRawLocation().makeAbsolute().toFile();
		this.dbConnection = new DbConnection(true, databaseTempFile.getAbsolutePath());
		DbReader dbReader = new DbReader(this.dbConnection);
		this.serverMethods = dbReader.readServerMethods();
		this.thriftMethodName = dbReader.readThriftMethodNames();
		this.project = dbReader.readAllDataForFirstProject();
	}

	/**
	 *  copy the generated thrift classes 
	 * @param packageFragment
	 * @param thriftPackageName
	 * @param projectName
	 * @throws CoreException
	 * @throws IOException
	 */
	private void copyThriftClass(IPackageFragment packageFragment, String thriftPackageName, String projectName)
			throws CoreException, IOException {
		IFolder tempDirectory = JDTUtils.getTempDirectory(this.selectedProject);
		IFolder thriftClassLocation = tempDirectory.getFolder(ConfigurationConstants.THRIFTGEN_OUTPUT_FOLDER);
		String fileName = projectName + ".java";
		IFile thriftClass = thriftClassLocation.getFile(fileName);
		thriftClass.refreshLocal(1, null);
		BufferedReader in = new BufferedReader(new InputStreamReader(thriftClass.getContents()));
		StringBuffer sb = new StringBuffer();
		if (!thriftPackageName.isEmpty()) {
			sb.append("package ");
			sb.append(packageFragment.getElementName().toString());
			sb.append(";\n\n");
		}
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine);
			sb.append('\n');
		}
		in.close();
		packageFragment.createCompilationUnit(fileName, sb.toString(), false, null);
	}

	/**
	 * create the server main class
	 * @param packageFragment
	 * @param thriftPackageName
	 * @param filename
	 * @return
	 * @throws IOException
	 * @throws JavaModelException
	 */
	private ICompilationUnit createClass(IPackageFragment packageFragment, String thriftPackageName, String filename)
			throws IOException, JavaModelException {
		Bundle bundle = Platform.getBundle("net.autodist.ServerGenerator");
		URL fileURL = bundle.getEntry(ConfigurationConstants.RESOURCE_FOLDER + "/" + filename);
		InputStream inputStream = fileURL.openConnection().getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer sb = new StringBuffer();
		if (!thriftPackageName.isEmpty()) {
			sb.append("package ");
			sb.append(packageFragment.getElementName().toString());
			sb.append(";\n\n");
		}
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine);
		}
		in.close();
		return packageFragment.createCompilationUnit(filename, sb.toString(), false, null);
	}

	private TypeDeclaration getThriftServerImplementationType(CompilationUnit compilationUnit, String className) {
		TypeDeclaration thriftServerImplementation = null;
		for (Object o : compilationUnit.types()) {
			if (o instanceof TypeDeclaration) {
				TypeDeclaration typeDecl = (TypeDeclaration) o;
				if (typeDecl.getName().getIdentifier().equals(className)) {
					thriftServerImplementation = typeDecl;
				}
			}
		}
		return thriftServerImplementation;
	}

	/**
	 * Import Thrift Iface class 
	 * @param icu
	 * @param thriftPackageName
	 * @param projectName
	 * @throws JavaModelException
	 * @throws IllegalArgumentException
	 * @throws MalformedTreeException
	 * @throws BadLocationException
	 * @throws IOException
	 */
	private void createImport(ICompilationUnit icu, String thriftPackageName, String projectName)
			throws JavaModelException, IllegalArgumentException, MalformedTreeException, BadLocationException,
			IOException {
		icu.becomeWorkingCopy(null);
		String importIface = thriftPackageName + "." + projectName + ".Iface";
		icu.createImport(importIface, null, null);

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(icu);
		parser.setResolveBindings(true);
		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);

		ASTRewrite astRewrite = ASTRewrite.create(compilationUnit.getAST());

		TextEdit edits = astRewrite.rewriteAST();
		Document document = new Document(icu.getSource());
		edits.apply(document);
		icu.getBuffer().setContents(document.get());
		icu.commitWorkingCopy(true, null);
	}
	
	/**
	 * generate the class with the thrift method implementation Iface
	 * @param packageFragment package in which the new class is created
	 * @param thriftPackageName package to be imported by the new class
	 * @param projectName class to be imported by the new class
	 * @param methodIds list of methods {@link Method}
	 * @throws IOException
	 * @throws JavaModelException
	 * @throws SQLException
	 * @throws MalformedTreeException
	 * @throws BadLocationException
	 */
	@SuppressWarnings("unchecked")
	private void createThriftServerClass(IPackageFragment packageFragment, String thriftPackageName, String projectName,
			ArrayList<Integer> methodIds)
					throws IOException, JavaModelException, SQLException, MalformedTreeException, BadLocationException {
		ICompilationUnit icu = createClass(packageFragment, thriftPackageName,
				ConfigurationConstants.THRIFTSERVERIMPLEMENTATION_TEMPLATE_FILENAME);
		createImport(icu, thriftPackageName, projectName);

		IType thriftServerImplementationClass = icu
				.getType(ConfigurationConstants.THRIFTSERVERIMPLEMENTATION_TEMPLATE_CLASSNAME);
		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(icu);
		parser.setResolveBindings(true);
		
		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		compilationUnit.recordModifications();
		
		AST ast = compilationUnit.getAST();
		ASTRewrite astRewrite = ASTRewrite.create(ast);

		TypeDeclaration thriftServerImplementation = getThriftServerImplementationType(compilationUnit,
				thriftServerImplementationClass.getElementName());

		icu.becomeWorkingCopy(null);
		
		ListRewrite methodListRewrite = astRewrite.getListRewrite(thriftServerImplementation, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		
		DbReader reader = new DbReader(this.dbConnection);

		// generate methods
		for (Integer methodId : methodIds) {
			Method method = reader.readSpecificMethod(methodId.intValue());

			MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
			methodDeclaration.setConstructor(false);

			@SuppressWarnings("rawtypes")
			List modifiers = methodDeclaration.modifiers();
			modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

			SimpleName simpleName = ast.newSimpleName(thriftMethodName.get(method.getId()));
			methodDeclaration.setName(simpleName);

			methodDeclaration.setReturnType2(JDTUtils.getType(method.getReturn_Type(), ast));
			
			for (Parameter parameter : method.getParameters()) {				
				SingleVariableDeclaration variableDeclaration = ast.newSingleVariableDeclaration();
				variableDeclaration.setType(JDTUtils.getType(parameter.getType(), ast));
				variableDeclaration.setName(ast.newSimpleName(parameter.getName()));
				methodDeclaration.parameters().add(variableDeclaration);
			}
			String methodBody = method.getBody();
			StringBuilder methodBodyBuilder = new StringBuilder(method.getBody());
			int openBracket = methodBody.indexOf('{');
			methodBodyBuilder.replace(openBracket, openBracket + 1, "");
			int closeBracket = methodBodyBuilder.toString().lastIndexOf('}');
			methodBodyBuilder.replace(closeBracket, closeBracket + 1, "");
			ASTParser methodParser = ASTParser.newParser(AST.JLS8);
			methodParser.setKind(ASTParser.K_STATEMENTS);
			methodParser.setSource(methodBodyBuilder.toString().toCharArray());
			methodParser.setResolveBindings(true);

			Block block = (Block) methodParser.createAST(null);
			block = (Block) ASTNode.copySubtree(ast, block);

			methodDeclaration.setBody(block);
			methodListRewrite.insertFirst(methodDeclaration, null);
		}
	 
		TextEdit edits = astRewrite.rewriteAST();
		Document document = new Document(icu.getSource());
		edits.apply(document);
		icu.getBuffer().setContents(document.get());
		icu.commitWorkingCopy(true, null);
		JDTUtils.formatUnitSourceCode(icu, null);
		icu.commitWorkingCopy(true, null);
	}

	/**
	 * generate server main class
	 * @param packageFragment
	 * @param thriftPackageName
	 * @param host
	 * @param port
	 * @param projectName
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws JavaModelException
	 * @throws MalformedTreeException
	 * @throws BadLocationException
	 */
	private void createServerClass(IPackageFragment packageFragment, String thriftPackageName, String host, int port,
			String projectName) throws URISyntaxException, IOException, JavaModelException, MalformedTreeException,
					BadLocationException {
		ICompilationUnit icu = createClass(packageFragment, thriftPackageName, ConfigurationConstants.SERVER_TEMPLATE_FILENAME);

		if (!thriftPackageName.isEmpty() && !packageFragment.getElementName().equals(thriftPackageName))
			icu.createImport(thriftPackageName + ".*", null, null);

		String importIface = thriftPackageName + "." + projectName + ".Iface";
		String importProcessor = thriftPackageName + "." + projectName + ".Processor";
		icu.createImport(importIface, null, null);
		icu.createImport(importProcessor, null, null);

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(icu);
		parser.setResolveBindings(true);
		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);

		String source = icu.getSource();
		Document document = new Document(source);

		ASTRewrite astRewrite = ASTRewrite.create(compilationUnit.getAST());
		compilationUnit.recordModifications();

		IType serverMainClass = icu.getType(ConfigurationConstants.SERVER_TEMPLATE_CLASSNAME);
		TypeDeclaration myTypeDeclaration = null;
		for (Object o : compilationUnit.types()) {
			if (o instanceof TypeDeclaration) {
				TypeDeclaration typeDecl = (TypeDeclaration) o;
				if (typeDecl.getName().getIdentifier().equals(serverMainClass.getElementName())) {
					myTypeDeclaration = typeDecl;
				}
			}
		}
		AST ast = myTypeDeclaration.getAST();
		ListRewrite listRewrite = astRewrite.getListRewrite(myTypeDeclaration,
				TypeDeclaration.BODY_DECLARATIONS_PROPERTY);

		listRewrite.insertFirst(JDTUtils.getFieldDeclaration(ast, "PORT", port), null);
		listRewrite.insertFirst(JDTUtils.getFieldDeclaration(ast, "HOST", host), null);

		icu.becomeWorkingCopy(null);

		TextEdit edits = astRewrite.rewriteAST();
		edits.apply(document);
		String newSource = document.get();
		icu.getBuffer().setContents(newSource);
		icu.commitWorkingCopy(true, null);
		JDTUtils.formatUnitSourceCode(icu, null);
		icu.commitWorkingCopy(true, null);
	}

	/**
	 * create a server project
	 * @param project
	 * @param rawThriftPath
	 * @return
	 * @throws Exception
	 */
	public static IJavaProject createJavaProject(IProject project, String rawThriftPath) throws Exception {
		IJavaProject javaProject = JavaCore.create(project);
		IFolder binFolder = project.getFolder("bin");
		if (!binFolder.exists())
			binFolder.create(false, true, null);
		javaProject.setOutputLocation(binFolder.getFullPath(), null);

	    IClasspathEntry[] entries = new IClasspathEntry[0];	    
	    javaProject.setRawClasspath(entries , null);
	    
		LibUtils.addSystemLibrary(javaProject);
		
		IFolder sourceFolder = project.getFolder("src");
		if (!sourceFolder.exists())
			sourceFolder.create(false, true, null);	

		LibUtils.addPathToClasspath(javaProject, sourceFolder.getFullPath());
		LibUtils.addThriftLibrary(javaProject, rawThriftPath);
		LibUtils.addSLF4JLibrary(javaProject);
		LibUtils.addLibraryFolderToClasspath(javaProject);
		return javaProject;
	}
	

}
