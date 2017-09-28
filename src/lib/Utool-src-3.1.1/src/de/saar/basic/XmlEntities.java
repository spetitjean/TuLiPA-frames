/*
 * @(#)XmlEntities.java created 13.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A collection of functions for dealing with XML entities. 
 * 
 * @author Alexander Koller
 *
 */
public class XmlEntities {
    private static Map<String,String> entityDefs = new HashMap<String,String>();
    
    static {
        entityDefs.put("lt","<");
        entityDefs.put("gt",">");
        entityDefs.put("amp","&");
        entityDefs.put("quot","\"");
        entityDefs.put("apos","'");
    }
    
    static String entityPatternSrc = "&#(\\d+);|&(\\w+);";
    static Pattern entityPattern = Pattern.compile(entityPatternSrc);
    
    
    /**
     * Replaces special characters (&lt;, &gt;, quotes, and &amp;)
     * with the XML entities.
     * 
     * @param s a string
     * @return a string with the characters replaced by entities
     */
    public static String encode(String s) {
        if( s == null ) {
            return null;
        } else {
            return s.replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("'", "&apos;")
            .replaceAll("\"", "&quot;");
        }
    }
    
    /**
     * Replaces XML entities (&amp;amp; etc.) by the corresponding
     * characters.<p>
     * 
     * The implementation of this method was taken from Steven Brandt,
     * "RegexRecipes", http://www.javaregex.com/RegexRecipesV1.pdf. 
     * 
     * @param s a string
     * @return a string with the entities replaced by characters.
     * @throws XmlDecodingException if the string contains an undefined
     * character entity.
     */
    public static String decode(String s) throws XmlDecodingException {
        if( s == null ) {
            return null;
        } else {
            Matcher m = entityPattern.matcher(s);
            StringBuffer sb = null;
            
            while(m.find()) {
                if(sb == null) {
                    sb = new StringBuffer();
                }
                
                if(m.group(1) != null) { 
                    // numeric entity
                    int i = Integer.parseInt(m.group(1));
                    
                    m.appendReplacement(sb,
                            Character.toString((char) i));
                    
                    
                } else { 
                    // named entity
                    String entityStr = m.group(2);
                    String entityVal = entityDefs.get(entityStr);
                    
                    if(entityVal != null) {
                        m.appendReplacement(sb,entityVal);
                    } else {
                        throw new XmlDecodingException("Unkown entity: "+entityStr);
                    }
                }
            }
            
            if(sb != null) {
                m.appendTail(sb);
                return sb.toString();
            } else {
                return s;
            }
        }
    }
}

