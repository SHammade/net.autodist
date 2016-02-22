package net.autodist.annotation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoteCalls {
	RemoteCall[] value() default {};

}