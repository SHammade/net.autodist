package net.autodist.thrift.gen;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.activation.UnsupportedDataTypeException;

import net.autodist.DAO.Method;
import net.autodist.DAO.Project;
import net.autodist.DAO.database.DbConnection;
import net.autodist.DAO.database.DbReader;
import net.autodist.DAO.database.views.ServerMethod;

public class ThriftIdlBuilder implements IdlBuilder {

	private ArrayList<IdlServiceDefinition> services;
	private DbReader reader;

	/**
	 * Fills the ArrayList<IdlServiceDefinition>
	 * A IdlServiceDefinition for every Server,Port combination in the Database
	 * So there will be one Thrift Service for every Server,Port combination with the corresponding methods.
	 * 
	 * @throws SQLException
	 */
	private void importMultipleServicesForDifferentServers() throws SQLException{
		HashMap<Integer, String> methodNames = reader.readThriftMethodNames();
		Project project = reader.readAllDataForFirstProject();
		ArrayList<ServerMethod> servers = reader.readServerMethods();
		for (ServerMethod serverMethod : servers) {
			IdlServiceDefinition newService = new IdlServiceDefinition(serverMethod.getServer(), serverMethod.getPort(), serverMethod.getServiceName());
			for (Integer methodId : serverMethod.getMethodIds()) {
				Method aMethod = project.findMethod(methodId);
				newService.addMethod(aMethod,methodNames.get(methodId));
			}
			this.services.add(newService);
		}
		 
	}
	
	
	/* (non-Javadoc)
	 * @see net.autodist.thrift.gen.IdlBuilder#generateIdlAsString(net.autodist.DAO.database.DbConnection)
	 */
	@Override
	public String generateIdlAsString(DbConnection dbConnection) throws SQLException, UnsupportedDataTypeException {		
		this.reader = new DbReader(dbConnection);
		this.services = new ArrayList<IdlServiceDefinition>();
		this.importMultipleServicesForDifferentServers();
		StringBuilder sb = new StringBuilder();
		for (IdlServiceDefinition ServiceDefinition : services) {
			sb.append(ServiceDefinition.toIdlString());
			sb.append("\n\n");
		}
		return sb.toString();
	}
	


}
