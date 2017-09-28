/*
 *  File Derivations.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:00:19 CEST 2007
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
package de.tuebingen.parser;

import java.util.*;

import de.tuebingen.rcg.*;

public class Derivations {
	
	private int                                  index; //global index of steps
	private List<Step>                     deriv_steps;
	private Map<PredLabel, List<Step>>  stepByLhsLabel;
	private Map<PredLabel, List<Integer>>      nextone; //maps predlabel to clause ids
	
	public Derivations(){
		index          = 0;
		deriv_steps    = new LinkedList<Step>();
		stepByLhsLabel = new Hashtable<PredLabel, List<Step>>();
		nextone        = new Hashtable<PredLabel, List<Integer>>();
	}
	
	public Derivations(Derivations d){
		// constructor copying the nextone map
		// because the forest extraction algo is destructive 
		// for this structure (cf method remove)
		// this constructor allows to try several extractions
		index          = d.getIndex();
		deriv_steps    = d.getDeriv_steps();
		stepByLhsLabel = d.getStepByLhsLabel();
		nextone        = new Hashtable<PredLabel, List<Integer>>();
		Map<PredLabel, List<Integer>> nexts = d.getNextone();
		Set<PredLabel> spl = nexts.keySet();
		Iterator<PredLabel> it = spl.iterator();
		while(it.hasNext()){
			PredLabel pl = it.next();
			List<Integer> li  = nexts.get(pl);
			List<Integer> li2 = new LinkedList<Integer>();
			for(int i = 0 ; i < li.size() ; i++){
				li2.add(new Integer(li.get(i)));
			}
			nextone.put(pl, li2);
		}
	}
	
	public int getIndex(){
		int num = index;
		index++;
		return num;
	}
	
	/* delegate without probabilities */
	public void addOne(ClauseKey ck, RCG g) {
		addOne(ck,g,1);
	}

	public void addOne(ClauseKey ck, RCG g, float prob) {
		// we do no longer avoid duplicates, ie "if (!(deriv_steps.contains(ck)))" 
		// because we need them to extract the forest
	
		//System.err.println("\n ====> ADDING "+g.getClause(ck.getCindex()).toString()+ ", prob: " + f + "\n");
		
		Step s = new Step(ck, getIndex(), g.getClause(ck.getCindex()), prob);
		deriv_steps.add(s);
		
		// we also update the sorted list of clauses
		PredLabel     pl = g.getClause(ck.getCindex()).getLhs().getLabel();
		// NB: the tree clauses are indexed without taking 
		// the LPA into account (for forest extraction)
		PredLabel realpl = null;
		if (pl instanceof PredComplexLabel) {
			PredComplexLabel ppl = (PredComplexLabel) pl;
			if (ppl.getType() == PredComplexLabel.TREE) {
				realpl = new PredComplexLabel(PredComplexLabel.TREE, ppl.getTreeid(), ppl.getNodeid());
			} else {
				realpl = ppl;
			}
		} else {
			realpl = pl;
		}
		
		// we retrieve the list of steps and the list of positions
		List<Step>    sl = stepByLhsLabel.get(realpl);
		List<Integer> il = nextone.get(realpl);
		if (sl == null) {
			sl = new LinkedList<Step>();
			stepByLhsLabel.put(realpl, sl);
			// we initialize the 1st position
			il = new LinkedList<Integer>();
			nextone.put(realpl, il);
		}
		sl.add(s);
		il.add(new Integer(s.getId()));
	}

	public void addSteps(ClauseKey ck, Derivations d, RCG g) {
		// ck is an element that is not added
		List<Step> derivs = d.getDeriv_steps();
		// add derivations in the list
		for(int i = 0 ; i < derivs.size() ; i++) {
			if (ck == null || !(derivs.get(i).getCk().equals(ck))) {
				addOne(derivs.get(i).getCk(), g, derivs.get(i).getProb());
			}
		}
	}
	
	public int size(){
		return deriv_steps.size();
	}
	
	public Step get(int i){
		return deriv_steps.get(i);
	}

	public void moveNextones(int id){
		// moves all pointers in nextone up to id
		Set<PredLabel> keys    = nextone.keySet();
		Iterator<PredLabel> it = keys.iterator();
		while(it.hasNext()){
			PredLabel pl = it.next();
			while(this.getNextIdForLabel(pl) < id && this.getNextIdForLabel(pl) >= 0){
				this.setNextIdForLabel(pl);
			}
		}
	}
	
	public List<Step> getStepsForLabel(PredLabel label) {
		// this is used when extracting the forest
		// to retrieve the used clauses according to their LHS label
		return stepByLhsLabel.get(label);
	}
	
	public void setNextIdForLabel(PredLabel label) {
		List<Integer> li = nextone.get(label);
		li.remove(0); //pop the list
	}

	public Step getNextStepForLabel(PredLabel label) {
		// retrieve the next clause to be processed 
		// for a given label (AND UPDATE THE INDEX) 
		Integer index = nextone.get(label).get(0);
		nextone.get(label).remove(0);
		return deriv_steps.get(index);
	}
	
	public int getNextIdForLabel(PredLabel label) {
		// retrieves the id of the next clause to be processed 
		// for a given label (BUT DOES NOT CHANGE THIS ID)
		if (!(nextone.get(label).isEmpty())) {
			return nextone.get(label).get(0);	
		} else {
			return -1;
		}
	}

	public boolean hasNext(int curIndex, PredLabel branching, PredLabel tree){
		// we retrieve the next branching's step id
		int    next = this.getNextIdForLabel(branching);
		// we retrieve the next tree's step id
		int   ntree = this.getNextIdForLabel(tree);
		// if the next id is not bigger than the current 
		// AND if the next id is not lesser than the next tree id 
		return (next > curIndex && (ntree == -1 || next < ntree));	
	}
	
	public List<Step> getDeriv_steps() {
		return deriv_steps;
	}
	
	public String toString(){
		String res ="";
		for (int i = 0 ; i < deriv_steps.size() ; i++) {
			res += deriv_steps.get(i).toString();
			if (deriv_steps.get(i).getProb() != -1) {
				res += " " + deriv_steps.get(i).toString();
			}
			res += " \n";
		}
		return res;
	}

	public Map<PredLabel, List<Step>> getStepByLhsLabel() {
		return stepByLhsLabel;
	}

	public Map<PredLabel, List<Integer>> getNextone() {
		return nextone;
	}
}
