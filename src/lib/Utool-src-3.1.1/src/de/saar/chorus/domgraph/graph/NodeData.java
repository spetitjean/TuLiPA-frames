/*
 * @(#)NodeData.java created 25.01.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.graph;


/**
 * The data associated with a node. At this point,
 * this is simply the node type (labelled or unlabelled).
 *  
 * @author Alexander Koller
 *
 */
public class NodeData {
    private NodeType type;
    
    public NodeData(NodeType type) {
        this.type = type;
    }
    
    
    
    /**
     * Returns the node type.
     * 
     * @return the type
     */
    public NodeType getType() {
        return type;
    }
    
    /**
     * Returns a string representation of this node-data object.
     */
    public String toString() {
        return ((type==NodeType.LABELLED)?"[L]":"[U]");
    }


    /**
     * Set the node type.
     * 
     * @param type the new type of the node
     */
    public void setType(NodeType type) {
        this.type = type;
    }



    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeData) {
            NodeData objAsNd = (NodeData) obj;
            return type.equals(objAsNd.type);
        } else {
            return false;
        }
    }



    @Override
    public int hashCode() {
        return type.hashCode();
    }
    
    
}
