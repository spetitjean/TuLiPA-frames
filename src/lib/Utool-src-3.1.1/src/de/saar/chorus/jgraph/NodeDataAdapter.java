/*
 * @(#)DummyNodeData.java created 06.03.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.jgraph;

public class NodeDataAdapter<E> implements INodeData<Object> {
    private E realData;

    public NodeDataAdapter(E realData) {
        this.realData = realData;
    }

    public Object getType() {
        return null;
    }

    public String getToolTipText() {
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
