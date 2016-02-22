package net.autodist.thrift.gen;

/**
 * 
 * This class represents one argument of a ethod in a Thrift Service. It is used in order to generate a String representation of for Thrift.idl file.
 * 
 * @author Retzlaff, Hammade
 *
 */
public class IdlArgument {
	private String name;
	private String type;

	public IdlArgument(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


}
