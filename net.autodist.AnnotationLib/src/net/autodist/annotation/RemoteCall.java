package net.autodist.annotation;

import java.lang.annotation.*;

/**
 * Remote Call Annotation to mark methods to be distributed
 * @author Retzlaff, Hammade
 */
@Inherited 
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(value = RemoteCalls.class)
public @interface RemoteCall {
	String servername();
	int serverport();
}

