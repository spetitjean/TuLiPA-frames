/*
 * Created on 28.07.2004
 *
 */
package de.saar.chorus.ubench;

import de.saar.chorus.jgraph.IEdgeData;

/**
 * The data that can be stored in the edge of a dominance graph -- namely,
 * a name and an edge type.
 * 
 * In addition, objects of this class can serve as popup targets, i.e.
 * they provide a menu item for a popup menu.
 *  
 * @author Alexander Koller
 *
 */
public class EdgeData extends DomGraphPopupTarget implements IEdgeData<EdgeType> {
	

	private EdgeType type;
	private String name;
		

	/**
	 * @param type
	 * @param name
	 */
	public EdgeData(EdgeType type, String name, JDomGraph parent) {
	    super(parent);
	    
		this.type = type;
		this.name = name;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return Returns the type.
	 */
	public EdgeType getType() {
		return type;
	}
	
	public String getDesc() {
		return "(edge " + name + " type=" + type + ")";
	}
	
	public String toString() {
		return "";
	}
	
	/* (non-Javadoc)
     * @see de.saar.chorus.ubench.DomGraphPopupTarget#getMenuLabel()
     */
    public String getMenuLabel() {
        return "Edge " + name;
    }
}
