package net.autodist.thrift.gen;

import net.autodist.DAO.database.DbConnection;

/**
 * @author Retzlaff, Hammade
 *
 */
public interface IdlBuilder {
	
	/**
	 * generated the Thrift.idl as a String, using the newest Project of the given Database Connection
	 * 
	 * @param dbConnection
	 * @return the Thrift.idl as String
	 * @throws Exception
	 */
	public String generateIdlAsString(DbConnection dbConnection) throws Exception;
}
