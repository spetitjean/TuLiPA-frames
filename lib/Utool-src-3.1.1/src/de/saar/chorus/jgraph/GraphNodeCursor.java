package de.saar.chorus.jgraph;

import java.util.Set;

import org.jgraph.graph.DefaultGraphCell;

import de.saar.chorus.treelayout.NodeCursorInterface;

/**
 * Class to layout solved forms implementing the <code>NodeCursorInterface</code>.
 * This class provides the methods needed to compute the 
 * node coordinates of solved forms, resp.
 * of a whole <code>JDomGraph</code> that is a forest.
 * 
 * The abstract method <code>processCurrentNode</code> has to be
 * implemented by subclasses.
 * 
 * @author Marco Kuhlmann
 * @author Michaela Regneri
 *
 */
abstract  class GraphNodeCursor implements NodeCursorInterface {

	
	private DefaultGraphCell startNode;	// the graph root
    private DefaultGraphCell node;		// the recently processed node
    private ImprovedJGraph graph;			// the graph to layout
    private Set<DefaultGraphCell> nodesToLayout;
	
    /**
     * A new instance of <code>GraphNodeCursor<code>
     * 
     * @param theNode the graph root
     * @param theGraph the graph to layout
     */
    public GraphNodeCursor(DefaultGraphCell theNode, 
								ImprovedJGraph theGraph) {
        this.startNode = theNode;
        this.node = theNode;
		this.graph = theGraph;
		nodesToLayout = theGraph.getNodes();
    }
    
    /**
     * A new instance of <code>GraphNodeCursor<code>
     * 
     * @param theNode the graph root
     * @param theGraph the graph to layout
     * @param allowedNodes nodes the layout shall arrange, all are taken if not specified
     */
    public GraphNodeCursor(DefaultGraphCell theNode, 
								ImprovedJGraph theGraph,
								Set<DefaultGraphCell> allowedNodes) {
        this.startNode = theNode;
        this.node = theNode;
		this.graph = theGraph;
		nodesToLayout = allowedNodes;
    }
	
    /**
     * Returns the recently processed node.
     */
    public DefaultGraphCell getCurrentNode() {
        return node;
    }
    
    /**
     * Checking whether the current node has a direct 
     * parent node.
     * 
     * @return true if there is a parent node
     */
    public boolean mayMoveUpwards() {
    	
    	/*
    	 * The node must not be the start node, neither the
    	 * graph root.
    	 */
        return ((node != startNode) && (!graph.isRoot(node)) && nodesToLayout.contains( graph.getParents(node).get(0) ));
    }
    
    /**
     * Moving to the current node's parent node 
     * (assuming that there is one).
     */
    public void moveUpwards() {
        node = (DefaultGraphCell) graph.getParents(node).get(0);
    }
    
    /**
     * Checking whether the current node has at least
     * one child.
     * 
     * @return true if there are one ore more children
     */
    public boolean mayMoveDownwards() {
        return (!graph.getChildren(node).isEmpty()) && nodesToLayout.contains( graph.getChildren(node).get(0) );
    }
    
    /**
     * Moving to the current node's most left child
     * (assuming that there is one).
     */
    public void moveDownwards() {
        node = (DefaultGraphCell) graph.getChildren(node).get(0);
    }
    
    /**
     * Checking whether the current node has a sibling on
     * the right.
     * 
     * @return true if there is a right sibling
     */
    public boolean mayMoveSidewards() {
		DefaultGraphCell sibling = graph.getRightSibling(node);
        return (sibling != null) && nodesToLayout.contains(graph.getRightSibling(node));
    }
    
    /**
     * Moving to the current node's right sibling
     * (assuming that there is one).
     * 
     * @see <code>JDomGraph.getRightSibling(DefaultGraphCell node)</code>
     */
    public void moveSidewards() {
        node = graph.getRightSibling(node);
    }
    
    abstract public void processCurrentNode(); 

}
