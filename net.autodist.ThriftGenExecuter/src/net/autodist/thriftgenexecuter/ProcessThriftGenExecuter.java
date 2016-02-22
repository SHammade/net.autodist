package net.autodist.thriftgenexecuter;

import java.io.*;

public class ProcessThriftGenExecuter implements ThriftGenExecuter {
	
	@Override
	public void execute(File thriftBinaryFile, File thriftIDLFile, File outputLocation, String language) throws Exception {
		Runtime rt = Runtime.getRuntime();
		Process ps = rt.exec(getGenerateCommand(thriftBinaryFile, thriftIDLFile, outputLocation, language));
		ps.waitFor();
		if(ps.exitValue() != 0)
			throw new Exception(ps.getErrorStream().toString());		
	}

	@Override
	public String getVersion(File thriftBinaryFile) throws Exception {
		String version = "0.9.0";
		Runtime rt = Runtime.getRuntime();
		Process ps = rt.exec(getVersionCommand(thriftBinaryFile));
		ps.waitFor();
		if(ps.exitValue() != 0)
			throw new Exception(ps.getErrorStream().toString());
		BufferedReader reader = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		StringBuilder builder = new StringBuilder();
		String line = null;
		while ( (line = reader.readLine()) != null) {
		   builder.append(line);
		   builder.append(System.getProperty("line.separator"));
		}	
		
		String[] output = builder.toString().split(" ");
		if(output.length == 3)
			version = output[2].trim().replaceAll("(\\r|\\n)", "");
		return version;
	}
	
	/**
	 * Get the Thrift-Command
	 * @param thriftBinaryFile
	 * @param thriftIDLFile
	 * @param outputLocation
	 * @param language
	 * @return Command as String
	 */
	private String getGenerateCommand(File thriftBinaryFile, File thriftIDLFile, File outputLocation, String language)
	{
		StringBuffer sb = new StringBuffer(thriftBinaryFile.getAbsolutePath().toString());
		sb.append(" -out ");
		sb.append(outputLocation.getAbsolutePath().toString());
		sb.append(" -gen ");
		sb.append(language);
		sb.append(" ");		
		sb.append(thriftIDLFile.getAbsolutePath().toString());
		return sb.toString();
	}
	
	/**
	 * Get the Thrift-Version
	 * @param thriftBinaryFile
	 * @return Version as String
	 */
	private String getVersionCommand(File thriftBinaryFile)
	{
		StringBuffer sb = new StringBuffer(thriftBinaryFile.getAbsolutePath().toString());
		sb.append(" --version");
		return sb.toString();
	}
}
