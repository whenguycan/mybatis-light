package com.daoketa.mybatis.light.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wangcy 2024/9/29 16:18
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

	String value() default "";
	
	String comment() default "";
	
	Cnd cnd() default Cnd.eq;
	
}
