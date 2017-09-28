/*
 * @(#)Chart.java created 25.01.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.chart;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org._3pq.jgrapht.util.ModifiableInteger;


/**
 * A chart for storing intermediate results of the graph-chart solver.
 * This data structure assigns to a weakly connected subgraph G of the original 
 * dominance graph a list of splits for G. A split records the choice of a fragment F
 * of G as the root fragment of a solved form of G, and how the other
 * fragments of G must be distributed over the holes of F. That is, it
 * splits G into a root fragment F and the weakly connected components
 * that remain after F is removed.<p>
 *  
 * This class supports the dynamic addition and removal of splits and
 * subgraphs, and maintains the invariant that all subgraphs and splits
 * in the chart can be used in some solved form -- if they can't, they
 * are removed from the chart. It uses reference counters to keep track
 * of this; to initialise them, the user must specify one or more
 * subgraphs as "top-level" subgraphs, which receive a reference count
 * of 1. One important limitation is that it is not allowed to delete
 * a subgraph (or the last split in the subgraph) if this subgraph
 * is still referenced from elsewhere. The relevant methods throw an
 * UnsupportedOperationException if you attempt this.
 * 
 * @author Alexander Koller
 *
 */
public class Chart implements Cloneable {
    private Map<Set<String>, List<Split>> chart;
    private Map<Set<String>, ModifiableInteger> refcount;
    private Map<Set<String>, BigInteger> numSolvedForms;
    private int size;
    private List<Set<String>> toplevelSubgraphs;


    /**
     * The constructor.
     */
    public Chart() {
        chart = new HashMap<Set<String>, List<Split>>();
        refcount = new HashMap<Set<String>, ModifiableInteger>();
        numSolvedForms = new HashMap<Set<String>, BigInteger>();
        toplevelSubgraphs = new ArrayList<Set<String>>();
        size = 0;
    }
    
    /**
     * Adds a split for the given subgraph.
     * 
     * @param subgraph a subgraph of some dominance graph
     * @param split a split of this subgraph
     */
    public void addSplit(Set<String> subgraph, Split split) {
        List<Split> splitset = chart.get(subgraph);
        if( splitset == null ) {
            splitset = new ArrayList<Split>();
            chart.put(subgraph, splitset);
            
        }
        
        // add split to the chart
        splitset.add(split);
        
        // update reference counts
        for( Set<String> subsubgraph : split.getAllSubgraphs() ) {
            incReferenceCount(subsubgraph);
        }
        
        size++;
    }
    
    /**
     * Increments the reference count for the given subgraph.
     * 
     * @param subgraph a subgraph
     */
    private void incReferenceCount(Set<String> subgraph) {
        if( refcount.containsKey(subgraph)) {
            ModifiableInteger x = refcount.get(subgraph);
            x.setValue(x.intValue()+1);
        } else {
            refcount.put(subgraph, new ModifiableInteger(1));
        }
    }
    
    /**
     * Decrements the reference count for the given subgraph.
     * 
     * @param subgraph a subgraph
     */
    private void decReferenceCount(Set<String> subgraph) {
        assert(refcount.containsKey(subgraph));
        
        ModifiableInteger x = refcount.get(subgraph);
        x.setValue(x.intValue()-1);
    }
    
    private int getReferenceCount(Set<String> subgraph) {
        return refcount.get(subgraph).getValue();
    }
    
    public int countSubgraphs() {
    	return chart.size();
    }

    /**
     * Sets the splits for a given subgraph. If the subgraph already
     * had splits, these are deleted first. 
     * 
     * @param subgraph a subgraph
     * @param splits the new splits for this subgraph
     * @throws UnsupportedOperationException - if you try to delete all splits
     * of a subgraph that is still referenced from some other split. If this 
     * happens, the chart remains unchanged.
     */
    public void setSplitsForSubgraph(Set<String> subgraph, List<Split> splits) {
        Set<Set<String>> subgraphsAllSplits = new HashSet<Set<String>>();
        List<Split> oldSplits = getSplitsFor(subgraph);
        
        numSolvedForms.clear();
        
        if( splits.isEmpty() && (getReferenceCount(subgraph) > 0)) {
            throw new UnsupportedOperationException("The subgraph is still referenced " 
                    + getReferenceCount(subgraph) + " times. You may not remove its last split.");
        }
        
        // update reference count effects of deleting the old splits
        for( Split oldSplit : oldSplits ) {
            List<Set<String>> subsubgraphs = oldSplit.getAllSubgraphs(); 
            subgraphsAllSplits.addAll(subsubgraphs);
            
            for( Set<String> subsubgraph : subsubgraphs ) {
                decReferenceCount(subsubgraph);
            }
        }
        
        // delete the old splits
        size -= oldSplits.size();
        oldSplits.clear();
        
        
        // add the new split
        for( Split split : splits ) {
            addSplit(subgraph, split);
        }
        
        // remove subgraphs with zero reference count from the chart
        for( Set<String> s : subgraphsAllSplits ) {
            if( getReferenceCount(s) == 0 ) {
                deleteSubgraph(s);
            }
        }
    }

    
    
    /**
     * Deletes a subgraph and all of its splits from the chart. This method
     * updates the reference counts, and recursively deletes all other subgraphs that
     * become unreachable.
     * 
     * @param subgraph a subgraph
     * @throws UnsupportedOperationException - if you try to delete a subgraph
     * that is still referenced from some split. If this happens, the chart
     * remains unchanged.
     * 
     */
    public void deleteSubgraph(Set<String> subgraph) {
        List<Split> splits = getSplitsFor(subgraph);
        
        if( getReferenceCount(subgraph) > 0 ) {
            throw new UnsupportedOperationException("The subgraph is still referenced "
                    + getReferenceCount(subgraph) + " times. You may not delete it.");
        }
        
        // update reference counts for referred sub-subgraphs and
        // recursively delete those if they drop to zero
        for( Split split : splits ) {
            for( Set<String> subsubgraph : split.getAllSubgraphs() ) {
                decReferenceCount(subsubgraph);
                
                if( getReferenceCount(subsubgraph) == 0 ) {
                    deleteSubgraph(subsubgraph);
                }
            }
        }
        
        size -= splits.size();
        splits.clear(); // TODO - or perhaps delete the subgraph altogether?
    }
    

    /**
     * Returns the list of all splits for the given subgraph.
     * 
     * @param subgraph a subgraph
     * @return the list of splits for this subgraph.
     */
    public List<Split> getSplitsFor(Set<String> subgraph) {
        return chart.get(subgraph);
    }
    
    /**
     * Checks whether the chart contains a split for the given subgraph.
     * 
     * @param subgraph a subgraph
     * @return true iff the chart contains any splits for this subgraph.
     */
    public boolean containsSplitFor(Set<String> subgraph) {
        return chart.containsKey(subgraph);
    }
    

    /**
     * Returns the number of splits in the entire chart.
     * 
     * @return the number of splits
     */
    public int size() {
        return this.size;
    }
    
    /**
     * Returns a string representation of the chart.
     */
    public String toString() {
        StringBuilder ret = new StringBuilder();
        
        for( Set<String> fragset : chart.keySet() ) {
            for( Split split : chart.get(fragset) ) {
                ret.append(fragset.toString() + " -> " + split + "\n");
            }
        }
        
        return ret.toString();
    }
    

    /**
     * Returns the list of all top-level subgraphs.
     * 
     * @return the top-level subgraphs.
     */
    public List<Set<String>> getToplevelSubgraphs() {
        return toplevelSubgraphs;
    }

    /**
     * Adds a top-level subgraph. This should be one of the maximal
     * weakly connected subgraphs of the dominance graph; if the entire
     * graph is connected, then this should be the set of all nodes
     * of the dominance graph.
     * 
     * @param subgraph a weakly connected subgraph.
     */
    public void addToplevelSubgraph(Set<String> subgraph) {
        this.toplevelSubgraphs.add(subgraph);
    }
    
    
    /**
     * Returns the number of solved forms represented by this chart.
     * This method doesn't compute the solved forms themselves (and
     * is much faster than that), but it can take a few hundred
     * milliseconds for a large chart.<p>
     * 
     * The method assumes that the chart belongs to a solvable dominance
     * graph, i.e. that it represents any solved forms in the first place.
     * You can assume this for all charts that were generated by
     * ChartSolver#solve with a return value of <code>true</code>.
     * 
     * @return the number of solved forms
     */
    public BigInteger countSolvedForms() {
        BigInteger ret = BigInteger.ONE;
                
        for( Set<String> subgraph : getToplevelSubgraphs() ) {
            ret = ret.multiply(countSolvedFormsFor(subgraph, numSolvedForms));
        }
        
        return ret;
    }
    
    public BigInteger countSolvedFormsFor(Set<String> subgraph) {
    	return countSolvedFormsFor(subgraph, numSolvedForms);
    }

    private BigInteger countSolvedFormsFor(Set<String> subgraph, Map<Set<String>,BigInteger> numSolvedForms) {
        BigInteger ret = BigInteger.ZERO;
        
        if( numSolvedForms.containsKey(subgraph) ) {
            return numSolvedForms.get(subgraph);
        } else if( !containsSplitFor(subgraph) ) {
            // no split for subgraph => subgraph contains only one fragment
        	return BigInteger.ONE;
        } else {
            for( Split split : getSplitsFor(subgraph) ) {
                BigInteger sfsThisSplit = BigInteger.ONE;
                
                for( Set<String> subsubgraph : split.getAllSubgraphs() ) {
                    sfsThisSplit = sfsThisSplit.multiply(countSolvedFormsFor(subsubgraph, numSolvedForms));
                }
                
                ret = ret.add(sfsThisSplit);
            }
            
            numSolvedForms.put(subgraph, ret);
            return ret;
        }
    }
    

    /** 
     * Computes a clone of the chart. Splits and subgraphs can be added and deleted,
     * and toplevel subgraphs changed, on the clone without affecting
     * the original chart object. However, the clone contains referneces to the same
     * individual subgraphs and splits as the original chart, so be sure
     * not to modify the subgraphs and splits themselves. (This would be
     * a bad idea anyway.)
     * 
     * @return a <code>Chart</code> object which is a clone of the current
     * chart
     */
    @Override
    public Object clone() {
        Chart ret = new Chart();
        
        for( Map.Entry<Set<String>, List<Split>> entry : chart.entrySet() ) {
            ret.chart.put(entry.getKey(), new ArrayList<Split>(entry.getValue()));
        }
        
        for( Map.Entry<Set<String>, ModifiableInteger> entry : refcount.entrySet() ) {
            ret.refcount.put(entry.getKey(), new ModifiableInteger(entry.getValue().getValue()));
        }

        ret.size = size;
        
        ret.toplevelSubgraphs = new ArrayList<Set<String>>(toplevelSubgraphs);
        
        return ret;
    }
}



/*
 * UNIT TESTS:
 *  - clone is different object than original chart
 *  - maps in clone are equal
 *  - changing stuff in clone doesn't make a difference
 *  
 */