/*
 * @(#)ImprovedJGraphAdapter.java created 06.03.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.jgraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org._3pq.jgrapht.Edge;
import org._3pq.jgrapht.Graph;
import org._3pq.jgrapht.ext.JGraphModelAdapter;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;

 abstract public class ImprovedJGraphAdapter<NodeType,
                                    NodeData extends INodeData<NodeType>,
                                    EdgeType,
                                    EdgeData extends IEdgeData<EdgeType>> 
extends ImprovedJGraph<NodeType,NodeData,EdgeType,EdgeData> {
     protected Graph graph;
     
     protected Map<DefaultGraphCell,Object> cellsToNodes;
     protected Map<Object,DefaultGraphCell> nodesToCells;
     
     
     /**
      * Converts a JGraphT graph into an ImprovedJGraph. You can specify
      * the graph and a <code>DataFactory</code> which assigns node data
      * and edge data to the nodes and edges of the graph.<p>
      * 
      * Using this method is different than creating a new object of
      * class <code>ImprovedJGraphAdapter</code> in that this method
      * can put the information from the graph and data into objects
      * of any subclass of <code>ImprovedJGraph</code> that you like,
      * whereas converting via the constructors of this class just
      * creates objects of a particular subclass (namely
      * <code>ImprovedJGraphAdapter</code>.
      *  
      * @param <NodeType> a node type class
      * @param <NodeData> a class representing node data
      * @param <EdgeType> an edge type class
      * @param <EdgeData> a class representing edge data
      * @param graph a JGraphT graph
      * @param factory a factory providing data for each node and edge
      * @param jgraph the <code>ImprovedJGraph</code> object that should
      * be filled with the information in the <code>graph</code>
      * @return a mapping of graph nodes to cells in the <code>ImprovedJGraph</code>.
      */
    public static 
     <NodeType, NodeData extends INodeData<NodeType>, EdgeType, EdgeData extends IEdgeData<EdgeType>>
     Map<Object,DefaultGraphCell>
     convert(Graph graph, DataFactory<NodeData,EdgeData> factory,
             ImprovedJGraph<NodeType,NodeData,EdgeType,EdgeData> jgraph) {
         Map<Object,DefaultGraphCell> nodesToCells = new HashMap<Object,DefaultGraphCell>();
         Map<Object,Set<Object>> seenEdges = new HashMap<Object,Set<Object>>();
         
         jgraph.clear();
         
         // add nodes
         for( Object node : graph.vertexSet() ) {
             DefaultGraphCell cell = jgraph.addNode(node.toString(), factory.getNodeData(node));
             
             nodesToCells.put(node, cell);
         }

         // add edges
         for( Object _edge : graph.edgeSet() ) {
             Edge edge = (Edge) _edge;
             Object srcNode = edge.getSource();
             Object tgtNode = edge.getTarget();
             Set<Object> seenThisSrc = seenEdges.get(srcNode);
             
             if( seenThisSrc == null ) {
                 seenThisSrc = new HashSet<Object>();
                 seenEdges.put(srcNode, seenThisSrc);
             }
             
             if( !seenThisSrc.contains(tgtNode)) {
                 seenThisSrc.add(tgtNode);
                 
                 DefaultGraphCell src = nodesToCells.get(srcNode);
                 DefaultGraphCell tgt = nodesToCells.get(tgtNode);
                 
                 jgraph.addEdge(factory.getEdgeData(edge), src, tgt);
             }
         }
         
         jgraph.computeAdjacency();
         
         return nodesToCells;
     }

    
    public ImprovedJGraphAdapter(Graph graph, DataFactory<NodeData,EdgeData> factory) {
        super();
        
        this.graph = graph;
        nodesToCells = convert(graph,factory,this);
        
        cellsToNodes = new HashMap<DefaultGraphCell,Object>();
        for( Map.Entry<Object,DefaultGraphCell> entry : nodesToCells.entrySet() ) {
            cellsToNodes.put(entry.getValue(), entry.getKey());
        }
    }
    

    public ImprovedJGraphAdapter(Graph graph, final Map<Object,NodeData> nodedata, final Map<Edge,EdgeData> edgedata) {
        this(graph, new DataFactory<NodeData,EdgeData>() {
            public NodeData getNodeData(Object node) {
                return nodedata.get(node);
            }

            public EdgeData getEdgeData(Edge edge) {
                return edgedata.get(edge);
            }
        });
    }
    
    public Object getNodeForCell(DefaultGraphCell cell) {
        return cellsToNodes.get(cell);
    }
    
    public DefaultGraphCell getCellForNode(Object node) {
        return nodesToCells.get(node);
    }


    protected AttributeMap defaultNodeAttributes(NodeType type) {
        return JGraphModelAdapter.createDefaultVertexAttributes();
    }


    protected AttributeMap defaultEdgeAttributes(EdgeType type) {
        return JGraphModelAdapter.createDefaultEdgeAttributes(graph);
    }




}
