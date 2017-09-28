package de.saar.chorus.treelayout;
/**
 * 
 * @author Marco Kuhlmann
 *
 */

public class Coordinate {
    
    private int x;
    private int y;
    
    public Coordinate() {
        this.x = 0;
        this.y = 0;
    }
    
    public Coordinate(int theX, int theY) {
        this.x = theX;
        this.y = theY;
    }
    
    public int x() {
        return x;
    }
    
    public int y() {
        return y;
    }
    
    public void set(int theX, int theY) {
        this.x = theX;
        this.y = theY;
    }

}
