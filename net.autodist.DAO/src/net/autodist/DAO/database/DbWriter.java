package net.autodist.DAO.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.autodist.DAO.Annotation;
import net.autodist.DAO.Attribute;
import net.autodist.DAO.Method;
import net.autodist.DAO.Parameter;
import net.autodist.DAO.Project;
import net.autodist.DAO.Thrown_Exception;

/**
 * This Class is used to write the Database Representation Objects net.autodist.DAO.*
 * @author Retzlaff, Hammade
 */
public class DbWriter {
	private static final String SQL_PREP_INSERT_ANNOTATION = "INSERT INTO ANNOTATION (TYPE, MethodID) VALUES (?,?)";
	private static final String SQL_PREP_DELETE_ANNOTATION = "DELETE FROM ANNOTATION";
	private static final String SQL_PREP_INSERT_ATTRIBUTE = "INSERT INTO ATTRIBUTE (ANNOTATIONSID, TYPE, VALUE) VALUES (?,?,?)";
	private static final String SQL_PREP_DELETE_ATTRIBUTE = "DELETE FROM ATTRIBUTE";
	private static final String SQL_PREP_DELETE_METHOD = "DELETE FROM METHOD";
	private static final String SQL_PREP_INSERT_METHOD = "INSERT INTO METHOD (PATH ,NAME ,PROJECTID ,RETURN_TYPE ,SOURCE ,BODY) VALUES (?,?,?,?,?,?)";
	private static final String SQL_PREP_INSERT_PARAMETER = "INSERT INTO PARAMETER (TYPE, NAME, METHODID) VALUES (?,?,?)";
	private static final String SQL_PREP_DELETE_PARAMETER = "DELETE FROM PARAMETER";
	private static final String SQL_PREP_INSERT_PROJECT = "INSERT INTO PROJECT (NAME, PACKAGE) VALUES (?, ?)";
	private static final String SQL_PREP_INSERT_THROWN_EXCEPTION = "INSERT INTO THROWN_EXCEPTION (METHODID, EXCEPTION) VALUES (?,?)";
	private static final String SQL_PREP_DELETE_THROWN_EXCEPTION = "DELETE FROM THROWN_EXCEPTION";

	private DbConnection conn;

	private PreparedStatement projectInsert;
	private PreparedStatement methodInsert;
	private PreparedStatement thrownExeptionsInsert;
	private PreparedStatement annotationsInsert;
	private PreparedStatement attributeInsert;
	private PreparedStatement parameterInsert;
	private PreparedStatement thrownExeptionsDelete;
	private PreparedStatement parameterDelete;
	private PreparedStatement attributeDelete;
	private PreparedStatement annotationDelete;
	private PreparedStatement methodDelete;

	/**
	 * Constructor, to be used with an existing Database Connection. Since the
	 * Database is Embedded, there is only one connection possible.
	 * 
	 * @param conn
	 *            the database connection of Type DbConnection
	 */
	public DbWriter(DbConnection conn) {
		this.conn = conn;
		this.createPreparedStatements();
	}

	/**
	 * Constructor which opens a new DbConnection with Autocommit=false
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public DbWriter() throws ClassNotFoundException, SQLException {
		this.conn = new DbConnection(false);
		this.createPreparedStatements();
	}

	private void createPreparedStatements() {
		this.methodInsert = conn.prepareStatement(SQL_PREP_INSERT_METHOD);
		this.projectInsert = conn.prepareStatement(SQL_PREP_INSERT_PROJECT);
		this.thrownExeptionsInsert = conn.prepareStatement(SQL_PREP_INSERT_THROWN_EXCEPTION);
		this.annotationsInsert = conn.prepareStatement(SQL_PREP_INSERT_ANNOTATION);
		this.attributeInsert = conn.prepareStatement(SQL_PREP_INSERT_ATTRIBUTE);
		this.parameterInsert = conn.prepareStatement(SQL_PREP_INSERT_PARAMETER);

		this.thrownExeptionsDelete = conn.prepareStatement(SQL_PREP_DELETE_THROWN_EXCEPTION);
		this.parameterDelete = conn.prepareStatement(SQL_PREP_DELETE_PARAMETER);
		this.attributeDelete = conn.prepareStatement(SQL_PREP_DELETE_ATTRIBUTE);
		this.annotationDelete = conn.prepareStatement(SQL_PREP_DELETE_ANNOTATION);
		this.methodDelete = conn.prepareStatement(SQL_PREP_DELETE_METHOD);
	}

	/**
	 * 
	 * Persists a Project object to the Database, and adds the autogenerated ID to it
	 * @param project
	 * @return the autogenerated Project Id
	 * @throws SQLException
	 */
	public int writeProject(Project project) throws SQLException {
		projectInsert.setString(1, project.getName());
		projectInsert.setString(2, project.getPackageName());
		int affectedRows = projectInsert.executeUpdate();
		if (affectedRows == 0) {
			throw new SQLException("insert project failed, no rows affected.");
		} else {
			project.setId(this.getGeneratedKey(methodInsert));
		}
		return project.getId();
	}

	/**
	 * Persists a Project object to the Database, and adds the autogenerated ID to it
	 * @param method
	 * @return the autogenerated Method Id
	 * @throws SQLException
	 */
	public int writeMethod(Method method) throws SQLException {
		methodInsert.setString(1, method.getPath());
		methodInsert.setString(2, method.getName());
		methodInsert.setInt(3, method.getProject().getId());
		methodInsert.setString(4, method.getReturn_Type());
		methodInsert.setString(5, method.getSource());
		methodInsert.setString(6, method.getBody());

		int affectedRows = methodInsert.executeUpdate();
		if (affectedRows == 0) {
			throw new SQLException("insert method failed, no rows affected.");
		} else {
			method.setId(this.getGeneratedKey(methodInsert));
		}
		return method.getId();
	}

	/**
	 * 
	 * Persists a Thrown_Exception object to the Database, and adds the autogenerated ID to it
	 * 
	 * @param Thrown_Exception te
	 * @return the autogenerated Thrown_Exception Id
	 * @throws SQLException
	 */
	public int writeThrownExceptions(Thrown_Exception te) throws SQLException {
		thrownExeptionsInsert.setInt(1, te.getMethod().getId());
		thrownExeptionsInsert.setString(2, te.getException());

		int affectedRows = thrownExeptionsInsert.executeUpdate();
		if (affectedRows == 0) {
			throw new SQLException("insert thrownExeception failed, no rows affected.");
		} else {
			te.setId(this.getGeneratedKey(thrownExeptionsInsert));
		}
		return te.getId();
	}

	/**
	 * Persists a Annotation object to the Database, and adds the autogenerated ID to it
	 * 
	 * @param annotation
	 * @return the autogenerated Annotation Id
	 * @throws SQLException
	 */
	public int writeAnnotation(Annotation annotation) throws SQLException {
		annotationsInsert.setString(1, annotation.getType());
		annotationsInsert.setInt(2, annotation.getMethod().getId());

		int affectedRows = annotationsInsert.executeUpdate();
		if (affectedRows == 0) {
			throw new SQLException("insert annotation failed, no rows affected.");
		} else {
			annotation.setId(this.getGeneratedKey(annotationsInsert));
		}
		return annotation.getId();
	}

	/**
	 * Persists a Attribute object to the Database, and adds the autogenerated ID to it
	 * 
	 * @param attribute
	 * @return the autogenerated Attribute Id
	 * @throws SQLException
	 */
	public int writeAttribute(Attribute attribute) throws SQLException {
		attributeInsert.setInt(1, attribute.getAnnotation().getId());
		attributeInsert.setString(2, attribute.getType());
		attributeInsert.setString(3, attribute.getValue());

		int affectedRows = attributeInsert.executeUpdate();
		if (affectedRows == 0) {
			throw new SQLException("insert attribute failed, no rows affected.");
		} else {
			attribute.setId(this.getGeneratedKey(attributeInsert));
		}
		return attribute.getId();
	}

	
	/**
	 * Persists a Parameter object to the Database, and adds the autogenerated ID to it
	 * 
	 * @param parameter
	 * @return the autogenerated Parameter Id
	 * @throws SQLException
	 */
	public int writeParameter(Parameter parameter) throws SQLException {
		parameterInsert.setString(1, parameter.getType());
		parameterInsert.setString(2, parameter.getName());
		parameterInsert.setInt(3, parameter.getMethod().getId());

		int affectedRows = parameterInsert.executeUpdate();
		if (affectedRows == 0) {
			throw new SQLException("insert parameter failed, no rows affected.");
		} else {
			parameter.setId(this.getGeneratedKey(parameterInsert));
		}
		return parameter.getId();
	}

	
	/**
	 * gets the Autogenerated Key for a Prepared Statement that has just been executed
	 * 
	 * @param aPreparedStatement that has been executed ang triggered an autogeneration of a Key
	 * @return the Autobenerated Key
	 * @throws SQLException
	 */
	private int getGeneratedKey(PreparedStatement aPreparedStatement) throws SQLException {
		try (ResultSet generatedKeys = aPreparedStatement.getGeneratedKeys()) {
			if (generatedKeys.next()) {
				return generatedKeys.getInt(1);
			} else {
				throw new SQLException("Creating user failed, no ID obtained.");
			}
		}
	}

	/**
	 * persists a Method inclusive all related objects to the database
	 * 
	 * @param method
	 * @return
	 * @throws SQLException
	 */
	private boolean writeMethodIncludingDependencies(Method method) throws SQLException {
		boolean ok = true;
		if (this.writeMethod(method) < 1) {
			ok = false;
		} else {
			ArrayList<Parameter> parameters = method.getParameters();
			for (Parameter parameter : parameters) {
				if (this.writeParameter(parameter) < 1) {
					ok = false;
				}
			}
			ArrayList<Thrown_Exception> thrownExceptions = method.getThrown_Exceptions();
			for (Thrown_Exception thrownException : thrownExceptions) {
				if (this.writeThrownExceptions(thrownException) < 1) {
					ok = false;
				}
			}
			ArrayList<Annotation> annotations = method.getAnnotations();
			for (Annotation annotation : annotations) {
				if (this.writeAnnotationIncludingDependencies(annotation) < 1) {
					ok = false;
				}
			}
		}
		return ok;
	}

	/**
	 *  persists a Annotation inclusive all related objects to the database
	 * 
	 * @param annotation
	 * @return
	 * @throws SQLException
	 */
	private int writeAnnotationIncludingDependencies(Annotation annotation) throws SQLException {
		int id = this.writeAnnotation(annotation);
		ArrayList<Attribute> attributes = annotation.getAttribute();
		for (Attribute attribute : attributes) {
			this.writeAttribute(attribute);
		}
		return id;
	}

	/**
	 * Persists a complete Project to the Database within a Database Transaction, overwriting 
	 * an already persisted Project with the same ID
	 * 
	 * @param project
	 * @return
	 * @throws SQLException
	 */
	public boolean updateProjectIncludingDependencies(Project project) throws SQLException {
		boolean ok = true;
		deleteProjectDependencies(project);
		ArrayList<Method> methods = project.getMethods();
		for (Method method : methods) {
			if (!writeMethodIncludingDependencies(method)) {
				ok = false;
			}
		}
		if (ok) {
			conn.commit();
		} else {
			conn.rollback();
			throw new SQLException("Something went wrong, writing to the Database. Data not committed!");
		}
		return ok;
	}

	/**
	 * Persists a complete Project to the Database within a Database Transaction
	 * 
	 * @param project
	 * @return
	 * @throws SQLException
	 */
	public boolean writeProjectIncludingDependencies(Project project) throws SQLException {
		boolean ok = true;
		if (this.writeProject(project) < 1) {
			ok = false;
		} else {
			ArrayList<Method> methods = project.getMethods();
			for (Method method : methods) {
				if (!writeMethodIncludingDependencies(method)) {
					ok = false;
				}
			}
		}
		if (ok) {
			conn.commit();
		} else {
			conn.rollback();
			throw new SQLException("Something went wrong, writing to the Database. Data not committed!");
		}
		return ok;
	}

	/**
	 * Closes the Database Connection
	 */
	public void close() {
		conn.close();
	}

	/**
	 * Removes all Dependencies fpr the given Project from the Database, not changing the local Project Object.
	 * 
	 * @param project
	 * @throws SQLException
	 */
	public void deleteProjectDependencies(Project project) throws SQLException {
		thrownExeptionsDelete.executeUpdate();
		parameterDelete.executeUpdate();
		attributeDelete.executeUpdate();
		annotationDelete.executeUpdate();
		methodDelete.executeUpdate();
		conn.commit();
	}
}
