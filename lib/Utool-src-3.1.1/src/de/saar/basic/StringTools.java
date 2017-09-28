/*
 * @(#)StringTools.java created 13.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.basic;

import java.util.Collection;



/**
 * A collection of various utility functions for strings. 
 * 
 * @author Alexander Koller
 *
 */
public class StringTools {
    /**
     * Joins a list of strings into a larger string. This is similar
     * to Perl's <code>join()</code> function. 
     * 
     * @param strings a list of strings
     * @param separator a string that is inserted between any two members of the list
     * @return the joined string
     */
    public static <E> String join(Collection<E> strings, String separator) {
        boolean first = true;
        StringBuffer sb = new StringBuffer();
        
        for( Object s : strings ) {
            if( first ) {
                first = false;
            } else {
                sb.append(separator);
            } 
            
            sb.append(s.toString());
        }
        
        return sb.toString();
        
    }

}
