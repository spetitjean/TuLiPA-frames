/*
 * Created on 31.07.2004
 *
 */
package de.saar.chorus.ubench;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.util.JGraphUtilities;

/**
 * A fragment in a dominance graph. This fragment contains nodes (labelled
 * and unlabelled) and solid edges, such that the contained nodes are all
 * connected by the contained edges.
 * 
 * Fragments of a dominance graph are actually computed by the computeFragments
 * method in JDomGraph.
 * 
 * Objects of this class can serve as popup targets, i.e.
 * they provide a menu item for a popup menu.
 * 
 * @author Alexander Koller
 *
 */
public class Fragment extends DomGraphPopupTarget {
    private Set<DefaultGraphCell> nodes;
    private Set<DefaultEdge> edges;
    private DefaultGraphCell groupObject;
    private StringBuilder fragmentName;
   
    
    Fragment(JDomGraph parent) {
        super(parent);
        
        nodes = new HashSet<DefaultGraphCell>();
        edges = new HashSet<DefaultEdge>();
        fragmentName = new StringBuilder("fragment: ");
        groupObject = new DefaultGraphCell(fragmentName);
    }
    
    /**
     * Add a node to this fragment.
     * 
     * @param node
     */
    void add(DefaultGraphCell node) {
        nodes.add(node);
        groupObject.add(node);
        fragmentName.append(((NodeData)node.getUserObject()).getName() + " ");
    }
    
    /**
     * Add a solid edge to this fragment.
     * 
     * @param edge
     */
    void add(DefaultEdge edge) {
        edges.add(edge);
        groupObject.add(edge);
    }
    
    /**
     * Add all nodes and edges of some other fragment to this one.
     * This is useful when two fragments are merged when computing
     * the maximal fragments of a dominance graph.
     * 
     * @param frag
     */
    void addAll(Fragment frag) {
        for( DefaultGraphCell node : frag.getNodes() ) {
            add(node);
        }
        
        for( DefaultEdge edge : frag.getEdges() ) {
            add(edge);
        }
    }
    
    /**
     * Get all nodes in this fragment.
     * 
     * @return the set of all nodes.
     */
    public Set<DefaultGraphCell> getNodes() {
        return nodes;
    }
    
    /**
     * Get all cells in this fragment. This includes all nodes, all edges,
     * and the primary ports of the nodes which were used to connect the
     * edges.
     * 
     * @return all cells in this fragment.
     */
    Set<Object> getAllCells() {
        Set<Object> ret = new HashSet<Object>();
        ret.addAll(getNodes());
        ret.addAll(getEdges());
        
        // ports
        for( DefaultGraphCell node : getNodes() ) {
            ret.add(node.getChildAt(0));
        }
        
        return ret;
    }
    
    /**
     * Get all edges in this fragment.
     * 
     * @return the set of all edges.
     */
    Set<DefaultEdge> getEdges() {
        return edges;
    }

    
    
    /**
     * Get the number of nodes in this fragment.
     * 
     * @return the number of nodes.
     */
    public int size() {
        return nodes.size();
    }

    /**
     * Get the graph cell corresponding to the entire fragment.
     * This will be a group cell to which all the nodes, primary ports,
     * and edges (as given by getAllCells) belong.
     * 
     * @return the cell.
     */
    DefaultGraphCell getGroupObject() {
        return groupObject;
    }
    
    /**
     * Returns the children of a given node contained in
     * this Fragment. Returns null if the given source 
     * node is not contained in the fragment.
     * 
     * @param node, the source node
     * @return a Set of the node's children.
     */
    List<DefaultGraphCell> getChildren(DefaultGraphCell node){
    	
    	List<DefaultGraphCell> fragChildren = new ArrayList<DefaultGraphCell>();
    	
    	//if there is no node, there are no children.
    	if(! getNodes().contains(node))
    		return null;
    	else {
    		//iterating over the edges.
    		for(DefaultEdge edge : getEdges()){
    			
    			//if we find one going out from our source...
    			if(JGraphUtilities.getSourceVertex(getParent(),edge).equals(node)){
    				DefaultGraphCell potChild = 
    					(DefaultGraphCell) JGraphUtilities.getTargetVertex(getParent(), edge);
    				
    				//we add it, in case it is part of the fragment.
    				if(getNodes().contains(potChild))
    					fragChildren.add(potChild);
    			}
    		}
    		
    	}
    	return fragChildren;
    }
    
    /**
     * Indicates if a node is a Leaf - considering
     * just the nodes this fragment contains.
     * Returns false if the node is not contained
     * in this fragment.
     * 
     * @param node, the node to check
     * @return true if the node is a leaf;
     */
    boolean isLeaf(DefaultGraphCell node) {
    	if(getChildren(node) == null) {
    		return false;
    	}
    	return getChildren(node).isEmpty();
    }
    
   
   
    /**
     * returns the parent node of a fragment cell.
     * returns null if the given node is the root of 
     * the fragment or if it is not contained in the
     * fragment.
     * 
     * @param child, the node to find the parent from
     * @return the parent or null
     */
    DefaultGraphCell getParent(DefaultGraphCell child) {
    	for(DefaultEdge edge : getEdges()){
    		DefaultGraphCell potChild = 
    			(DefaultGraphCell) JGraphUtilities.getTargetVertex(getParent(), edge);
    		if(potChild.equals(child)) {
    			return (DefaultGraphCell) JGraphUtilities.getSourceVertex(getParent(), edge);
    		}
    	}
    	return null;
    }
    
    
   /**
    * Returns the neighbor of a given node - or null,
    * if there is none.
    * @param node, the node to compute the neighbor from
    * @return the neighbor or null
    */
    DefaultGraphCell getNeighbour(DefaultGraphCell node){
    	for(DefaultEdge edge : this.getEdges()){
    		
    		if(JGraphUtilities.getTargetVertex(getParent(), edge).equals(
    				this.getParent(node))){
    			DefaultGraphCell potNeighbor = 
    				(DefaultGraphCell) JGraphUtilities.getTargetVertex(getParent(), edge);
    			if(! potNeighbor.equals(node)) {
    				return potNeighbor;
    			}
    		}
    	}
    	
    	return null;
    }
    
    
    /**
     * Returns all the leaves, considering the nodes
     * of this fragment.
     * 
     * @return the leaves
     */
    List<DefaultGraphCell> getLeaves(){
    	ArrayList<DefaultGraphCell> leaves = new ArrayList<DefaultGraphCell>();
    	
    	for(DefaultGraphCell node: this.getNodes()){
    		if(isLeaf(node))
    			leaves.add(node);
    	}
    	return leaves;
    }
    
    public String toString() {
        return "<" + fragmentName.toString() + ">";
    }

    public String getMenuLabel() {
	    return fragmentName.toString();
	}

	
	
	
	
}
