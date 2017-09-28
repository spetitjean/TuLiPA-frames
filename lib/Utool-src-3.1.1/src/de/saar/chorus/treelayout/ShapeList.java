package de.saar.chorus.treelayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * 
 * @author Marco Kuhlmann
 *
 */
public class ShapeList {
    
    // list of the individual shapes
    private ArrayList shapes;
    
    // minimal separation between the rightmost extent of a left shape and the
    // leftmost extent of a right shape
    private int minimalSeparation;
    
    // whether a merge operation needs to be performed
    private boolean needsMerge;
    
    // the merged shape
    private Shape mergedShape;
    
    // list of the offsets, relative to the axis of the merged shape
    private ArrayList offsetList;
    
    // constructor
    public ShapeList(int theMinimalSeparation) {
        this.minimalSeparation = theMinimalSeparation;
        this.shapes = new ArrayList();
        this.needsMerge = false;
    }
    
    public ShapeList(Collection theShapes, int theMinimalSeparation) {
        this.minimalSeparation = theMinimalSeparation;
        this.shapes = new ArrayList();
        Iterator theShapesIterator = theShapes.iterator();
        while (theShapesIterator.hasNext()) {
            Shape nextShape = (Shape) theShapesIterator.next();
            shapes.add(nextShape);
        }
        this.needsMerge = true;
    }
    
    // add a new shape to the list of shapes
    public void add(Shape theShape) {
        shapes.add(theShape);
        needsMerge = true;
    }
    
    // -------------------------------------------------------------------------
    // methods
    // -------------------------------------------------------------------------
    
    // Return the minimal distance between the axes of shape1 and shape2 that
    // ensures that the rightmost extent of shape1 and the leftmost extent of
    // shape2 obey the minimal separation.
    private int getAlpha(Shape shape1, Shape shape2) {
        int alpha = minimalSeparation;
        int extentR = 0;
        int extentL = 0;
        Iterator extentIterator1 = shape1.iterator();
        Iterator extentIterator2 = shape2.iterator();
        while (extentIterator1.hasNext() && extentIterator2.hasNext()) {
            extentR += ((Extent) extentIterator1.next()).extentR;
            extentL += ((Extent) extentIterator2.next()).extentL;
            alpha = Math.max(alpha, extentR - extentL + minimalSeparation);
        }
        return alpha;
    }
    
    // Merge shape1 and shape2 into a new shape.  The axes of the two shapes
    // are assumed to be alpha units apart.  The merged shape will have the
    // same axis as shape1; this fact makes this operation asymmetric.
    private static Shape merge(Shape shape1, Shape shape2, int alpha) {
        if (shape1.depth() == 0) {
            return shape2;
        } else if (shape2.depth() == 0) {
            return shape1;
        } else {
            Shape result = new Shape();
            Iterator extentIterator1 = shape1.iterator();
            Iterator extentIterator2 = shape2.iterator();
            Extent currentExtent1 = (Extent) extentIterator1.next();
            Extent currentExtent2 = (Extent) extentIterator2.next();
            
            // Extend the topmost right extent by alpha.  This, in effect,
            // moves the second shape to the right by alpha units.
            int topmostL = currentExtent1.extentL;
            int topmostR = currentExtent2.extentR;
            Extent topmostExtent = new Extent(topmostL, topmostR);
            topmostExtent.extend(0, alpha);
            result.add(topmostExtent);
            
            // Now, since extents are given in relative units, in order to
            // compute the extents of the merged shape, we can just collect the
            // extents of shape1 and shape2, until one of the shapes ends.  If
            // this happens, we need to "back-off" to the axis of the deeper
            // shape in order to properly determine the remaining extents.
            int backoffTo1 =
                currentExtent1.extentR - alpha - currentExtent2.extentR;
            int backoffTo2 =
                currentExtent2.extentL + alpha - currentExtent1.extentL;
            while (extentIterator1.hasNext() && extentIterator2.hasNext()) {
                currentExtent1 = (Extent) extentIterator1.next();
                currentExtent2 = (Extent) extentIterator2.next();
                int newExtentL = currentExtent1.extentL;
                int newExtentR = currentExtent2.extentR;
                Extent newExtent = new Extent(newExtentL, newExtentR);
                result.add(newExtent);
                backoffTo1 += currentExtent1.extentR - currentExtent2.extentR;
                backoffTo2 += currentExtent2.extentL - currentExtent1.extentL;
            }
            
            // If shape1 is deeper than shape2, back off to the axis of shape1,
            // and process the remaining extents of shape1.
            if (extentIterator1.hasNext()) {
                currentExtent1 = (Extent) extentIterator1.next();
                int newExtentL = currentExtent1.extentL;
                int newExtentR = currentExtent1.extentR;
                Extent newExtent = new Extent(newExtentL, newExtentR);
                newExtent.extend(0, backoffTo1);
                result.add(newExtent);
                while (extentIterator1.hasNext()) {
                    currentExtent1 = (Extent) extentIterator1.next();
                    result.add(currentExtent1);
                }
            }
            
            // Vice versa, if shape2 is deeper than shape1, back off to the
            // axis of shape2, and process the remaining extents of shape2.
            if (extentIterator2.hasNext()) {
                currentExtent2 = (Extent) extentIterator2.next();
                int newExtentL = currentExtent2.extentL;
                int newExtentR = currentExtent2.extentR;
                Extent newExtent = new Extent(newExtentL, newExtentR);
                newExtent.extend(backoffTo2, 0);
                result.add(newExtent);
                while (extentIterator2.hasNext()) {
                    currentExtent2 = (Extent) extentIterator2.next();
                    result.add(currentExtent2);
                }
            }
            
            return result;
        }
    }
    
    // Compute the shape that results from merging all the shapes in the shape
    // list and centering its axis between the axis of the leftmost and the
    // axis of the rightmost shape.  This method also computes the offset list,
    // which determines the amount of space that each shape in the list needs
    // to be shifted in order to be properly aligned with respect to the axis
    // of the merged shape, where properly is defined as in Kennedy's paper.
    private void merge() {
        int numberOfShapes = shapes.size();
        if (numberOfShapes == 1) {
            mergedShape = (Shape) shapes.get(0);
            offsetList = new ArrayList();
            offsetList.add(new Integer(0));
        } else {
            // alphaL[] and alphaR[] store the necessary distances between the
            // axes of the shapes in the list: alphaL[i] gives the distance
            // between shape[i] and shape[i-1], when shape[i-1] and shape[i]
            // are merged left-to-right; alphaR[i] gives the distance between
            // shape[i] and shape[i+1], when shape[i] and shape[i+1] are merged
            // right-to-left.
            int[] alphaL = new int[numberOfShapes];
            int[] alphaR = new int[numberOfShapes];
            
            // distance between the leftmost and the rightmost axis in the list
            int width = 0;
            
            Shape currentShapeL = (Shape) shapes.get(0);
            Shape currentShapeR = (Shape) shapes.get(numberOfShapes - 1);
            
            for (int i = 1; i < numberOfShapes; i++) {
                // Merge left-to-right.  Note that due to the asymmetry of the
                // merge operation, nextAlphaL is the distance between the
                // *leftmost* axis in the shape list, and the axis of
                // nextShapeL; what we are really interested in is the distance
                // between the *previous* axis and the axis of nextShapeL.
                // This explains the correction.
                Shape nextShapeL = (Shape) shapes.get(i);
                int nextAlphaL = getAlpha(currentShapeL, nextShapeL);
                currentShapeL = merge(currentShapeL, nextShapeL, nextAlphaL);
                alphaL[i] = nextAlphaL - width;
                width = nextAlphaL;
                
                // Merge right-to-left.  Here, a correction of nextAlphaR is
                // not required.
                Shape nextShapeR = (Shape) shapes.get(numberOfShapes - 1 - i);
                int nextAlphaR = getAlpha(nextShapeR, currentShapeR);
                currentShapeR = merge(nextShapeR, currentShapeR, nextAlphaR);
                alphaR[numberOfShapes - i] = nextAlphaR;
            }
            
            // The merged shape for the shape list is the last shape from any
            // of the merge directions; here, we pick currentShapeR.
            mergedShape = currentShapeR;
            
            // After the loop, the merged shape has the same axis as the
            // leftmost shape in the list.  What we want is to move the axis
            // such that it is the center of the axis of the leftmost shape in
            // the list and the axis of the rightmost shape.
            int halfWidth = width / 2;
            mergedShape.move(- halfWidth);
            
            // Finally, for the offset lists.  Now that the axis of the merged
            // shape is at the center of the two extreme axes, the first shape
            // needs to be offset by -halfWidth units with respect to the new
            // axis.  As for the offsets for the other shapes, we take the
            // median of the alphaL and alphaR values, as suggested in
            // Kennedy's paper.
            int offset = - halfWidth;
            offsetList = new ArrayList(numberOfShapes);
            offsetList.add(new Integer(offset));
            for (int i = 1; i < numberOfShapes; i++) {
                offset += (alphaL[i] + alphaR[i]) / 2;
                offsetList.add(new Integer(offset));
            }
        }
        needsMerge = false;
    }
    
    // accessor for the merged shape
    public Shape getMergedShape() {
        if (needsMerge) {
            merge();
        }
        return mergedShape;
    }
    
    // return an iterator for the offset list
    public Iterator offsetIterator() {
        if (needsMerge) {
            merge();
        }
        return offsetList.iterator();
    }
    
}
