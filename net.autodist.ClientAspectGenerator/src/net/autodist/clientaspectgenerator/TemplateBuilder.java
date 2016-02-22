package net.autodist.clientaspectgenerator;

public class TemplateBuilder {
	private static final String T_ASPECT_CONTENT = "CONTENT";
	private static final String T_ASPECT_IMPORT = "IMPORT";
	
	private static final String T_METHOD_NAME = "METHODNAME";
	private static final String T_METHOD_ORGINAL_NAME = "ORGINALNAME";
	private static final String T_METHOD_ARGS_DEF = "METHODARGSDEF";
	private static final String T_METHOD_ARGS_LIST = "METHODARGSLIST";
	private static final String T_METHOD_RETURN_TYPE = "METHODRETURNTYPE";
	private static final String T_METHOD_DEFAULT_VALUE = "METHODDEFAULTVALUE";
	
	private static final String T_METHOD_DEF_ARGS_LIST = "METHODDEFARGSLIST";
	
	private static final String T_METHOD_DECLARATION = "METHODRETURNDECLARATION";
	private static final String V_METHOD_DECLARATION = "METHODRETURNTYPE value = METHODDEFAULTVALUE;";
	
	private static final String T_METHOD_THRIFT_CALL = "METHODTHRIFTCALL";
	private static final String V_METHOD_THRIFT_CALL = "value = ";
	
	private static final String T_METHOD_RETURN = "METHODRETURNVALUE";
	private static final String V_METHOD_RETURN = "return value;";
	
	private static final String T_CLIENT = "Client";
	
	public static final String ASPECT_FILE_NAME = "DoRemoteCallAspect.aj";
	
	public static final String T_ASPECT_FILE_NAME = "aspect.tpl";
	public static final String T_POINTCUT_FILE_NAME = "pointcut.tpl";
	public static final String T_ADVICE_FILE_NAME = "advice.tpl";

	private String templateAspect;
	private String templatePointcut;
	private String templateAdvice;
	
	private StringBuffer content;
	
	public TemplateBuilder(String templateAspect, String templatePointcut, String templateAdvice) {		
		this.templateAspect = templateAspect;
		this.templatePointcut = templatePointcut;
		this.templateAdvice = templateAdvice;
		this.content = new StringBuffer();
	}

	public String buildAdvice(String methodName, String methodArgDef, String methodArgsList, String methodReturnType, String fullClientName, String defaultValue) {
		StringBuffer sbAdvice = new StringBuffer(this.templateAdvice);
		
		if(methodReturnType.equals("void"))
		{
			replaceAll(sbAdvice, T_METHOD_DECLARATION, "");
			replaceAll(sbAdvice, T_METHOD_THRIFT_CALL, "");
			replaceAll(sbAdvice, T_METHOD_RETURN, "");			
		}
		else
		{
			replaceAll(sbAdvice, T_METHOD_DECLARATION, V_METHOD_DECLARATION);
			replaceAll(sbAdvice, T_METHOD_THRIFT_CALL, V_METHOD_THRIFT_CALL);
			replaceAll(sbAdvice, T_METHOD_RETURN, V_METHOD_RETURN);
		}
		if(methodArgsList.length() > 0)
			replaceAll(sbAdvice, T_METHOD_DEF_ARGS_LIST, "," + methodArgsList);
		else
			replaceAll(sbAdvice, T_METHOD_DEF_ARGS_LIST, methodArgsList);

		replaceAll(sbAdvice, T_METHOD_NAME, methodName);
		replaceAll(sbAdvice, T_METHOD_ARGS_DEF, methodArgDef);
		replaceAll(sbAdvice, T_METHOD_ARGS_LIST, methodArgsList);
		replaceAll(sbAdvice, T_METHOD_RETURN_TYPE, methodReturnType);
		replaceAll(sbAdvice, T_METHOD_DEFAULT_VALUE, defaultValue);
		replaceAll(sbAdvice, T_CLIENT, fullClientName);
		return sbAdvice.toString();

	}
	public String buildPointcut(String methodName, String methodArgDef, String methodArgsList, String methodReturnType, String orginalMethodName) {
		StringBuffer sbPointcut = new StringBuffer(this.templatePointcut);		
		replaceAll(sbPointcut, T_METHOD_ORGINAL_NAME, orginalMethodName);
		replaceAll(sbPointcut, T_METHOD_NAME, methodName);
		replaceAll(sbPointcut, T_METHOD_ARGS_DEF, methodArgDef);
		replaceAll(sbPointcut, T_METHOD_ARGS_LIST, methodArgsList);
		replaceAll(sbPointcut, T_METHOD_RETURN_TYPE, methodReturnType);
		return sbPointcut.toString();
	}
	public String buildAspect() {
		StringBuffer sbAspect = new StringBuffer(this.templateAspect);

		StringBuffer sbImport = new StringBuffer();
		
		replaceAll(sbAspect, T_ASPECT_IMPORT, sbImport.toString());		
		replaceAll(sbAspect, T_ASPECT_CONTENT, content.toString());				
		return sbAspect.toString();
	}

	public void addMethod(String methodName, String methodArgDef, String methodArgsList, String methodReturnType, String fullClientName, String orginalMethodName, String defaultValue)
	{
		content.append(buildPointcut(methodName, methodArgDef, methodArgsList, methodReturnType, orginalMethodName));			
		content.append(buildAdvice(methodName, methodArgDef, methodArgsList, methodReturnType, fullClientName, defaultValue));
	}
	
	private static void replaceAll(StringBuffer builder, String from, String to)
	{		
	    int index = builder.indexOf(from);
	    while (index != -1)
	    {
	        builder.replace(index, index + from.length(), to);
	        index += to.length();
	        index = builder.indexOf(from, index);
	    }
	}
}
