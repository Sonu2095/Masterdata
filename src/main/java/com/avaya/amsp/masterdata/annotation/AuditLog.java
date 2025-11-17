package com.avaya.amsp.masterdata.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author kumar32 This class will act as an annotation for Audit purpose on pre&post changes on apis
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
	
	/**
	 * Purpose of this below fields :-
	 * action: what kind of modification operation like-Post,Put,Delete
	 * entity: which entity has impact on above performed operation
	 * functionality: which functionality/UI-screen operation we're performing
	 */
	String action();
	String entity();
	String functionality();

}
