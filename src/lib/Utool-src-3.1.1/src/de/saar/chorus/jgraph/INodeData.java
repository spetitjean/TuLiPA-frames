/*
 * @(#)INodeData.java created 20.09.2005
 * 
 * Copyright (c) 2005 Alexander Koller
 *  
 */

package de.saar.chorus.jgraph;

public interface INodeData<NodeType> {
    public NodeType getType();
    public String getToolTipText();
}
