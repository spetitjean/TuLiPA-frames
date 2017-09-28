/*
 * @(#)ReplacingStringWriter.java created 13.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.basic;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * A class which replaces strings by other strings in every call
 * to the write methods.
 * 
 * @author Alexander Koller
 *
 */
public class ReplacingWriter extends FilterWriter {
    private List<String> lhs;
    private List<String> rhs;
    
    public ReplacingWriter(Writer writer) {
        super(writer);
        lhs = new ArrayList<String>();
        rhs = new ArrayList<String>();
    }
    
    /**
     * Adds a replacement rule. From now on, <code>L</code> will
     * be replaced by <code>R</code> in every call to <code>write</code>.
     * 
     * @param L the left-hand side of a replacement rule
     * @param R the right-hand side of a replacement rule
     */
    public void addReplacementRule(String L, String R) {
        lhs.add(L);
        rhs.add(R);
    }

    private void replacingWrite(String s) throws IOException {
        for( int i = 0; i < lhs.size(); i++ ) {
            s = s.replaceAll(lhs.get(i), rhs.get(i));
        }
        
        out.write(s);
    }
    
    
    public void write(char[] cbuf, int off, int len) throws IOException {
        replacingWrite(new String(cbuf, off, len));
        
    }
    
    public void write(int c) throws IOException {
        replacingWrite(new String(new int[] { c }, 0, 1));
    }

    public void write(String str, int off, int len) throws IOException {
        write(str.toCharArray(), off, len);
    }
}
