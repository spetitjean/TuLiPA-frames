/*
 * @(#)PropertiesDomGraph.java created 25.03.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides methods for accessing global properties of the Domgraph
 * system, such as the version number. This class reads from a properties
 * file <code>domgraph.properties</code>, which must be located in the same
 * place as the compiled class file. This is the case in the Jar distribution. 
 * 
 * @author Alexander Koller
 *
 */
public class GlobalDomgraphProperties {
    private static Properties props = new Properties();
    
    static {
        InputStream in = 
            GlobalDomgraphProperties.class.getResourceAsStream("domgraph.properties");
        
        if( in != null ) {
            try {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Couldn't open domgraph.properties! This means your distribution is broken.");
                props = new Properties(); 
            }
        }
    }
    
    
    /**
     * Returns the version identifier of this Domgraph system.
     * 
     * @return the version number
     */
    public static String getVersion() {
        return (String) props.get("domgraph.version");
    }
    
    /**
     * Returns the name of the Utool system (including a version number).
     * 
     * @return the system name
     */
    public static String getSystemName() {
        return "Utool " + getVersion();
    }
    
    /**
     * Returns the URL of the Utool homepage.
     * 
     * @return the URL
     */
    public static String getHomepage() {
        return (String) props.get("utool.homepage");
    }
    
    public static boolean allowExperimentalCodecs() {
    	String entry = (String) props.get("utool.allowExperimentalCodecs");
    	
    	return entry == null ? false : Boolean.valueOf(entry);
    }
    

}
