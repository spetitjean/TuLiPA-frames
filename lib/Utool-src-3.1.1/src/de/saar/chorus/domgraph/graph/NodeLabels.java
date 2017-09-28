/*
 * @(#)NodeLabels.java created 25.01.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.graph;

import java.util.HashMap;
import java.util.Map;



/**
 * A map which is used for storing the node labels of a labelled
 * dominance graph.  
 * 
 * @author Alexander Koller
 *
 */
public class NodeLabels {
    private Map<String,String> labels;
    
    public NodeLabels() {
        labels = new HashMap<String,String>();
    }
    
    /**
     * Removes all node-label mappings from this map.
     */
    public void clear() {
        labels.clear();
    }
    
    /**
     * Adds a label for a given node.
     * 
     * @param node the node
     * @param label the label for this node
     */
    public void addLabel(String node, String label) {
        labels.put(node,label);
    }
    
    /**
     * Gets the label for a given node. If the node was not
     * assigned a label, returns null.
     * 
     * @param node a node
     * @return the label, or null if the node has no label
     */
    public String getLabel(String node) {
        return labels.get(node);
    }
    
    public String toString() {
        return labels.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeLabels) {
            NodeLabels l = (NodeLabels) obj;
            return labels.equals(l.labels);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return labels.hashCode();
    }
    
    
}
