/*
 *  File PolarityAutomaton.java
 *
 *  Authors:
 *     Johannes Dellert  <jdellert@sfs.uni-tuebingen.de> (initial version)
 *     Yannick Parmentier <parmenti@loria.fr> (modifications)
 *     
 *  Copyright:
 *     Johannes Dellert, 2008
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fri May 16 16:03:14 CEST 2008
 *
 *  This file is part of the Polarity Filter
 *
 *  The Polarity Filter is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The Polarity Filter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package fr.loria.disambiguation;

import java.util.*;

import de.tuebingen.anchoring.InstantiatedTagTree;

public class PolarityAutomaton
{  
    ArrayList<PolarityAutomatonState> states;
    String axiom;
    boolean verbose;
    private int leftRemoved   = 0;
    private int mergedRemoved = 0;
    private int totalStates   = 0;
    
    public PolarityAutomaton(List<String> listOfTokens, Map<String, List<InstantiatedTagTree>> tokens, String axiom, boolean verbose, List<String> lexicals, Map<String, List<String>> coancNodes, boolean withLeftContext)
    {
    	this.leftRemoved   = 0;
        this.mergedRemoved = 0;
        this.totalStates   = 0;
    	this.axiom = axiom;
    	this.verbose = verbose;
    	int statesNum = 0;
        states = new ArrayList<PolarityAutomatonState>();
        //build an initial state that will be the entry point for traversal
        states.add(new PolarityAutomatonState(new Polarities(), statesNum++));
        //previousStates will always contain all the states reached by processing a given token
        ArrayList<PolarityAutomatonState> previousStates = new ArrayList<PolarityAutomatonState>();
        // initialization of the automaton
        previousStates.add(states.get(0));
        //in newStates, the construction of the state frontier for the next token takes place 
        ArrayList<PolarityAutomatonState> newStates    = new ArrayList<PolarityAutomatonState>();
        // for merging states with equal polarities _for a given token_
        Map<Polarities, PolarityAutomatonState> merged = new HashMap<Polarities, PolarityAutomatonState>();
        //driver loop: we process all tokens
        // be careful: the order matters!
        for(String ptoken : listOfTokens)
        {
        	//System.err.println("Token " + ptoken + "\nTrees: ");
        	
        	//every state reached by the previous token must allow for processing the next token
            for (PolarityAutomatonState previousState : previousStates)
            {
            	//get all the trees for this token
            	HashSet<InstantiatedTagTree> trees = new HashSet<InstantiatedTagTree>();
            	if (tokens.containsKey(ptoken))
	                for (InstantiatedTagTree ttree : tokens.get(ptoken))
	                {
	                	//System.err.println(ttree.toString());
	                	trees.add(ttree);
	                }
            	
                // on top of the selected trees, we check the possibility to 
                // either be a lexical item in a tree or a coanchor (resource => polarity PLUS)
                if (lexicals.contains(ptoken)) {
                	trees.add(new InstantiatedTagTree("", new Polarities(ptoken, ptoken, Polarities.PLUS)));
                	//System.err.println("Added polarity + for item " + ptoken);
                }
                
                if (coancNodes.containsKey(ptoken)) {
                	Iterator<String> it = coancNodes.get(ptoken).iterator();
                	while (it.hasNext()) {
                		String cat = it.next();
                		trees.add(new InstantiatedTagTree("", new Polarities(cat, cat, Polarities.PLUS)));
                	}
                }
                              
                //build new states reachable by edges labeled with the tree ID 
                for (InstantiatedTagTree atree : trees)
                {
	                Polarities addedPolarities = atree.getPolarities();
	                //System.err.println("*** Polarities to add:\n" + addedPolarities);
	                Polarities newStatePolarities = Polarities.add(previousState.polarities, addedPolarities, withLeftContext);
	                //System.err.println("*** Previous State:\n" + previousState.polarities);
	                //System.err.println("*** New      State:\n" + newStatePolarities);
	                // if newStatePolarities is not null (i.e. the left constraint is respected), we build new states
	                if (newStatePolarities != null) {
	                	// Here we need to merge edges with identical polarities (automaton minimization)
	                	if (!merged.containsKey(newStatePolarities)) {
	                		// if the polarity has not been computed before
	                		// we create a new state for it
	                		PolarityAutomatonState newState = new PolarityAutomatonState(newStatePolarities, statesNum++);
	                		// and store this state both locally and globally (the former is for minimization, the latter for traversal)
	                		merged.put(newStatePolarities, newState);
		                	states.add(newState); // NB: the state can be later updated in terms of outgoing edges
		                	// we update the list of current states (only when a new state is created)
		                	newStates.add(merged.get(newStatePolarities));
	                	} else {
	                		mergedRemoved+=1;
	                	}
	                	// in all cases we update the in-going edges:
	                	int currentState = merged.get(newStatePolarities).getStateId();
	                	HashMap<Integer, List<String>> comingEdges = previousState.edges;
	                	if (!comingEdges.containsKey(currentState)) {
	                		// initialization (in case it is the first time we have a state with such a polarity)
	                		List<String> ls = new LinkedList<String>();
	                		comingEdges.put(currentState, ls);
	                	}
	                	// we add the current tree name to the edge leading to the current State (whose id is statesNum - 1)
	                	if (!comingEdges.get(currentState).contains(atree.getTreeName()))
	                		comingEdges.get(currentState).add(atree.getTreeName());
	                } else {
	                	leftRemoved+=1;
	                	if (verbose)
	                		System.err.println("Tree " + atree.getTreeName() + " discarded for token " + ptoken + " because of left context.");
	                }
                }
            }
            //go on to the next token
            previousStates = newStates;
            newStates = new ArrayList<PolarityAutomatonState>();
            merged    = new HashMap<Polarities, PolarityAutomatonState>();
        }
        totalStates = states.size();
        //print information about automaton in verbose mode
        //String type = (withLeftContext) ? "Left" : "Global";
        //System.err.println("\t@@ " + type + " polarity automaton size: " + states.size());
        if (verbose) 
        {
        	System.err.println("Polarity automaton size: " + states.size());
        	System.err.println(states.toString());
        }
    }
    
    public List<List<List<String>>> getPossibleSets()
    { 
        //the valid sets
        //NB: sets have following structure: [tupleID, tupleID, tupleID, ..., tupleID] 
        List<List<List<String>>> validSets = new ArrayList<List<List<String>>>(); 
        //all paths (FIFO queue)
        List<TreePath> nextState = new ArrayList<TreePath>();
        // initial state
        TreePath init  = new TreePath(0);
        nextState.add(init);
        //until we cannot build any further paths
        while (nextState.size() > 0)
        {
        	// we get the next state id to traverse
        	TreePath next = nextState.remove(0);
        	
        	//we retrieve the corresponding state:
            PolarityAutomatonState state = states.get(next.getState());

            // if we reached the end of an automaton path ...
            if (state.edges.size() == 0) 
            { 
            	//... we can check the polarities
            	boolean valid = true;
            	for (String label : state.polarities.getCharges().keySet())
            	{
            		if (state.polarities.getCharges().get(label) != null) 
            		{
            			//polarity of the axiom symbol must be +1
            			if (label.equals(axiom)) 
            			{
	                    	if (state.polarities.getCharges().get(label) != 1) {
                    			valid = false;
                    			break;
                    		}
                    	}
                    	//polarity of all other symbols must be neutral
                    	else 
                    	{
                    		if (state.polarities.getCharges().get(label) != 0) {
                    			valid = false;
                    			break;
                    		}
                        }
                	} 
                	else 
                	{
                		if (verbose) System.err.println("Label " + label + " has no polarities!");
                	}
                }

            	//if the polarities indicate a valid tree set
                if (valid) 
                {      	
                    validSets.add(next.getTrees());
                }
            }
            //we go on traversing the automaton to build complete paths
            for (int edge : state.edges.keySet())
            {  
            	TreePath newNext = new TreePath(edge);
            	List<String> trees = state.edges.get(edge);
            	newNext.copyTrees(next.getTrees());
            	newNext.add(trees);
            	nextState.add(newNext);
            }
        }
        return validSets;
    }

	public int getLeftRemoved() {
		return leftRemoved;
	}

	public int getMergedRemoved() {
		return mergedRemoved;
	}

	public int getTotalStates() {
		return totalStates;
	}

}

