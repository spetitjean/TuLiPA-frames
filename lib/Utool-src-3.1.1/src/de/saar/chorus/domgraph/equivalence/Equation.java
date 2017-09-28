/*
 * @(#)FragmentPair.java created 06.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.equivalence;

class Equation {
    private FragmentWithHole q1, q2;

    public Equation(FragmentWithHole q1, FragmentWithHole q2) {
        super();

        this.q1 = q1;
        this.q2 = q2;
    }

    public FragmentWithHole getQ1() {
        return q1;
    }

    public void setQ1(FragmentWithHole q1) {
        this.q1 = q1;
    }

    public FragmentWithHole getQ2() {
        return q2;
    }

    public void setQ2(FragmentWithHole q2) {
        this.q2 = q2;
    }
    
    public String toString() {
        return q1 + "=" + q2;
    }
    
    public boolean equals(Object _other) {
        Equation other;
        
        try {
            other = (Equation) _other;
        } catch(ClassCastException e) {
            return false;
        }
        
        return 
            ((q1 == null) && (other.q1 == null))
            || ((q2 == null) && (other.q2 == null))
            || (q1.getRootLabel().equals(other.q1.getRootLabel())
                    && q2.getRootLabel().equals(other.q2.getRootLabel())
                    && (q1.getHoleIndex() == other.q1.getHoleIndex())
                    && (q2.getHoleIndex() == other.q2.getHoleIndex()))
            || (q1.getRootLabel().equals(other.q2.getRootLabel())
                    && (q2.getRootLabel().equals(other.q1.getRootLabel()))
                    && (q1.getHoleIndex() == other.q2.getHoleIndex())
                    && (q2.getHoleIndex() == other.q1.getHoleIndex()));
    }
    
    public int hashCode() {
        if( (q1 == null) || (q2 == null) ) {
            return 0;
        } else {
            return q1.toString().hashCode() + q2.toString().hashCode();
        }
    }
}
