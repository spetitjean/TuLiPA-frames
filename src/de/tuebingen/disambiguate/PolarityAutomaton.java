/*
 *  File PolarityAutomaton.java
 *
 *  Authors:
 *     Johannes Dellert  <jdellert@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Johannes Dellert, 2008
 *
 *  Last modified:
 *     Thu Feb 28 15:43:52 CET 2008
 *
 *  This file is part of the TuLiPA system
 *     http://www.sfb441.uni-tuebingen.de/emmy-noether-kallmeyer/tulipa
 *
 *  TuLiPA is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TuLiPA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.tuebingen.disambiguate;

import java.util.*;

public class PolarityAutomaton
{  
    ArrayList<PolarityAutomatonState> states;
    String axiom;
    boolean verbose;
    
    public PolarityAutomaton(List<String> toksentence, List<PolarizedToken> tokens, String axiom, boolean verbose, List<String> lexicals, Map<String, List<String>> coancNodes)
    {
    	this.axiom = axiom;
    	this.verbose = verbose;
    	int statesNum = 0;
        states = new ArrayList<PolarityAutomatonState>();
        //build an initial state that will be the entry point for traversal
        states.add(new PolarityAutomatonState(new Polarities(), statesNum++, ""));
        //previousStates will always contain all the states reached by processing a given token
        ArrayList<PolarityAutomatonState> previousStates = new ArrayList<PolarityAutomatonState>();
        previousStates.add(states.get(0));
        //in newStates, the construction of the state frontier for the next token takes place 
        ArrayList<PolarityAutomatonState> newStates = new ArrayList<PolarityAutomatonState>();
        //driver loop: we process all tokens
        int tokNum = 0;
        for (String tok : toksentence)//(PolarizedToken token : tokens)
        {
        	
        	PolarizedToken token = tokens.get(tokNum);
        	tokNum+=1;
            //every state reached by the previous token must allow for processing the next token
            for (PolarityAutomatonState previousState : previousStates)
            {
                //get all the tuples for this token
            	HashSet<PolarizedTuple> tuples = new HashSet<PolarizedTuple>();       	
                for (String lemmaID : token.getLemmas().keySet())
                {
                    PolarizedLemma lemma = token.getLemmas().get(lemmaID);
                    for (PolarizedTuple tuple: lemma.getTuples().values())
                    {
                    	tuples.add(tuple);
                    }
                }
                // on top of the tuples, we check the possibility to be 
                // either a lexical item in a tree or a coanchor
                if (lexicals.contains(tok))
                	tuples.add(new PolarizedTuple("", new Polarities(token.getToken(), token.getToken(), Polarities.PLUS)));
                
                if (coancNodes.containsKey(tok)) {
                	Iterator<String> it = coancNodes.get(token.getToken()).iterator();
                	while (it.hasNext()) {
                		String cat = it.next();
                		tuples.add(new PolarizedTuple("", new Polarities(cat, cat, Polarities.PLUS)));
                	}
                }
                
                //build new states reachable by edges labeled with the tuple IDs
                for (PolarizedTuple tuple : tuples)
                {
	                Polarities addedPolarities = tuple.getPol();
	                Polarities newStatePolarities = Polarities.add(previousState.polarities, addedPolarities);
	                PolarityAutomatonState newState = new PolarityAutomatonState(newStatePolarities, statesNum++, tuple.getTupleID());
	                states.add(newState);
	                newStates.add(newState);
	                previousState.edges.put(tuple.getTupleID(),states.size() - 1);
                }
            }
            //go on to the next token
            previousStates = newStates;
            newStates = new ArrayList<PolarityAutomatonState>();
        }
        //print information about automaton in verbose mode
        //System.err.println("\t@@ Global polarity automaton size: " + states.size());
        if (verbose) 
        {
        	System.err.println("Polarity automaton size: " + states.size());
        	//System.err.println(this.toString()); // Be careful: when the automaton gets big, this can make java run out of memory!
        }
    }
    
    public List<List<String>> getPossibleTupleSets()
    { 
        //the valid paths
        List<List<String>> validTupleSets = new ArrayList<List<String>>(); 
        //all paths (FIFO queue)
        List<List<String>> processedTupleSets = new ArrayList<List<String>>();
        //paths have following structure: [tupleID, tupleID, tupleID, ..., stateID]
        //initial path (trigger) starts at starting state
        ArrayList<String> firstTupleSet = new ArrayList<String>();             
        firstTupleSet.add("0");
        processedTupleSets.add(firstTupleSet);
        //until we cannot build any further paths
        while (processedTupleSets.size() > 0)
        {
            //store how many incomplete tuple sets where on the FIFO queue
            int numberProcessed = processedTupleSets.size();
            for (int i = 0; i < numberProcessed; i++)
            {
            	//we retrieve the next tuple set in the FIFO:
                List<String> nextTupleSet = processedTupleSets.remove(0);
                //we retrieve the corresponding state:
                // but first we save a copy (c.f. destructive traversal)
                List<String> tupleSet = new ArrayList<String>();
                for(String s : nextTupleSet) {
                	tupleSet.add(s);
                }
                PolarityAutomatonState state = states.get(Integer.parseInt(nextTupleSet.remove(nextTupleSet.size() - 1)));
                // if. we reached the end of an automaton path ...
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
                    //if the polarities indicate a valid tuple set
                    if (valid) 
                    {      	
                        //delete last staple ID
                        tupleSet.remove(tupleSet.size() - 1);
                        //tuple IDs remaining on list constitute a valid set of tuples
                        validTupleSets.add(tupleSet);
                    }
                }
                //we go on traversing the automaton to build complete paths
                for (String edge : state.edges.keySet())
                {  
                    //a new path is built and initialized with the path leading to the current state
                    List<String> newSet = new ArrayList<String>();
                    for (String s : nextTupleSet)
                    {
                        newSet.add(s);
                    }
                    //the next tuple ID is added...
                    newSet.add(edge);
                    //... and the state we end up in
                    newSet.add(state.edges.get(edge).toString());
                    //the extended path is added to the queue
                    processedTupleSets.add(newSet);
                }
            }
        }
        return validTupleSets;
    }
    
    public String toString() 
    {
    	String res = "";
    	for (int i = 0 ; i < states.size() ; i++) 
    	{
    		res += states.get(i).toString();
    	}
    	return res;
    }
}
