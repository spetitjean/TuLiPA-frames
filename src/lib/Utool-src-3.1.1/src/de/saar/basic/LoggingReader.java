/*
 * @(#)LineBufferingReader.java created 15.09.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.basic;

import java.io.IOException;
import java.io.Reader;


public class LoggingReader extends Reader {
    private Logger logger;
    private StringBuffer buffer;
    private Reader reader;
    private String prefix;
    
    public LoggingReader(Reader reader, Logger logger, String prefix) {
        this.reader = reader;
        this.logger = logger;
        this.prefix = prefix;
        
        buffer = new StringBuffer();
    }
    
    
    @Override
    public int read(char[] arg0, int arg1, int arg2) throws IOException {
        try {
            int ret = reader.read(arg0,arg1,arg2);
            
            buffer.append(arg0,arg1,arg2);
            logBufferIfNeeded();
            
            return ret;
        } catch(IOException e) {
            flushLog();
            throw e;
        }
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


    @Override
    public void close() throws IOException {
        flushLog();
        reader.close();
    }
    
    public void flushLog() {
        logger.log(prefix + buffer.toString());
        clearBuffer();
    }

    private void clearBuffer() {
        buffer.delete(0,buffer.length()+1);
    }

}
