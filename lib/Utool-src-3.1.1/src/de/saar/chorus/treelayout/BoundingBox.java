package de.saar.chorus.treelayout;
/**
 * 
 * @author Marco Kuhlmann
 *
 */
public class BoundingBox {
    public int left;
    public int right;
    public int depth;

    public BoundingBox(int l, int r, int d) {
        left = l;
        right = r;
        depth = d;
    }
}
