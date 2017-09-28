/*
 * @(#)LoggingPrintWriter.java created 15.09.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.basic;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

public class LoggingWriter extends FilterWriter {
    private Logger logger;
    private String prefix;
    private StringBuffer buffer;
    
    
    public LoggingWriter(Writer arg0, Logger logger, String prefix) {
        super(arg0);
        
        this.logger = logger;
        this.prefix = prefix;
        
        buffer = new StringBuffer();
    }

    @Override
    public void close() throws IOException {
        flushLog();
        super.close();
    }

    @Override
    public void flush() throws IOException {
        flushLog();
        super.flush();
    }

    @Override
    public void write(char[] arg0, int arg1, int arg2) throws IOException {
        try {
            super.write(arg0, arg1, arg2);
        } catch(IOException e) {
            flushLog();
            throw e;
        }
        
        buffer.append(arg0,arg1,arg2);
        logBufferIfNeeded();
    }

    @Override
    public void write(int arg0) throws IOException {
        try {
            super.write(arg0);
        } catch(IOException e) {
            flushLog();
            throw e;
        }

        buffer.append((char) arg0);
        logBufferIfNeeded();
    }

    @Override
    public void write(String arg0, int arg1, int arg2) throws IOException {
        try {
            super.write(arg0, arg1, arg2);
        } catch(IOException e) {
            flushLog();
            throw e;
        }
        
        buffer.append(arg0, arg1, arg2);
        logBufferIfNeeded();
    }
    

    
    

    private void logBufferIfNeeded() {
        String contents = cutBufferContents();
        
        if( contents != null ) {
            logger.log(prefix + contents);
        }
    }

    
    private String cutBufferContents() {
        int pos = buffer.indexOf("\n");
        
        if( pos >= 0 ) {
            String ret = buffer.substring(0, pos+1);
            buffer.delete(0, pos+1);
            return ret;
        } else {
            return null;
        }
    }
    
    public void flushLog() {
        logger.log(prefix + buffer.toString());
        clearBuffer();
    }

    private void clearBuffer() {
        buffer.delete(0,buffer.length()+1);
    }

    
}
