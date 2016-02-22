package net.autodist.clientaspectgenerator;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.*;
import org.osgi.framework.Bundle;

import net.autodist.DAO.Method;
import net.autodist.DAO.Parameter;
import net.autodist.DAO.Project;
import net.autodist.DAO.database.DbConnection;
import net.autodist.DAO.database.DbReader;
import net.autodist.DAO.database.views.ServerMethod;
import net.autodist.configuration.ConfigurationConstants;
import net.autodist.utils.JDTUtils;
import net.autodist.utils.LibUtils;

public class JDTClientAspectGenerator implements ClientAspectGenerator {	
	private IProject selectedProject;
	private Project project;
	private DbConnection dbConnection;
	private ArrayList<ServerMethod> serverMethods;
	private HashMap<Integer, String> thriftServiceName;
	private HashMap<Integer, String> thriftMethodName;
	@Override
	public void generateClient(IProject selectedProject, String rawThriftPath) throws Exception {		
		this.selectedProject = selectedProject;
		this.loadDatabase();
		
		IProject clientProject = copyProject(selectedProject);
		IJavaProject clientJavaProject = JavaCore.create(clientProject);

		this.addAJBuildSpec(clientJavaProject);
		this.addAJNature(clientJavaProject);
		this.addDependencies(clientJavaProject, rawThriftPath);
		this.generateAspect(clientJavaProject);
		IPackageFragment packageFragment = JDTUtils.createPackage(clientJavaProject, this.project.getPackageName());
		this.copyThriftClasses(clientJavaProject, packageFragment, this.project.getPackageName());
		clientProject.refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	private void copyThriftClasses(IJavaProject clientJavaProject, IPackageFragment packageFragment, String thriftPackageName) throws CoreException, IOException {
		IFolder tempDirectory = JDTUtils.getTempDirectory(this.selectedProject);
		IFolder thriftClassLocation = tempDirectory.getFolder(ConfigurationConstants.THRIFTGEN_OUTPUT_FOLDER);
		for(IResource thriftClassFile : thriftClassLocation.members())
		{
			if(thriftClassFile instanceof IFile)
			{
				IFile thriftClass = (IFile) thriftClassFile;
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
				packageFragment.createCompilationUnit(thriftClass.getName(), sb.toString(), false, null);
			}
		}
	}

	private void loadDatabase() throws CoreException, SQLException, ClassNotFoundException {
		IFile iDatabaseTempFile = JDTUtils.getTempDirectory(this.selectedProject).getFile(ConfigurationConstants.DATABASE_FILE);
		File databaseTempFile = iDatabaseTempFile.getRawLocation().makeAbsolute().toFile();
		this.dbConnection = new DbConnection(true, databaseTempFile.getAbsolutePath());
		DbReader dbReader = new DbReader(this.dbConnection);
		this.serverMethods = dbReader.readServerMethods();
		this.thriftServiceName = dbReader.readThriftServiceNames();
		this.thriftMethodName = dbReader.readThriftMethodNames();
		this.project = dbReader.readAllDataForFirstProject();
	}
	
	private void generateAspect(IJavaProject clientJavaProject) throws IOException, CoreException, SQLException
	{
		IPackageFragment iPackageFragment = JDTUtils.createPackage(clientJavaProject, this.project.getPackageName());
		String templateAspect = JDTUtils.readFile(ConfigurationConstants.RESOURCE_FOLDER + "/" + TemplateBuilder.T_ASPECT_FILE_NAME, "net.autodist.ClientAspectGenerator");
		String templatePointcut = JDTUtils.readFile(ConfigurationConstants.RESOURCE_FOLDER + "/" + TemplateBuilder.T_POINTCUT_FILE_NAME, "net.autodist.ClientAspectGenerator");
		String templateAdvice = JDTUtils.readFile(ConfigurationConstants.RESOURCE_FOLDER + "/" + TemplateBuilder.T_ADVICE_FILE_NAME, "net.autodist.ClientAspectGenerator");
		HashMap<String, String> defaultTypeValue = new HashMap<>();		
		defaultTypeValue.put("char", "0");
		defaultTypeValue.put("byte", "0");
		defaultTypeValue.put("short", "0");
		defaultTypeValue.put("int", "0");
		defaultTypeValue.put("long", "0");
		defaultTypeValue.put("float", "0");
		defaultTypeValue.put("double", "0");
		defaultTypeValue.put("void", "");
		defaultTypeValue.put("String", "\"\"");

		TemplateBuilder templateBuilder = new TemplateBuilder(templateAspect, templatePointcut, templateAdvice);
		for (ServerMethod serverMethod : this.serverMethods) {				
			DbReader reader = new DbReader(this.dbConnection);
			for (Integer methodId : serverMethod.getMethodIds()) {
				Method method = reader.readSpecificMethod(methodId.intValue());
				StringBuffer methodArgDef = new StringBuffer(",");
				StringBuffer methodArgsList = new StringBuffer();				
				Iterator<Parameter> it = method.getParameters().iterator();
				int argCounter = 0;
				while(it.hasNext())
				{
					Parameter p = it.next();
					methodArgDef.append(p.getType());
					methodArgDef.append(' ');
					methodArgDef.append(p.getName());
					methodArgsList.append(p.getName());
					if(it.hasNext())
					{
						methodArgsList.append(',');
						methodArgDef.append(',');
					}
					argCounter++;
				}
				
				if(argCounter == 0)
				{
					methodArgDef = new StringBuffer();
				}
				String defaultValue = "null";
				
				if(defaultTypeValue.containsKey(method.getReturn_Type()))
					defaultValue = defaultTypeValue.get(method.getReturn_Type());
				String clientClassName = thriftServiceName.get(method.getId()).concat(".Client");			
				templateBuilder.addMethod(thriftMethodName.get(method.getId()), methodArgDef.toString(), methodArgsList.toString(), method.getReturn_Type(), clientClassName, method.getName(), defaultValue);
			}
		}
		IFile aspectFile = clientJavaProject.getProject().getWorkspace().getRoot().getFile(iPackageFragment.getPath().append(TemplateBuilder.ASPECT_FILE_NAME));
	    aspectFile.refreshLocal(IResource.DEPTH_INFINITE,null);	    
		createAspect(aspectFile, iPackageFragment.getElementName(), templateBuilder.buildAspect());		
	}
	
	private void createAspect(IFile file, String packageName, String content) throws CoreException {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("package ");
		stringBuffer.append(packageName);
		stringBuffer.append(";\n");
		stringBuffer.append(content);
	    InputStream source = new ByteArrayInputStream(stringBuffer.toString().getBytes());
	    file.create(source, IResource.NONE, null);
	    file.refreshLocal(IResource.DEPTH_INFINITE,null);
	}
	
	private IProject copyProject(IProject selectedProject) throws CoreException
	{
		IWorkspaceRoot root = selectedProject.getWorkspace().getRoot();
		root.refreshLocal(IResource.DEPTH_INFINITE,null);
		IProject project = root.getProject(selectedProject.getName().concat("Client"));		
		root.refreshLocal(IResource.DEPTH_INFINITE,null);
		if (project.exists()) {
			if (project.isOpen())
				project.close(null);
			project.delete(true, false, null);
		}
		project = root.getProject(selectedProject.getName().concat("Client"));
		return JDTUtils.copyProject(selectedProject, project.getFullPath());
	}

	private void addDependencies(IJavaProject clientJavaProject, String rawThriftPath) throws Exception {
		Bundle bundle = Platform.getBundle(ConfigurationConstants.ASPECTJ_BUNDLE_NAME);
		if(bundle == null)
		{
			LibUtils.addAspectJLibrary(clientJavaProject);	
		}
		LibUtils.addSLF4JLibrary(clientJavaProject);
		LibUtils.addThriftLibrary(clientJavaProject, rawThriftPath);
		LibUtils.addLibraryFolderToClasspath(clientJavaProject);
	}
	
	private void addAJBuildSpec(IJavaProject clientJavaProject) throws CoreException 
	{
		JDTUtils.addBuildSpec(clientJavaProject.getProject(), org.eclipse.ajdt.core.AspectJPlugin.ID_BUILDER);
	}
	
	private void addAJNature(IJavaProject clientJavaProject) throws CoreException
	{
		if(!AspectJPlugin.isAJProject(clientJavaProject.getProject()))
		{
			JDTUtils.addNature(clientJavaProject.getProject(), org.eclipse.ajdt.core.AspectJPlugin.ID_NATURE);
		}
	}
}
