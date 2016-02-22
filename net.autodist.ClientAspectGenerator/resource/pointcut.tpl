	pointcut remoteCallMethodMETHODNAME(net.autodist.annotation.RemoteCall anno METHODARGSDEF) :  
	execution(METHODRETURNTYPE ORGINALNAME(..)) && @annotation(anno) && args(METHODARGSLIST);
