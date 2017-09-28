/*
 * Created on 28.07.2004
 *
 */
package de.saar.chorus.ubench;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.saar.chorus.jgraph.INodeData;



/**
 * The data that can be stored in the node of a dominance graph -- namely,
 * a name, a label, and a node type.
 * 
 * In addition, objects of this class can serve as popup targets, i.e.
 * they provide a menu item for a popup menu.
 *  
 * @author Alexander Koller
 *
 */
public class NodeData extends DomGraphPopupTarget implements INodeData<NodeType> {
	private String name;
    private String simplifiedLabel;
	private String label;
	private NodeType type;
    private boolean showLabel;
	
	/**
	 * New node data for a labelled node (with label).
	 * 
	 * @param name
	 * @param label
	 * @param type
	 */
	public NodeData(NodeType type, String name, String label, JDomGraph parent) {
	    super(parent);
	    
        this.name = name;
		setLabel(label);
		this.type = type;
        showLabel = false;
	}
	
	/**
	 * New node data for an unlabelled node.
	 * 
	 * @param name
	 * @param type
	 */
	public NodeData(NodeType type, String name, JDomGraph parent) {
	    super(parent);
	    
		this.name = name;
		this.type = type;
		this.label = "";
        showLabel = false;
	}
    
    private void setLabel(String label) {
        Pattern p = Pattern.compile("\\s+\\S+:");
        Matcher m = p.matcher(label);
        simplifiedLabel = m.replaceAll(",");
        
        this.label = label;
    }
	
	public String getMenuLabel() {
	    if( label.equals("") ) {
	        return "Node " + name + " (hole)";
	    } else {
	        return "Node " + name + " (" + label + ")";
	    }
	}
	
	
	
	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
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
	public NodeType getType() {
		return type;
	}
	
	public String getDesc() {
		return "(node " + name + " type=" + type + ", label=" + label + ")";
	}
	
	public String toString() {
		
		if(type.equals(NodeType.labelled)) {
			if(showLabel) {
				return simplifiedLabel;
			} else {
				return name;
			}
			
		} else {
			return "(" + name + ")";
		}
	}
    
    public void setShowLabel(boolean b) {
        showLabel = b;
    }

    public String getToolTipText() {
        if(getType().equals(NodeType.labelled)) {
            return getLabel() + " (" + getName() + ")";
        } else {
            return "<hole> (" + getName() +")";
        }
    }
}
