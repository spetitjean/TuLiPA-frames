/*
 * @(#)FragmentWithHole.java created 06.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.equivalence;

class FragmentWithHole {
    private String rootlabel;
    private int holeindex;
    
    public FragmentWithHole(String rootlabel, int holeindex) {
        super();

        this.holeindex = holeindex;
        this.rootlabel = rootlabel;
    }
    
    public FragmentWithHole() {
        rootlabel = null;
        holeindex = -1;
    }

    public int getHoleIndex() {
        return holeindex;
    }

    public String getRootLabel() {
        return rootlabel;
    }
    
    public void setHoleIndex(int holeindex) {
        this.holeindex = holeindex;
    }

    public void setRootLabel(String rootlabel) {
        this.rootlabel = rootlabel;
    }
    
    public boolean equals(Object o) {
        if( o == null ) {
            return false;
        }
        
        try {
            FragmentWithHole other = (FragmentWithHole) o;
            return rootlabel.equals(other.rootlabel) && (holeindex == other.holeindex);
        } catch(ClassCastException e) {
            return false;
        }
    }
    
    public int hashCode() {
        return toString().hashCode();
    }

    // change this method with care (if at all), as the
    // comparison operators in FragmentPair depend on it.
    public String toString() {
        return rootlabel + "/" + holeindex;
    }
    
    
}
