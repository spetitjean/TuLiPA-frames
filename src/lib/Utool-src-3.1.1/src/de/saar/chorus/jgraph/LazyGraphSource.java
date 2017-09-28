/*
 * @(#)LazyGraphSource.java created 06.09.2005
 * 
 * Copyright (c) 2005 Alexander Koller
 *  
 */

package de.saar.chorus.jgraph;

import java.util.HashMap;
import java.util.Map;

public abstract class LazyGraphSource<GraphType extends ImprovedJGraph> implements IGraphSource {
    private Map<Integer,GraphType> map;
    private int mysize;
    
    public LazyGraphSource(int size) {
        map = new HashMap<Integer,GraphType>();
        mysize = size;
    }
    
    public int size() {
        return mysize;
    }
    
    public GraphType get(int i) {
        Integer ii = new Integer(i);
        
        if( !map.containsKey(ii)) {
            map.put(ii, compute(i));
        }
        
        return map.get(ii);
    }

    abstract protected GraphType compute(int i);
}
