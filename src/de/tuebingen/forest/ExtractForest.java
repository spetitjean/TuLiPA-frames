/*
 *  File ExtractForest.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fr 18. Jan 19:44:44 CET 2008
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
package de.tuebingen.forest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tuebingen.parser.ClauseKey;
import de.tuebingen.parser.DNode;
import de.tuebingen.parser.DStep;
import de.tuebingen.parser.ForestExtractor;
import de.tuebingen.parser.ForestExtractorInitializer;
import de.tuebingen.rcg.PredComplexLabel;
import de.tuebingen.rcg.RCG;
import de.tuebingen.util.Pair;

public class ExtractForest extends ForestExtractor {
	
	private boolean                     verbose;
	private RCG                         grammar;
	private Map<Tidentifier, List<Rule>> forest;
	private List<ClauseKey>             answers;
	private Map<ClauseKey, DStep>        dtable;
	private Map<ClauseKey, TreeOp>         done;
	private List<Tidentifier>             start;
	
	public ExtractForest(boolean v, RCG g, List<ClauseKey> a, Map<ClauseKey, DStep> t){
		verbose = v;
		grammar = g;
		answers = a;
		dtable  = t;
		forest  = new HashMap<Tidentifier, List<Rule>>();
		done    = new HashMap<ClauseKey, TreeOp>();
		start   = new LinkedList<Tidentifier>();
	}
		
	public ExtractForest() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(boolean v, RCG g,
			ForestExtractorInitializer fi) {
		verbose = v;
		grammar = g;
		answers = (List<ClauseKey>) fi.poll();
		dtable  = (Map<ClauseKey, DStep>) fi.poll();
		forest  = new HashMap<Tidentifier, List<Rule>>();
		done    = new HashMap<ClauseKey, TreeOp>();
		start   = new LinkedList<Tidentifier>();
		
	}

	@Override
	public void extract(){
		for(int i = 0 ; i < answers.size() ; i++){
			if (verbose)
				System.err.println("Answer n°" + i);
			this.processClause(answers.get(i));
		}
	}
	
	public TreeOp processClause(ClauseKey ck){
		TreeOp top = null;
		if (verbose)
			System.err.println("Processing ... " + ck.toString());
		if (!done.containsKey(ck)) {
			PredComplexLabel lab = (PredComplexLabel) grammar.getClause(ck.getCindex()).getLhs().getLabel();
			int type = lab.getType();
			switch(type) {
			case PredComplexLabel.TREE:
				top = this.processTreeClause(ck);
				break;
			case PredComplexLabel.ADJ:
				top = this.processBranchingClause(ck);
				break;
			case PredComplexLabel.SUB:
				top = this.processBranchingClause(ck);
				break;
			case PredComplexLabel.START:
				DStep ds = dtable.get(ck);
				//System.err.println("first step: " + ds.toString());
				List<ClauseKey> lck = ds.getAllFirstCk();
				for(ClauseKey ckey : lck) {
					top = this.processClause(ckey);
					start.add(top.getId());
				}
				break;
			default: //skip
			}
			done.put(ck, top);
		} else {
			top = done.get(ck);
			if (verbose)
				System.err.println("****** tabulated " + top.toString());
		}
		return top;
	}
	
	public TreeOp processTreeClause(ClauseKey ck) {
		if (verbose)
			System.err.println("Processing (tree) ... " + ck.toString());
		TreeOp op = null;
		// 1. We extract the derivation steps from the tabulated items
		DStep ds = dtable.get(ck);
		// 2. We get the tree identity
		PredComplexLabel lab = (PredComplexLabel) grammar.getClause(ck.getCindex()).getLhs().getLabel();
		String treename  = lab.getTreeid();
		int num = ck.getCindex();
		// 3. We initialize the corresponding entry in the forest 
		Tidentifier id   = new Tidentifier(num, treename, ck.getArgs());
		op = new TreeOp(id);
		List<Rule> rules = new LinkedList<Rule>();
		forest.put(id, rules);
		// 4. We process the binding ambiguity (one rule per binding)
		if (ds != null) { // ds can be null when the Tree clause has no RHS, i.e. dtable does not contain ck
			Iterator<String> its = ds.iterator();
			while (its.hasNext()) {
				String binding = its.next();
				Rule rule = new Rule(id);
				rules.add(rule);
				if (verbose) 
					System.err.println("Binding processing ... " + binding);
				rule.setRhs(this.processBinding(ds.getCinstantiation().get(binding)));
				if (verbose)
					System.err.println("... for id: " + id.toString());
			}
		} else { // for forest processing, rules need (even empty) RHS
			Rule rule = new Rule(id);
			rules.add(rule);
		}
		if (verbose)
			System.err.println(ck.toString() + " ==> " + op.toString());
			
		return op;
	}
	
	public Combination processBinding(DNode[] ops) {
		// 1. We must filter the ops with clauses which are keys of the table
		List<DNode> realOps = new LinkedList<DNode>();
		for(int i = 0 ; i < ops.length ; i++){
			DNode dn = ops[i];
			//System.err.println("filtering ... " + dn.toString());
			ClauseKey ck = dn.getInstantiation();
			if (dtable.containsKey(ck))
				realOps.add(dn);
			else
				if (verbose)
					System.err.println("removed ... " + dn.toString() + " (empty RHS)");
			// -> thus the empty-RHS branching rules are removed from the bindings (i.e. they are not considered)
		}
		// 2. We process the predicates in the RHS 
		Combination combi = new Combination();
		for(int i = 0 ; i < realOps.size() ; i++) {
			TreeOp called = this.processDNode(realOps.get(i), null); 
			if (verbose)
				System.err.println("Added in RHS: " + called.toString());
			combi.addOp(called);
		}
		return combi;
	}
	
	public TreeOp processDNode(DNode dn, Pair opInfo) {
		//System.err.println("DNode: " + opInfo.toString());
		TreeOp top = null;
		ClauseKey ck1 = dn.getInstantiation();
		top = this.processClause(ck1);
		if (top.getType() != PredComplexLabel.TREE)
			top = new TreeOp(top); //if there already was a branching clause for this DNode (but maybe using another node) 
		if (opInfo != null) { //if the caller is a branching clause
			top.setType((Integer) opInfo.getKey());
			//System.out.println("Setting Node ID in ExtractForest: "+(String) opInfo.getValue());
			top.getId().setNodeId((String) opInfo.getValue());
		}
		if (dn.isAmbiguous()) {
			//System.err.println("___ ambig. " + dn.toString());
			top.setOr(this.processDNode(dn.getDerivStep(), opInfo));
		}
		return top;
	}
	
	public TreeOp processBranchingClause(ClauseKey ck) {
		if (verbose)
			System.err.println("Processing (branching) ... " + ck.toString());
		TreeOp top = null;
		// 1. We extract the derivation steps from the tabulated items
		DStep ds = dtable.get(ck);
		PredComplexLabel lab = (PredComplexLabel) grammar.getClause(ck.getCindex()).getLhs().getLabel();
		int type = lab.getType();
		String node = lab.getNodeid();
		//System.err.println(" *** Current operation: "+ lab.toString());
		Pair opInfo = new Pair(type, node);
		
		// 2. Branching clause are made of a single RHS predicate (but might have several bindings)
		Iterator<String> its = ds.iterator();
		while (its.hasNext()) {
			String binding = its.next();
			if (top != null)
				top.setOr(this.processDNode(ds.getFirstDNode(binding), opInfo));
			else
				top = this.processDNode(ds.getFirstDNode(binding), opInfo);
		}

		//System.err.println("... " + ck.toString() + " ==> " + top.toString());
		return top;
	}
	
	public Map<Tidentifier, List<Rule>> getForest() {
		return forest;
	}
	
	public List<Tidentifier> getStart() {
		return start;
	}

	public String printForest() {
		String res = "\n\nForest: \n(start ids: ";
		for (Tidentifier t : start) {
			res += t.getClauseId() + " ";
		}
		res += ")\n";
		Set<Tidentifier> keys   = forest.keySet();
		Iterator<Tidentifier> it= keys.iterator();
		while(it.hasNext()) {
			Tidentifier tid = it.next();
			res += tid.toString() + "\t";
			List<Rule>  l   = forest.get(tid);
			for(int i = 0 ; i < l.size() ; i++) {
				if (tid.getClauseId() < 10)
					res += "0" + tid.getClauseId() + " ";
				else
					res += tid.getClauseId() + " ";
				res += "--> ";			
				res += l.get(i).toString(); 
			}
		}
		return res;
	}

}
