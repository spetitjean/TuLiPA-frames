/*
 * @(#)CodecTools.java created 03.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.codec;

import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.NodeLabels;
import de.saar.chorus.domgraph.graph.NodeType;


/**
 * Methods that are useful in the implementation of codecs. 
 * 
 * @author Alexander Koller
 *
 */
public class CodecTools {
    
    /**
     * Computes a string that is a valid Oz or Prolog atom from
     * the argument. If the argument starts with a lowercase letter
     * and all later symbols are letters, digit, or underscore,
     * the argument itself is returned; otherwise, the argument
     * is surrounded by quotes '...'. 
     * 
     * @param label a string
     * @return the string possibly surrounded with quotes
     */
    public static String atomify(String label) {
    	// check first character    	
    	if (label.charAt(0) < 'a' || label.charAt(0) > 'z')
    		return ("'" + label + "'");

    	// check rest
    	for (int i = 1; i < label.length(); ++i) {
    		// we cannot use Character.isLetterOrDigit here because the result of this
    		// method depends on the encoding (i.e., possibly treats umlauts etc. as letters).
    		if ((label.charAt(0) < 'a' || label.charAt(0) > 'z') &&
    			(label.charAt(0) < 'A' || label.charAt(0) > 'Z') &&
    			(label.charAt(0) < '0' || label.charAt(0) > '9') &&
    			(label.charAt(0) != '_'))
    			return ("'" + label + "'");    		
    	}
     	return label;	
    }

    /**
     * Computes a string that is a valid Prolog variable from
     * the argument. If the argument starts with an uppercase
     * letter or an underscore, it is returned directly; otherwise,
     * it is prefixed with an underscore.<p>
     * 
     * Note that the returned string will still not be a valid
     * Prolog variable name if it contains symbols that are not
     * valid in Prolog variable names.
     * 
     * @param label a straing
     * @return the string, possibly prefixed with an underscore
     */
    public static String varify(String label) {
        if( !label.startsWith("_") && !Character.isUpperCase(label.charAt(0)) ) {
            return "_" + label;
        } else {
            return label;
        }
    }
    
    /**
     * Asserts that all labelled nodes in the dominance graph actually have
     * labels. If this is not the case and assertions are enabled, then
     * an assertion exception is thrown. This is useful in debugging codecs.
     * 
     * @param graph a dominance graph
     * @param labels a matching labels object
     */
    public static void graphLabelsConsistencyAssertion(DomGraph graph, NodeLabels labels) {
        for( String node : graph.getAllNodes() ) {
            if( graph.getData(node).getType() == NodeType.LABELLED ) {
                assert (labels.getLabel(node) != null) : "no label for labelled node " + node;
            }
        }
    }

}
