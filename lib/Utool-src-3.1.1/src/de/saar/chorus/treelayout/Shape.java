//
//  Shape.java
//  GecodeExplorer
//
//  Created by Marco Kuhlmann on 2005-01-17.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//

package de.saar.chorus.treelayout;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @author Marco Kuhlmann
 *
 */
public class Shape {

    private ArrayList shape;
	
    public Shape() {
        shape = new ArrayList();
    }
	
    public Shape(Extent extent) {
        shape = new ArrayList(1);
        shape.add(extent);
    }
	
    public Shape(Extent extent, Shape subShape) {
        shape = new ArrayList(subShape.depth() + 1);
        shape.add(extent);
        Iterator extentIterator = subShape.iterator();
        while (extentIterator.hasNext()) {
            Extent currentExtent = (Extent) extentIterator.next();
            shape.add(currentExtent);
        }
    }
    
    public int depth() {
        return shape.size();
    }
    
    public void add(Extent Extent) {
        shape.add(Extent);
    }
    
    public Extent get(int i) {
        return ((Extent) shape.get(i));
    }
    
    public Iterator iterator() {
        return shape.iterator();
    }
    
    public void extend(int deltaL, int deltaR) {
        if (shape.size() > 0) {
            ((Extent) shape.get(0)).extend(deltaL, deltaR);
        }
    }
    
    public void move(int delta) {
        if (shape.size() > 0) {
            ((Extent) shape.get(0)).move(delta);
        }
    }
    
    public Extent getExtentAtDepth(int depth) {
        Iterator extentIterator = shape.iterator();
        int currentDepth = 0;
        int extentL = 0;
        int extentR = 0;
        while (extentIterator.hasNext() && currentDepth <= depth) {
            Extent currentExtent = (Extent) extentIterator.next();
            extentL += currentExtent.extentL;
            extentR += currentExtent.extentR;
            currentDepth++;
        }
        if (currentDepth == depth + 1) {
            return new Extent(extentL, extentR);
        } else {
            return null;
        }
    }
    
	
    public BoundingBox getBoundingBox() {
        Iterator extents = iterator();
        int lastLeft = 0;
        int lastRight = 0;
        int left = 0;
        int right = 0;
        int depth = 0;
        while (extents.hasNext()) {
            Extent curExtent = (Extent) extents.next();
            depth++;
            lastLeft = lastLeft + curExtent.extentL;
            lastRight = lastRight + curExtent.extentR;
            if (lastLeft < left)
                left = lastLeft;
            if (lastRight > right)
                right = lastRight;
        }
        return new BoundingBox(left, right, depth);
    }

    public String toString() {
        return shape.toString();
    }
    
}
