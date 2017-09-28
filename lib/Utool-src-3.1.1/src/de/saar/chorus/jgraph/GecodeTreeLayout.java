package de.saar.chorus.jgraph;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

import de.saar.chorus.treelayout.BoundingBox;
import de.saar.chorus.treelayout.PostOrderNodeVisitor;
import de.saar.chorus.treelayout.PreOrderNodeVisitor;
import de.saar.chorus.treelayout.Shape;


/**
 * A layout algorithm for a solved form of a dominance graph
 * represented by a <code>JDomGraph</code>. This tree layout
 * uses the <code>GECODE</code> tree layout classes.
 * 
 * @author Michaela Regneri
 *
 */
public class GecodeTreeLayout extends ImprovedJGraphLayout {
	
	// the solved form to layout
	private ImprovedJGraph graph;
	
	private int globalXOffset;
	// the relative positions...
	
	//...to the parent node
	private Map<DefaultGraphCell,Integer> relXtoParent;
	
	//to the graph root
	private Map<DefaultGraphCell,Integer> relXtoRoot;
	private Map<DefaultGraphCell,Integer> relYpos;
	
	// the absolute position of a node in the graph
	private Map<DefaultGraphCell,Integer> xPos;
	private Map<DefaultGraphCell,Integer> yPos;
	
	// maps the root of every subtree to the subtree shape
	private Map<DefaultGraphCell, Shape> nodesToShape;

	/**
	 * Initializes a new <code>SolvedFormLayout</code> with the
	 * given <code>JDomGraph</code>.
	 * 
	 * @param gr the solved form as <code>JDomGraph</code> to layout
	 */
	public GecodeTreeLayout(ImprovedJGraph gr) {
		graph = gr;
		globalXOffset = 0;
		
		relXtoParent = new HashMap<DefaultGraphCell,Integer>();
		relXtoRoot = new HashMap<DefaultGraphCell, Integer>();
		relYpos = new HashMap<DefaultGraphCell,Integer>();
		
		xPos = new HashMap<DefaultGraphCell,Integer>();
		yPos = new HashMap<DefaultGraphCell,Integer>();
		
		nodesToShape = new HashMap<DefaultGraphCell, Shape>();
		
	}
	
	/**
	 * Initializes a new <code>SolvedFormLayout</code> with the
	 * given <code>JDomGraph</code>.
	 * 
	 * @param gr the solved form as <code>JDomGraph</code> to layout
	 */
	public GecodeTreeLayout(ImprovedJGraph gr, int offset) {
		graph = gr;
		globalXOffset = offset;
		
		relXtoParent = new HashMap<DefaultGraphCell,Integer>();
		relXtoRoot = new HashMap<DefaultGraphCell, Integer>();
		relYpos = new HashMap<DefaultGraphCell,Integer>();
		
		xPos = new HashMap<DefaultGraphCell,Integer>();
		yPos = new HashMap<DefaultGraphCell,Integer>();
		
		nodesToShape = new HashMap<DefaultGraphCell, Shape>();
		
	}
  
   
	/**
	 * Computes the root of a <code>JDomGraph</code>, assuming
	 * that it is a forest.
	 * (Otherwise it will return the first node without
	 *  incoming edges.)
	 * 
	 * @param theGraph, the <code>JDomGraph</code> to compute the root for
	 * @return null if there is no node, the root otherwise
	 */
	private DefaultGraphCell computeGraphRoot() {
		for( DefaultGraphCell node : (Set<DefaultGraphCell>) graph.getNodes() ) {
			if( graph.isRoot(node) ) {
				return node;
			}
		}
		System.err.println("no Root found!");
		return null;
	}
	
	
	/**
	 * 
	 * Computes the x- and y-positions of every node and
	 * stores it in the xPos resp. yPos map.
	 * 
	 */
	private void computeNodePositions() {
		
		// the root is the node to start DFS with.
		 DefaultGraphCell root = computeGraphRoot();
		 
		 // computing the x-positions, dependent on the _direct_
		 // parent
		 GraphLayoutCursor layCursor = new GraphLayoutCursor(root, this, graph);
	     PostOrderNodeVisitor postVisitor = new PostOrderNodeVisitor(layCursor);
	     postVisitor.run();
		 
		 // another DFS computes the y- and x-positions relativ to the
		 // _root_
		 GraphDrawingCursor drawCursor = new GraphDrawingCursor(root, this, graph);
		 PreOrderNodeVisitor preVisitor = new PreOrderNodeVisitor(drawCursor);
	     preVisitor.run();
	     
	     
	     // the whole tree shape (subtree with the graph root as root)
	     Shape box = nodesToShape.get(root);
	     
	     // the box containing the tree
	     BoundingBox bb = box.getBoundingBox();
	     
	     // the extrend to the left (starting from the root)
	     int extL = bb.left;
	 
	     // the offset to move the nodes in order to
	     // start at 0 and get positive x-coordinates.
	     int offset = 0 - extL;
	     
	     // computing the absolute coordinates for every node
	     // resp. their left upper corner.
	     for( DefaultGraphCell node : (Set<DefaultGraphCell>) graph.getNodes() ) {
	    	
	    	 /*
	    	  * The stored value represents the x-coordinate of the 
	    	  * middle axis relative to the graph root middle axis.
	    	  * So we move everithing to the right (to start at x=0)
	    	  * and additionally reduce by the half width of the node.
	    	  */
	    	 int x = relXtoRoot.get(node) + offset - graph.computeNodeWidth(node)/2;
	    	 xPos.put(node, x + globalXOffset);
	    	 
	    	 // the root y-position is zero, that's why the relative
	    	 // y-positions are equivalent to the absolute ones.
	    	 int y = relYpos.get(node);
	    	 yPos.put(node,y);
	     }
	     	
	}
	
	/**
	 * places a node at a given position and remembers
	 * the information in a given Attribute Map.
	 * @param node, the node to place
	 * @param x the x-value of the upper left corner
	 * @param y the y-value of the upper left corner
	 * @param viewMap hte viewMap to save the position in
	 */
	private void placeNodeAt(DefaultGraphCell node, int x, int y, 
			Map<DefaultGraphCell,AttributeMap> viewMap) {
		
		CellView view = graph.getGraphLayoutCache().getMapping(node, false);
		Rectangle2D rect = (Rectangle2D) view.getBounds().clone();
		Rectangle bounds =  new Rectangle((int) rect.getX(),
				(int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
		
		bounds.x = x;
		bounds.y = y;
		
		AttributeMap map = graph.getModel().createAttributes();
		GraphConstants.setBounds(map, (Rectangle2D) bounds.clone());
		
		viewMap.put(node, map);
	}
	
	/**
	 * places the nodes in the graph model.
	 * Not meaningful without having computed
	 * the fragment graph as well as the relative
	 * x- and y-positions.
	 */
	private void placeNodes() {
		//the view map to save all the node's positions.
		Map<DefaultGraphCell,AttributeMap> viewMap = new 
		HashMap<DefaultGraphCell,AttributeMap>();
		
		
		//place every node on its position
		//and remembering that in the viewMap.
		for(DefaultGraphCell node : (Set<DefaultGraphCell>) graph.getNodes() ) {
			int x = xPos.get(node).intValue();
			int y = yPos.get(node).intValue();
			
			placeNodeAt(node, x, y, viewMap);
		}
		
		
		//updating the graph.
		graph.getGraphLayoutCache().edit(viewMap, null, null, null);
	}
	
	
	
	/**
	 * Starts the layout algorithm.
	 */
	public void run(JGraph gr, Object[] cells, int arg2) {
		computeNodePositions();
		placeNodes();
	}

    public Integer getRelXtoParent(DefaultGraphCell node) {
        return relXtoParent.get(node);
    }

    
    public void addRelXtoParent(DefaultGraphCell node, Integer x) {
        relXtoParent.put(node,x);
    }

    public void addRelXtoRoot(DefaultGraphCell node, Integer x) {
        relXtoRoot.put(node,x);
    }

    public void addRelYpos(DefaultGraphCell node, Integer y) {
        relYpos.put(node,y);
    }

	/**
	 * Returns the node width computed by the <code>JDomGraph</code>.
     * 
	 * @param node the node to compute the width for
	 * @return the width
	 */
	public int getNodeWidth(DefaultGraphCell node) {
		return graph.computeNodeWidth(node);
	}
	
	public Shape getNodesToShape(DefaultGraphCell node) {
		return nodesToShape.get(node);
	}
    
    public void putNodeToShape(DefaultGraphCell node, Shape shape) {
        nodesToShape.put(node,shape);
    }
	
	
	
}
