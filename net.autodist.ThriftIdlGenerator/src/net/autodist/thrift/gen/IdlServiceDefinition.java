package net.autodist.thrift.gen;

import java.util.ArrayList;
import java.util.Iterator;

import javax.activation.UnsupportedDataTypeException;

import net.autodist.DAO.Method;
import net.autodist.DAO.Parameter;
import net.autodist.DAO.Thrown_Exception;

/**
 * @author Retzlaff, Hammade
 * 
 * This class represents a Thrift service in order to generate a String representation of for Thrift.idl file. 
 *
 */
public class IdlServiceDefinition {
	private String thriftServiceName;
	private String servername;
	private int serverport;
	private ArrayList<IdlMethodDefinition> methods;
	
	public IdlServiceDefinition() {
		methods = new ArrayList<IdlMethodDefinition>();
	}	
	
	public IdlServiceDefinition(String servername, int serverport, String thriftServiceName) {
		this.servername = servername;
		this.serverport = serverport;
		this.thriftServiceName = thriftServiceName;
		methods = new ArrayList<IdlMethodDefinition>();
	}

	/**
	 * Adds an IdlMethodDefinition directly to the Arraylist methods
	 * 
	 * @param method
	 */
	public void addMethod(IdlMethodDefinition method){
		methods.add(method);
	}
	
	/**
	 * 
	 * Creates an IdlMethodDefinition and adds it to the ArrayList methods.
	 * @param method to be used to create the IdlMethodDefinition
	 * @param thriftMethodName to be used to create the IdlMethodDefinition
	 */
	public void addMethod(Method method, String thriftMethodName){
		IdlMethodDefinition idlMethod = new IdlMethodDefinition(method.getReturn_Type(), thriftMethodName);
		for (Thrown_Exception throwable : method.getThrown_Exceptions()) {
			idlMethod.addThrowable(throwable.getException());
		}
		for (Parameter parameter : method.getParameters()) {
			idlMethod.addArgument(new IdlArgument(parameter.getName(), parameter.getType()));
		}
		this.addMethod(idlMethod);
	}
	
	/**
	 * Returns a idl String for a single service, wits all methods in the methods ArrayList
	 * 
	 * @return the Idl String
	 * @throws UnsupportedDataTypeException 
	 */
	public String toIdlString() throws UnsupportedDataTypeException {
		StringBuilder sb = new StringBuilder();
		sb.append("service ");
		sb.append(this.thriftServiceName);
		sb.append(" {"
				+ "\n");
		
		Iterator<IdlMethodDefinition> iter = methods.iterator();
		while(iter.hasNext()){
			IdlMethodDefinition md = iter.next();
			sb.append("  ");
			sb.append(md.toIdlString() );
			if(iter.hasNext())
				sb.append(",");
			sb.append("\n");
		}
		sb.append("}");
		
		return sb.toString();
	}

	public String getServername() {
		return servername;
	}

	public void setServername(String servername) {
		this.servername = servername;
	}

	public int getServerport() {
		return serverport;
	}

	public void setServerport(int serverport) {
		this.serverport = serverport;
	}
}
