/*
 * @(#)SplitSource.java created 03.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org._3pq.jgrapht.Edge;
import org.testng.annotations.Test;

import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.EdgeType;



/**
 * An abstract superclass for classes that compute splits. A {@link ChartSolver}
 * relies on an object of a subclass of this class to provide the splits
 * for a subgraph. You can provide your own subclass by implementing
 * the <code>computeSplits</code> method of this class. 
 * 
 * @author Alexander Koller
 *
 */
public abstract class SplitSource {
    protected DomGraph graph;
    /*
    protected List<String> potentialFreeRoots;
    protected List<Split> splits;
    protected Iterator<Split> splitIt;
    */
    
    public SplitSource(DomGraph graph) {
        this.graph = graph;
    }
    
    
    
    /**
     * Implement this abstract method when you write your own
     * subclass of <code>SplitSource</code>. The method gets a subgraph
     * as its argument, and has the task of computing an iterator
     * over the splits of this subgraph.<p>
     * 
     * @param subgraph a subgraph 
     * @return an iterator over some or all splits of this subgraph
     */
    abstract protected Iterator<Split> computeSplits(Set<String> subgraph);

    
    
    /**
     * Computes the list of all nodes in the subgraphs which have no
     * incoming edges. These nodes are candidates for being free roots;
     * however, you still need to check that the holes are in different
     * biconnected components. 
     * 
     * @param subgraph a subgraph
     * @return the list of nodes without in-edges in the subgraph
     */
    protected List<String> computePotentialFreeRoots(Set<String> subgraph) {
        // initialise potentialFreeRoots with all nodes without
        // incoming dom-edges
        List<String> potentialFreeRoots = new ArrayList<String>();
        for( String node : subgraph ) {
            if( graph.indegOfSubgraph(node, null, subgraph) == 0 ) {
                potentialFreeRoots.add(node);
            }
        }
        
        return potentialFreeRoots;
    }


    
    
    /**
     * A nested class for computing the split induced by a free root. 
     * 
     * @author Alexander Koller
     *
     */
    protected static class SplitComputer {
        private DomGraph graph;

        private String theRoot;
        private Set<String> rootFragment;
        
        // node in rootfrag -> edge in this node -> wcc
        private Map<String,Map<Edge,Set<String>>> splitmap;

        // node -> dom edge out of root frag by which this node is reached
        private Map<String,Edge> domEdgeForNodeWcc;
        
        // set of nodes already visited by DFS
        // this set is implemented as a field of the class in order
        // to save on object allocations
        private Set<String> visited;
        
                
        public SplitComputer(DomGraph graph) {
            this.graph = graph;
            rootFragment = new HashSet<String>();
            domEdgeForNodeWcc = new HashMap<String,Edge>();
            splitmap = new HashMap<String,Map<Edge,Set<String>>>();
            
            visited = new HashSet<String>();
        }
        
        
        
        /**
         * Performs a depth-first search through the graph which
         * determines the wccs of the split for the given node
         * and enters them into domEdgeForNodeWcc and splitmap.
         * The method returns true if this was successful, and
         * false if the given node was not the root of a free fragment.
         * <p>
         * INVARIANT: this method never visits a node in the root
         * fragment coming from outside the root fragment
         * <p>
         *  INVARIANT: this method is only called on unvisited nodes
         * 
         * @param node the root of a fragment
         * @param subgraph the subgraph for which we want to compute
         * the split
         * @param visited the set of nodes we already visited during 
         * this DFS
         * @return true iff the root was indeed free
         */
        private boolean dfs(String node, Set<String> subgraph, Set<String> visited) {
            // INVARIANT: this method is only called on unvisited nodes
            assert !visited.contains(node) : "DFS visited node twice";
            
            if( !subgraph.contains(node) ) {
                return true;
            }
            
            visited.add(node);
            
            // If node is not in the root fragment, then determine
            // its wcc set and assign it to it.
            if( !rootFragment.contains(node) ) {
                assert domEdgeForNodeWcc.containsKey(node);
                assignNodeToWcc(node);
            }
            
            // otherwise, iterate over all adjacent edges, visiting
            // tree edges first
            List<Edge> edgeList = graph.getAdjacentEdges(node, EdgeType.TREE);
            edgeList.addAll(graph.getAdjacentEdges(node, EdgeType.DOMINANCE));
            
            for( Edge edge : edgeList ) {
                String neighbour = (String) edge.oppositeVertex(node);
                
                if( rootFragment.contains(neighbour) && !rootFragment.contains(node)) {
                    // edge into the root fragment from outside: we never traverse
                    // such an edge, but must check whether the neighbour is
                    // consistent with the wcc assignment

                    // ASSUMPTION: edge is a dom edge from neighbour to node
                    assert node.equals(edge.getTarget());
                    assert neighbour.equals(edge.getSource());
                    assert graph.getData(edge).getType() == EdgeType.DOMINANCE;
                    
                    if( !neighbour.equals(theRoot)
                            && !neighbour.equals(domEdgeForNodeWcc.get(node).getSource()) ) {
                        // dom edge goes into a hole that is not the one we came from
                        return false;
                    }
                } else {
                    // any other edge -- let's explore it
                    if( !visited.contains(neighbour)) {
                        updateDomEdge(neighbour, node, edge);
                        
                        //System.err.println("Explore edge: " + edge);
                        
                        // recurse into children
                        if( !dfs(neighbour, subgraph, visited) ) {
                            return false;
                        }
                    }
                }
            }
            
            return true;
        }
        
        private void updateDomEdge(String neighbour, String node, Edge edge) {
            String src = (String) edge.getSource(); // not necessarily = node
            String tgt = (String) edge.getTarget();
            
            /*
            System.err.println("ude: " + neighbour + " from " + node + " via " + edge);
            System.err.println(graph.getData(edge).getType());
            System.err.println(rootFragment.contains(src));
            System.err.println(src == node);
            //System.err.println("src = /" + src);
             * */
            
            if( (graph.getData(edge).getType() == EdgeType.DOMINANCE)
                    && (src.equals(node))
                    && (rootFragment.contains(src)) ) {
                // dom edge out of fragment => initialise dEFNW
                // NB: If two dom edges out of the same hole point into the
                // same wcc, we will only ever use one of them, because the others'
                // target node will have been visited when we consider them.
                domEdgeForNodeWcc.put(tgt,edge);
                //System.err.println("put(" + tgt + "," + edge + ")");
            } else if( !rootFragment.contains(neighbour) ) {
                // otherwise make neighbours inherit my dEFNW
                domEdgeForNodeWcc.put(neighbour, domEdgeForNodeWcc.get(node));
                //System.err.println("inherit(" + neighbour + "," + domEdgeForNodeWcc.get(node));
            } else {
              //  System.err.println("Can't inherit domedge: " + edge);
            }
        }


        private void assignNodeToWcc(String node) {
            Edge dominatorEdge = domEdgeForNodeWcc.get(node);
            String dominator = (String) dominatorEdge.getSource();
            
            Map<Edge,Set<String>> thisMap = splitmap.get(dominator);
            if( thisMap == null ) {
                thisMap = new HashMap<Edge,Set<String>>();
                splitmap.put(dominator, thisMap);
            }
            
            Set<String> thisWcc = thisMap.get(dominatorEdge);
            if( thisWcc == null ) {
                thisWcc = new HashSet<String>();
                thisMap.put(dominatorEdge, thisWcc);
            }
            
            thisWcc.add(node);
        }


        /**
         * Compute the split induced by the free root of a subgraph.
         * The method assumes that the given root is a node without
         * incoming edges. It does _not_ assume that the root is actually
         * free, but if it isn't, the method will throw a <code>RootNotFreeException</code>.<p>
         * 
         * The runtime of this method is O(m+n) for a subgraph with m edges
         * and n nodes (it performs a single DFS through the graph).
         * 
         * @param root a node without incoming edges
         * @param subgraph a subgraph 
         * @return the split induced by this root, or null if the root
         * is not the root of a free fragment
         */
        public Split computeSplit(String root, Set<String> subgraph)
        {
            // initialise root fragment
            rootFragment.clear();
            rootFragment.add(root);
            rootFragment.addAll(graph.getChildren(root, EdgeType.TREE));
            theRoot = root;
            
            // perform DFS
            domEdgeForNodeWcc.clear();
            splitmap.clear();
            visited.clear();
            
            if( !dfs(root,subgraph, visited) ) {
                return null;
            }

            // build Split object
            Split ret = new Split(root);
            
            for( String dominator : splitmap.keySet() ) {
                for( Edge key : splitmap.get(dominator).keySet() ) {
                    ret.addWcc(dominator, splitmap.get(dominator).get(key));
                }
            }
            
            return ret;
        }
    }

    

        /*
         * unit tests:
         * 
         * - check whether computeSplit computes correct splits
         * - computeSplit returns null if root is not free
         */
}
