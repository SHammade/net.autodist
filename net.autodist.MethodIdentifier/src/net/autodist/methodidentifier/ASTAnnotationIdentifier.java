package net.autodist.methodidentifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.*;

import net.autodist.DAO.Project;
import net.autodist.DAO.database.DbConnection;
import net.autodist.DAO.database.DbReader;
import net.autodist.DAO.database.DbWriter;
import net.autodist.annotationidentifier.parser.CompilationUnitASTVisitor;
import net.autodist.configuration.ConfigurationConstants;
import net.autodist.utils.JDTUtils;

/**
 * Search for Annotations with {@link ASTParser} and write back the annotated method and the attributes of the annotation 
 * @see ASTParser
 */
public class ASTAnnotationIdentifier implements AnnotationIdentifier {
	private IProject selectedProject;
	
	private CompilationUnitASTVisitor compilationUnitASTVisitor;
	private DbConnection dbConnection;

	/**
	 * analyze a {@link IProject} for annotations
	 * @param selectedProject selected Project in Workspace by User
	 */
	@Override
	public void analyze(IProject selectedProject) throws Exception {
		this.selectedProject = selectedProject;
		this.dbConnection = this.loadDatabase();
		DbReader dbReader = new DbReader(this.dbConnection);
		Project project = dbReader.readAllDataForFirstProject();
		this.compilationUnitASTVisitor = new CompilationUnitASTVisitor(project);
		
		DbWriter dbWriter = new DbWriter(this.dbConnection);
		analyseAllClasses();
		dbWriter.updateProjectIncludingDependencies(project);
	}

	/**
	 * load local Database from {@link IProject}
	 * @return {@link DbConnection}
	 * @throws CoreException
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	private DbConnection loadDatabase() throws CoreException, ClassNotFoundException, SQLException {
		IFile iDatabaseTempFile = JDTUtils.getTempDirectory(this.selectedProject).getFile(ConfigurationConstants.DATABASE_FILE);
		File databaseTempFile = iDatabaseTempFile.getRawLocation().makeAbsolute().toFile();
		return new DbConnection(false, databaseTempFile.getAbsolutePath());
	}

	/**
	 * Iterate over all java-Files in {@link IProject}
	 * @throws IOException
	 * @throws CoreException 
	 */
	public void analyseAllClasses() throws IOException, CoreException {
		File[] files = JDTUtils.getJavaFiles(this.selectedProject);
		for (File f : files) {
			String sourcecode = new String(Files.readAllBytes(f.toPath()));
			analyzeClass(sourcecode);
		}
	}
		
	/**
	 * Analyze a Class with {@link ASTParser} 
	 * @param source Source Code of a Class
	 */
	private void analyzeClass(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(source.toCharArray());
		CompilationUnit ast = (CompilationUnit) parser.createAST(null);
		ast.accept(this.compilationUnitASTVisitor);
	}
}
