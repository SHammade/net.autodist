package net.autodist.thrift.gen;

import java.util.ArrayList;
import java.util.Iterator;

import javax.activation.UnsupportedDataTypeException;

/**
 * This class represents one method in a Thrift Service. It is used in
 * order to generate a String representation of for Thrift.idl file.
 * @author Retzlaff, Hammade
 */
public class IdlMethodDefinition {
	private String returnType;
	private String thriftMethodName;
	private ArrayList<IdlArgument> arguments;
	private ArrayList<String> throwables;
	private TypeMapper tmapper = new TypeMapper();

	public IdlMethodDefinition(String returnType, String thriftMethodName) {
		this.returnType = returnType;
		this.thriftMethodName = thriftMethodName;
		this.arguments = new ArrayList<IdlArgument>();
		this.throwables = new ArrayList<String>();
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public void addArgument(IdlArgument argument) {
		this.arguments.add(argument);
	}

	public void addThrowable(String thwobable) {
		this.throwables.add(thwobable);
	}

	public String getThriftMethodName() {
		return thriftMethodName;
	}

	public void setThriftMethodName(String thriftMethodName) {
		this.thriftMethodName = thriftMethodName;
	}

	/**
	 * Creates the String representation of the Method to be used in a Thrift
	 * Service idl file
	 * 
	 * @return String
	 * @throws UnsupportedDataTypeException
	 */
	public Object toIdlString() throws UnsupportedDataTypeException {
		StringBuilder sb = new StringBuilder();
		sb.append(tmapper.getThriftType(returnType));
		sb.append(" ");
		sb.append(this.thriftMethodName);
		sb.append("(");
		sb.append(this.argumentsToString());
		sb.append(")");
		if (throwables.size() > 0) {
			sb.append(" throws (");
			sb.append(this.throwablesToString());
			sb.append(")");
		}

		return sb.toString();
	}

	/**
	 * Converts the arguments into a String that can be used to generate the IDL
	 * File
	 * 
	 * @return
	 * @throws UnsupportedDataTypeException
	 */
	private String argumentsToString() throws UnsupportedDataTypeException {
		StringBuilder sb = new StringBuilder();
		Iterator<IdlArgument> iter = arguments.iterator();
		int num = 1;
		while (iter.hasNext()) {
			IdlArgument arg = iter.next();
			if (!arg.getType().contains("Exception")) {
				this.checkArgumentThriftCompatibility(arg);
				sb.append(num++ + ":");
				sb.append(tmapper.getThriftType(arg.getType()) + " ");
				sb.append(arg.getName());
			}
			if (iter.hasNext()) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	/**
	 * Converts the thrownExceptions into a String that can be used to generate
	 * the IDL File
	 * 
	 * @return
	 */
	private String throwablesToString() {
		StringBuilder sb = new StringBuilder();
		Iterator<String> iter = throwables.iterator();
		int num = 1;
		while (iter.hasNext()) {
			String aThrowable = iter.next();
			sb.append(num++ + ":");
			sb.append(aThrowable);
			if (iter.hasNext()) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	private void checkArgumentThriftCompatibility(IdlArgument arg) throws UnsupportedDataTypeException {
		if (tmapper.isArgumentCompatibleWithThrift(arg)) {
			throw new UnsupportedDataTypeException(arg.getType() + " is not yet Supported in the Thrift IDL generator");
		}

	}

}
