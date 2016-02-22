package net.autodist.DAO.database.views;

/**
 * This Class is a Representation of a Database View, to connect the correct MethodIds to the 
 * Thrift services, The Service Name and The Method Names are Created in the Database View
 * @author Retzlaff, Hammade 
 */
public class ThriftServiceName {
	private int methodId;
	private String serviceName;
	private String methodName;

	public ThriftServiceName(int methodId, String serviceName, String methodName) {
		super();
		this.methodId = methodId;
		this.serviceName = serviceName;
		this.methodName = methodName;
	}

	public int getMethodId() {
		return methodId;
	}

	public void setMethodId(int methodId) {
		this.methodId = methodId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
}
