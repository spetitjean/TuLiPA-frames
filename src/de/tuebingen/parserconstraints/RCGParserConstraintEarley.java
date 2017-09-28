/*
 *  File RCGParserEarley.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@loria.fr>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2008
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Mi 8. Okt 10:21:32 CET 2008
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

package de.tuebingen.parserconstraints;

import java.io.File;
import java.util.*;

import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.io.RCGReader;
import de.tuebingen.io.TextRCGReader;
import de.tuebingen.parser.*;
import de.tuebingen.rcg.*;
import de.tuebingen.tag.TagTree;
import de.tuebingen.tokenizer.BuiltinTokenizer;
import de.tuebingen.tokenizer.Tokenizer;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.tree.Grammar;
import de.tuebingen.util.Pair;

public class RCGParserConstraintEarley extends RCGParser {
	
	private int verbose = -1;
	
	// counts the items 
	private int itemcount = 0;

	private HashMap<String, ItemKey>  actives;
	private HashMap<String, ItemKey> passives;
	
	private Map<String,List<ConstraintItem>> activeByLabel;
	private LinkedList<ConstraintItem>              agenda;
	
	private Map<String, List<ConstraintItem>>        predP; // mapping between a predlabel and a list of predicted passive items (i.e. uncompleted items)
	private Map<String, List<ConstraintItem>>       parseF; // mapping between a predlabel and a list of completed passive item 
	private Map<Integer, List<int[]>>               forest; // mapping between the hash of a completed passive item
	                                                        // and the list of instantiated clauses (represented by hash of instantiated predicates)
	private Map<Integer, List<ClauseKey>>            links; // to store links between a hash and an item
	
	private List<Integer>                          answers; // roots of the derivation forest
	
	private Map<String, TagTree>                     gDict; // map giving the correspondance betweem tree id and original names 
	                                                        // used for adding constraints
	
	
	public RCGParserConstraintEarley(Grammar g) {
		super(g);
		activeByLabel = new HashMap<String,List<ConstraintItem>>();
		agenda        = new LinkedList<ConstraintItem>();
		actives       = new HashMap<String, ItemKey>();
		passives      = new HashMap<String, ItemKey>();
		predP         = new HashMap<String, List<ConstraintItem>>();
		parseF        = new HashMap<String, List<ConstraintItem>>();
		forest	      = new HashMap<Integer, List<int[]>>();
		links         = new HashMap<Integer, List<ClauseKey>>();
		answers       = new LinkedList<Integer>();
	}
	
	public boolean parseSentence(boolean v, List<Word> sentence) {
		verbose = (v) ? 0 : -2;
		return parse(sentence);
	}

	public boolean parse(List<Word> input) {
		boolean res = (this.recognize(input));
		//System.err.println(this.printForest());
		if (res && verbose > -1) {
			System.err.println(this.printForest());
		}
		return res;
	}

	public boolean recognize(List<Word> input) {
		List<Argument> al = new ArrayList<Argument>();
		al.add(Argument.argFromWord(input));
		if (verbose > 1) {
			System.err.println("parsing " + al.toString());
		}
		ConstraintItem axiom = new ConstraintItem();
		axiom.setPl(((RCG)super.getGrammar()).getStartPredicateLabel());
		ConstraintVector vect = new ConstraintVector(1); // 1 for there is 1 range variable (with 2 boundaries) 
		ArgContent dummy = new ArgContent(ArgContent.VAR, "X");
		Constraint c1 = new Constraint(Constraint.EQUALS, "X"+Constraint.LEFT, Constraint.VAL, 0, 0);
		Constraint c2 = new Constraint(Constraint.EQUALS, "X"+Constraint.RIGHT, Constraint.VAL, 0, input.size());
		vect.putVal("X"+Constraint.LEFT, 0);
		vect.putVal("X"+Constraint.RIGHT, input.size());
		vect.addConstraint(c1);
		vect.addConstraint(c2);
		// dummy is stored twice, for left and right!
		vect.getBoundaries().add(dummy.getName()+Constraint.LEFT);
		vect.getBoundaries().add(dummy.getName()+Constraint.RIGHT);
		vect.getRanges().put(dummy.getName(), dummy);
		vect.updateMapNames(dummy.getName(), dummy.getName());
		vect.setSentence_length(input.size());
		vect.setInput(input);
		axiom.setVect(vect);
		
		agenda.add(axiom);
		
		if (verbose > 2) {
			System.err.println("agenda start: " + agenda);
		}

		ConstraintItem goalitem = new ConstraintItem();
		goalitem.setPl(axiom.getPl());
		goalitem.setCompleted(true);
		goalitem.setVect(new ConstraintVector(axiom.getVect()));
	
		if (verbose > 1) {
			System.err.println("axiom: " + axiom);
			System.err.println("goal : " + goalitem);
		}

		return earleyparse(goalitem, Tokenizer.tok2string(input));
	}

	//operations: predict-rule, predict-pred, complete, convert
	
	private boolean earleyparse(ConstraintItem goalitem, List<String> input) {
		boolean found = false;
		
		while (agenda.size() > 0) {
			ConstraintItem next = agenda.poll();
			if (verbose > 2) {
			//if (next.getCl() != null && next.getCl().getCindex() == 42) {
				System.err.print("\nNEXT IN THE AGENDA: \n");
				//System.err.print(next);
				System.err.print("\n\t"+next.toStringRenamed()+"\n");
			}
			
			// if next is the goal item, we're done:
			if (goal_found(next, goalitem)) {found = true;}  
				
			if (next.isActive()) {
				// we predict LHS-predicates according to
				// which is the predicate highlighted by the dot in the current clause
				predict_pred(next);
			} else {
				if (next.isCompleted()) {
					// we look for active items that can be completed
					// using the completed passive item just found
					complete(next);
				}
				else {
					// we predict rules according to which is the predicate that has been predicted  
					predict_rule(next);
				}
			}
		}
		if (verbose > -1) System.err.println("\n\nFound ? " + found + "\t Agenda size: " + agenda.size());
		
		return found;
	}
	

	private boolean goal_found(ConstraintItem item, ConstraintItem goal) {
		boolean res = true;
		res &= item.isCompleted() && (goal.getPl().toString().equals(item.getPl().toString()));
		res &= (item.getVect().getLastBoundValue() == goal.getVect().getInput().size()); 
		return res;
	}

	
	private void add2agenda(ConstraintItem predicted) {
		++itemcount;
		//System.err.println("adding " + predicted.toStringRenamed());
		int type = predicted.isActive() ? ItemKey.ACTIVE : ItemKey.PASSIVE;
		int ok   = -1;
		String pred = null;
		ItemKey ik  = null;
		switch (type) {
			case ItemKey.ACTIVE:
				pred = predicted.getCl().getLhs().getLabel().toString();
				if (actives.containsKey(pred))
					ik = actives.get(pred);
				else {
					ik = new ItemKey(ItemKey.ACTIVE);
					actives.put(pred, ik);
				}
				ok = ik.add2map(predicted);
				break;
			case ItemKey.PASSIVE:
				pred = predicted.getPl().toString();
				if (passives.containsKey(pred))
					ik = passives.get(pred);
				else {
					ik = new ItemKey(ItemKey.PASSIVE);
					passives.put(pred, ik);
				}
				ok = ik.add2map(predicted);
				break;
		}
		if (ok == 0) {
			// if the item has not been seen before
			// it can be added to the agenda
			agenda.add(predicted);
		}
	}
	
	
	private void add2active(ConstraintItem item) {
		int dot = item.getDotpos();
		PredLabel pl = item.getCl().getRhs().get(dot).getLabel();
		List<ConstraintItem> lci = null;
		if (activeByLabel.containsKey(pl.toString())) {
			lci = activeByLabel.get(pl.toString());
		} else {
			lci = new LinkedList<ConstraintItem>();
			activeByLabel.put(pl.toString(), lci);
		}
		if (!lci.contains(item)) {
			if (verbose > 1) {
				System.err.println("\n\n--UNCOMPLETE ACTIVE ITEM FOUND--");
				System.err.println("\n\tpredicate: "+ pl.toString() + "\n\tvalue: " + item.toString());
				System.err.println("\t\t" + item.toStringRenamed());
			}
			lci.add(item);
		}
	}
	
	
	private int add2predP(ConstraintItem item) {
		// this method checks whether, for a given predicted passive item,
		// we already have a more general predicated passive item in the agenda 
		// The return value < 0 means that no more general item has been found,
		// otherwise the position of the more general item in the list is returned
		String key = item.getPl().toString();
		List<ConstraintItem> lci = predP.get(key);
		if (lci == null) {
			lci = new LinkedList<ConstraintItem>();
			predP.put(key, lci);
		}
		int found = -1;
		int i = 0;
		while (i < lci.size() && found < 0) {
			ConstraintItem ci = lci.get(i);
			if (ci.getVect().isMoreGeneral(item.getVect())) {
				found = i;
			}
			i++;
		}
		if (found < 0) 
			lci.add(item);
		else {
			if (verbose > 1) {
				System.err.println("More general item found: ");
				System.err.println("Less\t" + item.toStringRenamed());
				System.err.println("More\t" + lci.get(found).toStringRenamed());
			}
		}
		return found;
	}
	
	
	private void add2parseF(ConstraintItem item) {
		// item is a completed passive item, computed by the convert operation
		String pred = item.getPl().toString();
		List<ConstraintItem> lci = null; 
		if (parseF.containsKey(pred)) {
			lci = parseF.get(pred);
		} else {
			lci = new LinkedList<ConstraintItem>();
			parseF.put(pred, lci);
		}
		if (!lci.contains(item))
			lci.add(item);
	}
	
	
	private void add2forest(Integer key, int[] value, ClauseKey ck) {
		List<int[]> li = forest.get(key);
		List<ClauseKey> lck = links.get(key);
		if (li == null) {
			li = new LinkedList<int[]>();
			lck = new LinkedList<ClauseKey>();
			forest.put(key, li);
			links.put(key, lck);
		}
		if (!this.duplicated(key, value)) {
			li.add(value);
			// finally, we store the clause key that led to this instantiation of a RHS 
			lck.add(ck);
		}
	}
	
	
	private boolean duplicated(Integer key, int[] v) { //ClauseKey ck) {
		//return (links.containsKey(key) && links.get(key).contains(ck));
		boolean res = false;
		if (forest.containsKey(key)) {
			for (int[] x : forest.get(key)) {
				res |= Arrays.equals(x, v);
			}
		}
		return res;
	}
	
	
	private void  predict_rule (ConstraintItem next) {
		
		ConstraintVector itemVect = next.getVect(); // vector with current constraints
		
		List<Clause> lcl = ((RCG)super.getGrammar()).getClausesForLabel(next.getPl());
		if (lcl == null) {
			if (verbose > 0)
				System.err.println("[Error] - Unknown label: " + next.getPl().toString());
		} else {
		
			for (Clause cl : lcl) {
				
				if (verbose > -1)
					System.err.println("\n\n--PREDICT RULE--");
				
				if (verbose > 0) 
					System.err.println("pred-rule:" + next);
				if (verbose > -1)
					System.err.println("\t" + next.toStringRenamed());

				// we create a constraint vector for the clause with new unique names
				// NB: the clause itself is renamed (hence cl2)
				Clause cl2 = cl.createVect(new NameFactory(), new CanonicalNameFactory(), gDict);
			
				// we create a new _active_ item for the clause
				ConstraintItem predicted = new ConstraintItem();
				predicted.setActive(true); 
				predicted.setDotpos(0);
				predicted.setCl(cl2);
				// NB: in this new Item, PredLabel==null, for we are creating an active item!
				
				ConstraintVector predVect = new ConstraintVector(cl2.getVect()); // copy of the constraints of the selected clause
				predicted.setVect(predVect); // the new constraint vector is given to the item
				predVect.setSentence_length(itemVect.getSentence_length());
				predVect.setInput(itemVect.getInput());
				
				// update constraints (we modify predVect by looking at the boundaries list from the item being processed)
				//--------------------------------------------------------------------------------------------------------
				this.selectConstraints(predVect.getBoundaries(), itemVect, predVect);
				//---------------------------------------------------------------------------------------------------------
				
				if (verbose > 1) {
					System.err.println("\nProcessing clause " + cl2.print());
					System.err.println("Before solving: " + predVect.print() + "\n\t" +predVect.toStringRenamed());
				}
				// Here we check whether the new constraint vector actually has a solution
				// if so, we can go on the prediction
				// plus, we do a partial solving of the constraints to get the known ranges
				// NB: this is done by the static hasSol method
				// if (ConstraintVectorSolving.hasSol((verbose>1), predVect)) {
				// 	predVect.checkConsistency(verbose>1, new Binding());
				// 	if (predVect.isConsistent()) {
				// 		if (verbose > -1) 
				// 			System.err.println("\tOUTPUT: "+predicted.toStringRenamed());
						
				// 		if (cl2.getRhs().size() == 0) {
				// 			// for epsilon-clauses, we call convert directly
				// 			this.convert(predicted);
				// 		}
				// 		else  {
				// 			// Arguments processed, new item put in the agenda:
				// 			add2agenda(predicted);
				// 			// we also store the _active_ item in the activeByLabel structure
				// 			add2active(predicted);
				// 		}
				// 	}
				// }
				// otherwise, we do not put any item in the agenda			
			}
		}
	}
	

	private void predict_pred (ConstraintItem next) {
		
		if (verbose > -1)
			System.err.println("\n\n--PREDICT PRED--");
		
		if (verbose > 0) 
			System.err.println("pred-pred:" + next);
		if (verbose > -1)
			System.err.println("\t" + next.toStringRenamed());
		
		Clause cl   = next.getCl();
		int dot     = next.getDotpos();
		int end     = cl.getRhs().size();
		if (dot == end)
			// this case should never happen since epsilon-clauses are detected in predict_rule directly
			System.err.println("[Error] Epsilon clause in predict_pred: " + cl.toStringRenamed());
		else {
			Predicate p = new Predicate(cl.getRhs().get(dot)); 
			ConstraintVector itemVect = next.getVect();
	
			// we create a new _passive_ item for the clause
			ConstraintItem predicted = new ConstraintItem(); 
			predicted.setPl(p.getLabel());
			ConstraintVector predVect = new ConstraintVector(p.getNbOfRanges());
			predicted.setVect(predVect);
			predicted.getVect().setSentence_length(itemVect.getSentence_length());
			predVect.setInput(itemVect.getInput()); // we store the input for we may want to check 
			                                        // ranges with respect to terminals of the input string
			
			List<String> bounds = new LinkedList<String>();
			List<String> lefts  = new LinkedList<String>();
			List<String> rights = new LinkedList<String>();
			Map<String,String> newNames= new HashMap<String, String>();
			// we use a renaming of the predicate arguments, cf uniqueness of constraint items
			CanonicalNameFactory cnf = new CanonicalNameFactory();
			
			for (Argument arg : p.getArgs()) {
				ArgContent lmost = arg.getRec(0);
				ArgContent rmost = arg.getRec(arg.getSize() -1);
				String newLeft = cnf.getName(lmost.getName())+Constraint.LEFT;
				String newRight= cnf.getName(rmost.getName())+Constraint.RIGHT;
				String left = lmost.getName()+Constraint.LEFT;
				String right= rmost.getName()+Constraint.RIGHT;
				newNames.put(left, newLeft);
				bounds.add(newLeft);
				lefts.add(left);
				newNames.put(right, newRight);
				bounds.add(newRight);
				rights.add(right);
				predVect.getMapNames().put(newLeft, itemVect.getMapNames().get(left));
				predVect.getMapNames().put(newRight, itemVect.getMapNames().get(right));
				predVect.getRanges().put(newLeft, lmost);
				predVect.getRanges().put(newRight, rmost);
			}
			predVect.setBoundaries(bounds);
			// itemVect reads itself and returns the selected constraints
			predVect.addAllConstraints(itemVect.select(lefts, rights, newNames));
			
			// Tabulation:
			// we check whether we already have a more general item in the predP structure:  
			int ok = add2predP(predicted);
			// otherwise, the new _passive_ item is put in the agenda:
			if (ok < 0) {
				if (verbose > -1) 
					System.err.println("\tOUTPUT: "+predicted.toStringRenamed());

				add2agenda(predicted);
			} else
				autocomplete(next);
		}
	}
	
	
	private void autocomplete(ConstraintItem active) {
		// this operation is triggered when encountering an already predicted predicate
		// NB: active is an _active item_, the completed passive item needed to complete this 
		//     active item (if any) is stored in the parseF structure!

		// 1. We look up the potential completed items to be used for moving the dot in active
		//    NB: these items (if any) are in the parseF structure
		if (verbose > -1)
			System.err.println("\n\n--AUTOCOMPLETE--");
				
		if (verbose > 0) 
			System.err.println("complete:" + active);
		if (verbose > -1)
			System.err.println("\t" + active.toStringRenamed());
			
		// we get the constraint items that are related to the predicate of the active item
		int dot = active.getDotpos();
		Predicate p = active.getCl().getRhs().get(dot);
		String  key = p.getLabel().toString();
		
		List<ConstraintItem> lci = parseF.get(key);
		if (lci != null) {
			List<ConstraintItem> lci2= new LinkedList<ConstraintItem>();
			// we need a copy of the valid entries of parseF, 
			// to avoid concurrent access exception
			for (ConstraintItem ci : lci) {
				lci2.add(new ConstraintItem(ci));
			}
			for (ConstraintItem ci : lci2) {
				/*System.err.println("***parseF:\n" + this.printForest());
				System.err.println("***Active: " + active.toStringRenamed());
				System.err.println("***Passive: " + ci.toStringRenamed());*/
				
				// 2. For each of the completed item found in parseF,
				//    we complete the current active item (i.e. we move the dot)
				do_complete(ci, active);
			}
		}
	}
	
	
	private void complete(ConstraintItem next) {
		// this operation moves the dot over a predicate if the corresponding passive item has been completed
		// NB: next is a _completed passive item_, the active item with the clause whose dot is to be moved
		//     is stored in the activeByLabel structure!
		
		if (verbose > -1)
			System.err.println("\n\n--COMPLETE--");
			
		if (verbose > 0) 
			System.err.println("complete:" + next);
		if (verbose > -1)
			System.err.println("\t" + next.toStringRenamed());
			
		// we get the constraint items that are related to the predicate of the completed passive item
		PredLabel pl = next.getPl();

		List<ConstraintItem> lci2 = activeByLabel.get(pl.toString());
		// we first copy the list of active items for this predicate
		// NB: this copy is mandatory to avoid java concurrency exception
		//     (case of duplicated predicate in a LHS of an active clause)
		if (lci2 != null) {
			List<ConstraintItem> lci  = new LinkedList<ConstraintItem>();
			for (ConstraintItem ci : lci2) {
				lci.add(new ConstraintItem(ci));
			}
			
			// if there are uncompleted active item for the predicate
			for (ConstraintItem active : lci) {
				if (verbose > 1)
					System.err.println("\n*** TRYING: " + active.toString() + "\n\t" + active.toStringRenamed());
				//System.err.println("***" + active.getVect().print() + "\n\t" + active.getVect().toStringRenamed());			
				do_complete(next, active);
			}
		}
		// otherwise (there are no active item for the predicate)
	}
		
	
	private void do_complete(ConstraintItem completed, ConstraintItem active) {
		// sub-routine used by the complete and autocomplete operations
		// we fetch the constraint item, and update the current vector
		// NB: the constraint item already has values for each range boundary!
		ConstraintVector itemVect = new ConstraintVector(completed.getVect());

		// we get the boundaries of the arguments of the predicate pointed at by the dot
		// NB: we create a copy of the _active item_ being processed
		ConstraintItem predicted = new ConstraintItem(active);
		ConstraintVector predVect= predicted.getVect();
	
		int dot = active.getDotpos();
		Predicate p = active.getCl().getRhs().get(dot);
		List<String> bounds = new LinkedList<String>();
		
		// we compute the boundaries to take into account
		for (Argument arg : p.getArgs()) {
			ArgContent lmost = arg.getRec(0);
			ArgContent rmost = arg.getRec(arg.getSize() -1);
			String left = lmost.getName()+Constraint.LEFT;
			String right= rmost.getName()+Constraint.RIGHT;
			bounds.add(left);
			bounds.add(right);
		}
					
		this.selectConstraints(bounds, itemVect, predVect);
		
		// if it still has a solution, we move the dot, 
		// and store the new item (a) in the agenda (for predict_pred)
		// and (b) in the activeByLabel (for complete)
		// if (ConstraintVectorSolving.hasSol((verbose>1), predVect)) {
		// 	predVect.checkConsistency(verbose>1, new Binding());
		// 	if (predVect.isConsistent()) {
		// 		predicted.setDotpos(predicted.getDotpos() + 1);
		// 		if (verbose > -1) 
		// 			System.err.println("\tOUTPUT: "+predicted.toStringRenamed());

		// 		if (predicted.getDotpos() == predicted.getCl().getRhs().size()) {
		// 			convert(predicted);
		// 		}
		// 		else {
		// 			add2agenda(predicted);
		// 			add2active(predicted);
		// 		}
		// 	}
		// 	//else System.err.println("__unconsistent solution__");
		// }
		//else System.err.println("___no solution___");	
	}
	
	private void selectConstraints(List<String> bounds, ConstraintVector itemVect, ConstraintVector predVect) {
		// first, we need to get the constraints applying on the ranges of arguments only
		// i.e. the constraints related to the bounds stored in itemVect.getBoundaries()
		Pair nameDict = new Pair();
		List<String> bounds1 = new LinkedList<String>();
		List<String> bounds2 = new LinkedList<String>();
			
		for (int i = 0 ; i < itemVect.getBoundaries().size() ; i++) {
			String bound1 = itemVect.getBoundaries().get(i);	
			String bound2 = bounds.get(i);
			//System.err.println("****Old bound: " + bound1);
			//System.err.println("****New bound: " + bound2);
			bounds1.add(bound1);
			bounds2.add(bound2);
		}
		nameDict.setKey(bounds1);
		nameDict.setValue(bounds2);
		// the itemVect selects among its constraints, the ones that are relevant
		// and rename them according to the nameDict dictionary
		predVect.addAllConstraints(itemVect.renameConstraints(nameDict));
		//System.err.println("PredVect: " + predVect.print() +"\n\t" + predVect.toStringRenamed());
	}
	

	private void convert(ConstraintItem next) {
		// this operation converts an active item into a _completed_ passive item
		// provided its dot reached the end of the RHS
		
		int dot = next.getDotpos();
		int end = next.getCl().getRhs().size();
		
		if (dot == end) {
			if (verbose > -1)
				System.err.println("\n\n--CONVERT--");
			
			if (verbose > 0) 
				System.err.println("convert:" + next);
			if (verbose > -1)
				System.err.println("\t" + next.toStringRenamed());
			
			// we get the constraints
			ConstraintVector itemVect = next.getVect();			
			// we check (a) whether the vector in the item has solutions
			// List<int[][]> sols = ConstraintVectorSolving.computeSol(itemVect);
			// for (int i = 0 ; i < sols.size() ; i++) {
			// 	// for each solution, we copy the constraint vector
			// 	ConstraintVector newVect = 	ConstraintVectorSolving.decode((verbose>1), itemVect, sols.get(i), i);
			// 	// for each solution, we create a binding environment where to store
			// 	// the bindings: Range <-> Symbols of the input
			// 	Binding bd = new Binding();
			// 	// we check for consistency, i.e. 
			// 	// whether these solutions correspond to substrings of the input string
			// 	// that are the actual clause's parameters
			// 	newVect.checkConsistency((verbose>0), bd);
			// 	// if the solution is consistent with respect to the input string
			// 	// we set the ConstraintItem to completed and store it
			// 	if (newVect.isConsistent()) {
			// 		// we create a new completed _passive_ item from the item being processed 
			// 		ConstraintItem newItem = new ConstraintItem(next);
			// 		newItem.setActive(false);
			// 		newItem.setPl(next.getCl().getLhs().getLabel());
			// 		newItem.setCompleted(true);
			// 		newItem.setVect(newVect);
			// 		// we update the parseF structure
			// 		try {
			// 			ConstraintItem newItem2 = new ConstraintItem(newItem);
			// 			List<Argument> la = Predicate.instantiate(newItem2.getCl().getLhs(), bd);
			// 			//------------------------
			// 			// we need to rename the constants (c.f. they have been renamed for multiple occurrences)
			// 			List<Argument> la2 = new LinkedList<Argument>();
			// 			for (Argument arg : la) {
			// 				Argument arg2 = new Argument();
			// 				List<ArgContent> ac2 = new LinkedList<ArgContent>();
			// 				for (int k = 0 ; k < arg.getContent().size() ; k++) {
			// 					ArgContent ac = arg.getContent().get(k);
			// 					if (ac.getType() == ArgContent.TERM || ac.getType() == ArgContent.EPSILON) {
			// 						String name = itemVect.getMapNames().get(ac.getName());
			// 						if (name != null)
			// 							ac.setName(name);
			// 					}
			// 					if ((k==0 && arg.getContent().size()==1) || (ac.getType() != ArgContent.EPSILON))
			// 						ac2.add(ac);
			// 				}
			// 				arg2.addArg(new ArgContent(ac2));
			// 				la2.add(arg2);
			// 			}
			// 			//------------------------
			// 			ClauseKey ck = new ClauseKey(newItem2.getCl().getCindex(), la2);
			// 			add2parseF(newItem2);
			// 			if (verbose > -1) {
			// 			//if (ck.getCindex() == 42) {
			// 				System.err.println("\tOUTPUT: "+newItem2.toStringRenamed());
			// 				System.err.println("\t        "+ck.toString(((RCG)super.getGrammar())));
			// 			}
			// 			//------------------------
			// 			// we update the forest table, containing pointers (hashes)
			// 			Integer key = new Integer(newItem2.getHash());
			// 			//System.err.print("#### " + newItem2.getCl().getLhs().getLabel().toString() + " " + key + " -> ");

			// 			// if the item concerns the start predicate, we found an answer!
			// 			if (newItem2.getCl().getLhs().getLabel().equals(((RCG)super.getGrammar()).getStartPredicateLabel()) && !(answers.contains(key)))
			// 				answers.add(key);

			// 			List<Predicate> lp = newItem2.getCl().getRhs();
			// 			int[] rhs = new int[lp.size()];
			// 			for (int pidx = 0 ; pidx < lp.size() ; pidx++) {
			// 				Predicate p = lp.get(pidx);
							
			// 				// we build the hash and store it
			// 				ConstraintItem tempItem   = new ConstraintItem(newItem2);
			// 				ConstraintVector tempVect = new ConstraintVector(newItem2.getVect());
			// 				tempItem.setVect(tempVect);
			// 				tempItem.setPl(p.getLabel());
			// 				List<String> bounds = new LinkedList<String>();
							
			// 				for (Argument arg : p.getArgs()) {
			// 					ArgContent lmost = arg.getRec(0);
			// 					ArgContent rmost = arg.getRec(arg.getSize() -1);
			// 					String left = lmost.getName()+Constraint.LEFT;
			// 					String right= rmost.getName()+Constraint.RIGHT;
			// 					bounds.add(left);
			// 					bounds.add(right);
			// 				}

			// 				tempVect.setBoundaries(bounds); // needed to compute the vector's hash
			// 				tempVect.setMapNames(newItem2.getVect().getMapNames()); // idem (for using the canonical names)
							
			// 				rhs[pidx] = tempItem.getHash();
			// 				//System.err.print(" " + p.getLabel().toString() + " " + rhs[pidx]);
			// 			}
			// 			//System.err.println(" ####");
			// 			this.add2forest(key, rhs, ck);
			// 			//------------------------
			// 		} catch (RCGInstantiationException e) {
			// 			System.err.println("Predicate instantiation error " + e.getMessage());
			// 		}
			// 		// we store it in the agenda
			// 		add2agenda(newItem);
			// 	} else {
			// 		if (verbose > 1)
			// 			System.err.println("## Unconsistent item: " + newVect.toStringRenamed());
			// 	}
			// }
		}
		else {
			// should never happen for end of RHS is checked within complete directly
			System.err.println("[Error] convert called with a clause whose dot is not in the end of its RHS: " + next.getCl().toStringRenamed());
		}
	}
	
	
	public String printForest() {
		// this methods prints the content of the parseF structure:
		StringBuffer sb = new StringBuffer();
		sb.append("\nANSWERS:\n");
		for (Integer j : answers) {
			sb.append(String.valueOf(j));
			sb.append("\n");
		}
		sb.append("\nFOREST:\n");
		for(Integer i : forest.keySet()) {
			sb.append("hash: ");
			sb.append(String.valueOf(i));
			//sb.append("\t\t" + forest.get(i).size() + " " + links.get(i).size());
			sb.append("\n\t");
			for (int k = 0 ; k < links.get(i).size() ; k++) {
				ClauseKey ck = links.get(i).get(k);
				sb.append(ck.toString((RCG)super.getGrammar()));
				//sb.append(ck.toString());
				sb.append("\n\t");
				int[] tab = forest.get(i).get(k);
				for (int l = 0 ; l < tab.length ; l++) {
					sb.append(tab[l]);
					sb.append(" ");
				}
				sb.append("\n\t");
			}
			sb.append("\n");
		}
		String res=sb.toString();
		return res;
	}

	public List<ClauseKey> getAnswers() {
		return null;
	}

	public List<ClauseKey> getEmptyRHS() {
		return null;
	}

	public Hashtable<ClauseKey, DStep> getParse() {
		return null;
	}

	public Map<Integer, List<int[]>> getForest() {
		return forest;
	}

	public Map<Integer, List<ClauseKey>> getLinks() {
		return links;
	}
	
	public List<Integer> getAns() {
		return answers;
	}
	
	public void setVerbose(int v) {
		verbose = v;
	}
	
	public Map<String, TagTree> getGDict() {
		return gDict;
	}

	public void setGDict(Map<String, TagTree> dict) {
		gDict = dict;
	} 
	
	/*
	 * just for testing   
	 */
	public static void main(String[] args) throws Exception {
		RCGReader rcggr  = new TextRCGReader(new File("/tmp/rcg.txt"));
		RCG g = rcggr.getRCG();
		System.err.println(g.toString());
		Tokenizer tok = new BuiltinTokenizer();
		//tok.setSentence("a b b a a b b a");
		//tok.setSentence("a a b a a b");
		//tok.setSentence("b c"); 
		tok.setSentence("a a ");  
		System.err.println("\nSentence to parse: " + tok.getSentence());
		List<Word> input = tok.tokenize();
		long startTime = System.nanoTime();
		RCGParserConstraintEarley p = new RCGParserConstraintEarley(g);
		p.setVerbose(-21);
		boolean res = p.parseSentence(true,input);
		long parseTime = System.nanoTime() - startTime;
		System.err.println("Parsing time: " + (parseTime)/(Math.pow(10, 9)) + " sec.");
		System.err.print("Result: parse ");
		if (!res) 
			System.err.print("not ");
		System.err.println("found");
		System.err.println("addcount: " + p.itemcount);
		
		//------------------------------------
//		System.err.println(p.printForest());
		//------------------------------------
	}

	@Override
	public ForestExtractorInitializer getForestExtractorInitializer() {
		ForestExtractorInitializer ret = new ForestExtractorInitializer();
		ret.addField(getAns());
		ret.addField(getForest());
		ret.addField(getLinks());
		return ret;
	}

}
