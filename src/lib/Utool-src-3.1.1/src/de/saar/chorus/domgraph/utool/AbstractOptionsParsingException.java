/*
 * @(#)AbstractOptionsParsingException.java created 10.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.utool;

public class AbstractOptionsParsingException extends Exception {
  
	private static final long serialVersionUID = 3527362476007640864L;

	private int exitcode = 0;
    
    private boolean haveCause;
    private boolean haveMessage;
    
    
    
    public AbstractOptionsParsingException(int exitcode) {
        super();
        this.exitcode = exitcode;
        haveCause = false;
        haveMessage = false;
    }
    
    
    public AbstractOptionsParsingException(String message, Throwable cause, int exitcode) {
        super(message, cause);
        this.exitcode = exitcode;
        haveCause = true;
        haveMessage = true;
    }
    
    public AbstractOptionsParsingException(String message, int exitcode) {
        super(message);
        this.exitcode = exitcode;
        haveMessage = true;
        haveCause = false;
    }
    
    public AbstractOptionsParsingException(Throwable cause, int exitcode) {
        super(cause);
        this.exitcode = exitcode;
        haveCause = true;
        haveMessage = false;
    }
    
    
    
    public AbstractOptionsParsingException() {
        this(0);
    }
    
    public AbstractOptionsParsingException(String message, Throwable cause) {
        this(message,cause,0);
    }
    
    public AbstractOptionsParsingException(String message) {
        this(message,0);
    }
    
    public AbstractOptionsParsingException(Throwable cause) {
        this(cause,0);
    }
    
    
    
    public int getExitcode() { 
        return exitcode; 
    }
    
    public String toString() {
        return "(exitcode = " + exitcode + ")\n" + super.toString();
    }
    
    public String comprehensiveErrorMessage() {
        StringBuffer ret = new StringBuffer();
        
        if( haveMessage ) {
            ret.append(getMessage());
            ret.append("\n");
        }
        
        if( haveCause ) {
            ret.append(getCause());
            ret.append("\n");
        }
        
        return ret.toString();
    }
}
