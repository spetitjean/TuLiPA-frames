/*
 * @(#)PermutabilityRedundancyElimination.java created 10.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.equivalence;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.saar.chorus.domgraph.chart.Split;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.NodeLabels;


/**
 * An implementation of the redundancy elimination algorithm
 * which checks whether a subgraph has a single permutable split.
 * This is the bases of the Koller & Thater ICoS 06 submission.
 * If a subgraph has a permutable split, then all other splits
 * are redundant in the sense of the 
 * {@link de.saar.chorus.domgraph.equivalence.IndividualRedundancyElimination}
 * class, i.e. the redundancy elimination performed by this class
 * is a bit weaker than that of <code>IndividualRedundancyElimination</code>.
 * 
 * @author Alexander Koller
 *
 */
public class PermutabilityRedundancyElimination extends RedundancyElimination {

    public PermutabilityRedundancyElimination(DomGraph graph, NodeLabels labels, EquationSystem eqs) {
        super(graph, labels, eqs);
    }



    /**
     * Computes the irredundant splits of a subgraph. If the subgraph
     * has a single permutable split (i.e. a split that is permutable
     * with all other fragments), then this method returns a singleton
     * list containing this split. Otherwise, it returns the complete
     * list of splits for this subgraph.
     * 
     * @param subgraph a subgraph
     * @param splits the complete list of splits for this subgraph
     * @return a list of irredundant splits
     */
    public List<Split> getIrredundantSplits(Set<String> subgraph, List<Split> splits) {
        List<Split> ret = new ArrayList<Split>(1);
        
        for( Split split : splits ) {
            if( isPermutableSplit(split, subgraph)) {
                // i.e. the split with this index is permutable => eliminate all others
                ret.add(split);
                return ret;
            }
        }
        
        return splits;
    }


    private boolean isPermutableSplit(Split s, Set<String> subgraph) {
        String splitRoot = s.getRootFragment();
        
        //System.err.println("\nCheck split " + s + " for permutability.");
        
        for( String root : subgraph ) {
            if( graph.isRoot(root) &&  !root.equals(splitRoot) ) {
                if( isPossibleDominator(root, splitRoot)) {
                    if( !isPermutable(root, splitRoot) ) {
                        //System.err.println("  -- not permutable with " + root);
                        return false;
                    } else {
                        //System.err.println("  -- permutable with " + root);
                    }
                } else {
                    //System.err.println("  -- other root " + root + " is not a p.d.");
                }
            }
        }
        
        //System.err.println("  -- split is permutable!");
        return true;
    }
}
