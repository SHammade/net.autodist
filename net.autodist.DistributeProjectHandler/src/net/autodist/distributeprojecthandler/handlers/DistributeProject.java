package net.autodist.distributeprojecthandler.handlers;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.sql.SQLException;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

import net.autodist.DAO.Project;
import net.autodist.DAO.database.DbConnection;
import net.autodist.DAO.database.DbWriter;
import net.autodist.clientaspectgenerator.ClientAspectGenerator;
import net.autodist.clientaspectgenerator.JDTClientAspectGenerator;
import net.autodist.configuration.Activator;
import net.autodist.configuration.ConfigurationConstants;
import net.autodist.methodidentifier.ASTAnnotationIdentifier;
import net.autodist.methodidentifier.AnnotationIdentifier;
import net.autodist.servergenerator.JDTServerGenerator;
import net.autodist.servergenerator.ServerGenerator;
import net.autodist.thrift.gen.IdlBuilder;
import net.autodist.thrift.gen.ThriftIdlBuilder;
import net.autodist.thriftgenexecuter.ProcessThriftGenExecuter;
import net.autodist.thriftgenexecuter.ThriftGenExecuter;
import net.autodist.utils.JDTUtils;

/**
 * Distribute a project 
 * @author Hammade, Retzlaff
 */
public class DistributeProject {

	private String rawThriftPath;
	private IProject project;
	private String packageName;
	private IProgressMonitor monitor;
	private DbConnection connection;
	
	public DistributeProject(IProject project, String packageName, String rawThriftPath) {
		this.project = project;
		this.packageName = packageName;
		this.rawThriftPath = rawThriftPath;
		this.monitor = new NullProgressMonitor();
	}

	public IStatus perform(IProgressMonitor monitor) {
		try {
			cleanAutoDistConfigurations();			
			executeAnnotationIdentifier();
			executeThriftIDLGenerator();
			executeThriftGenExecuter();
			executeServerGenerator();
			executeClientAspectGenerator();
			this.connection.close();
			project.refreshLocal(IResource.DEPTH_INFINITE,this.monitor);
		} catch (Exception e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Distribution failed: The Project '"
					+ this.project.getFullPath() + "' couldn't distributed", e);				
		}
		return Status.OK_STATUS;
	}

	private void cleanAutoDistConfigurations() throws CoreException, IOException, URISyntaxException, SQLException, ClassNotFoundException {		
		JDTUtils.deleteTempDirectory(project);		
		this.connection = setupDatabase();
		DbWriter dbWriter = new DbWriter(this.connection);			
		Project p = new Project();
		p.setName(project.getName());
		p.setPackageName(this.packageName);			
		dbWriter.writeProject(p);
	}

	private DbConnection setupDatabase() throws CoreException, IOException, URISyntaxException, SQLException, ClassNotFoundException {
		Bundle bundle = Platform.getBundle("net.autodist.DAO");

		URL fileURL = bundle.getEntry(ConfigurationConstants.RESOURCE_FOLDER + "/" + ConfigurationConstants.SQL_FILE);
		URL resolvedFileURL = FileLocator.toFileURL(fileURL);
		URI resolvedURI = new URI(resolvedFileURL.getProtocol(), resolvedFileURL.getPath(), null);
		File bundleSQLFile = new File(resolvedURI);

		IFile iLocalSQLFile = JDTUtils.getTempDirectory(project).getFile(ConfigurationConstants.SQL_FILE);
		File localSQLFile = iLocalSQLFile.getRawLocation().makeAbsolute().toFile();
		Files.copy(bundleSQLFile.toPath(), localSQLFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

		IFile iDatabaseTempFile = JDTUtils.getTempDirectory(project)
				.getFile(ConfigurationConstants.DATABASE_FILE);
		File databaseTempFile = iDatabaseTempFile.getRawLocation().makeAbsolute().toFile();
		String localSQLFilePath = "'" + localSQLFile.getCanonicalPath().replace("[*]", "") + "'";
		DbConnection dbConnection = new DbConnection(true, localSQLFilePath, databaseTempFile.getAbsolutePath());
		dbConnection.createTables();
		return dbConnection;
	}
	
	
	private void executeAnnotationIdentifier() throws Exception {
		AnnotationIdentifier i = new ASTAnnotationIdentifier();
		i.analyze(project);
	}

	private void executeThriftIDLGenerator() throws Exception {
		IFolder tempDirectory = JDTUtils.getTempDirectory(project);
		IFile thriftIDLFile = tempDirectory.getFile(ConfigurationConstants.THRIFT_IDL_FILE);
		if (!thriftIDLFile.exists()) {
			IdlBuilder idlBuilder = new ThriftIdlBuilder();
			String idl = idlBuilder.generateIdlAsString(this.connection);
			byte[] bytes = idl.getBytes();
			InputStream source = new ByteArrayInputStream(bytes);
			thriftIDLFile.create(source, IResource.NONE, this.monitor);
		}
		project.refreshLocal(IResource.DEPTH_INFINITE,this.monitor);
	}

	private void executeThriftGenExecuter() throws Exception {
		File thriftBinaryPath = new File(rawThriftPath);
		IFolder tempDirectory = JDTUtils.getTempDirectory(project);
		IFolder iOutputLocation = tempDirectory.getFolder(ConfigurationConstants.THRIFTGEN_OUTPUT_FOLDER);
		if (!iOutputLocation.exists()) {
			iOutputLocation.create(IResource.NONE, true, this.monitor);
		}
		File outputLocation = iOutputLocation.getRawLocation().makeAbsolute().toFile();

		IFile iThriftIDLFile = tempDirectory.getFile(ConfigurationConstants.THRIFT_IDL_FILE);
		File thriftIDLFile = iThriftIDLFile.getRawLocation().makeAbsolute().toFile();

		ThriftGenExecuter thriftGenExecuter = new ProcessThriftGenExecuter();
		thriftGenExecuter.execute(thriftBinaryPath, thriftIDLFile, outputLocation, ConfigurationConstants.THRIFT_LANGUAGE);
	}

	private void executeServerGenerator() throws Exception {
		ServerGenerator serverGenerator = new JDTServerGenerator();
		serverGenerator.generate(project, rawThriftPath);
	}

	private void executeClientAspectGenerator() throws Exception {
		ClientAspectGenerator clientAspectGenerator = new JDTClientAspectGenerator();
		clientAspectGenerator.generateClient(project, rawThriftPath);
	}
}
