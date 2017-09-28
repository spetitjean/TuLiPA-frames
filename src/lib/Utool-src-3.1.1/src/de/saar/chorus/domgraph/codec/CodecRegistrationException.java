/*
 * @(#)CodecRegistrationException.java created 27.01.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.codec;


/**
 * This exception represents an error that occurs while registering
 * a codec. 
 * 
 * @author Alexander Koller
 *
 */
public class CodecRegistrationException extends Exception {

    public CodecRegistrationException() {
        super();
    }

    public CodecRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodecRegistrationException(String message) {
        super(message);
    }

    public CodecRegistrationException(Throwable cause) {
        super(cause);
    }

}
