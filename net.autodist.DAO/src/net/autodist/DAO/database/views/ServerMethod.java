package net.autodist.DAO.database.views;

import java.util.ArrayList;

/**
 * This Class is a Representation of a Database View, to connect the correct MethodIds to the 
 * Thrift services which are created for each Server,Port combination
 * @author Retzlaff, Hammade
 */
public class ServerMethod {
	private int annotationId;
	private ArrayList<Integer> methodIds;
	private String server;
	private String serviceName;
	private int port;

	public ServerMethod(String server, int port, String serviceName) {
		this.server = server;
		this.port = port;
		this.serviceName = serviceName;
		methodIds = new ArrayList<Integer>();
	}

	public void addMethod(int methodId) {
		methodIds.add(methodId);
	}

	public int getAnnotationId() {
		return annotationId;
	}

	public void setAnnotationId(int annotationId) {
		this.annotationId = annotationId;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ArrayList<Integer> getMethodIds() {
		return methodIds;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
}
