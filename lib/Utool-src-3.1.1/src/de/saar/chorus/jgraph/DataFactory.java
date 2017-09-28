/*
 * @(#)DataFactory.java created 06.03.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.jgraph;

import org._3pq.jgrapht.Edge;

public interface DataFactory<NodeData,EdgeData> {
    public NodeData getNodeData(Object node);
    public EdgeData getEdgeData(Edge edge);
}
