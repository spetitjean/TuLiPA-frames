/*
 * @(#)IndividualRedundancyElimination.java created 10.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.equivalence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.saar.chorus.domgraph.chart.Split;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.NodeLabels;

/**
 * An implementation of the redundancy elimination algorithm
 * which checks for each individual split whether it can be
 * removed. This is the basis of the Koller & Thater ACL 06
 * submission. It is generally stronger than the "permutable
 * splits" algorithm implemented in 
 * {@link de.saar.chorus.domgraph.equivalence.PermutabilityRedundancyElimination}.
 * 
 * @author Alexander Koller
 *
 */
public class IndividualRedundancyElimination extends RedundancyElimination {
    public IndividualRedundancyElimination(DomGraph graph, NodeLabels labels,
            EquationSystem eqs) {
        super(graph, labels, eqs);
    }

    /**
     * Computes the irredundant splits of a subgraph. A split is
     * <i>redundant</i> iff all of its solved forms are equivalent
     * to solved forms of some other split of the same subgraph.
     * This method starts with the complete list of splits of the
     * subgraph, and then successively removes redundant splits.
     * It returns the remaining (irredundant) splits.
     * 
     * @param subgraph a subgraph
     * @param allSplits the complete list of splits for this subgraph
     * @return a list of irredundant splits
     */
    public List<Split> getIrredundantSplits(Set<String> subgraph, List<Split> allSplits) {
        List<Split> splits = new ArrayList<Split>(allSplits);
        int i = 0;
        
        while( i < splits.size() ) {
            Split split = splits.get(i);
            
            if( isEliminableSplit(split, splits)) {
                splits.remove(split);
            } else {
                i++;
            }
        }
        
        return splits;
    }

    private boolean isEliminableSplit(Split split, List<Split> splitsForSubgraph) {
        Map<String,Set<String>> rootsToWccs = new HashMap<String,Set<String>>();
        String splitRoot = split.getRootFragment();
        Set<String> allRoots = graph.getAllRoots();
        
        // compute rootsToWccs
        for( Set<String> wcc : split.getAllSubgraphs() ) {
            for( String root : wcc ) {
                rootsToWccs.put(root, wcc);
            }
        }
        
        splitloop:
        for( Split otherSplit : splitsForSubgraph ) {
            if( !split.equals(otherSplit)) {
                String root = otherSplit.getRootFragment();
                Set<String> wcc = rootsToWccs.get(root);
                
                // check: is every other root in the same wcc, including splitRoot,
                // permutable with root?
                if( !isPermutable(root, splitRoot) ) {
                    continue;
                }
                
                for( String node : wcc ) {
                    if( allRoots.contains(node) && !root.equals(node) ) {
                        if( isPossibleDominator(node, root)) {
                            if( !isPermutable(root, node)) {
                                // if not, then continue
                                continue splitloop;
                            }
                        }
                    }
                }
                
                return true;
            }
        }
        
        return false;
    }
}
