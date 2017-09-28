/*
 * @(#)ImprovedJGraph.java created 20.09.2005
 * 
 * Copyright (c) 2005 Alexander Koller
 *  
 */

/*
 * TODO: Perhaps move the popup management here?
 */

package de.saar.chorus.jgraph;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.util.JGraphUtilities;

abstract public class ImprovedJGraph<NodeType,
                                     NodeData extends INodeData<NodeType>,
                                     EdgeType,
                                     EdgeData extends IEdgeData<EdgeType>> 
                        extends JGraph {
    // the nodes and edges of the graph
    protected Set<DefaultGraphCell> nodes;
    protected Set<DefaultEdge> edges;
    
    // map node names to nodes.
    protected Map<String,DefaultGraphCell> nameToNode;

    //adjacency lists for the graph
    protected Map<DefaultGraphCell,List<DefaultGraphCell>> parents;
    protected Map<DefaultGraphCell,List<DefaultGraphCell>> children;

    // A name or ID for the graph (displayed in window title, id attribute in graph element)
    protected String name;
    
    // the font in which node labels are displayed
    protected Font nodeFont;
    
    // maps the edges to their order of inserting
    protected Map<DefaultEdge,Integer> edgeOrder;
    
    // the current number of edges (to indicate their later order)
    protected int edgeCounter;

    public ImprovedJGraph() {
        super();

        nodes = new HashSet<DefaultGraphCell>();
        edges = new HashSet<DefaultEdge>();
        nameToNode = new HashMap<String,DefaultGraphCell>();
        
        parents = new HashMap<DefaultGraphCell, List<DefaultGraphCell>>();
        children = new HashMap<DefaultGraphCell, List<DefaultGraphCell>>();
        
        edgeOrder = new HashMap<DefaultEdge, Integer>();
        edgeCounter = 0;
        
        nodeFont = GraphConstants.DEFAULTFONT.deriveFont(Font.PLAIN, 17);
        //nodeFont = new Font("Arial Unicode MS", Font.PLAIN, 17);
        
        getModel().remove(JGraphUtilities.getAll(this));
        nodes.clear();
        edges.clear();
        nameToNode.clear();
        name = null;

        // set up tooltip handling
        ToolTipManager.sharedInstance().registerComponent(this);
    }
    

    /**
     * Remove all nodes and edges in the graph.
     */
    public void clear() {
        getModel().remove(JGraphUtilities.getAll(this));
        nodes.clear();
        edges.clear();
        nameToNode.clear();
        name = null;
    }
    
    /**
     * Adds a new node to the graph (and the underlying model). The attributes
     * of the new node are computed automatically from the node data.
     * 
     * @param data the data for the new node.
     * @return a new DefaultGraphCell object in this graph.
     */
    public DefaultGraphCell addNode(String name, NodeData data) {
        DefaultGraphCell ret = new DefaultGraphCell(data);
        GraphModel model = getModel();
        
        AttributeMap style = defaultNodeAttributes(data.getType());
        Map attributes = new HashMap();
        attributes.put(ret, style);
        
        DefaultPort port = new DefaultPort();
        ret.add(port);
        
        model.insert(new Object[] { ret, port }, attributes, new ConnectionSet(), null, null);
        
        nodes.add(ret);
        nameToNode.put(name, ret);
        
        return ret;
    }
    
    abstract protected AttributeMap defaultNodeAttributes(NodeType type);
    
    

    /**
     * Adds a new edge to the graph (and the underlying model). The edge goes from the
     * 0-th port of the node src to the 0-th port of the node tgt. The style attributes
     * of the new edge are computed automatically from the edge data.
     * 
     * @param data the data for the new edge.
     * @param src the node cell at which the edge should start.
     * @param tgt the node cell at which the edge should end.
     * @return a new DefaultEdge object in this graph.
     */
    public DefaultEdge addEdge(EdgeData data, DefaultGraphCell src, DefaultGraphCell tgt) {
        
        DefaultEdge ret = new DefaultEdge(data);
        
        edgeCounter++;
        edgeOrder.put(ret, edgeCounter);
        
        GraphModel model = getModel();
        
        AttributeMap style = defaultEdgeAttributes(data.getType());
        Map attributes = new HashMap();
        attributes.put(ret, style);
        
        ConnectionSet cs = new ConnectionSet();
        cs.connect(ret, src.getChildAt(0), tgt.getChildAt(0));
        model.insert(new Object[] { ret }, attributes, cs, null, null );
        
        edges.add(ret);
        
        return ret;
    }
    
    abstract protected AttributeMap defaultEdgeAttributes(EdgeType type);
    
    

    
    
    /**
     * Get all nodes of the graph.
     * 
     * @return the set of all nodes.
     */
    public Set<DefaultGraphCell> getNodes() {
        return nodes;
    }
    
    /**
     * Get all edges of the graph.
     * 
     * @return the set of all edges.
     */
    public Set<DefaultEdge> getEdges() {
        return edges;
    }
    
    
    
    
    /**
     * Computes the width of a string in pixels, given the
     * font rendering parameters of the root pane that contains
     * the graph. If the graph hasn't been added to a Swing
     * structure in such a way that it _has_ a well-defined
     * root pane, or if the frame the graph belongs to is
     * invisible, the method returns a default value of 30 pixels.
     * 
     * @param str the string whose width we want to know
     * @return the width of str in pixels, or 30 if this can't be determined.
     */
    private int computeTextWidth(String str) {
        JRootPane pane = SwingUtilities.getRootPane(this);
        int ret = 30; // a default node width
        
        if( (pane != null)  && !"".equals(str) ) {
            Graphics2D g2 = (Graphics2D) pane.getGraphics();
            
            TextLayout tl = new TextLayout(str, nodeFont, g2.getFontRenderContext());
            int trueWidth = (int) (1.2*tl.getBounds().getWidth() + 10);
            if( trueWidth > ret ) {
                ret = trueWidth;
            }
        }
        
        return ret;
    }
    
    /**
     * Computes the height of a string in pixels, given the
     * font rendering parameters of the root pane that contains
     * the graph. If the graph hasn't been added to a Swing
     * structure in such a way that it _has_ a well-defined
     * root pane, or if the frame the graph belongs to is
     * invisible, the method returns a default value of 30 pixels.
     * 
     * @param str the string whose height we want to know
     * @return the height of str in pixels, or 30 if this can't be determined.
     */
    private int computeTextHeight(String str) {
        JRootPane pane = SwingUtilities.getRootPane(this);
        int ret = 30; // a default node height
        
        if( (pane != null) && isVisible() && !"".equals(str) ) {
            Graphics2D g2 = (Graphics2D) pane.getGraphics();
            TextLayout tl = new TextLayout(str, nodeFont, g2.getFontRenderContext());
            int trueWidth = (int) (1.2*tl.getBounds().getHeight() + 10);
            
            if( trueWidth > ret ) {
                ret = trueWidth;
            }
        }
        
        return ret;
    }
    
    /**
     * Computes the width of the label of a node. This is
     * based on <code>computeTextWidth</code>, and is subject to the same
     * caveats.
     * 
     * @see <code>computeTextWidth</code>
     * @param node the node whose width we want to know
     * @return the width of node in pixels, or 30 if this can't be determined.
     */
    public int computeNodeWidth(DefaultGraphCell node) {
        return computeTextWidth(getNodeData(node).toString());
    }
    
    /**
     * Computes the height of the label of a node. This is
     * based on <code>computeTextHeight</code>, and is subject to the same
     * caveats.
     * 
     * @see <code>computeTextHeight</code>
     * @param node the node whose height we want to know
     * @return the height of node in pixels, or 30 if this can't be determined.
     */
    public int computeNodeHeight(DefaultGraphCell node) {
        return computeTextHeight(getNodeData(node).toString());
    }
    
    /**
     * Compute the true widths of the node labels in the graph,
     * change the node widths such that all the labels fit in, and
     * recompute the layout.
     * 
     * A typical sequence of method calls is as follows:
     * 1. graph.computeLayout(): This computes an initial layout.
     * 2. display the JFrame; this determines the window size based
     *    on the results of the preliminary layout computation
     * 3. graph.adjustNodeWidths(): This corrects the node sizes
     *    and graph layout.
     */
    public void adjustNodeWidths() {
        Map<DefaultGraphCell,AttributeMap> viewMap = new HashMap<DefaultGraphCell,AttributeMap>();
        
        for( DefaultGraphCell node : nodes ) {
            AttributeMap map = getModel().createAttributes();
            
            GraphConstants.setBounds(map,
                    map.createRect(0,0,computeNodeWidth(node), 30));
            viewMap.put(node, map);
        }
        
        getGraphLayoutCache().edit(viewMap, null, null, null);
        computeLayout();
    }

    abstract public void computeLayout();
    

    /**
     * Get the node data of a node cell.
     * 
     * @param node
     * @return the node data.
     */
    public NodeData getNodeData(DefaultGraphCell node) {
        return (NodeData) node.getUserObject();
    }
    
    /**
     * Get the edge data of an edge cell.
     * 
     * @param edge
     * @return the edge data.
     */
    public EdgeData getEdgeData(DefaultEdge edge) {
        return (EdgeData) edge.getUserObject();
    }
    
    /**
     * Look up the node with the specified name.
     * 
     * @param name
     * @return that node.
     */
    public DefaultGraphCell getNodeForName(String name) {
        return nameToNode.get(name);
    }
    
    /**
     *  Overrides the <code>getToolTipText</code> method of 
     *  <code>JComponent</code>.
     */
    public String getToolTipText(MouseEvent e) {
        if(e != null) {
            // Fetch Cell under Mousepointer
            DefaultGraphCell c = findNodeOrEdgeAt(e.getX(), e.getY());
            if (c != null) {
                if( !(c instanceof DefaultEdge) ) {
                    NodeData data = getNodeData(c);
                    return data.getToolTipText();
                }
            }
        } 
        
        return null;
    }   
    
    
    /**
     * Return the node at the mouse position (x,y). 
     * 
     * @param x
     * @param y
     * @return reference to the node cell; null if there is no node at the position.
     */
    public DefaultGraphCell findNodeOrEdgeAt(int x, int y) {
        Set<Object> cells = new HashSet<Object>();    
        Object cell = getFirstCellForLocation(x,y);
        
        while( (cell != null) && !cells.contains(cell) ) {
            cells.add(cell);
            
            if( nodes.contains(cell) ) {
                return (DefaultGraphCell) cell;
            } else if( edges.contains(cell) ) {
                return (DefaultGraphCell) cell;
            }
            
            cell = getNextCellForLocation(cell, x, y);            
        }
        
        return null;
    }
    
    
    public boolean isRoot(DefaultGraphCell node) {
        return getParents(node).isEmpty();
    }
    
    public boolean isRelativeRoot(DefaultGraphCell node, Collection<DefaultGraphCell> subgraph) {
        Collection<DefaultGraphCell> parents = new HashSet<DefaultGraphCell>();
        
        parents.addAll(getParents(node));
        parents.retainAll(subgraph);
        return parents.isEmpty();
    }
    
    public boolean isLeaf(DefaultGraphCell node) {
        return getChildren(node).isEmpty();
    }
    
    public boolean isRelativeLeaf(DefaultGraphCell node, Collection<DefaultGraphCell> subgraph) {
        Collection<DefaultGraphCell> children = new HashSet<DefaultGraphCell>();
        
        children.addAll(getChildren(node));
        children.retainAll(subgraph);
        
        return children.isEmpty();
    }

    
    /**
     * Go through all the edges of the graph and record 
     * the adjacency matrix
     * in the "parents" and "children" maps.
     * 
     */
    public void computeAdjacency() {
    	
        children.clear();
        parents.clear();
        
        // make sure that every node is assigned a non-null value in the
        // adjacency structures
        for( DefaultGraphCell node : nodes ) {
            children.put(node, new ArrayList<DefaultGraphCell>());
            parents.put(node, new ArrayList<DefaultGraphCell>());
        }
        
        /*
         * The edges returned by the graph are sorted from
         * the left to the right, so the nodes they return as
         * source and target should be sorted from the left to
         * the right, too.
         */
        
        //iterating through the list of sorted edges
        for(DefaultEdge edge : getSortedEdges()) {
            DefaultGraphCell source = 
                (DefaultGraphCell) JGraphUtilities.getSourceVertex(this, edge);
            
            DefaultGraphCell target = 
                (DefaultGraphCell) JGraphUtilities.getTargetVertex(this, edge);
           
            children.get(source).add(target);
            parents.get(target).add(source);
        }
    }
    

    /**
     * Generic method that handles maps from an Object to 
     * a list of objects and ads a new entry to the value list with
     * the specified object key. If the map does not contain the
     * key yet, it is added.
     * @param <E> the key type
     * @param <T> the type of the list elements
     * @param map the map
     * @param key the key to which list the new value shall be added
     * @param nVal the new value
     */
    public static <E,T> void addToMapList(Map<E,List<T>> map, E key, T nVal) {
        List<T> typedList;
        if(map.containsKey(key)) {
            typedList = map.get(key);
        } else {
            typedList = new ArrayList<T>();
            map.put(key,typedList);
        }
        typedList.add(nVal);
    }
    

    /**
     * Return all edges of the graph in a sorted
     * <code> List </code>.
     * 
     * @return the edges sorted by their order of inserting
     */
    public List<DefaultEdge> getSortedEdges(){
        List<DefaultEdge> sortedEdges = new ArrayList<DefaultEdge>();
        sortedEdges.addAll(edges);
        Collections.sort(sortedEdges, new EdgeSortingComparator());
        
        return sortedEdges;
    }


    
    /**
     * Set the name (= ID) of the dominance graph. This name could e.g. be displayed in the
     * window title. 
     * 
     * @param name new name of the graph.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get the name (= ID) of the  graph.
     * 
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Compute the incoming edges for a node. 
     * The returned list lists the complete edges (with types).
     * The list is sorted before returning by the <code> EdgeSortingComparator </code>
     *
     * @param node the node whose in-edges we want
     * @return the list of its in-edges.
     */
    public List<DefaultEdge> getInEdges(DefaultGraphCell node) {
        List<DefaultEdge> ret = new ArrayList<DefaultEdge>();
        
        for( DefaultEdge edge : edges ) {
            if( getTargetNode(edge) == node )
                ret.add(edge);
        }
        
        Collections.sort(ret, new EdgeSortingComparator());
        
        return ret;
    }
    
    
    /**
     * Returns the source node of an edge. 
     * @param edge the edge
     * @return the edge's source node
     */
    public DefaultGraphCell getSourceNode(DefaultEdge edge) {
        return (DefaultGraphCell) JGraphUtilities.getSourceVertex(this, edge);
    }
    
    
    /**
     * Returns the target node of and edge.
     * @param edge the edge
     * @return the edge's target node
     */
    public DefaultGraphCell getTargetNode(DefaultEdge edge) {
        return (DefaultGraphCell) JGraphUtilities.getTargetVertex(this, edge);
    }
    

    /**
     * Compute the outgoing edges for a node. 
     * The returned list lists the complete edges (with types).
     * The list is sorted before returning by the <code> EdgeSortingComparator </code>
     * 
     * @param node the node to compute the outgoing edges for
     * @return the sorted list of out-edges
     */
    public List<DefaultEdge> getOutEdges(DefaultGraphCell node) {
        List<DefaultEdge> ret = new ArrayList<DefaultEdge>();
        
        for( DefaultEdge edge : edges ) {
            if( getSourceNode(edge) == node )
                ret.add(edge);
        }

        Collections.sort(ret, new EdgeSortingComparator());

        return ret;
    }
    

    /**
     * Determines whether or not this graph is a forest.
     * TODO Consider possible cycles here!!
     * @return true if this graph is a forest.
     */
    public boolean isForest() {
        for( DefaultGraphCell node : nodes ) {
            if( parents.get(node).size() > 1 ) {
                return false;
            }
        }
        
        return true;
    }
    

    /**
     * @return Returns the children.
     */
    public List<DefaultGraphCell> getChildren(DefaultGraphCell node) {
        return children.get(node);
    }
    
    

    /**
     * @return Returns the parents.
     */
    public List<DefaultGraphCell> getParents(DefaultGraphCell node) {
        return parents.get(node);
    }
    

    /**
     * Returns the right sibling of a given node (if there is one).
     * If the node has more than one parent, then the method returns null.
     * 
     * @param node the node (the left sibling)
     * @return the right sibling, or null if there is none
     */
    public DefaultGraphCell getRightSibling(DefaultGraphCell node) {
        List<DefaultGraphCell> myparents = getParents(node);
        
        if( myparents == null )
            return null;
        
        if( myparents.size() != 1 ) {
            return null;
        }
        
        List<DefaultEdge> outEdges = getOutEdges(myparents.get(0));
        boolean foundMyself = false;
        
        for( DefaultEdge edge : outEdges ) {
            DefaultGraphCell tgt = getTargetNode(edge);
            if( foundMyself )
                return tgt;
            else if( tgt == node ) 
                foundMyself = true;
        }
        
        return null;
    }
    

    // This comparator helps sorting edges by their order of inserting
    public class EdgeSortingComparator implements Comparator<DefaultEdge> {
        public int compare(DefaultEdge x, DefaultEdge y) {
            
            //returns the comparision result of the two integers
            //the edges are mapped to.
            return edgeOrder.get(x).compareTo(edgeOrder.get(y));
        }
    }
    
    protected List<Set<DefaultGraphCell>> wccs() {
    	Set<DefaultGraphCell> visited = new HashSet<DefaultGraphCell>();
    	List<Set<DefaultGraphCell>> wccs = new ArrayList<Set<DefaultGraphCell>>();
    	
    	
    	for(DefaultGraphCell node : nodes ) {
    		if(! visited.contains(node)) {
    			Set<DefaultGraphCell> lastWCC = new HashSet<DefaultGraphCell>();
    			wccDFS(visited,node,lastWCC);
    			wccs.add(lastWCC);
    		}
    	}
    	
    	return wccs;
    }
    
    private void wccDFS(Set<DefaultGraphCell> visited, 
    					DefaultGraphCell recentNode,
    					Set<DefaultGraphCell> recentWCC ) {
    	
    	if(! visited.contains(recentNode)) {
    		visited.add(recentNode);
    		recentWCC.add(recentNode);
    		for( DefaultGraphCell par : parents.get(recentNode) ) {
    			wccDFS(visited, par, recentWCC);
    		} 
    		for( DefaultGraphCell child : children.get(recentNode) ) {
    			wccDFS(visited,child,recentWCC);
    		}
    	}
    }
}
