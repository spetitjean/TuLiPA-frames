/*
 * Created on 07.02.2005
 * 
 */
package de.saar.chorus.ubench;

import static de.saar.chorus.ubench.DomGraphLayoutParameters.fragmentXDistance;
import static de.saar.chorus.ubench.DomGraphLayoutParameters.fragmentYDistance;
import static de.saar.chorus.ubench.DomGraphLayoutParameters.nodeYDistance;
import static de.saar.chorus.ubench.DomGraphLayoutParameters.towerXDistance;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.util.JGraphUtilities;

import de.saar.chorus.jgraph.GraphDrawingCursor;
import de.saar.chorus.jgraph.GraphLayoutCursor;
import de.saar.chorus.jgraph.ImprovedJGraphLayout;
import de.saar.chorus.treelayout.BoundingBox;
import de.saar.chorus.treelayout.PostOrderNodeVisitor;
import de.saar.chorus.treelayout.PreOrderNodeVisitor;
import de.saar.chorus.treelayout.Shape;



/**
 * A layout algorithm for a dominance graph represented by 
 * a JDomGraph. 
 * 
 * @author Alexander Koller
 * @author Michaela Regneri
 */
public class DomGraphLayout extends ImprovedJGraphLayout {
	
	// the dominance graph
	private JDomGraph graph; 
	
	// the fragments
	private Set<Fragment> fragments;
	
	// the fragment positions 
	private Map<Fragment,Integer> fragXpos;
	private Map<Fragment,Integer> fragYpos;
	
	private Map<Fragment,Integer> fragWidth;
	private Map<Fragment,Integer> fragHeight;
	
	//the x-offset of the nodes of a fragment
	private Map<Fragment,Integer> fragOffset;
	
	// dominance edges that we want to ignore so as to avoid
	// walking hypernormal paths
	private Set<DefaultEdge> deactivatedEdges;
	
	// map each fragment to its towers
	private Map<Fragment, List<FragmentTower>> fragmentToTowers;
	
	// the position of a node within its fragment
	private Map<DefaultGraphCell,Integer> relXtoParent;
	private Map<DefaultGraphCell,Integer> relXtoRoot;
	private Map<DefaultGraphCell,Integer> relYpos;
	
	// the depth of a node in its fragment - AK ???
	private Map<DefaultGraphCell,Integer> nodesToDepth;
	private Map<DefaultGraphCell, Shape> nodesToShape;
	
	// the absolute position of a node in the graph
	private Map<DefaultGraphCell,Integer> xPos;
	private Map<DefaultGraphCell,Integer> yPos;
	
	private Fragment movedRoot;
	private int yOffset;
	
	
	
	/**
	 * Initializes a new dominance graph layout
	 * of a given dominanc graph.
	 * 
	 * @param gr the graph to compute the layout for
	 */
	DomGraphLayout(JDomGraph gr) {
		/*
		 * initializing the graph and its attributes
		 * by getting them from the graph
		 */
		this.graph = gr;
		fragments = graph.getFragments();
		
		movedRoot = null;
		yOffset = 0;
		//all the other fields are initialized empty
		deactivatedEdges = new HashSet<DefaultEdge>();
		
		fragXpos = new HashMap<Fragment, Integer>();
		fragYpos = new HashMap<Fragment, Integer>();
		
		fragWidth = new HashMap<Fragment, Integer>();
		fragHeight = new HashMap<Fragment, Integer>();
		
		fragOffset = new HashMap<Fragment, Integer>();
		
		relXtoParent = new HashMap<DefaultGraphCell,Integer>();
		relYpos = new HashMap<DefaultGraphCell,Integer>();
		
		xPos = new HashMap<DefaultGraphCell,Integer>();
		yPos = new HashMap<DefaultGraphCell,Integer>();
		
		nodesToShape = new HashMap<DefaultGraphCell, Shape>();
		nodesToDepth = new HashMap<DefaultGraphCell,Integer>();
		fragmentToTowers = new HashMap<Fragment, List<FragmentTower>>();
		relXtoRoot = new HashMap<DefaultGraphCell, Integer>();
		
	}
	
	
	
	/**
	 * <code>Cost</code>
	 * Class to represent assessment criteria for 
	 * layouts.
	 *
	 */
	private static class Cost implements Comparable<Cost> {
		
		private int degree, 						// maximal amount of "box tiers"
		crossings, 						// amount of crossings
		towers,  						// amount of towers
		averageTowerHeight, 			// averaged height of towers
		completeTowerHeight, 			// summed up height of towers
		maxBoxHeight, maxBoxWidth,  	// dimensions of the largest box
		maxEdgeLength, minEdgeLength, 	// extreme values of edge length
		boxes, 							// amount of boxes
		edges; 							// amount of edges
		
		private float boxRatio; 					// ratio maxBoxHeight/maxBoxWidth 
		private double maxEdgeRange; 				// diference of longest and shortest edge 
		
		public static Cost maxCost = new Cost(Integer.MAX_VALUE);
		
		/**
		 * 
		 * @return maximal edge range
		 */
		public double getMaxEdgeRange() {
			return maxEdgeRange;
		}
		
		/**
		 * 
		 * @param maxEdgeRange
		 */
		public void setMaxEdgeRange(double maxEdgeRange) {
			this.maxEdgeRange = maxEdgeRange;
		}
		
		/**
		 * initializes all fields, most of them with zero,
		 * the values to set "minimal" later on with 
		 * <code>Integer.MAX_VALUE</code>
		 *
		 */
		public Cost() {
			degree = 0;
			crossings = 0;
			towers = 0;
			averageTowerHeight = 0;
			completeTowerHeight = 0;
			maxBoxHeight = 0;
			maxBoxWidth = 0;
			boxRatio = 0;
			boxes = 0;
			maxEdgeLength = 0;
			minEdgeLength = Integer.MAX_VALUE;
			maxEdgeRange = Integer.MAX_VALUE;
		}
		
		/**
		 * This constructor sets 
		 * all fields in the cost to the value of the
		 * parameter x. It can be used to create Cost objects that are
		 * maximally expensive. (Use it as new Cost(Integer.MAX_VALUE).)
		 */
		private Cost(int x) {
			degree = x;
			crossings = x;
			towers = x;
			averageTowerHeight = x;
			completeTowerHeight = x;
			maxBoxHeight = x;
			maxBoxWidth = x;
			boxRatio = x;
			boxes = x;
			maxEdgeLength = x;
			minEdgeLength = 0;
			maxEdgeRange = x;
		}
		
		/**
		 * adds up another Cost object to this one.
		 * (deprecated?)
		 * @param c
		 */
		public void add(Cost c) {
			degree += c.degree;
			crossings += c.crossings;
			towers += c.towers;
			averageTowerHeight += c.averageTowerHeight;
			completeTowerHeight += c.completeTowerHeight;
			maxBoxHeight = Math.max(maxBoxHeight,c.maxBoxHeight);
			maxBoxWidth = Math.max(maxBoxWidth, c.maxBoxWidth);
			boxRatio = (boxRatio + c.boxRatio)/2;
			boxes += c.boxes;
		}
		
		/**
		 * returns the most relevant values of this <code>Cost</code> 
		 * object as <code>String</code>
		 * @return the String reperesentation 
		 */
		public String toString() {
			StringBuffer content = new StringBuffer();
			
			content.append("<Cost: crossings= " + crossings + ">");
			content.append(System.getProperty("line.separator"));
			content.append("<Cost: box ratio = " + boxRatio + ">");
			content.append(System.getProperty("line.separator"));
			content.append("<Cost: maximal edge range = " + maxEdgeRange + ">");
			content.append(System.getProperty("line.separator"));
			content.append("<Cost: towers = " + towers + ">");
			content.append(System.getProperty("line.separator"));
			content.append("<Cost: average tower height = " + averageTowerHeight + ">");
			content.append(System.getProperty("line.separator"));
			content.append("<Cost: max. superposed boxes = " + degree + ">");
			content.append(System.getProperty("line.separator"));
			
			return content.toString();
		}
		
		/**
		 * Updates the saved difference of the minimal and 
		 * the maximal edge length in this <code>Cost</code> 
		 * object.
		 * @param edgeLength the edge length to consider
		 */
		public void updateEdgeCost(double edgeLength) {
			edges++;
			
			if( edgeLength < minEdgeLength ) {
				minEdgeLength = (int) edgeLength;
				if(edges == 1) {
					maxEdgeLength = minEdgeLength;
				}
			} else if ( edgeLength > maxEdgeLength ) {
				maxEdgeLength = (int) edgeLength;
			}
			
			maxEdgeRange = maxEdgeLength - minEdgeLength;
		}
		
		
		
		/**
		 * @return Returns the crossings.
		 */
		int getCrossings() {
			return crossings;
		}
		/**
		 * @param crossings The crossings to set.
		 */
		void setCrossings(int crossings) {
			this.crossings = crossings;
		}
		
		
		
		/**
		 * @return Returns the maxBoxHeight.
		 */
		int getMaxBoxHeight() {
			return maxBoxHeight;
		}
		
		/**
		 * @param maxBoxHeight The maxBoxHeight to set.
		 */
		void setMaxBoxHeight(int height) {
			if(height> maxBoxHeight) {
				maxBoxHeight = height;
			}
		}
		
		void raiseCrossings(){
			crossings++;
		}
		/**
		 * @return Returns the degree.
		 */
		int getDegree() {
			return degree;
		}
		/**
		 * @param degree The degree to set.
		 */
		void setDegree(int degree) {
			this.degree = degree;
		}
		
		/**
		 * Changes degree if the parameter is
		 * a higher degree.
		 * @param deg the degree to compare with
		 */
		void setDegreeIfGreater(int deg) {
			if(deg> degree){
				degree = deg;
			}
		}
		
		/**
		 * @return Returns the towers.
		 */
		int getTowers() {
			return towers;
		}
		/**
		 * @param towers The towers to set.
		 */
		void setTowers(int towers) {
			this.towers = towers;
		}
		
		/**
		 * raises the amount of towers by one and
		 * computes (approximately) the average
		 * tower height. 
		 * @param towHeight
		 */
		void raiseTowers(int towHeight) {
			towers++;
			completeTowerHeight += towHeight;
			averageTowerHeight = completeTowerHeight/towers;
		}
		
		/**
		 * raises the amount of towers by one.
		 *
		 */
		void raiseTowers() {
			towers++;
		}
		
		/**
		 * Compares this <code>Cost</code> object to
		 * another one.
		 * @param anotherCost they other cost object
		 * @return 0 if the two objects are equal,
		 *  	a negative int value if the other Cost object
		 *  	is greater, otherwise a positive int value.
		 */
		public int compareTo(Cost anotherCost) {
			return compare(this, anotherCost);
		}
		
		
		
		
		
		/**
		 * Compares the calling <code>Cost</cost> object to
		 * another one.
		 * @param obj, the second cost object
		 * @return true if the two objects represent
		 * 			the same cost values
		 */
		public boolean equals(Object obj) {	
			return (compare(this, (Cost) obj) == 0);
		}
		
		
		/**
		 * Compares two <code>Cost</code> objects.
		 * @param costX, the first cost object
		 * @param costY, the second cost object
		 * @return 0 if the two objects are equal,
		 *  	a negative int value if the second Cost object
		 *  	is greater, otherwise a positive int value.
		 */
		private int compare(Cost costX, Cost costY) {
			
			if (costX.getCrossings() == costY.getCrossings()) {
				
				if (costX.getMaxEdgeRange() == costY.getMaxEdgeRange()) {
					
					if (costX.getBoxRatio() == costY.getBoxRatio()) {
						
						if (costX.getDegree() == costY.getDegree()) {
							
							if (costX.getMaxBoxHeight() == costY
									.getMaxBoxHeight()) {
								
								if (costX.getTowers() == costY.getTowers()) {
									
									if (costY.getAverageTowerHeight() == costX
											.getAverageTowerHeight()) {
										return 0;
									} else {
										return costX.getAverageTowerHeight()
										- costY.getAverageTowerHeight();
									}
								} else {
									return costY.getTowers()
									- costX.getTowers();
								}
							} else {
								return costX.getMaxBoxHeight()
								- costY.getMaxBoxHeight();
							}
						} else {
							return costX.getDegree() - costY.getDegree();
						}
						
					} else {
						// TODO probably this could be simplified?!
						BigDecimal ratBigInt = new BigDecimal((double) (
								Math.abs(1 - costX.getBoxRatio()) 
								- Math.abs(1 - costY.getBoxRatio())));
						return ratBigInt.signum();
					}
				} else {
					BigDecimal edgLgth = new BigDecimal(costX.getMaxEdgeRange()
							- costY.getMaxEdgeRange());
					return edgLgth.signum();
				}
			} else {
				return costX.getCrossings() - costY.getCrossings();
			} 
		}
		
		
		
		/**
		 * @return Returns the averageTowerHeight.
		 */
		private int getAverageTowerHeight() {
			return averageTowerHeight;
		}
		
		/**
		 * 
		 * @return the number of boxes
		 */
		public int getBoxes() {
			return boxes;
		}
		
		/**
		 * @param boxes
		 */
		public void setBoxes(int boxes) {
			this.boxes = boxes;
		}
		
		/**
		 * 
		 * @return the box ratio
		 */
		public float getBoxRatio() {
			return boxRatio;
		}
		
		/**
		 * 
		 * @param boxRatio
		 */
		public void setBoxRatio(float boxRatio) {
			this.boxRatio = boxRatio;
		}
		
		/**
		 * 
		 * @return the maximal box width
		 */
		public int getMaxBoxWidth() {
			return maxBoxWidth;
		}
		
		/**
		 * Sets the maximal box width.
		 * @param maxBoxWidth 
		 */
		public void setMaxBoxWidth(int maxBoxWidth) {
			this.maxBoxWidth = maxBoxWidth;
		}
		
		/**
		 * updates this <code>Cost</code> object's boxRatio.
		 * Notes if a parameter is a new extreme value for box
		 * height or box width.
		 * Counts up the boxes and calculates the new ratio of
		 * the maximal box width and the maximal box height.
		 * @param boxHeight the new box height to check
		 * @param boxWidth the new box width
		 */
		public void updateBoxParameter(int boxHeight, int boxWidth) {
			boxes++;
			if( boxHeight > maxBoxHeight ) {
				maxBoxHeight = boxHeight;
			}
			
			if( boxWidth > maxBoxWidth) {
				maxBoxWidth = boxWidth;
			}
			
			boxRatio = (float) maxBoxHeight / maxBoxWidth;
		}
		
		public int hashCode() {
			return maxBoxWidth * maxBoxHeight * boxes * averageTowerHeight;
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
	 * Perform DFS in a fragment to resolve its leave nodes in
	 * the right order.
	 * 
	 * @param frag the fragment to get the leaves from
	 * @param root the recent visited node
	 * @param leaves the leaves resolved yet
	 */
	private void fragLeafDFS(Fragment frag, DefaultGraphCell root,
			List<DefaultGraphCell> leaves) {
		
		//if we found a leaf, we add it tot the list.
		if(frag.isLeaf(root)) {
			leaves.add(root);
		} else {
			/*
			 * if the node ist no (fragment-) leave, there have
			 * to be some children.
			 * DFS is performed for each child contained in the
			 * fragment.
			 */
			for(int i = 0; i< graph.getChildren(root).size(); i++) {
				if(frag.getNodes().contains(graph.getChildren(root).get(i)))
					fragLeafDFS(frag, graph.getChildren(root).get(i), leaves);
			}
		}
	}
	
	/**
	 * Resolve all leaves of a fragment (in the
	 * right order).
	 *  
	 * @param frag the fragment to get the leaves for
	 * @return the list of leaves.
	 */
	private List<DefaultGraphCell> getFragLeaves(Fragment frag) {
		
		//initializing the leave list
		List<DefaultGraphCell> leaves = new ArrayList<DefaultGraphCell>();
		
		//performing DFS to fill the leave list.
		fragLeafDFS(frag, getFragRoot(frag), leaves);
		
		return leaves;
	}
	
	/**
	 * Resolving the holes of a fragment in the right order.
	 * This does not the same job as <code>getFragLeaves</code>,
	 * because leaves that are roots at the same time have to be
	 * excluded.
	 * 
	 * @param frag the fragment to get the holes from
	 * @return the list of holes; an empty list if there are none.
	 */
	private List<DefaultGraphCell> getFragHoles(Fragment frag) {
		
		List<DefaultGraphCell> holes = new ArrayList<DefaultGraphCell>();
		
		for( DefaultGraphCell leaf : getFragLeaves(frag) ) {
			if(graph.getNodeData(leaf).getType().equals(NodeType.unlabelled)) {
				holes.add(leaf);
			}
		}
		
		return holes;
		
	}
	
	/**
	 * Compute the incoming edges of a fragment.
	 * 
	 * @param frag the fragment to compute the in-edges for
	 * @return the sorted list of incoming edges.
	 */
	private List<DefaultEdge> getFragInEdges(Fragment frag) {
		
		//the in-edges of a fragment are the equivalent of
		//the in-edges of the fragment's root.
		return graph.getInEdges(getFragRoot(frag));
	}
	
	/**
	 * Compute the outgoing edges of a fragment.
	 *  
	 * @param frag the fragment to compute the in-edges for
	 * @return the sorted list of outgoing edges.
	 */
	private List<DefaultEdge> getFragOutEdges(Fragment frag) {
		
		List<DefaultEdge> outEdges = new ArrayList<DefaultEdge>();
		
		
		/*
		 * the outgoing edges are the edges going out
		 * from the fragment's holes.
		 * The holes are saved in the right order and
		 * getOutEdges(DefaulGraphCell) returnes a 
		 * sorted list of edges.
		 * So the list of out-edges is resolved by
		 * computing the out-edges of a hole and appending
		 * the out-edges of the next hole(s).
		 */
		for(DefaultGraphCell hole : getFragHoles(frag))  {
			outEdges.addAll(graph.getOutEdges(hole));
		}
		return outEdges;
		
	}
	
	/**
	 * Computes (approximately) the length of the Edge between two 
	 * Fragments. 
	 * 
	 * @param from
	 * @param to
	 * @return the distance
	 */
	private double getFragmentDistance(Fragment from, Fragment to,
			Map<Fragment,Integer> xCoordinates, Map<Fragment, Integer> yCoordinates) {
		int fromX = xCoordinates.get(from) + (fragWidth.get(from)/2);
		int fromY = yCoordinates.get(from) + fragHeight.get(from);
		int toX = xCoordinates.get(to) + (fragWidth.get(to)/2);
		int toY = yCoordinates.get(to);
		
		double roughDistance =
			Math.sqrt(
					Math.pow(Math.abs(fromX - toX),2) 
					+ Math.pow(Math.abs(fromY-toY),2)
			);
		
		return roughDistance;
	}
	
	/**
	 * Resolving the number of dominance edges adjacent
	 * to a fragment.
	 * 
	 * @param frag the fragment to compute the degree for
	 * @return the fragment degree (considering the fragment graph)
	 */
	private int getFragDegree(Fragment frag) {
		
		//adding indegree and outdegree.
		return getFragInEdges(frag).size() + 
		getFragOutEdges(frag).size();
	}
	
	
	/**
	 * computes the tree layout for each fragment.
	 *
	 */
	private void computeFragmentLayouts() {
		
		// iterating over the fragments
		for( Fragment frag : fragments ) {
			
			// the recent root
			DefaultGraphCell root = getFragRoot(frag);
			// computing the x-positions, dependent on the _direct_
			// parent
			
			
			GraphLayoutCursor layCursor = new GraphLayoutCursor(root, this, graph, frag.getNodes());
			PostOrderNodeVisitor postVisitor = new PostOrderNodeVisitor(layCursor);
			postVisitor.run();
			
			// another DFS computes the y- and x-positions relativ to the
			// _root_
			GraphDrawingCursor drawCursor = new GraphDrawingCursor(root, this, graph, frag.getNodes());
			PreOrderNodeVisitor preVisitor = new PreOrderNodeVisitor(drawCursor);
			preVisitor.run();
			
			
		}
		
	}
	
	/**
	 * computes the height of a fragment depndent
	 * on its maximal depth.
	 * node: this method is not meaningful before
	 * having performed computeXandDepth.
	 * 
	 * @param frag the fragment
	 */
	private int computeFragHeight(Fragment frag) {
		
		Shape box = nodesToShape.get(getFragRoot(frag));
		
		//the height is the maximal depth* (node + distance) -
		//the height of the last node (depth starts at 1 at
		//shape)
		return box.depth()*(30+ nodeYDistance) - nodeYDistance;
	}
	
	/**
	 * computes the width of all fragments dependent 
	 * on the number of their leaves.
	 */
	private int computeFragWidth(Fragment frag) {
		
		DefaultGraphCell fragRoot = getFragRoot(frag);
		Shape box = nodesToShape.get(fragRoot);
		BoundingBox bb = box.getBoundingBox();
		int extL = bb.left;
		int extR = bb.right;
		
		
		
		int offset = 0 - extL;
		fragOffset.put(frag, offset);
		
		return extR - extL;
	}
	
	public void putNodeToShape(DefaultGraphCell node, Shape shape) {
		nodesToShape.put(node,shape);
	}
	
	/**
	 * computes the dimensions of all fragments using
	 * computeFragHeight and computeFragWidth.
	 * Before computing a fragment's height, 
	 * computeXandDepth is called.
	 *
	 */
	private void computeFragDimensions() {
		computeFragmentLayouts();
		for(Fragment frag : graph.getFragments() ) {
			
			fragHeight.put(frag, new Integer(computeFragHeight(frag)));
			fragWidth.put(frag, new Integer(computeFragWidth(frag)));
		}
	}
	
	
	/**
	 * Compute the boxes for all fragments via DFS.
	 * 
	 * @param fragment the fragment recently considered
	 * @param visited the fragments visited yet
	 * @param box the rectangle bordering the fragment box
	 * @param xStart the x-position of the left upper box corner
	 * @param yStart the y-position of the left upper box corner
	 * @param cost the assessment values for DFS with the given root
	 * @param dfsDescendants the descendants of "fragment" in the DFS (including "fragment" itself)
	 */
	private Cost fragmentBoxDFS(Fragment fragment, Set<Fragment> visited, 
			Rectangle box, int xStart, int yStart, 
			Collection<Fragment> dfsDescendants, 
			Cost co, boolean mayBuildTowers,
			Map<Fragment,Integer> xStorage, Map<Fragment,Integer> yStorage) {
		
		Cost cost = co;
		List<FragmentTower> myTowers = new ArrayList<FragmentTower>();
		
		int nextY = yStart;
		int nextX = xStart;
		
		// for counting edge crossings
		boolean crossedOutEdges = false;
		boolean encounteredVisitedHole = false;
		
		// coordinates for the different regions
		int maxTowerHeight = 0;
		int towersMaxX = 0;        // right-hand border of rightmost tower
		int towersMinY = 0;        // top border of all towers
		int rightHandPartX = 0;    // x-position of my DFS children
		int yBottomDomParent = -1; // y-pos of lowest dom. parent
		
		// the list of fragments that I have to lay out as dominance
		// parents, from top to bottom
		List<Fragment> myDominanceParents = new ArrayList<Fragment>();
		
		// the list of fragments that are dominance parents of my
		// towers, from top to bottom (disjoint from myDominanceParents)
		List<Fragment> myTowerDominanceParents = new ArrayList<Fragment>();
		
		// the list of fragments that I have to lay out as dominance
		// children, from top to bottom
		List<Fragment> myDominanceChildren = new ArrayList<Fragment>();
		
		// The set of all holes that have dominance children
		// which have been visited already. All edges going out of
		// such holes will be deactivated.
		Set<DefaultGraphCell> holesWithVisitedChildren = 
			new HashSet<DefaultGraphCell>();
		
		Set<Fragment> myDeactivatedChilrden = new HashSet<Fragment>();
		
		// if I have been visited before (as fragment or tower), return immediately
		if(visited.contains(fragment)) {
			return cost;
		} else {
			visited.add(fragment);
			dfsDescendants.add(fragment);
			
			
			// 0. Compute the hole by which we entered (if there is one)
			for( DefaultGraphCell hole : getFragHoles(fragment) ) {
				for( DefaultEdge outedge : graph.getOutEdges(hole) ) {
					if( visited.contains(graph.getTargetFragment(outedge) ) ) {
						holesWithVisitedChildren.add(hole);
						break;
					}
				}
			}
			
			
			// 1. Compute the towers
			if(mayBuildTowers) {
				computeTowers(fragment, visited, myTowers);
			}
			fragmentToTowers.put(fragment, myTowers);
			
			// 2a. Determine my dominance parents.
			for( DefaultEdge edge : getFragInEdges(fragment)) {
				Fragment sf = graph.getSourceFragment(edge);
				if( !deactivatedEdges.contains(edge) &&
						!visited.contains(sf) ) {
					myDominanceParents.add(sf);
				}
			}
			
			// 2b. Remove border towers.
			
			// first case: at least one tower and no (other) dominance
			// parents
			if(myDominanceParents.isEmpty() && (! myTowers.isEmpty())) {
				FragmentTower toRemove = myTowers.remove(0);
				visited.removeAll(toRemove.getFragments());
				fragmentToTowers.get(fragment).remove(toRemove);
				myDominanceParents.add(toRemove.getFoot());
			}
			
			
			
			
			// 2c. compute maximal tower height and the towers'
			// dominance parents now.
			for( FragmentTower tower : myTowers ) {
				
				cost.raiseTowers(tower.getHeight());
				
				// compute maximum tower height
				if( tower.getHeight() > maxTowerHeight )
					maxTowerHeight = tower.getHeight();
				
				// compute the right-hand border of the rightmost
				// tower
				if( towersMaxX == 0 ) {
					towersMaxX += tower.getWidth();
				} else {
					towersMaxX += towerXDistance + tower.getWidth();
				}
				
				// remember the towers' dominance parents
				myTowerDominanceParents.addAll(tower.getDominanceParents());
			}
			
			// 2d. Determine my dominance children.
			List<DefaultGraphCell> holes = getFragHoles(fragment);
			Collections.reverse(holes);
			
			for( DefaultGraphCell hole : holes ) {
				List<DefaultEdge> outedges = graph.getOutEdges(hole);
				boolean firstOutEdge = true;
				
				// all dominance edges out of the hole we came in by
				// are non-hypernormal
				if( holesWithVisitedChildren.contains(hole) ) {
					for( DefaultEdge edge : outedges ) {
						deactivatedEdges.add(edge);
					}
					
					encounteredVisitedHole = true;
				}
				
				for( DefaultEdge edge : outedges ) {
					Fragment tf = graph.getTargetFragment(edge);
					if( !deactivatedEdges.contains(edge) ) {
						if( visited.contains(tf) ) {
							encounteredVisitedHole = true;
						} else {
							if( firstOutEdge ) {
								myDominanceChildren.add(tf);
								firstOutEdge = false;
								
								if( encounteredVisitedHole ) {
									crossedOutEdges = true;
								}
							} else {
								deactivatedEdges.add(edge);
								myDeactivatedChilrden.add(tf);
							}
						}
					} 
				}
			}
			
			if( crossedOutEdges ) { 
				cost.raiseCrossings();
			}
			
			cost.setDegreeIfGreater(myDominanceChildren.size() 
					+ myDominanceParents.size());
			
			// 3. Place myself (X).
			nextX = xStart + Math.max(fragWidth.get(fragment), towersMaxX);
			xStorage.put(fragment, 
					xStart + Math.max(fragWidth.get(fragment), towersMaxX)/2
					- fragWidth.get(fragment)/2);
			
			// 4. Determine the x-position of the right-hand sub-boxes
			// (i.e. both kinds of dominance parents and dominance children)
			rightHandPartX = xStart + Math.max(fragWidth.get(fragment), towersMaxX) 
			+ fragmentXDistance;
			nextY = yStart;
			
			// 5. Place the towers' dominance parents
			for( Fragment tdp : myTowerDominanceParents ) {
				Rectangle tdpBox = new Rectangle();
				Cost tdpCost = 
					fragmentBoxDFS(tdp, visited, tdpBox,  
							rightHandPartX, nextY, dfsDescendants, cost, false,
							xStorage, yStorage);
				
				nextY += tdpBox.getHeight() + fragmentYDistance;
				
				updateBox(tdp, box, tdpBox);
			}
			
			towersMinY = nextY; // towers start at this y-position
			
			// 6. Place my own dominance parents
			for( Fragment dp : myDominanceParents ) {
				Rectangle dpBox = new Rectangle();
				
				Cost dpCost = 
					fragmentBoxDFS(dp, visited, dpBox,  
							rightHandPartX, nextY, dfsDescendants, cost, false,
							xStorage, yStorage);
				
				yBottomDomParent = yStorage.get(dp) + fragHeight.get(dp);
				
				nextY += dpBox.getHeight() + fragmentYDistance;
				
				updateBox(dp, box, dpBox);
			}
			
			// if the dominance parents' boxes are heigher than the 
			// heighest towers, the towers are moved down (to align
			// with the lowes dominance-parent-box
			towersMinY = Math.max(towersMinY, (yBottomDomParent - maxTowerHeight));
			
			
			// 7. Place the towers
			
			// placing the towers dependent on the middle axis of the
			// recent fragment
			nextX = xStorage.get(fragment) + (fragWidth.get(fragment) - towersMaxX)/2;
			for( FragmentTower tower : myTowers ) {
				tower.place(nextX, towersMinY, xStorage, yStorage);
				nextX += tower.getWidth() + towerXDistance;
				updateBox(fragment, box, tower.getBox());
			}
			
			// 8. Place myself (Y) and my topmost dominance child (if
			// there is one).
			Fragment topChild = null;
			if(! myDominanceChildren.isEmpty() ) {
				topChild = myDominanceChildren.remove(0);
			}  
			
			if( !myTowers.isEmpty() || !myDominanceParents.isEmpty() ) {
				// have towers or dominance parents -> place myself
				// below the towers or parents
				int myY = 0;
				int domParentLowerY = 0;
				
				if( !myTowers.isEmpty() ) {
					myY = towersMinY + maxTowerHeight + fragmentYDistance;
				}
				
				if( !myDominanceParents.isEmpty() ) {
					domParentLowerY = yBottomDomParent + fragmentYDistance;
					myY = Math.max(myY, domParentLowerY);
				}
				
				//my own position depends on my dominance parents (and towers)
				yStorage.put(fragment, myY);
				
				if( topChild != null ) {
					/*
					 * My topmost dominance child has to be placed under myself,
					 * so possibly its box has to be moved down (and not start directly
					 * under the dominance parents' box) 
					 */
					//	Fragment topChild = myDominanceChildren.remove(0);
					Rectangle topChildBox = new Rectangle();
					Set<Fragment> tcDescendants = new HashSet<Fragment>();
					Cost tcCost = 
						fragmentBoxDFS(topChild, visited, topChildBox, 
								rightHandPartX, 
								nextY, tcDescendants, cost, true,
								xStorage, yStorage);
					
					// the minimal acceptable y-Position of my topmost
					// dominance child.
					int hMe = yStorage.get(fragment) 
					+ fragHeight.get(fragment) 
					+ fragmentYDistance;
					
					
					// y-position of topChild
					int posCh = yStorage.get(topChild);
					
					for(Fragment frag : tcDescendants ) {
						if(myDeactivatedChilrden.contains(frag) ||
								myDominanceChildren.contains(frag)) {
							if(yStorage.get(frag) < posCh) {
								posCh = yStorage.get(frag);
							}
						}
					} 
					
					if( hMe > posCh ) {
						
						
						//the childbox is placed too far above;
						//move it down until it's placed far enough
						//under myself.
						translateFragments(tcDescendants, 0, hMe-posCh, xStorage, yStorage);
						
						topChildBox.translate(0, hMe-posCh);
					}
					dfsDescendants.addAll(tcDescendants);
					
					nextY = (int) topChildBox.getMaxY() + fragmentYDistance;
					
					updateBox(topChild, box, topChildBox);
				}
			} else if( topChild != null ) {
				// have no towers or dominance parents, but I do have
				// children -> arrange myself and the topmost child
				//  Fragment topChild = myDominanceChildren.remove(0);
				Rectangle topChildBox = new Rectangle();
				Set<Fragment> tcDescendants = new HashSet<Fragment>();
				Cost tcCost = 
					fragmentBoxDFS(topChild, visited, topChildBox, 
							rightHandPartX, 
							yStart, tcDescendants, cost, true,
							xStorage, yStorage);
				
				// By how much do I have to be above topChild?
				int hMe = fragHeight.get(fragment) + fragmentYDistance;
				
				// How much is topChild below yStart?
				int hTc = yStorage.get(topChild) - yStart;
				
				for(Fragment frag : tcDescendants ) {
					if(myDeactivatedChilrden.contains(frag) ||
							myDominanceChildren.contains(frag)) {
						if(yStorage.get(frag) < hTc) {
							hTc = yStorage.get(frag);
						}
					}
				} 
				
				
				if( hMe <= hTc ) {
					// tc-box is in the right place, now place myself
					yStorage.put(fragment, yStorage.get(topChild) - hMe);
				} else {
					// tc-box is shallow, need to move it down a little
					yStorage.put(fragment, yStart);
					
					translateFragments(tcDescendants, 0, hMe-hTc, xStorage, yStorage);
					topChildBox.translate(0, hMe-hTc);
				}
				
				
				dfsDescendants.addAll(tcDescendants);
				
				nextY = (int) topChildBox.getMaxY() + fragmentYDistance;
				
				updateBox(topChild, box, topChildBox);
			} else {
				// have no dfs children at all
				yStorage.put(fragment, yStart);
			}
			
			updateBox(fragment, box, new Rectangle(xStorage.get(fragment),
					yStorage.get(fragment),
					fragWidth.get(fragment),
					fragHeight.get(fragment))
			);
			
			
			// 9. Place the rest of the dominance children
			for( Fragment dc : myDominanceChildren ) {
				Rectangle dcBox = new Rectangle();
				
				
				Cost dcCost = 
					fragmentBoxDFS(dc, visited, dcBox, 
							rightHandPartX, nextY, dfsDescendants, 
							cost, true, xStorage, yStorage);
				
				nextY += dcBox.getHeight() + fragmentYDistance;
				
				updateBox(dc, box, dcBox);
			}
			
			
			// 10. update cost
			
			for( Fragment dp : myDominanceParents ) {
				cost.updateEdgeCost(getFragmentDistance(fragment,dp, xStorage, yStorage));
			}
			
			for( Fragment dc : myDominanceChildren ) {
				cost.updateEdgeCost(getFragmentDistance(fragment, dc, xStorage, yStorage));
			}
			
			cost.updateBoxParameter((int) box.getHeight(), (int) box.getWidth());
		}
		
		return cost;
	}
	
	/**
	 * Move all fragments in "frags" and the towers that belong
	 * to them right by "xoff" and down by "yoff".
	 * 
	 * @param frags
	 * @param xoff
	 * @param yoff
	 */
	private void translateFragments(Collection<Fragment> frags, 
			int xoff, int yoff, Map<Fragment,Integer> xStore,
			Map<Fragment,Integer> yStore) {
		for( Fragment frag : frags ) {
			if( xoff != 0 ) {
				int xMovement = xoff;
				if(xStore.containsKey(frag)) {
					xMovement += xStore.remove(frag);
				}
				xStore.put(frag, xMovement);
			}
			if( yoff != 0 ) {
				int yMovement = yoff;
				if(yStore.containsKey(frag)) {
					yMovement += yStore.remove(frag);
				}
				yStore.put(frag, yMovement);
			}
			if( fragmentToTowers.containsKey(frag)) {
				for( FragmentTower tower : fragmentToTowers.get(frag) ) {
					tower.translate(xoff, yoff, xStore, yStore);
				}
			}
		}
		
	}
	
	/**
	 * Enlarges the box "box" so that it contains "with".
	 * "box" is the box of the fragment "f". "type" is the
	 * box type as per BoxDebugger. If the field "drawBoxes"
	 * is set to true, the method will also draw the new box. 
	 * 
	 * @param f
	 * @param box
	 * @param with
	 */
	private void updateBox(Fragment f, Rectangle box, Rectangle with) {
		if( (box.getWidth() == 0) && (box.getHeight() == 0)) {
			box.setBounds(with);
		} else {
			box.add(with);
		}
	}
	
	/**
	 * Computes towers over a given base.
	 * 
	 * @param base the fragment whose towers should be computed
	 * @param visited the list of visited edges to be completed by this method
	 * @param domParents the resolved dominance parents of the tower
	 * @return the width of all towers (incl. distance), -1 if there are none.
	 */
	private void computeTowers(Fragment base, 
			Set<Fragment> visited, List<FragmentTower> towers) {
		
		towers.clear();
		
		// try each non-visited dominance parent of "base"
		// as the potential lowest node in a tower
		for(DefaultEdge edge : getFragInEdges(base)) {
			Fragment parentFrag = graph.getSourceFragment(edge);
			
			if(!visited.contains(parentFrag)) {
				FragmentTower thisTower = new FragmentTower();
				towerDFS(base, parentFrag, thisTower, visited);
				
				if( thisTower.getFragments().size() >= 1 ) {
					towers.add(thisTower);
				} 
			}
		}
	}
	
	
	
	/**
	 * Try to find a tower over base. A tower is a set of fragments
	 * with a linear backbone that has single leaf fragments dangling
	 * from it. Towers can consist of a single fragment.
	 * 
	 * The method will not consider fragments that have already been
	 * visited.
	 * 
	 * @param base the fragment to which the tower should belong
	 * @param frag the fragment we're visiting 
	 * @param tower the tower we create, null if there is none yet
	 * @param visited the list of edges visited (to be completed here)
	 */
	private void towerDFS(Fragment base, Fragment frag,  
			FragmentTower tower, Set<Fragment> visited) {
		int numInEdges = 0;
		Map<Fragment,Integer> leafXOffsets = new HashMap<Fragment,Integer>();
		DefaultEdge toFirstParent = new DefaultEdge();
		
		// skip visited fragments
		if( visited.contains(frag) ) {
			return;
		}
		
		// compute incoming dominance edges
		for(DefaultEdge edge : getFragInEdges(frag)) {
			if(!deactivatedEdges.contains(edge)) {
				if( numInEdges == 0 ) {
					toFirstParent = edge;
				}
				
				numInEdges++;
			}
		}
		
		// check outgoing dominance edges; if any dominance child
		// is not a leaf (or already visited), then frag doesn't belong
		// to the tower
		for(DefaultGraphCell sourceHole : getFragHoles(frag)) {
			boolean outEdgeFound = false;
			
			for(DefaultEdge edge : graph.getOutEdges(sourceHole)) {
				// consider only the first outgoing edge; all others
				// are deactivated
				if(outEdgeFound) {
					deactivatedEdges.add(edge);
				} else {
					outEdgeFound = true;
					DefaultGraphCell childRoot =
						(DefaultGraphCell) JGraphUtilities.getTargetVertex(graph, edge);
					Fragment childFrag = graph.findFragment(childRoot);
					
					if(!childFrag.equals(base)) {
						// TODO refactor this
						if(! (childFrag.isLeaf(childRoot) && (getFragDegree(childFrag) == 1))) {
							return;
						}
						else {
							// childFrag is a leaf fragment, so compute
							// x-offset relative to frag
							int leafXOffset =
								relXtoParent.get(sourceHole)
								+ graph.computeNodeWidth(sourceHole)/2
								- graph.computeNodeWidth(childRoot)/2;
							leafXOffsets.put(childFrag,leafXOffset);
						}
					}
				}
			}
		}
		
		
		// If we come to this point, "frag" belongs to a tower.
		visited.add(frag);		
		
		if(tower.isEmpty()) {
			tower.setFoot(frag);
		}
		
		// add myself to the tower
		tower.addBackboneFragment(frag, fragWidth.get(frag), fragHeight.get(frag));
		
		// add my leaf children to the tower
		for( Map.Entry<Fragment,Integer> childFrag : leafXOffsets.entrySet() ) {
			tower.addLeafFragment(childFrag.getKey(), frag, childFrag.getValue(),
					fragWidth.get(childFrag.getKey()), fragHeight.get(childFrag.getKey()));
			visited.add(childFrag.getKey());
		}
		
		// deal with my dominance parents
		if( numInEdges > 1 ) {
			// if there was more than one, record them as dominance parents
			// in the tower
			for( DefaultEdge edge : getFragInEdges(frag) ) {
				tower.addDominanceParent(graph.getSourceFragment(edge));
			}
		} else if( numInEdges == 1 ) {
			// otherwise, recurse to the unique dominance parent
			Fragment parent = graph.getSourceFragment(toFirstParent);
			towerDFS(base, parent, tower, visited);
			
			// if the parent wasn't added to the tower for some reason
			// (not a tower-capable fragment), add it as a dominance parent
			if( !tower.getFragments().contains(parent)) {
				tower.addDominanceParent(parent);
			}
		}
	}
	
	
	
	
	/**
	 * computes the whole fragment graph.
	 * computes the fragment's x-position with undirected DFS,
	 * the fragment's later y-position performing directed DFS 
	 * (for each root).
	 */
	private void computeFragmentPositions() {
		
		Cost absoluteCost = null;
		int possibleOffset = 0;
		Fragment bestRoot = null;
		int xStart = 0;
		
		for(Set<DefaultGraphCell> wccs : graph.getWccs() ) {
			
			/*
			 * the roots to perform the fragment DFS to 
			 * compute the x-positions of all fragments;
			 * the resulting edge crossing are compared and the
			 * root that causes the most less crossings is
			 * chosen as final root.
			 */
			
			List<Fragment> possibleRoots = new ArrayList<Fragment>(graph.getWccFragments(wccs));
			
			//List<Fragment> possibleRoots = new ArrayList<Fragment>(graph.getWccFragments(wccs));
			//	possibleRoots.addAll(graph.getFragments());
			
			// the best root we have found so far, with its cost 
			Cost costBestRoot = new Cost(Integer.MAX_VALUE);
			
			Map<Fragment, Integer> tempXpos = 
				new HashMap<Fragment,Integer>();
			
			Map<Fragment, Integer> tempYpos = 
				new HashMap<Fragment,Integer>();
			
			for(Fragment root : possibleRoots) {
				//	System.out.println("New DFS with Root: " + root);
				
				Set<Fragment> visited = new HashSet<Fragment>();
				Cost thisCost = 
					fragmentBoxDFS(root, visited, new Rectangle(),
							xStart,0, new HashSet<Fragment>(), new Cost(), false,
							tempXpos, tempYpos);
				
				
				/*
				 * Especially for not hnc. graphs:
				 * If there are fragments not visited 
				 * by the DFS (and contained in the same wcc),
				 * we place them afterwards.
				 * In general this disturbs the layout, so
				 * this way of arrangement is declared as 
				 * expensive. 
				 */
				for(Fragment frag : possibleRoots ) {
					// for each fragment not seen yet...
					if(! visited.contains(frag) ) {
						
						// place it (the correct possition is not yet relevant
						// (here, we are just computing costs)
						Set<Fragment> deactivatedPlacements = new HashSet<Fragment>();
						Cost add = fragmentBoxDFS(frag, visited, new Rectangle(),
								xStart + thisCost.getMaxBoxWidth() + fragmentXDistance,
								0, deactivatedPlacements, new Cost(), false, 
								fragXpos, fragYpos);
						
						// add its cost to the total
						thisCost.add(add);
						
						// and simulate a crossing for every DFS-descendant
						// placed by this DFS pass.
						for(int i = 0; i< deactivatedPlacements.size(); i++ ) {
							thisCost.raiseCrossings();
						}
					}
				}
				// update best root
				if(thisCost.compareTo(costBestRoot) < 0) {
					bestRoot = root;
					costBestRoot = thisCost;
				}
				
				//before the next pass, the maps to fill
				//by fragmentDFS are cleared.
				tempXpos.clear();
				tempYpos.clear();
				deactivatedEdges.clear();
				fragmentToTowers.clear();
			}
			
			//	System.err.println("Best Root: " + bestRoot);
			//	System.err.println("Crossings: " + costBestRoot.getCrossings());
			Set<Fragment> visited = new HashSet<Fragment>();
			Cost lastCost = fragmentBoxDFS(bestRoot,visited, new Rectangle(), 
					xStart,0, new HashSet<Fragment>(), new Cost(), false,
					fragXpos, fragYpos);
			
			
			// the next fragment (wccs / unseen fragment)
			// is placed to the right of the last box.
			xStart += costBestRoot.getMaxBoxWidth() + DomGraphLayoutParameters.fragmentXDistance;
			
			// for each fragment...
			for(Fragment frag : possibleRoots ) {
				
				// check whether DFS has visited it.
				if(! visited.contains(frag) ) {
					// if not so, place it to the right
					// of the box, and compute its y-position 
					// according to its lowermost dominance parent
					
					int yStart = 0;
					
					// iterating over the dominance parents
					for( DefaultEdge edge : graph.getInEdges(getFragRoot(frag))) {
						
						// place the unvisited fragment under its
						// dominance parent with the biggest y-value
						// (which is the lowermost arranged one here)
						Fragment source = graph.getSourceFragment(edge);
						if(fragYpos.containsKey(source)) {
							int potentialY = fragYpos.get(source) 
							+ fragHeight.get(source)
							+ fragmentYDistance;
							
							if(potentialY > yStart) {
								yStart = potentialY;
							}
						}
					}
					Cost add = fragmentBoxDFS(frag, visited, new Rectangle(),
							xStart,
							yStart, new HashSet<Fragment>(), new Cost(), false, 
							fragXpos, fragYpos);
					costBestRoot.add(add);
				}
			}
			
			if( absoluteCost == null ) {
				absoluteCost = costBestRoot;
			} else {
				absoluteCost.add(costBestRoot);
			}
			xStart += costBestRoot.getMaxBoxWidth() + DomGraphLayoutParameters.fragmentXDistance;
		}
		
		// if there is exactly one root,
		// it's assumed that it should be placed centred.
		if(getFragmentGraphRoots().size() == 1) {
			Fragment root = getFragmentGraphRoots().get(0);
			if(getFragHoles(root).size() == 1) {
				int rootX = (absoluteCost.getMaxBoxWidth()
						- fragWidth.get(root)) /2;
				GraphModel model = graph.getModel();
				
				fragXpos.remove(root);
				fragXpos.put(root, new Integer(rootX));
				
				for (DefaultEdge edge : getFragOutEdges(root)) {
					GraphConstants.setLineColor(model.getAttributes(edge), new Color(255,204,230));
				}
				
				if(root.equals(bestRoot)) {
					movedRoot = root;
					yOffset = fragHeight.get(bestRoot) + fragmentYDistance;
					possibleOffset = fragWidth.get(root) + fragmentXDistance;
					
					for( Fragment frag : fragments ) {
						int newOffset = fragOffset.get(frag) 
						- possibleOffset;
						fragOffset.remove(frag);
						fragOffset.put(frag, newOffset);
					} 
				}
				
			}
		} 
		//System.out.println(bestRoot);
		graph.setBoundingBox(new Rectangle(absoluteCost.getMaxBoxWidth() - possibleOffset, absoluteCost.getMaxBoxHeight()));
		
		//boxdebugger.drawAll();
		//paintDebugger();
		
	}
	
	/**
	 * Computes the roots for this fragment graph (roots are all 
	 * the fragments with no incoming edges).
	 * @return the roots
	 */
	private List<Fragment> getFragmentGraphRoots() {
		List<Fragment> roots = new ArrayList<Fragment>();
		
		for(Fragment frag : fragments) {
			if(getFragInEdges(frag).size() == 0) {
				
					roots.add(frag);
				
			}	
			
		}
		return roots;
	}
	
	
	/**
	 * computes the position of all nodes considering
	 * their relative poitions within a fragment and the
	 * position of their fragment (cp. its fragment node).
	 */
	private void computeNodePositions() {
		
		
		for(DefaultGraphCell node : graph.getNodes() ) {
			
			Fragment nodeFrag = graph.findFragment(node);
			
			int x = relXtoRoot.get(node); 
			int offset = fragOffset.get(nodeFrag) - graph.computeNodeWidth(node)/2;
			int xMovement = fragXpos.get(nodeFrag)  + offset;
			
			/*
			 * the absolute x- position is the relative
			 * position added to the fragment's x-position
			 */
			x += xMovement ;
			
			xPos.put(node, x);
			
			/*
			 * the absolute y- position is the relative
			 * position added to the fragment's y-position
			 * and decreased by the minimal y-position (that
			 * moves the graph down to let it start by 0). 
			 */
			
			
			int y = relYpos.get(node);
			int yMovement = fragYpos.get(nodeFrag);
			y += yMovement;
			
			if(yOffset> 0 && (! nodeFrag.equals(movedRoot)))
				y += yOffset;
			
			yPos.put(node,y);
			
			
		}
	}
	
	/**
	 * computes the root of a fragment
	 * @param frag the fragment
	 * @return the root
	 */
	private DefaultGraphCell getFragRoot(Fragment frag) {
		
		//starting the search with a node contained
		//in the fragment
		DefaultGraphCell root = (DefaultGraphCell) 
		frag.getNodes().toArray()[0];
		
		/*
		 * (Fragment).getParent(DefaultGraphCell)
		 * returns null, if the node to check is the
		 * root (or not contained in the fragment, 
		 * we avoided that before).
		 * Otherwise we get a new (parent-)node to check.
		 */
		while(! (frag.getParent(root) == null)) {
			root = frag.getParent(root);
		}
		
		return root;
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
		for(DefaultGraphCell node : graph.getNodes() ) {
			int x = xPos.get(node).intValue();
			int y = yPos.get(node).intValue();
			
			placeNodeAt(node, x, y, viewMap);
		}
		
		
		//updating the graph.
		graph.getGraphLayoutCache().edit(viewMap, null, null, null);
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
	 * Starts the layout algorithm.
	 */
	public void run(JGraph gr, Object[] cells, int arg2) {
		
		computeFragDimensions();
		computeFragmentPositions();
		computeNodePositions();
		placeNodes();
	}
	
	
	
	/**
	 * @return Returns the nodesToDepth.
	 */
	private Map<DefaultGraphCell, Integer> getNodesToDepth() {
		return nodesToDepth;
	}
	
	
	/**
	 * @param nodesToDepth The nodesToDepth to set.
	 */
	private void setNodesToDepth(Map<DefaultGraphCell, Integer> nodesToDepth) {
		this.nodesToDepth = nodesToDepth;
	}
	
	
	/**
	 * @return Returns the nodesToShape.
	 */
	public Map<DefaultGraphCell, Shape> getNodesToShape() {
		return nodesToShape;
	}
	
	
	public Integer getRelXtoParent(DefaultGraphCell node) {
		return relXtoParent.get(node);
	}
	
	public void addRelXtoParent(DefaultGraphCell node, Integer x) {
		relXtoParent.put(node,x);
	}
	
	public void addRelYpos(DefaultGraphCell node, Integer y) {
		relYpos.put(node,y);
	}
	
	/**
	 * 
	 * @param node
	 * @return the width of the node
	 */
	public int getNodeWidth(DefaultGraphCell node) {
		return graph.computeNodeWidth(node);
	}
	
	public Shape getNodesToShape(DefaultGraphCell node) {
		return nodesToShape.get(node);
	}
	
	public void addRelXtoRoot(DefaultGraphCell node, Integer x) {
		relXtoRoot.put(node,x);
	}
	
	/**
	 * @return Returns the relXtoRoot.
	 */
	public Map<DefaultGraphCell, Integer> getRelXtoRoot() {
		return relXtoRoot;
	}
	
	
}


