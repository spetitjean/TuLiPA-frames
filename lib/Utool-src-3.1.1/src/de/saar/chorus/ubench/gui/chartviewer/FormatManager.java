package de.saar.chorus.ubench.gui.chartviewer;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.util.JGraphUtilities;

import de.saar.chorus.domgraph.chart.Split;
import de.saar.chorus.ubench.EdgeType;
import de.saar.chorus.ubench.Fragment;
import de.saar.chorus.ubench.JDomGraph;
import de.saar.chorus.ubench.NodeType;

/**
 * This is a class providing methods to mark Splits and Subgraphs
 * via a <code>ChartViewer</code>. It manages the colors involved
 * and does the marking itself in a <color>JDomGraph</code>.
 * Further this class provides methods to get a HTML representation
 * of a Split oder a subgraph so as to color the parts of the String
 * according to the marking in the main window.
 * 
 * @author Michaela Regneri
 *
 */
public class FormatManager {
	
	// some constants
	private final static Color standardNodeForeground = Color.BLACK;
	
	private final static Color standardNodeBackground = Color.WHITE;
	
	private final static Font standardNodeFont = GraphConstants.DEFAULTFONT
													.deriveFont(Font.PLAIN, 17);
	
	private final static Font markedNodeFont = GraphConstants.DEFAULTFONT
													.deriveFont(Font.BOLD, 17);
	
	private final static Color standardSolidEdgeColor = Color.BLACK;
	
	private final static float standardSolidEdgeWidth = 1.7f;
	
	private final static Color standardDomEdgeColor = Color.RED;
	
	private final static float standardDomEdgeWidth = 1.2f;
	
	private final static float markedEdgeWidth = 2.5f;
	
	private final static Color deactivatedColor = Color.LIGHT_GRAY;
	
	private final static Color rootcolor = new Color(0, 204, 51);
	private final static Color[] subgraphcolors = {
		Color.BLUE, new Color(163, 0, 163),
		new Color(255,153,51), new Color(255,51,51),
		Color.CYAN		
	};
	
	// keeping track of the last subgraph color used.
	private static int subgraphcolorindex = 0;
	
	// storing html representations calculated before.
	private static Map<Split,String> splitToMarkedHTML = 
		new HashMap<Split,String>();
	
	private static Map<Set<String>,String> subgraphToMarkedHTML = 
		new HashMap<Set<String>,String>();	
	
	
	
	/**
	 * Mark a node with the given color
	 * 
	 * @param node
	 * @param color
	 */
	private static void markNode(DefaultGraphCell node, Color color,
			JDomGraph graph, Font font) {
		GraphConstants.setForeground(graph.getModel().getAttributes(node),
				color);
		GraphConstants.setFont(graph.getModel().getAttributes(node), font);
		
	}
	
	/**
	 * Mark an edge with the given color
	 * 
	 * @param edge
	 * @param color
	 */
	private static void markEdge(DefaultEdge edge, Color color, JDomGraph graph,
			float width) {
		GraphConstants
		.setLineColor(graph.getModel().getAttributes(edge), color);
		GraphConstants
		.setLineWidth(graph.getModel().getAttributes(edge), width);
	}
	
	/**
	 * Reset a node's color to the default color
	 * 
	 * @param node
	 * @param graph the graph the node belongs to
	 */
	private static void unmarkNode(DefaultGraphCell node, JDomGraph graph) {
		
		GraphModel model = graph.getModel();
		
		GraphConstants.setForeground(model.getAttributes(node),
				standardNodeForeground);
		GraphConstants.setBackground(model.getAttributes(node),
				standardNodeBackground);
		GraphConstants.setFont(model.getAttributes(node), standardNodeFont);
	}
	
	/**
	 * Reset an edge's color to the default color
	 * 
	 * @param edge
	 * @param graph the graph the edge belongs to
	 */
	private static void unmarkEdge(DefaultEdge edge, JDomGraph graph) {
		
		GraphModel model = graph.getModel();
		EdgeType type = graph.getEdgeData(edge).getType();
		boolean isSolid = type == EdgeType.solid;
		
		if (isSolid) {
			GraphConstants.setLineColor(model.getAttributes(edge),
					standardSolidEdgeColor);
			GraphConstants.setLineWidth(model.getAttributes(edge),
					standardSolidEdgeWidth);
		} else {
			GraphConstants.setLineColor(model.getAttributes(edge),
					standardDomEdgeColor);
			GraphConstants.setLineWidth(model.getAttributes(edge),
					standardDomEdgeWidth);
		}
		
	}
	
	/**
	 * Reset a whole graph's color to the default
	 * colors.
	 * 
	 * @param graph the graph
	 */
	public static void unmark(JDomGraph graph) {
		
		for (DefaultEdge edge : graph.getEdges()) {
			unmarkEdge(edge, graph);
		}
		for (DefaultGraphCell node : graph.getNodes()) {
			unmarkNode(node, graph);
		}
		
		refreshGraphLayout(graph);
	}
	
	
	/**
	 * Mark a subgraph of the given graph according
	 * to the index with the next mark-up color.
	 * 
	 * @param roots the roots of the fragments to mark
	 * @param graph	the graph
	 * @param subgraphindex the index indicating which color index to use
	 */
	public static void markSubgraph(Set<String> roots, JDomGraph graph, int subgraphindex) {
		int color = subgraphindex % subgraphcolors.length;
		
		markSubgraph(roots, subgraphcolors[color], graph, false);
	}
	
	/**
	 * Mark a subgraph of the given graph
	 * with the default marking color.
	 * 
	 * @param roots
	 * @param graph
	 */
	public static void markSubgraph(Set<String> roots, JDomGraph graph) {
		
		markSubgraph(roots, subgraphcolors[subgraphcolorindex],
				graph, true);
		
		refreshGraphLayout(graph);
		
	}
	
	/**
	 * Generates a HTML string to represent a subgraph.
	 * This "colors" the html string according to the colors
	 * that would be used to mark the nodes in the "real"
	 * graph.
	 * 
	 * @param subgraph the set of nodes to mark
	 * @return a HTML representation of the colored nodes
	 */
	public static String getHTMLforMarkedSubgraph(Set<String> subgraph) {
		if( subgraphToMarkedHTML.containsKey(subgraph) ) {
			return subgraphToMarkedHTML.get(subgraph);
		} else {
			String htmlsubgraph = "<html><font color=\"#" + 
			Integer.toHexString(subgraphcolors[subgraphcolorindex].getRGB()).substring(2) +
			"\"><b>" + subgraph + "</b></font></html>";
			
			subgraphToMarkedHTML.put(subgraph, htmlsubgraph);
			return htmlsubgraph;
		}
	}
	
	/**
	 * Generates a HTML string to represent a split.
	 * This "colors" the HTML string according to the colors
	 * that would be used to mark the split in the "real"
	 * graph.
	 * 
	 * @param split
	 * @param roots
	 * @return
	 */
	public static String getHTMLforMarkedSplit(Split split, Set<String> roots) {
		
		if( splitToMarkedHTML.containsKey(split) ) {
			return splitToMarkedHTML.get(split);
		} else {
			
			StringBuffer htmlsplit = new StringBuffer();
			
			htmlsplit.append("<html><b>&lt;<font color=\"#" + 
					Integer.toHexString(rootcolor.getRGB()).substring(2)
					+"\">" + split.getRootFragment() + "</font> ");
			
			boolean firsthole = true;
			for (String hole : split.getAllDominators()) {
				if(firsthole) {
					firsthole = false;
				} else {
					htmlsplit.append(", ");
				}
				htmlsplit.append("<font color=\"#" + 
						Integer.toHexString(subgraphcolors[subgraphcolorindex].getRGB()).substring(2)
						+ "\">" +hole +"=[</font>");
				List<Set<String>> x = new ArrayList<Set<String>>();
				
				boolean firstsubgraph = true;
				for (Set<String> wcc : split.getWccs(hole)) {
					if(firstsubgraph) {
						firstsubgraph = false;
					} else {
						htmlsplit.append(", ");
					}
					Set<String> copy = new HashSet<String>(wcc);
					copy.retainAll(roots);
					htmlsplit.append("<font color=\"#" + 
							Integer.toHexString(subgraphcolors[subgraphcolorindex].getRGB()).substring(2)
							+ "\">" + copy +"</font>");
					x.add(copy);
					if( ++subgraphcolorindex >= subgraphcolors.length) {
						subgraphcolorindex = 0;
					}
				}
				subgraphcolorindex--;
				htmlsplit.append("<font color=\"#" + 
						Integer.toHexString(subgraphcolors[subgraphcolorindex].getRGB()).substring(2)
						+ "\">]</font>");
				if( ++subgraphcolorindex >= subgraphcolors.length) {
					subgraphcolorindex = 0;
				}
			}
			
			
			htmlsplit.append("&gt;");
			htmlsplit.append("</b></html>");
			subgraphcolorindex = 0;
			
			splitToMarkedHTML.put(split,htmlsplit.toString());
			return htmlsplit.toString();
		}
	}
	
	/**
	 * Mark a subgraph with the given color.
	 * 
	 * @param roots the roots of the fragments to color
	 * @param color the color
	 * @param graph the parent graph
	 * @param shadeRemaining if set to true the remaining graph is colored grey.
	 */
	private static void markSubgraph(Set<String> roots, Color color, 
			JDomGraph graph, boolean shadeRemaining) {
		
		if(shadeRemaining) {
			shadeGraph(graph);
			
		}
		
		Set<Fragment> toMark = new HashSet<Fragment>();
		for (String otherNode : roots) {
			
			DefaultGraphCell gc = graph.getNodeForName(otherNode);
			if (graph.getNodeData(gc).getType() != NodeType.unlabelled) {
				Fragment frag = graph.findFragment(gc);
				toMark.add(frag);
			} else {
				markNode(gc, color, graph, markedNodeFont);
			}
			
			for (DefaultEdge edg : graph.getOutEdges(gc)) {
				
				if (graph.getEdgeData(edg).getType() == EdgeType.dominance) {
					markEdge(edg, color, graph, markedEdgeWidth);
					Fragment tgt = graph.getTargetFragment(edg);
					if (tgt != null) {
						toMark.add(tgt);
					}
				} else {
					markEdge(edg, color, graph, markedEdgeWidth);
				}
				
			}
		}
		
		for (Fragment frag : toMark) {
			for (DefaultGraphCell gc : frag.getNodes()) {
				markNode(gc, color, graph, markedNodeFont);
				for (DefaultEdge edg : graph.getOutEdges(gc)) {
					
					markEdge(edg, color, graph, markedEdgeWidth);
					
				}
			}
			
		}
		
	}
	
	/**
	 * Mark the whole graph with a given color.
	 * 
	 * @param color
	 * @param graph
	 */
	public static void markGraph(Color color, JDomGraph graph) {
		for (DefaultGraphCell node : graph.getNodes()) {
			markNode(node, color, graph, markedNodeFont);
		}
		
		for (DefaultEdge edge : graph.getEdges()) {
			markEdge(edge, color, graph, markedEdgeWidth);
		}
		
		refreshGraphLayout(graph);
	}
	
	
	/**
	 * Colors the graph in a light grey.
	 * 
	 * @param graph
	 */
	public static void shadeGraph(JDomGraph graph) {
		for (DefaultGraphCell node : graph.getNodes()) {
			markNode(node, deactivatedColor, graph, standardNodeFont);
		}
		
		for (DefaultEdge edge : graph.getEdges()) {
			if(graph.getEdgeData(edge).getType() == EdgeType.solid) {
				markEdge(edge, deactivatedColor, graph, standardSolidEdgeWidth);
			} else {
				markEdge(edge, deactivatedColor, graph, standardDomEdgeWidth);
			}
		}
	}
	
	/**
	 * This colors the root fragment of a split
	 * (which always gets a special color).
	 * 
	 * @param root the root fragment
	 * @param graph the parent graph
	 */
	public static void markRootFragment(Fragment root, JDomGraph graph) {
		for( DefaultGraphCell rfn : root.getNodes()) {
			markNode(rfn, rootcolor, graph, markedNodeFont);
			for( DefaultEdge edg : graph.getOutEdges(rfn) ) {
				markEdge(edg, rootcolor, graph, markedEdgeWidth);
			}
		}
	}
	
	/**
	 * This relayouts the graph so as to make the color
	 * changes visible. 
	 * 
	 * @param graph
	 */
	public static void refreshGraphLayout(JDomGraph graph) {
		JGraphUtilities.applyLayout(graph, new JDomGraphDummyLayout(graph));
	}
	
}
