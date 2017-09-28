/*
 * Created on 28.07.2004
 */


package de.saar.chorus.ubench;


/**
 * A node type -- either labelled or unlabelled (i.e. non-hole or hole).
 * 
 * TODO This should be replaced by a proper enumeration.
 * But Eclipse doesn't support enum types yet, so we'll use this hack for now.
 * It is safe (and encouraged) to use NodeType.labelled and NodeType.unlabelled
 * and compare them with ==.
 * 
 * @author Alexander Koller
 */
public class NodeType {
	
	public static final int
		labelledVal = 1,
		unlabelledVal = 2;
	
	public static final	NodeType
		labelled = new NodeType(labelledVal),
		unlabelled = new NodeType(unlabelledVal);
	
	private int type;
	
	

	/**
	 * @param type
	 */
	public NodeType(int type) {
		this.type = type;
	}
	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
	
	public String toString() {
		if( type == labelledVal )
			return "labelled";
		else
			return "unlabelled";
	}
}
