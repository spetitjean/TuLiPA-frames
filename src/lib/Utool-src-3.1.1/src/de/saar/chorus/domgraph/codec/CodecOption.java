/*
 * @(#)CodecOption.java created 20.10.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.codec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation type for adding metadata to codec options.
 * Each parameter of the codec constructor must carry an annotation
 * of this type. Use it to specify a name and a default value for
 * the parameter.
 * 
 * @author Alexander Koller
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CodecOption {
    String name();
    String defaultValue() default "";
}
