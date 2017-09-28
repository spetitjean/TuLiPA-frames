/*
 * @(#)MalformedDomgraphException.java created 25.01.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.codec;



/**
 * An exception representing semantic errors in codecs. Such exceptions
 * are thrown if it turns out that a USR cannot be processed using a certain
 * input or output codec.<p>
 * 
 * What's special about this exception class is that the codec can
 * specify an exit code. This exit code will then be used as an exit
 * code in the main program (Utool or Utool Server). Valid exit codes
 * are the numbers 0 ... 31 (the rightmost five bits); every codec
 * can assign these codes as it wishes.
 * 
 * @author Alexander Koller
 *
 */
public class MalformedDomgraphException extends Exception {
    private int exitcode = 0;
    private boolean hasMessage;

    
    public MalformedDomgraphException(int exitcode) {
        super();
        this.exitcode = exitcode;
        this.hasMessage = false;
    }

    public MalformedDomgraphException(String message, Throwable cause, int exitcode) {
        super(message, cause);
        this.exitcode = exitcode;
        this.hasMessage = true;
    }

    public MalformedDomgraphException(String message, int exitcode) {
        super(message);
        this.exitcode = exitcode;
        this.hasMessage = true;
    }

    public MalformedDomgraphException(Throwable cause, int exitcode) {
        super(cause);
        this.exitcode = exitcode;
        this.hasMessage = false;
    }
    
    public MalformedDomgraphException() {
        this(0);
    }

    public MalformedDomgraphException(String message, Throwable cause) {
        this(message,cause,0);
    }

    public MalformedDomgraphException(String message) {
        this(message,0);
    }

    public MalformedDomgraphException(Throwable cause) {
        this(cause,0);
    }

    
    public int getExitcode() { 
        return exitcode; 
    }
    
    public String toString() {
    	if( hasMessage ) {
    		return getMessage();
    	} else {
    		return super.toString() + " (code " + exitcode + ")";
    	}
    }
}
