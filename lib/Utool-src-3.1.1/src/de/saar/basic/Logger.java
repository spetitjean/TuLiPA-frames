/*
 * @(#)Logger.java created 12.09.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.basic;

import java.io.PrintWriter;

public class Logger {
    private  boolean logging = false;
    private PrintWriter logTo = null;
    
    public Logger(boolean logging, PrintWriter logTo) {
        this.logging = logging;
        
        if( logging ) {
            this.logTo = logTo;
        }
    }
    
   
    public void log(String x) {
        if( logging ) {
            logTo.println(x);
            logTo.flush();
        }
    }

}
