package de.saar.chorus.ubench;

import org._3pq.jgrapht.Edge;

import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.NodeLabels;
import de.saar.chorus.domgraph.graph.NodeType;


/**
 * This converts a <code>DomGraph</code> to a
 * <code>JDomGraph</code>
 * 
 * @author Michaela Regneri
 *
 */
public class DomGraphTConverter {
	
	DomGraph domGraph;
	JDomGraph jDomGraph;
	
	
	/**
	 * Setting up a new converter with the 
	 * graph to convert and its labels.
	 * 
	 * @param graph the graph
	 * @param labels the lables belonging to the graph nodes
	 */
	public DomGraphTConverter(DomGraph graph, NodeLabels labels) {
		domGraph = graph;
		
		// create a new JDomGraph
		jDomGraph = new JDomGraph(graph);
		
		
		// insert all the nodes of the DomGraph
		for(String node : domGraph.getAllNodes() ) {
			NodeData cloneData;
			if( domGraph.getData(node).getType().equals(NodeType.LABELLED) ) {
				cloneData = new NodeData(de.saar.chorus.ubench.NodeType.labelled, 
						node, labels.getLabel(node), jDomGraph);
			} else {
				cloneData = new NodeData(de.saar.chorus.ubench.NodeType.unlabelled, 
						node, jDomGraph);
			}
			
			cloneData.addMenuItem(node, node);
			jDomGraph.addNode(node, cloneData);
		}
		
		// insert all the edges
		for(Edge edge :  domGraph.getAllEdges() ) {
			EdgeData cloneData;
			
			if( domGraph.getData(edge).getType().equals(de.saar.chorus.domgraph.graph.EdgeType.TREE)) {
				cloneData = new EdgeData(EdgeType.solid, edge.toString(), jDomGraph );
			} else {
				cloneData = new EdgeData(EdgeType.dominance, edge.toString(), jDomGraph );
			}
			
			cloneData.addMenuItem(edge.toString(), edge.toString());
			jDomGraph.addEdge(cloneData, jDomGraph.getNodeForName((String) edge.getSource()), 
					jDomGraph.getNodeForName((String) edge.getTarget()));
		}
	}
	
	/**
	 * Returns the <code>JDomGraph</code> created by 
	 * this converter.
	 * 
	 * @return the <code>JDomGraph</code>
	 */
	public JDomGraph getJDomGraph() {
		return jDomGraph;
	}
	
	
}
