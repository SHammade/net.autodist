package net.autodist.thriftgenexecuter;

import java.io.File;

public interface ThriftGenExecuter {
	
	/**
	 * Execute Thrift
	 * @param thriftBinaryFile Thrift binary like /usr/bin/thrift
	 * @param thriftIDLFile Thrift IDL File like myFile.thrift
	 * @param outputLocation Output Location for generated files
	 * @param language Output language for thrift output
	 * @throws Exception
	 */
	void execute(File thriftBinaryFile, File thriftIDLFile, File outputLocation, String language) throws Exception;
	
	/**
	 * Execute Thrift to get the version of Thrift
	 * @param thriftBinaryFile thriftBinaryFile Thrift binary like /usr/bin/thrift
	 * @return Thrift Version
	 * @throws Exception
	 */
	String getVersion(File thriftBinaryFile) throws Exception;
}
