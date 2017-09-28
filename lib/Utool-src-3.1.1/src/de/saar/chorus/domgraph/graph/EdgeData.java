/*
 * @(#)EdgeData.java created 25.01.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.graph;


/**
 * The data associated with an edge. At this point,
 * this is simply the edge type (tree or dominance).
 * 
 * @author Alexander Koller
 *
 */
public class EdgeData {

    private EdgeType type;
        

    /**
     * The constructor.
     * 
     * @param type the type of this edge
     */
    public EdgeData(EdgeType type) {
        this.type = type;
    }


    /**
     * Returns the edge type.
     * 
     * @return the type
     */
    public EdgeType getType() {
        return type;
    }
    
    
    /**
     * Returns a string representation of this edge-data object.
     */
    public String toString() {
        return "[E:" + type + "]";
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EdgeData) {
            EdgeData objAsEd = (EdgeData) obj;
            
            return type.equals(objAsEd.type);
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        return type.hashCode();
    }
    
    
}
