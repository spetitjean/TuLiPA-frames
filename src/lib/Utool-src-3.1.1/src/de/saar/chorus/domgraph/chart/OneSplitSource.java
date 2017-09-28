/*
 * @(#)OneSplitSource.java created 13.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.chart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.saar.chorus.domgraph.graph.DomGraph;

/**
 * A split source which only computes the first split for each
 * subgraph. A <code>ChartSolver</code> which uses a split source
 * of this class will not compute the complete set of solved forms.
 * However, it is still guaranteed to detect whether a graph is
 * unsolvable, and will be considerably faster than a solver that
 * uses a <code>CompleteSplitSource</code>. 
 * 
 * @author Alexander Koller
 *
 */
public class OneSplitSource extends SplitSource {

    public OneSplitSource(DomGraph graph) {
        super(graph);
    }

    protected Iterator<Split> computeSplits(Set<String> subgraph) {
        SplitComputer sc = new SplitComputer(graph);
        List<Split> ret = new ArrayList<Split>();
        List<String> potentialFreeRoots = computePotentialFreeRoots(subgraph);

        for( String root : potentialFreeRoots ) {
            Split split = sc.computeSplit(root, subgraph);
            
            if( split != null ) {
                ret.add(split);
                return ret.iterator();
            }
        }

        return ret.iterator();
    }

    public static boolean isGraphSolvable(DomGraph graph) {
        DomGraph cpt = graph.compactify();
        Chart chart = new Chart();
        
        return ChartSolver.solve(cpt, chart, new OneSplitSource(graph));
    }
}
