/*
 * @(#)ImprovedJGraphLayout.java created 20.09.2005
 * 
 * Copyright (c) 2005 Alexander Koller
 *  
 */

package de.saar.chorus.jgraph;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.layout.JGraphLayoutAlgorithm;

import de.saar.chorus.treelayout.Shape;

public abstract class ImprovedJGraphLayout extends JGraphLayoutAlgorithm {
    
    /**
     * Starts the layout algorithm.
     */
    public abstract void run(JGraph gr, Object[] cells, int arg2);

    public abstract Integer getRelXtoParent(DefaultGraphCell node);

    public abstract void addRelXtoParent(DefaultGraphCell node, Integer x);

    public abstract void addRelXtoRoot(DefaultGraphCell node, Integer x);

    public abstract void addRelYpos(DefaultGraphCell node, Integer y);

    /**
     * Returns the node with computed by the <code>JDomGraph</code>.
     * 
     * @param node the node to compute the width for
     * @return the width
     */
    public abstract int getNodeWidth(DefaultGraphCell node);

    public abstract Shape getNodesToShape(DefaultGraphCell node);

    public abstract void putNodeToShape(DefaultGraphCell node, Shape shape);

}