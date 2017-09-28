/*
 * Created on 28.07.2004
 *
 */
package de.saar.chorus.ubench;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.util.JGraphUtilities;

import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.jgraph.GecodeTreeLayout;
import de.saar.chorus.jgraph.ImprovedJGraph;
import de.saar.chorus.ubench.gui.Preferences;

/**
 * A Swing component that represents a labelled dominance graph.
 * 
 * @see ImprovedJGraph
 * 
 * @author Alexander Koller
 * @author Michaela Regneri
 * 
 */
public class JDomGraph extends ImprovedJGraph<NodeType,NodeData,EdgeType,EdgeData> implements Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3205330183133471528L;

	// The fragments of the graph. This only makes sense after computeFragments
	// has been called.
	private Set<Fragment> fragments;
	
	// Dominance edges don't belong to any fragment, so we remember them separately.
	private Set<DefaultEdge> dominanceEdges;
	
	// Map nodes and solid edges to the fragments they belong to.
	private Map<DefaultGraphCell,Fragment> nodeToFragment;
	private Map<DefaultEdge,Fragment> edgeToFragment; 
	
	// The set of DomGraphPopupListeners that have been registered for this graph.
	private Set<DomGraphPopupListener> popupListeners;
	
	// If a popup menu is currently open, the cell the menu belongs to.
	private DefaultGraphCell activePopupCell;
	
	private Rectangle boundingBox;
	
	private DomGraph myDomGraph;
	
	private boolean hnc;
	
	
	private List<Set<DefaultGraphCell>> wccs;
	
	
	// This listener draws a popup menu when the right mouse button is ed.
	private class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}
		
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}
		
		private void maybeShowPopup(MouseEvent e) {
			if ((e != null) && e.isPopupTrigger()) {
				int x = e.getX(), y = e.getY();
				
				activePopupCell = findNodeOrEdgeAt(x,y);
				
				if( activePopupCell != null ) {
					JPopupMenu popup = new JPopupMenu();
					Fragment frag = findFragment(activePopupCell);
					
					if( activePopupCell instanceof DefaultEdge ) {
						// This instanceof test has to come first, because
						// DefaultEdge is a subclass of DefaultGraphCell.
						EdgeData data = (EdgeData) activePopupCell.getUserObject();
						JMenuItem item = data.getMenu();
						
						if( item != null )
							popup.add(item);	                        
					} else {
						NodeData data = (NodeData) activePopupCell.getUserObject();
						JMenuItem item = data.getMenu();
						
						if( item != null )
							popup.add(item);
					}  
					
					if( frag != null ) {
						JMenuItem item = frag.getMenu();
						
						if( item != null ) {
							popup.add(item);
						}
					}
					
					popup.show(e.getComponent(), e.getX(), e.getY());	                    
				}
			}
			
		}
	}
	
	/**
	 * Sets up an empty dominance graph.
     * 
	 * @param origin the <code>DomGraph</code> represented here.  
	 */
	public JDomGraph(DomGraph origin) {
		super();
		hnc = origin.isHypernormallyConnected();
	
		wccs = new ArrayList< Set<DefaultGraphCell>>();
		myDomGraph = origin;
		boundingBox = new Rectangle();
		
		fragments = new HashSet<Fragment>();
		nodeToFragment = new HashMap<DefaultGraphCell,Fragment>();
		dominanceEdges = new HashSet<DefaultEdge>();
		edgeToFragment = new HashMap<DefaultEdge,Fragment>();
		// set up popup handling
		popupListeners = new HashSet<DomGraphPopupListener>();
		addMouseListener(new PopupListener());		
		
		clear();
		
		// set up tooltip handling
		ToolTipManager.sharedInstance().registerComponent(this);
	}
	
	
	/**
	 * Remove all nodes and edges in the graph.
	 */
	public void clear() {
        super.clear();
		
		clearFragments();
	}
	
	/**
	 * Remove all information about fragments. This includes deleting
	 * the fragment cells from the graph model.
	 */
	private void clearFragments() {
		for( Fragment frag : fragments ) {
			getModel().remove(new Object[] { frag.getGroupObject() } );
		}
		
		fragments.clear();
		nodeToFragment.clear();
		dominanceEdges.clear();
		edgeToFragment.clear();
	}
	
	
	
	
	/**
	 * Compute JGraph attributes for a node of the given type.
	 * (So far, the type is ignored.)
	 * 
	 * @param type the node type.
	 * @return an AttributeMap object with reasonable style information.
	 */
	protected AttributeMap defaultNodeAttributes(NodeType type) {
		GraphModel model = getModel();
		AttributeMap map = model.createAttributes();
		
		if( type.equals(NodeType.labelled) ) {
			// labelled nodes
			GraphConstants.setBounds(map, map.createRect(0, 0, 30, 30));
			
			GraphConstants.setBackground(map, Color.white);
			GraphConstants.setForeground(map, Color.black);
			GraphConstants.setFont(map, nodeFont);
			GraphConstants.setOpaque(map, true);
		} else {
			//holes
			//TODO how to draw smaller rectangles??
			GraphConstants.setBounds(map, map.createRect(0, 0, 15, 15));
			//GraphConstants.setBorder(map, BorderFactory.createLineBorder(Color.black));
			GraphConstants.setSize(map, new Dimension(15,15));
			GraphConstants.setBackground(map, Color.white);
			GraphConstants.setForeground(map, Color.black);
			GraphConstants.setFont(map, nodeFont);
			GraphConstants.setOpaque(map, true);
		}
		return map;
	}
	
	/**
	 * Compute JGraph attributes for a node of the given type.
	 * 
	 * @param type the edge type.
	 * @return an AttributeMap object with reasonable style information.
	 */
	protected AttributeMap defaultEdgeAttributes(EdgeType type) { 
		if (type.getType() == EdgeType.solidVal) {
			AttributeMap solidEdge = getModel().createAttributes();
			GraphConstants.setLineEnd(solidEdge, GraphConstants.ARROW_NONE);
			GraphConstants.setEndSize(solidEdge, 10);
			GraphConstants.setLineWidth(solidEdge, 1.7f);
			return solidEdge;
		} else {
			AttributeMap domEdge = getModel().createAttributes();
			GraphConstants.setLineEnd(domEdge, GraphConstants.ARROW_NONE);
			GraphConstants.setEndSize(domEdge, 10);
			GraphConstants.setLineColor(domEdge, Color.RED);
			GraphConstants.setLineWidth(domEdge, 1.2f);
			GraphConstants.setDashPattern(domEdge, new float[] { 3, 3 });
			return domEdge;
		}
		
	}
	
	
	
	/**
	 * Add some sample nodes and edges to the graph. 
	 */
	public void addSampleData() {
		// nodes
		DefaultGraphCell nodeX = addNode("X", new NodeData(NodeType.labelled, "X", "f", this));
		DefaultGraphCell nodeX1 = addNode("X1", new NodeData(NodeType.unlabelled, "X1", this));
		DefaultGraphCell nodeY = addNode("Y", new NodeData(NodeType.labelled, "Y", "b", this));
		
		// edges
		addEdge(new EdgeData(EdgeType.solid, "x-x1", this), nodeX, nodeX1);
		addEdge(new EdgeData(EdgeType.dominance, "x1-y", this), nodeX1, nodeY);
	}
	
	/**
	 * Once the fragments have been computed, add some sample menu items to them.
	 */
	public void addSampleFragmentMenus() {
		for( Fragment frag : fragments ) {
			frag.addMenuItem("fFoo", "F Foo Foo");
			frag.addMenuItem("fBar", "F Bar Bar");
			frag.addMenuItem("fBaz", "F Baz Baz");
		}
	}
	
	/**
	 * prints position and dimensions of all 
	 * fragments and their node on screen.
	 *
	 */
	public void printPositions() {
		int frCount = 1;
		
		//putting height and widht of fragments and nodes on screen,
		//just for comparing.
		
		for(Fragment frag : fragments)
		{
			DefaultGraphCell recCell = frag.getGroupObject();
			Rectangle2D karton = GraphConstants.getBounds(recCell.getAttributes());
			System.out.print("Fragment No. " + frCount + " " + recCell.toString());
			System.out.println(" is at " + karton);
			frCount++;
			
			for( DefaultGraphCell recNode : frag.getNodes()) {
				Rectangle2D nodeRect = GraphConstants.getBounds(recNode.getAttributes());
				System.out.print("  Node " + getNodeData(recNode).getName() + "(" + recNode + ")");
				System.out.println(" is at " + nodeRect);
			}
			
			System.out.println("");
		}
		
		System.out.println("");
	}
	
	/**
	 * Apply a layout algorithm to this graph. Before the first call of
	 * this method, the graph won't be display properly because all nodes
	 * will be in the same place.
	 */
	public void computeLayout() {

		if(isForest() && (wccs().size() == 1) ) {
			JGraphUtilities.applyLayout(this, new GecodeTreeLayout(this));
		}  else {
			JGraphUtilities.applyLayout(this, new DomGraphLayout(this));
		}
	}
	

	/**
	 * Group the nodes of the graph into the maximal fragments.
	 * A fragment is a set of nodes that are connected by solid edges.
	 * Maximal fragments are maximal elements with respect to node set inclusion.
	 * This method adds new cells to the graph, one for each fragment,
	 * and adds the nodes and solid edges that belong to the fragment to 
	 * this cell (as a group).
	 */
	public void computeFragments() {
		clearFragments();
		
		// initially, put each nodes into a fragment of its own.
		for( DefaultGraphCell node : nodes ) {
			Fragment f = new Fragment(this);
			f.add(node);
			fragments.add(f);
			nodeToFragment.put(node, f);
		}
		
		// Now iterate over the edges. If two nodes are connected by a solid edge,
		// merge their fragments.
		
		for( DefaultEdge edge : getEdges() ) {
			
			if( getEdgeData(edge).getType() == EdgeType.solid ) {
				// merge fragments
				DefaultGraphCell src = (DefaultGraphCell) JGraphUtilities.getSourceVertex(this, edge);
				DefaultGraphCell tgt = (DefaultGraphCell) JGraphUtilities.getTargetVertex(this, edge);
				
				
				
				Fragment sFrag = nodeToFragment.get(src);
				Fragment tFrag = nodeToFragment.get(tgt);
				
				if( sFrag.size() > tFrag.size() ) {
					mergeInto( sFrag, tFrag );
					
					sFrag.add(edge);
					edgeToFragment.put(edge, sFrag);
				} else {
					mergeInto( tFrag, sFrag );
					
					tFrag.add(edge);
					edgeToFragment.put(edge, tFrag);
				}
			} else {
				dominanceEdges.add(edge);
			}
		}
		
		
		// insert fragment cells into the graph.
		for( Fragment frag : fragments ) {
			
			getModel().insert( new Object[] { frag.getGroupObject() },
					null, null, null, null );
		}
		
		computeAdjacency();
		wccs = wccs();
		removeRedundandEdges();
	}
	
	
	
	
	/**
	 * Merge one fragment into another. This means that all nodes of the "from"
	 * fragment become nodes of the "into" fragment, and the "from" fragment
	 * is deleted.
	 * 
	 * @param into
	 * @param from
	 */
	private void mergeInto(Fragment into, Fragment from ) {
		into.addAll(from);
		
		for( DefaultGraphCell node : from.getNodes() ) {
			nodeToFragment.put(node, into);
		}
		
		for( DefaultEdge edge : from.getEdges() ) {
			edgeToFragment.put(edge, into);
		}
		
		fragments.remove(from);
	}
	
	/**
	 * Checks for redundand dominance edges and removes them.
	 */
	private void removeRedundandEdges() {
		
		// the found redundand edges are stored here.
		Set<DefaultEdge> redundandEdges = new HashSet<DefaultEdge>();
		
		// iterating over all dominance edges
		for ( DefaultEdge edge : dominanceEdges ) {
			
			// determining source and target of the edge
			DefaultGraphCell src =  (DefaultGraphCell)
						JGraphUtilities.getSourceVertex(this, edge);
			DefaultGraphCell tgt =   (DefaultGraphCell)
						JGraphUtilities.getTargetVertex(this, edge);
			
			// checking if there is another path from the source
			// to the target
			if (children.containsKey(src)) {
				
				// iterating over the children of the source vertex
				for( DefaultGraphCell child : children.get(src) ) {
					
					// if there is another path to the target vertex...
					if( cellDFS(child, tgt) ) {
						
						// mark the edge as redundand
						redundandEdges.add(edge);
						
						// remove the direct path from the adjacency
						// matrix
						children.get(src).remove(tgt);
						parents.get(tgt).remove(src);
						
						// and go on with the next edge
						break;
					}
				}
			}
			
		}
		
		// removing the redundand edges
		for(DefaultEdge edge : redundandEdges) {
			
			dominanceEdges.remove(edge);
			edges.remove(edge);
			getModel().remove(new Object[]{ edge });
			
		}
	}
	
	/**
	 * Performs DFS through all nodes and checks if the
	 * second given vertex ist a descendant of the first one.
	 * Helper method to determin redundand edges.
	 * 
	 * @param recent the vertex to start with
	 * @param find the potential descendant
	 * @return true if find is a descendant of recent
	 */
	private boolean cellDFS(DefaultGraphCell recent, DefaultGraphCell find) {
		if(children.containsKey(recent)) {
			for( DefaultGraphCell child : children.get(recent) ) {
				if(child.equals(find)) {
					return true;
				} else {
					if( cellDFS(child, find) ) {
						return true;
					} else continue;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * @return Returns the dominanceEdges.
	 */
	public Set<DefaultEdge> getDominanceEdges() {
		return dominanceEdges;
	}
	
	/**
	 * Get the set of all fragments.
	 * 
	 * @return the fragments.
	 */
	public Set<Fragment> getFragments() {
		return fragments;
	}
	
	
	
	//// popup handling methods
	
	/**
	 * Add a popup listener. This listener will be called every time
	 * the user selects an item from a (node or fragment) popup menu.
	 * 
	 * @param listener
	 */
	public void addPopupListener(DomGraphPopupListener listener) {
		popupListeners.add(listener);
	}
	
	/**
	 * Remove a popup listener.
	 * 
	 * @param listener
	 */
	public void removePopupListener(DomGraphPopupListener listener) {
		popupListeners.remove(listener);
	}
	
	/**
	 * Notify all registered popup listeners that the user selected
	 * a popup menu item. This means that the <code>popupSelected</code>
	 * methods of these listener objects are called.
	 * 
	 * @param menuItemLabel the label of the selected menu item.
	 */
	void notifyPopupListeners(String menuItemLabel) {
		for( DomGraphPopupListener listener : popupListeners ) {
			listener.popupSelected(activePopupCell, 
					findFragment(activePopupCell), 
					menuItemLabel);
		}
		
		activePopupCell = null;
	}
	
	
	
	
	//// tooltips
	
	

	/**
	 * Return the fragment a node or edge belongs to. This method
	 * is only meaningful after fragments have been computed.
	 
	 * @param cell a node or edge in the graph
	 * @return the fragment it belongs to; null if it doesn't belong to any fragment (e.g. a dominance edge)
	 */
	public Fragment findFragment(DefaultGraphCell cell) {
		if( nodeToFragment.containsKey(cell)) {
			return nodeToFragment.get(cell);
		} else if( edgeToFragment.containsKey(cell)) {
			return edgeToFragment.get(cell);
		} else
			return null;
	}
	
	
	

	/**
	 * Returns the fragment the source node of an edge
	 * belongs to.
	 * 
	 * @param edge the edge
	 * @return the fragment of the edge's source node
	 */
	public Fragment getSourceFragment(DefaultEdge edge) {
		return findFragment(getSourceNode(edge));
	}
	
	/**
	 * Returns the fragment the target node of an edge
	 * belongs to.
	 * 
	 * @param edge the edge
	 * @return the fragment of the edge's target node
	 */
	public Fragment getTargetFragment(DefaultEdge edge) {
		return findFragment(getTargetNode(edge));
	}
	
	

	/**
	 *  Return all dominance edges of the graph in a sorted
	 * <code> List </code>.
	 * 
	 * @return the edges sorted by their order of inserting
	 */
	List<DefaultEdge> getSortedDomEdges() {
		List<DefaultEdge> sortedEdges = new ArrayList<DefaultEdge>();
		sortedEdges.addAll(dominanceEdges);
		Collections.sort(sortedEdges, new EdgeSortingComparator());
		
		return sortedEdges;
	}
	
	
	
	/**
	 * @return Returns the boundingBox.
	 */
	public Rectangle getBoundingBox() {
		return boundingBox;
	}
	
	/**
	 * @param boundingBox The boundingBox to set.
	 */
	public void setBoundingBox(Rectangle boundingBox) {
		this.boundingBox = boundingBox;
	}
	
	

	/**
	 * Clones this graph.
	 * @return the clone
	 */
	public JDomGraph clone() {
		
		// setting up a new graph
		JDomGraph clone = new JDomGraph((DomGraph) myDomGraph.clone());
		
		// copy the nodes by creating new (equivalent)
		// node data
		for(DefaultGraphCell cell : nodes ) {
			NodeData cellData = getNodeData(cell);
			NodeData cloneData;
			if( cellData.getType().equals(NodeType.labelled)) {
				cloneData = new NodeData(NodeType.labelled, cellData.getName(), cellData.getLabel(), clone); 
			} else {
				cloneData = new NodeData(NodeType.unlabelled, cellData.getName(), clone); 
			}
			cloneData.addMenuItem(cellData.getMenuLabel(), cellData.getName());
			clone.addNode(cellData.getName(), cloneData);
		}
		
		// copy the edges by creating new (equivalent)
		// edge data
		for (DefaultEdge edge : getSortedEdges() ) {
			
			EdgeData cellData = getEdgeData(edge);
			EdgeData cloneData;
			
			if( cellData.getType().equals(EdgeType.solid)) {
				cloneData = new EdgeData(EdgeType.solid, cellData.getName(), clone );
			} else {
				cloneData = new EdgeData(EdgeType.dominance, cellData.getName(), clone );
			}
			
			cloneData.addMenuItem(cellData.getMenuLabel(), cellData.getName());
			clone.addEdge(cloneData, clone.getNodeForName(getNodeData(getSourceNode(edge)).getName()), 
					clone.getNodeForName(getNodeData(getTargetNode(edge)).getName()));
		}
		
		// setting the scale
		clone.setScale(getScale());
		clone.setShowLabels(Preferences.getInstance().isShowLabels());
		return clone;
	}
	
	/**
	 * Determines whether a given node is a leaf
	 * in its fragment.
	 * 
	 * @param node the node
	 * @return true if there are no outgoing solid edges
	 */
	public boolean isFragLeaf(DefaultGraphCell node) {
		return findFragment(node).isLeaf(node);
	}
	
	/**
	 * Determines wheter a given node is the root
	 * of its fragment.
	 * @param node the node
	 * @return true if there are no incoming solid edges
	 */
	public boolean isFragRoot(DefaultGraphCell node) {
		return findFragment(node).getParent(node) == null;
	}
	
	
    /**
     * Changing the preferences for displaying node labels
     * @param b indicating whether or not to show labels (insted
     * 			of names, default is true)
     */
    public void setShowLabels(boolean b) {
        for( DefaultGraphCell node : nodes ) {
            getNodeData(node).setShowLabel(b);
        }
    }
    
   
    /**
     * Removes all dominance edges from this graph.
     *
     */
    public void clearDominanceEdges() {
    	for(DefaultEdge edge : dominanceEdges) {
			getModel().remove(new Object[]{ edge });
			edges.remove(edge);
			edgeCounter--;
		}
    	dominanceEdges.clear();
    }
    
    /**
     * Computes the Fragments contained in a wcc.
     * In principle, this works for an abitrary Set of
     * nodes (not just for wccs); it will return a Set of
     * all fragments of which at least one node was
     * in the node set.
     * 
     * @param wcc a <Code>Set</Code> of nodes 
     * @return the <Code>Set</Code> of fragments represented by the argument node set
     */
    public Set<Fragment> getWccFragments(Set<DefaultGraphCell> wcc) {
    	
    	Set<Fragment> frags = new HashSet<Fragment>();
    	for(DefaultGraphCell node : wcc) {
    		frags.add(findFragment(node));
    	}
    	return frags;
    	
    }

    /**
     * 
     * @return true if this graph is hypernormally connected
     */
	public boolean isHnc() {
		return hnc;
	}

	/**
	 * 
	 * @return the node sets representing this graph's weakly connected components
	 */
	public List<Set<DefaultGraphCell>> getWccs() {
		return wccs;
	}
	
	
}
