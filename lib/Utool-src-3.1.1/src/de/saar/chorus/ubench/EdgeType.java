/*
 * Created on 28.07.2004
 *
 */



package de.saar.chorus.ubench;


/**
 * An edge type -- either solid or dominance.
 * 
 * TODO This should be replaced by a proper enumeration.
 * But Eclipse doesn't support enum types yet, so we'll use this hack for now.
 * It is safe (and encouraged) to use EdgeType.solid and EdgeType.dominance and compare
 * them with ==.
 * 
 * @author Alexander Koller
 */

public class EdgeType {
	public static final int
		solidVal = 1,
		dominanceVal = 2;
	
	public static final EdgeType
		solid = new EdgeType(solidVal),
		dominance = new EdgeType(dominanceVal);
	
	private int type;
	

	/**
	 * @param type
	 */
	public EdgeType(int type) {
		this.type = type;
	}
	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	public String toString() {
		if( type == solidVal )
			return "solid";
		else
			return "dominance";
	}
}
