package de.saar.chorus.domgraph.codec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation type for adding metadata to a codec class.
 * Each codec class must be annotated with CodecMetadata in order
 * to be registered with the <code>CodecManager</code>. Use this
 * annotation to specify a name and extension for the codec.
 * You may optionally annotate a codec as "experimental=true" to
 * mark it as experimental.
 * 
 * @author Alexander Koller
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CodecMetadata {
	String name();
	String extension();
	boolean experimental() default false;
}
