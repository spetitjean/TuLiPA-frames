/*
 * @(#)DummyEdgeData.java created 06.03.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.jgraph;

public class EdgeDataAdapter<E> implements IEdgeData<Object>  {
    private E realData;
    
    public EdgeDataAdapter(E realData) {
        this.realData = realData;
    }

    public Object getType() {
        return null;
    }
    
    public String toString() {
        return realData.toString();
    }

    public boolean equals(Object obj) {
        return realData.equals(obj);
    }

    public int hashCode() {
        return realData.hashCode();
    }
    
    public E getData() {
        return realData;
    }

}
