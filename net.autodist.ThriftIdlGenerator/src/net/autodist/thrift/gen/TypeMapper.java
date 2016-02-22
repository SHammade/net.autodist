package net.autodist.thrift.gen;

import java.util.Hashtable;
import java.util.Map;

/**
 *
 * This class maps the supported Java Type Strings to the Thrift Type Strings and vice versa.
 *
 * @author Retzlaff, Hammade
 */
public class TypeMapper {
	private Map<String, String> java2Thrift = new Hashtable<String, String>();
	private Map<String, String> thrift2Java = new Hashtable<String, String>();

	public TypeMapper() {
		this.add("bool", "boolean");
		this.add("byte", "byte");
		this.add("i16", "short");
		this.add("i32", "int");
		this.add("i64", "long");
		this.add("double", "double");
		this.add("string", "String");
		this.add("binary", "ByteBuffer");
		this.add("binary", "java.nio.ByteBuffer");
	}

	
	/**
	 * Adds a Value to both Maps for Bidirectional Mapping
	 * 
	 * @param thrift
	 * @param java
	 */
	private synchronized void add(String thrift, String java) {
		thrift2Java.put(thrift, java);
		java2Thrift.put(java, thrift);
	}

	/**
	 * Gets the Thrift Type String for the given Java Type
	 * 
	 * @param javaType
	 * @return thriftType if found, else void
	 */
	public synchronized String getThriftType(String javaType) {
		if (java2Thrift.containsKey(javaType))
			return java2Thrift.get(javaType);
		else
			return "void";
	}

	/**
	 * Gets the Java Type String for the given Thrift Type
	 * 
	 * @param thriftType
	 * @return javaType if found, else void
	 */
	public synchronized String getJavaType(String thriftType) {
		if (thrift2Java.containsKey(thriftType))
			return thrift2Java.get(thriftType);
		else
			return "void";
	}


	public boolean isArgumentCompatibleWithThrift(IdlArgument arg) {
		return getThriftType(arg.getType()).compareTo("void") == 0;
	}
	

}
