/*
 * @(#)ListGraphSource.java created 25.08.2005
 * 
 * Copyright (c) 2005 Alexander Koller
 *  
 */

package de.saar.chorus.jgraph;

import java.util.List;

public class ListGraphSource<GraphType extends ImprovedJGraph> implements IGraphSource {
    private List<GraphType> graphs;
    
    public ListGraphSource(List<GraphType> graphs) {
        this.graphs = graphs;
    }

    public int size() {
        return graphs.size();
    }

    public ImprovedJGraph get(int index) {
        return graphs.get(index);
    }
}
