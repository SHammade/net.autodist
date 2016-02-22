package net.autodist.configuration;

public class ConfigurationConstants {
	/**
	 * Output folder for Thrift (see man thrift)
	 */
	public static final String THRIFTGEN_OUTPUT_FOLDER = "thrift-gen";
	
	/**
	 * Name of the Thrift-IDL file
	 */
	public static final String THRIFT_IDL_FILE = "generated.idl";
	
	/**
	 * Default language for Thrift
	 */
	public static final String THRIFT_LANGUAGE = "java";

	/**
	 * Default folder for ressources
	 */
	public static final String RESOURCE_FOLDER = "resource";
	

	/**
	 * Default folder for generated files
	 */
	public static final String TEMP_FOLDER = ".autodist";
	

	/**
	 * Default library folder
	 */
	public static final String LIB_FOLDER = "lib";
	

	/**
	 * Default source folder
	 */
	public static final String SRC_FOLDER = "src";
	
	/**
	 *	SQL filename for net.autodist.DAO  
	 */
	public static final String SQL_FILE = "ERDMethoden.sql";
	/**
	 * Default database filename
	 */
	public static final String DATABASE_FILE = "method.db";

	/**
	 * Classname for the server template
	 */
	public static final String SERVER_TEMPLATE_CLASSNAME = "Server";
	
	/**
	 * File name for the server template
	 */
	public static final String SERVER_TEMPLATE_FILENAME = SERVER_TEMPLATE_CLASSNAME+".java";
	
	/**
	 * File name of slf4j (Simple Logging Facade for Java)
	 */
	public static final String SLF4J_LIB_FILE = "slf4j-api-1.6.1.jar";

	/**
	 * URL to download slf4j if not included in eclipse
	 */
	public static final String SLF4J_LIB_URL = "http://central.maven.org/maven2/org/slf4j/slf4j-api/1.6.1/" + SLF4J_LIB_FILE;
	
	/**
	 * File name of aspectjrt (AspectJ Runtime Library)
	 */
	public static final String ASPECTJ_LIB_FILE = "aspectjrt-1.5.3.jar";

	/**
	 * URL to download aspectjrt if not included in eclipse
	 */
	public static final String ASPECTJ_LIB_URL = "http://central.maven.org/maven2/aspectj/aspectjrt/1.5.3/" + ASPECTJ_LIB_FILE;
	
	/**
	 * Bundle name for aspectjrt if included in eclipse
	 */
	public static final String ASPECTJ_BUNDLE_NAME = "org.aspectj.runtime";
	
	/**
	 * File name of Thrift
	 */
	public static final String THRIFT_LIB_FILE = "libthrift-0.9.3.jar";

	/**
	 * URL to download thrift
	 */
	public static final String THRIFT_LIB_URL = "https://repo1.maven.org/maven2/org/apache/thrift/libthrift/0.9.3/" + THRIFT_LIB_FILE;
	
	/**
	 * Class name for the server thrift implementation
	 */
	public static final String THRIFTSERVERIMPLEMENTATION_TEMPLATE_CLASSNAME = "ThriftServerImplementation";
	
	/**
	 * Filename for the server thrift implementation
	 */
	public static final String THRIFTSERVERIMPLEMENTATION_TEMPLATE_FILENAME = THRIFTSERVERIMPLEMENTATION_TEMPLATE_CLASSNAME + ".java";
	
}
