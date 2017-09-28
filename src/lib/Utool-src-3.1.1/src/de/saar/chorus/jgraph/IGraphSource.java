/*
 * @(#)GraphSource.java created 25.08.2005
 * 
 * Copyright (c) 2005 Alexander Koller
 *  
 */

package de.saar.chorus.jgraph;

public interface IGraphSource {
    public int size();
    public ImprovedJGraph get(int index);
}
