/*
 * @(#)Formula.java created 22.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.codec.glue;

import java.util.ArrayList;
import java.util.List;

class Formula {
    public enum Type {
        ATOM,
        VARIABLE,
        IMPLICATION
    }
    
    private Type type;
    private String symbol;
    private List<Formula> subformulas;
    
    public Formula(Type type, Formula sub1, Formula sub2) {
        this.type = type;
        this.subformulas = new ArrayList<Formula>(2);
        subformulas.add(sub1);
        subformulas.add(sub2);
        symbol = null;
    }
    
    public Formula(Type type, Formula sub1) {
        this.type = type;
        this.subformulas = new ArrayList<Formula>(1);
        subformulas.add(sub1);
        symbol = null;
    }

    public Formula(Type type, String symbol) {
        this.type = type;
        this.subformulas = null;
        this.symbol = symbol;
    }

    public List<Formula> getSubformulas() {
        return subformulas;
    }

    public String getSymbol() {
        return symbol;
    }

    public Type getType() {
        return type;
    }
    
    public String toString() {
        return toString(false);
    }
    
    private String toString(boolean addBrackets) {
        switch(type) {
        case ATOM:
        case VARIABLE:
            return symbol;
            
        case IMPLICATION:
            if( addBrackets ) {
                return "(" + subformulas.get(0).toString(true)
                + " -o " + subformulas.get(1).toString(true) + ")";
            } else {
                return subformulas.get(0).toString(true)
                + " -o " + subformulas.get(1).toString(true);
            }
        }
        
        return null;
    }

    public boolean equals(Object obj) {
        if( obj instanceof Formula ) {
            Formula f = (Formula) obj;
            
            if( type != f.type ) {
                return false;
            } else {
                switch(type) {
                case ATOM:
                case VARIABLE:
                    return symbol.equals(f.symbol);
                    
                case IMPLICATION:
                    return subformulas.get(0).equals(f.subformulas.get(0))
                    && subformulas.get(1).equals(f.subformulas.get(1));
                    
                default:
                    // unreachable
                    return false;
                }
            }
        } else {
            return false;
        }
    }
    
        
	public int depth() {
		switch(type) {
		case ATOM:
		case VARIABLE:
			return 0;
		case IMPLICATION:
			return 1 + subformulas.get(1).depth();
		}
		return 0;
	}

	public List<Formula> getSuffixes() {
		List suff = new ArrayList<Formula>();
	    switch ( type ) {
        case IMPLICATION:
        		suff.add(this);
                suff.addAll(subformulas.get(1).getSuffixes());
                break;
        case ATOM:
        case VARIABLE:
                suff.add(this);
        }
        return suff;
	}

	public boolean isSuffix(List<Formula> formulas) {
		for (int i = 0; i < formulas.size(); i++) {
			if ( formulas.get(i).getSuffixes().contains(this) )
				return true;
		}
		return false;
	}
	
	public boolean subsumes(Formula f) {
		switch(type) {
		case IMPLICATION:
			if ( (f.type != Type.IMPLICATION) || (!subformulas.get(0).equals(f.subformulas.get(0))) )
				return false;
			else {
				return subformulas.get(1).subsumes(f.subformulas.get(1));
			}
		case ATOM:
			if ( equals(f) )
				return true;
			else
				return false;
		case VARIABLE:
			return true;
		}
		return false;
	}
	
	// Tests only for right-unification! i.e. variables only tested for in the input formula (this)!
	public boolean isSuffixModuloUnif(List<Formula> formulas) {
		for (int i = 0; i < formulas.size(); i++) {
				List<Formula> suffixes = formulas.get(i).getSuffixes();
				for (int j = 0; j < suffixes.size(); j++)
					if ( subsumes(suffixes.get(j)) ) {
						System.out.println("\t\t" + this + " subsumes " + suffixes.get(j) + ", a suffix of " + formulas.get(i)  + ".");
						return true;
					}
		}
		return false;
	}
	
}
