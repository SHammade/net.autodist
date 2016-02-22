package net.autodist.DAO.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Retzlaff, Hammade
 */
public class DbConnection {

	private Connection conn;
	private String sqlFile = "'resource/ERDMethoden.sql'";
	private String databaseFilePath = "/tmp/methodDB2";

	/**
	 * Convinience Constructor to be used with default Values
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public DbConnection() throws ClassNotFoundException, SQLException {
		this(true);
	}

	/**
	 * Convinience Constructor to be used with default Values
	 * 
	 * @param autocommit
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * 
	 */
	public DbConnection(boolean autocommit) throws ClassNotFoundException, SQLException {
		this.connect();
		conn.setAutoCommit(autocommit);
	}

	/**
	 * @param autocommit
	 * @param databaseFilePath
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public DbConnection(boolean autocommit, String databaseFilePath) throws ClassNotFoundException, SQLException {
		this.databaseFilePath = databaseFilePath;
		this.connect();
		conn.setAutoCommit(autocommit);
	}

	/**
	 * @param autocommit
	 * @param sqlFile
	 *            Path to SQL File, which is to be executed with createTables()
	 * @param databaseFilePath
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public DbConnection(boolean autocommit, String sqlFile, String databaseFilePath) throws ClassNotFoundException, SQLException {
		this.sqlFile = sqlFile;
		this.databaseFilePath = databaseFilePath;
		this.connect();
		conn.setAutoCommit(autocommit);
	}

	/**
	 * Connects to the Database
	 * 
	 * @return true if no error occurs during connect attempt
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private boolean connect() throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
		conn = DriverManager.getConnection("jdbc:h2:" + databaseFilePath);
		return true;
	}

	/**
	 * Runs the script file the variable sqlFile points to.
	 * 
	 * @return true if no error occurs
	 * @throws SQLException 
	 */
	public boolean createTables() throws SQLException {
		String sql = "RUNSCRIPT FROM " + sqlFile;
		Statement stmt = conn.createStatement();
		stmt.execute(sql);
		stmt.close();
		conn.commit();
		return true;
	}

	/**
	 * closes the Database Connection
	 */
	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creats a Prepared Statement using the Database Connection
	 * 
	 * @param sql
	 *            String for the Prepared Statement
	 * @return the Prepared Statement or null if Preparation fails
	 */
	public PreparedStatement prepareStatement(String sql) {
		try {
			return conn.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * sends commit to the Database Connection
	 */
	public void commit() {
		try {
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * sends rollback to the Database Connection
	 */
	public void rollback() {
		try {
			conn.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}