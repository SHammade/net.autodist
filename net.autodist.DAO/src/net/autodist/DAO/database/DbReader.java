package net.autodist.DAO.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import net.autodist.DAO.Annotation;
import net.autodist.DAO.Attribute;
import net.autodist.DAO.Method;
import net.autodist.DAO.Parameter;
import net.autodist.DAO.Project;
import net.autodist.DAO.Thrown_Exception;
import net.autodist.DAO.database.views.ServerMethod;
import net.autodist.DAO.database.views.ThriftServiceName;

/**
 * @author Retzlaff, Hammade
 */
public class DbReader {
	private static final String SQL_PREP_SELECT_BY_METHODID_ANNOTATION = "SELECT ID, TYPE  FROM ANNOTATION WHERE METHODID = ?";
	private static final String SQL_PREP_SELECT_BY_ANNOTATIONSID_ATTRIBUTE = "SELECT ID, TYPE, VALUE  FROM ATTRIBUTE WHERE ANNOTATIONSID  = ?";
	private static final String SQL_PREP_SELECT_BY_PROJECTID_METHOD = "SELECT PATH, NAME, ID, RETURN_TYPE, SOURCE, BODY FROM METHOD WHERE PROJECTID = ?";
	private static final String SQL_PREP_SELECT_BY_ID_METHOD = "SELECT PATH, NAME, ID, RETURN_TYPE, SOURCE, BODY FROM METHOD WHERE ID = ?";
	private static final String SQL_PREP_SELECT_BY_METHODID_PARAMETER = "SELECT ID, TYPE, NAME  FROM PARAMETER WHERE METHODID = ?";
	private static final String SQL_PREP_SELECT_LATEST_PROJECT = "SELECT NAME, PACKAGE, DATE, ID FROM PROJECT WHERE DATE = (SELECT MAX(DATE) FROM PROJECT)";
	private static final String SQL_PREP_SELECT_BY_METHODID_THROWN_EXCEPTION = "SELECT ID, EXCEPTION  FROM THROWN_EXCEPTION WHERE METHODID = ?";
	private static final String SQL_PREP_SELECT_SERVER_BY_METHOD = "SELECT annotationid, SERVER_BY_METHOD.methodid as methodid, server, port, servicename "
			+ "FROM SERVER_BY_METHOD  inner join THRIFT_SERVICE_NAMES on(SERVER_BY_METHOD.methodid = THRIFT_SERVICE_NAMES.methodid)"
			+ "WHERE SERVER_BY_METHOD.methodid in(SELECT method.id FROM method, project "
			+ "WHERE method.projectid = project.id " + "and DATE = SELECT MAX(DATE) FROM PROJECT)"
			+ "ORDER BY server, port";
	private static final String SQL_PREP_SELECT_THRIFT_SERVICE_NAMES = "SELECT * FROM THRIFT_SERVICE_NAMES";

	private DbConnection conn;

	private PreparedStatement latestProjectSelect;
	private PreparedStatement methodSelect;
	private PreparedStatement thrownExeptionsSelect;
	private PreparedStatement annotationsSelect;
	private PreparedStatement attributeSelect;
	private PreparedStatement parameterSelect;
	private PreparedStatement methodsPerServerSelect;
	private PreparedStatement thriftServiceNameSelect;
	private PreparedStatement specificMethodSelect;

	
	
	/**
	 * Constructor, to be used with an existing Database Connection. 
	 * Since the Database is Embedded, there is only one connection possible.
	 * 
	 * @param conn the database connection of Type DbConnection
	 */
	public DbReader(DbConnection conn) {
		this.conn = conn;
		this.createPreparedStatements();
	}

	/**
	 * Constructor which opens a new DbConnection with Autocommit=false
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public DbReader() throws ClassNotFoundException, SQLException {
		this.conn = new DbConnection(false);
		this.createPreparedStatements();
	}

	/**
	 * 
	 */
	private void createPreparedStatements() {
		this.latestProjectSelect = conn.prepareStatement(SQL_PREP_SELECT_LATEST_PROJECT);
		this.methodSelect = conn.prepareStatement(SQL_PREP_SELECT_BY_PROJECTID_METHOD);
		this.specificMethodSelect = conn.prepareStatement(SQL_PREP_SELECT_BY_ID_METHOD);
		this.thrownExeptionsSelect = conn.prepareStatement(SQL_PREP_SELECT_BY_METHODID_THROWN_EXCEPTION);
		this.annotationsSelect = conn.prepareStatement(SQL_PREP_SELECT_BY_METHODID_ANNOTATION);
		this.attributeSelect = conn.prepareStatement(SQL_PREP_SELECT_BY_ANNOTATIONSID_ATTRIBUTE);
		this.parameterSelect = conn.prepareStatement(SQL_PREP_SELECT_BY_METHODID_PARAMETER);
		this.methodsPerServerSelect = conn.prepareStatement(SQL_PREP_SELECT_SERVER_BY_METHOD);
		this.thriftServiceNameSelect = conn.prepareStatement(SQL_PREP_SELECT_THRIFT_SERVICE_NAMES);
	}

	/**
	 * Creates an Arraylist of Servermethods (Thrift Service Name, Server, Port) and the corresponding Methods, 
	 * which are to be implemented in the Service
	 * 
	 * @return an Arraylist of ServerMethods
	 * @throws SQLException
	 */
	public ArrayList<ServerMethod> readServerMethods() throws SQLException {
		ArrayList<ServerMethod> serverMethods = new ArrayList<ServerMethod>();
		ResultSet result = methodsPerServerSelect.executeQuery();
		while (result.next()) {
			int port = result.getInt("PORT");
			String servername = result.getString("SERVER");
			String servicename = result.getString("SERVICENAME");
			int methodId = result.getInt("METHODID");
			boolean serverExists = false;
			for (ServerMethod sm : serverMethods) {
				if (sm.getServer() == servername && sm.getPort() == port) {
					serverExists = true;
					sm.addMethod(methodId);
				}
			}
			if (!serverExists) {
				ServerMethod newSm = new ServerMethod(servername, port, servicename);
				newSm.addMethod(methodId);
				serverMethods.add(newSm);
			}
		}
		return serverMethods;
	}

	/**
	 * Gets the last generated Project from the Database, All dependencies are also fetched.
	 * 
	 * @return A Project Object with all dependencies
	 * @throws SQLException
	 */
	public Project readAllDataForFirstProject() throws SQLException {
		Project project = null;
		project = this.readLatestProject();
		this.addMethodsToProject(project);

		return project;
	}

//	/**
//	 * Gets the Project with the given Project Id from the Database, All dependencies are also fetched.
//	 * 
//	 * @param projectId The Project id, which is equivalent to the Database Primary Key Id
//	 * @return A Project Object with all dependencies
//	 * @throws SQLException
//	 */
//	public Project readAllDataForProjectWithId(int projectId) throws SQLException {
//		Project project = null;
//		project = this.readAllDataForProjectWithId(projectId);//TODO this will most likely crash the program and has to be reimplemented if needed
//		this.addMethodsToProject(project);
//		return project;
//	}

	/**
	 * Uses a Database View to get the Unique Thrift ServiceerviceName together with the 
	 * Unique Methodname and the Method Ids
	 * 
	 * @return ArrayList<ThriftServiceName>
	 * @throws SQLException
	 */
	public ArrayList<ThriftServiceName> readAllThriftServiceNames() throws SQLException {
		ArrayList<ThriftServiceName> list = new ArrayList<>();
		ResultSet result = this.thriftServiceNameSelect.executeQuery();
		while (result.next()) {
			ThriftServiceName thriftServiceName = new ThriftServiceName(result.getInt("methodid"),
					result.getString("servicename"), result.getString("methodname"));
			list.add(thriftServiceName);
		}
		result.close();
		return list;
	}

	/**
	 * Uses a Database View to create a Hashmap with the MethodId as Key and the Servicename as Value.
	 * 
	 * @return HashMap<Integer->MethodId, String->ServiceName> 
	 * @throws SQLException
	 */
	public HashMap<Integer, String> readThriftServiceNames() throws SQLException {
		HashMap<Integer, String> resultMap = new HashMap<Integer, String>();
		ResultSet result = this.thriftServiceNameSelect.executeQuery();
		while (result.next()) {
			resultMap.put(result.getInt("methodid"), result.getString("servicename"));
		}
		result.close();

		return resultMap;
	}

	/**
	 * Uses a Database View to create a Hashmap with the MethodId as Key and the Thrift Methodname as Value.
	 * 
	 * @return HashMap<Integer->MethodId, String->Methodname> 
	 * @throws SQLException
	 */
	public HashMap<Integer, String> readThriftMethodNames() throws SQLException {
		HashMap<Integer, String> resultMap = new HashMap<Integer, String>();
		ResultSet result = this.thriftServiceNameSelect.executeQuery();
		while (result.next()) {
			resultMap.put(result.getInt("methodid"), result.getString("methodname"));
		}
		result.close();
		return resultMap;
	}

	/**
	 * Gets the methods for the given project from the Database and writes it to the given Project Object
	 * 
	 * @param project
	 * @throws SQLException
	 */
	private void addMethodsToProject(Project project) throws SQLException {
		methodSelect.setInt(1, project.getId());
		ResultSet rs = methodSelect.executeQuery();
		while (rs.next()) {
			Method method = new Method();
			method.setPath(rs.getString("PATH"));
			method.setName(rs.getString("NAME"));
			method.setId(rs.getInt("ID"));
			method.setReturn_Type(rs.getString("RETURN_TYPE"));
			method.setSource(rs.getString("SOURCE"));
			method.setBody(rs.getString("BODY"));
			method.setProject(project);
			this.addThrownExeptionsToMethod(method);
			this.addParametersTorMethod(method);
			this.addAnnotationsToMethod(method);
			project.addMethod(method);
		}
	}

	/**
	 * Gets the annotations for the given method from the Database and writes it to the given Method Object
	 * 
	 * @param method
	 * @throws SQLException
	 */
	private void addAnnotationsToMethod(Method method) throws SQLException {
		annotationsSelect.setInt(1, method.getId());
		ResultSet rs = annotationsSelect.executeQuery();
		while (rs.next()) {
			Annotation annotation = new Annotation();
			annotation.setId(rs.getInt("ID"));
			annotation.setType(rs.getString("TYPE"));
			this.addAttributesToAnnotation(annotation);
			method.addAnnotation(annotation);
		}

	}

	/**
	 * Gets the attributes for the given annotation from the Database and writes it to the given Annotation Object
	 * 
	 * @param annotation
	 * @throws SQLException
	 */
	private void addAttributesToAnnotation(Annotation annotation) throws SQLException {
		attributeSelect.setInt(1, annotation.getId());
		ResultSet rs = attributeSelect.executeQuery();
		while (rs.next()) {
			Attribute attribute = new Attribute();
			attribute.setId(rs.getInt("ID"));
			attribute.setType(rs.getString("TYPE"));
			attribute.setValue(rs.getString("VALUE"));
			annotation.addAttribute(attribute);
		}

	}

	/**
	 * Gets the parameters for the given method from the Database and writes it to the given Method Object
	 * 
	 * @param method
	 * @throws SQLException
	 */
	private void addParametersTorMethod(Method method) throws SQLException {
		parameterSelect.setInt(1, method.getId());
		ResultSet rs = parameterSelect.executeQuery();
		while (rs.next()) {
			Parameter parameter = new Parameter();
			parameter.setId(rs.getInt("ID"));
			parameter.setType(rs.getString("TYPE"));
			parameter.setName(rs.getString("NAME"));
			method.addParameter(parameter);
		}
	}

	/**
	 * Gets the Thrown Exceptions for the given method from the Database and writes it to the given Method Object
	 * 
	 * @param method
	 * @throws SQLException
	 */
	private void addThrownExeptionsToMethod(Method method) throws SQLException {
		thrownExeptionsSelect.setInt(1, method.getId());
		ResultSet rs = thrownExeptionsSelect.executeQuery();
		while (rs.next()) {
			Thrown_Exception thrownException = new Thrown_Exception();
			thrownException.setId(rs.getInt("ID"));
			thrownException.setException(rs.getString("EXCEPTION"));
			method.addThrown_Exception(thrownException);
		}

	}

	/**
	 * This methods reads the latest Project from the database but does not add any assiciated Method to it
	 * 
	 * @return
	 * @throws SQLException
	 */
	private Project readLatestProject() throws SQLException {
		Project project = new Project();
		ResultSet rs = latestProjectSelect.executeQuery();
		if (rs.next()) {
			project.setName(rs.getString("NAME"));
			project.setPackageName(rs.getString("PACKAGE"));
			project.setDate(rs.getTimestamp("DATE"));
			project.setId(rs.getInt("ID"));
			rs.close();
			return project;
		}
		return null;
	}

	/**
	 * Reads all Data for a specific Method along with all related Data 
	 * 
	 * @param methodId
	 * @return
	 * @throws SQLException
	 */
	public Method readSpecificMethod(int methodId) throws SQLException {
		Method method = new Method();
		specificMethodSelect.setInt(1, methodId);
		ResultSet rs = specificMethodSelect.executeQuery();
		if (rs.next()) {
			method.setPath(rs.getString("PATH"));
			method.setName(rs.getString("NAME"));
			method.setId(rs.getInt("ID"));
			method.setReturn_Type(rs.getString("RETURN_TYPE"));
			method.setSource(rs.getString("SOURCE"));
			method.setBody(rs.getString("BODY"));
			this.addThrownExeptionsToMethod(method);
			this.addParametersTorMethod(method);
			this.addAnnotationsToMethod(method);
		}
		return method;
	}
}
