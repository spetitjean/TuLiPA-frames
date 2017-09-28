/*
 * @(#)Split.java created 25.01.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A split in a dominance chart. A split of a subgraph is
 * induced by the choice of a certain free fragment as the root
 * of a solved form of this subgraph. It also records the
 * weakly connected components into which the subgraph is split
 * by removing the free fragment, and to which hole of the
 * fragment the nodes of each WCC are connected.
 * 
 * @author Alexander Koller
 *
 */
public class Split {
    private String rootFragment;
    private Map<String,List<Set<String>>> wccs;  // root/hole -> wccs
    
    /**
     * Creates a split with a given root fragment.
     * 
     * @param rootFragment the root fragment of this split
     */
    public Split(String rootFragment) {
        this.rootFragment = rootFragment;
        wccs = new HashMap<String,List<Set<String>>>();
    }
    
    /**
     * Adds a weakly connected component to a split representation.
     * 
     * @param hole the hole of the free fragment to which the wcc is connected
     * @param wcc a weakly connected component of the subgraph
     */
    public void addWcc(String hole, Set<String> wcc) {
        List<Set<String>> wccSet = wccs.get(hole);
        
        if( wccSet == null ) {
            wccSet = new ArrayList<Set<String>>();
            wccs.put(hole, wccSet);
        }
        
        wccSet.add(wcc);
    }
    
    
    /**
     * Returns the root fragment of this split.
     * 
     * @return the root fragment
     */
    public String getRootFragment() {
        return rootFragment;
    }

    /**
     * Returns the set of weakly connected components which
     * are connected to the specified hole.
     * 
     * @param hole a hole of the root fragment
     * @return the list of wccs connected to this hole, or
     * <code>null</code> if this is not a hole or no wccs
     * are connected to it.
     */
    public List<Set<String>> getWccs(String hole) {
        return wccs.get(hole);
    }
    
    /**
     * Returns the set of holes of the root fragment
     * which are connected to any wcc.
     * 
     * @return the set of holes
     */
    public Set<String> getAllDominators() {
        return wccs.keySet();
    }
    
    
    /**
     * Returns the set of WCCs into which the subgraph
     * is split by removing the root fragment.
     * 
     * @return the set of wccs
     */
    public List<Set<String>> getAllSubgraphs() {
        List<Set<String>> ret = new ArrayList<Set<String>>();
        
        for( String node : wccs.keySet() ) {
            ret.addAll(wccs.get(node));
        }
        
        return ret;
    }
    
    public String toString() {
        return "<" + rootFragment + " " + wccs + ">";
    }
}
