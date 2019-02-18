/*
 *  File RCGParserBoullier2.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni.tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2008
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Sa 19. Jan 14:59:23 CET 2008
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map;

import de.duesseldorf.frames.UnifyException;
import de.duesseldorf.frames.Value;
import de.tuebingen.io.RCGReader;
import de.tuebingen.rcg.*;
import de.tuebingen.tokenizer.BuiltinTokenizer;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.tree.Grammar;
import de.tuebingen.tree.Node;
import de.tuebingen.tag.*;
import de.tuebingen.io.TextRCGReader;

/**
 * A parser for Range Concatenation Grammar (RCG), following 
 * Boullier (2000), and using back-pointers for storing the derivation forest.  
 * 
 * Pierre Boullier (2000). <em>Range Concatenation Grammars</em>. In: Proceedings
 * of IWPT 2000.
 * 
 * @author wmaier, parmenti
 *
 */
public class RCGParserBoullier2 extends RCGParser {
	
	private Hashtable<PredLabelKey, Boolean>           prdct_tab;
	private Hashtable<ClauseKey, Boolean>             clause_tab;
	// tabulation of the last encountered item:
	private Hashtable<ClauseKey, DStep>              clauseD_tab;
	// tabulation of the clause instantiations matching a predicate
	private Hashtable<PredLabelKey, List<ClauseKey>>  prdctD_tab;
	// record of the emptyRHS clauses used during parsing (just in case)
	private List<ClauseKey>                             emptyRHS;
	// record of the answers (clauses successfully instantiated from the start predicate)
	private List<ClauseKey>                              answers;
	private boolean                                      verbose;
	// for storing the derivation forest
	private Hashtable<ClauseKey, DStep>               derivation;
	// for checking top-bottom feature unifications on non-adjoined nodes
	private Map<String, TagTree>                      dictionary;
	// for checking de facto null-adjunction wrt the subgrammar used
	private Map<String, Integer>                             cat;
	
	
	/**
	 * Constructor. 
	 * @v verbosity boolean
	 * @param grammar
	 */
	public RCGParserBoullier2(boolean v, Grammar grammar, Map<String, TagTree> dict, Map<String, Integer> cats) {
		super(grammar);
		prdct_tab  = new Hashtable<PredLabelKey, Boolean>();
		clause_tab = new Hashtable<ClauseKey, Boolean>();
		prdctD_tab = new Hashtable<PredLabelKey, List<ClauseKey>>(); 
		clauseD_tab= new Hashtable<ClauseKey, DStep>();
		emptyRHS   = new LinkedList<ClauseKey>();
		answers    = new LinkedList<ClauseKey>();
		verbose    = v;
		derivation = new Hashtable<ClauseKey, DStep>();
		dictionary = dict;
		cat        = cats;
	}
	
	public boolean parse(List<Word> input) {
		// parsing is performed by the recognize wrapper method
		return (this.recognize(input));
	}
	
	public boolean parseSentence(boolean v, List<Word> sentence) {
		boolean bres = false;
		if ((bres = parse(sentence)) && v) {
			System.err.println(this.printForest());
		}
		return bres;
	}

	public boolean recognize(List<Word> input) {
		Argument rl = Argument.argFromWord(input);
		List<Argument> al = new ArrayList<Argument>();
		al.add(rl);
		boolean res = prdct(new ClauseKey(-1, new ArrayList<Argument>()), ((RCG)getGrammar()).getStartPredicateLabel(), al, new HashSet<PredLabel>(), 0, new Binding());
		return res;
	}

	/**
	 * As in Boullier (2000) - treat predicates. True iff there exists a p-clause 
	 * in the grammar with which the given Argument can be reduced to epsilon.  
	 * @param p - a predicate label
	 * @param al - a List of Argument
	 * @return true if p can be reduced to epsilon given r.
	 */
	public boolean prdct(ClauseKey caller, PredLabel p, List<Argument> al, Set<PredLabel> trace, int RHSpos, Binding b) {
		boolean ret = false;
		if (verbose) {
			String ps = p.toString();
			if (p instanceof PredComplexLabel)
				ps = ((PredComplexLabel) p).toString(dictionary);
			System.err.println("\n === TRYING PREDLABEL: " + ps + " with " + al.toString() + "\n");
		}
		
		PredLabelKey plk = new PredLabelKey(p, al);
		
		// Predicate memoization 
		if (prdct_tab.containsKey(plk)) {
			//System.err.println(" ===> Predicate already computed : "+p.toString()+ " " + al.toString() + " result " + prdct_tab.get(new PredLabelKey(p, al)));
			
			if (prdct_tab.get(plk)) {
				// if this is a successful predicate, we update clauseD_tab 
				// with the successful clauses
				List<ClauseKey> lck = prdctD_tab.get(plk);
				for(int i = 0 ; i < lck.size() ; i++){
					if (verbose) {
						System.err.println("*****\nAlready encountered (and tabulated) ...");
						System.err.println(lck.get(i).toString());
					}
					this.updateClauseTable(caller, lck.get(i), RHSpos, b);
				}
			}
			return prdct_tab.get(plk);
		} else {
			// if the predicate has not been computed before with these arguments
			// we initialise the tabulated clause keys in prdctD_tab
			List<ClauseKey> lck = new LinkedList<ClauseKey>();
			prdctD_tab.put(plk, lck);
		}
		
		int pclausesnum = ((RCG)getGrammar()).getClausesForLabelNum(p);
		// loop through all p-clauses
		for (int i = 0; i < pclausesnum; ++i) {
			//System.err.println(" == NEXT CLAUSE: " + getGrammar().getClauseForLabel(p, i) + "( " + (i+1) + " / " + pclausesnum + " ) \n");
			ret |= clause(caller, p, i, al, trace, RHSpos, b);
		}
				
		// tabulation of "is it a dead search branch ?" -> yes|no
		prdct_tab.put(plk, new Boolean(ret));
		
		return ret;
	}

	/**
	 * As in Boullier (2000) - treat clauses
	 * @param clabel - the label of the clause we check
	 * @param cind - an index in the list of all predicate labels
	 * @param al - a List of Argument
	 * @return  
	 */	
	public boolean clause(ClauseKey caller, PredLabel clabel, int cind, List<Argument> al, Set<PredLabel> trace, int motherRHSpos, Binding mothb)  {
				
		// default value
		boolean ret = false;
		
		// fetch the clause
		Clause     c = ((RCG)getGrammar()).getClauseForLabel(clabel, cind);
		ClauseKey ck = new ClauseKey(c.getCindex(), al);
		int RHSsize  = c.getRhs().size();
		
		if (verbose) 
			System.err.println(" -- TRYING CLAUSE " + c.toString(dictionary) + " with " + al.toString());
		
		// lookup in the tabulation 
		if (clause_tab.containsKey(ck)) {
			// yes case should not occur since predicates are tabulated
			//System.err.println(" -- RETURNING CLAUSE " + c.toString() + " with " + al.toString() + " res: " + ret);
			return clause_tab.get(ck);
		} else {
			// initialization of the table
			DStep rhsStep = new DStep();
			clauseD_tab.put(ck, rhsStep);
		}

		// we avoid cycles using the trace (to be refined to deal with full class of positive RCG)
		if (c.getRhs().size() == 1) {
			List<Argument> lhsargs = c.getLhs().getArgs();
			List<Argument> rhsargs = c.getRhs().get(0).getArgs();
			if (lhsargs.size() == rhsargs.size()) {
				int i = 0;
				for ( ; i < lhsargs.size() && lhsargs.get(i).equals(rhsargs.get(i)); ++i);
				if (i == lhsargs.size()) {
					if (trace.contains(c.getRhs().get(0).getLabel())) {
						System.err.println("Loop detected: " + c.toString());
						return false;
					} else {
						trace.add(clabel);
					}
				}
			}
		}

		// get all possible bindings and loop through
		List<Binding> bs = new ArrayList<Binding>();
		
		Encode enc = new Encode(al, c.getLhs().getArgs(), cat);
		try {
			bs = enc.buildAllBindings(verbose);
		} catch (RCGInstantiationException e){
			if (verbose) {System.err.println(e);}
			// e.printStackTrace();
			// if the binding computation fails, we quit
			clause_tab.put(ck, new Boolean(false));
			return false;
		}
		
		if (verbose) 
			System.err.println("Bindings : " + bs.toString() + "\n");
		
		Iterator<Binding> it = bs.iterator();
		
		bindingsNext:
		while (it.hasNext()) {
			// a binding with respect to a clause
			Binding b = (Binding)it.next();
			
			boolean rhspRes = true;
			int RHSpos  = 0;

			// loop through all RHS predicates
			Iterator<Predicate> rhsit = c.getRhs().iterator();
			while (rhsit.hasNext() && rhspRes) {
				Predicate p = rhsit.next();
				
				// instantiate the current rhs predicate with the current binding 
				Predicate rhsp = new Predicate(p);
				try { 
					rhsp.setArgs(Predicate.instantiate(rhsp, b));
				} catch (RCGInstantiationException e) {
					// no success, try next instantiation. 
					if (verbose) {
						System.err.println("Instantiation failed: " + rhsp.toString());
						System.err.println(e.toString());
					}
					continue bindingsNext;
				}

				// check for not-adjoined nodes
				if (rhsp.getLabel() instanceof PredComplexLabel) {
					PredComplexLabel rhspl = (PredComplexLabel) rhsp.getLabel();
					if (rhspl.getType() == PredComplexLabel.ADJ) {
						if (rhsp.hasEpsArgs()) {
							//if (verbose)
								//System.err.println(" ### NO ADJ node found " + rhsp.toString(dictionary));
							// empty arguments -> no adjunction
							String treeId = rhspl.getTreeid();
							// dictionary is not null only when handling tree-based grammars!
							TagTree tree  = dictionary.get(treeId);
							String nodeId = rhspl.getNodeid();
							List<Node> ln = new LinkedList<Node>();
							tree.findNode(tree.getRoot(), nodeId, ln);
							TagNode tn = (TagNode) ln.get(0);
							try {
								Value top = tn.getLabel().getFeat("top");
								Value bot = tn.getLabel().getFeat("bot");
								if (top != null && bot != null) {
									//System.err.println("Unifying " + top.toString() + " and " + bot.toString());
									Value.unify(top, bot, new Environment(0));
								}
							}
							catch (UnifyException e) {
								if (verbose) {
									System.err.println(" ***** Top-bottom unification on an un-adjoined node failed: " + rhsp.toString());
									System.err.println(e.toString());
								}
								continue bindingsNext;								
							}
						}
					}
				}

				// create a slot in the forest
				clauseD_tab.get(ck).create(b, RHSsize);
			
				// call prdct for this instantiation
				rhspRes &= prdct(ck, rhsp.getLabel(), rhsp.getArgs(), trace, RHSpos++, b);
				
			}
			if (RHSsize == 0) {
				// 0. We store empty-RHS clauses (for decoding)
				emptyRHS.add(ck);
			}
			
			if (rhspRes) {
				// *************** CLAUSE SUCCESSFULLY INSTANTIATED ***************
				// 1. We can tabulate the clause instantiation 
				this.updateClauseTable(caller, ck, motherRHSpos, mothb);
				
				// 2. We can update the predicate table so that we can also
				// directly tabulate the predicate calls
				this.updatePredTable(clabel, al, ck);
			}
						
			ret |= rhspRes;
		}
 			
		clause_tab.put(ck, new Boolean(ret));
		
		if (ret && (caller.getCindex() == -1))
			answers.add(ck);

		if (ret && verbose)
			System.err.println(" -- SUCCESS: RETURNING CLAUSE " + c.toString(dictionary) + " with " + al.toString());
		return ret;
	}

	
	/**
	 * Method used to chain 2 derivation items together and tabulate them
	 * @param caller calling clause 
	 * @param called current clause to be instantiated
	 * @param motherRHSpos integer refering to the number of RHS predicate in the caller clause
	 * @param mothb binding used as a key to store the tabulated instantiations
	 */
	public void updateClauseTable(ClauseKey caller, ClauseKey called, int motherRHSpos, Binding mothb) {
		if (verbose) 
			System.err.println("Updating from " + caller.toString() + " to " + called.toString());
		
		//System.err.println("Tabulating " + called.toString() + " in position " + motherRHSpos + " of " + caller.toString() + " for binding " + mothb.toString());
		DStep mother = (caller.getCindex() != -1) ? clauseD_tab.get(caller) : new DStep();
		if (caller.getCindex() == -1)
			mother.create(new Binding(), 1);
		mother.put(called, motherRHSpos, mothb);
	}

	/**
	 * Method used to store the successful clause calls related to a given LHS predicate
	 * @param caller predicate of the RHS of the current clause to instantiate 
	 * @param al list of arguments for the predicate instantiation
	 * @param called successful clause for the predicate instantiation (stored in the list of clause that is tabulated)
	 */
	public void updatePredTable(PredLabel caller, List<Argument> al, ClauseKey called) {
		PredLabelKey    plk = new PredLabelKey(caller, al);
		List<ClauseKey> lck = prdctD_tab.get(plk);
		if (verbose)
			System.err.println("  ---PredTable -- Adding " + called.toString() + " to " + plk.toString(dictionary) + "\n");
		lck.add(called);
	}
	
	/**
	 * Method used to print the forest (tabulation of clause instantiations)
	 */
	public String printForest() {
		String res = "\n***********************\n\n";
		Set<ClauseKey> keys    = clauseD_tab.keySet();
		Iterator<ClauseKey> it = keys.iterator();
		while(it.hasNext()) {
			ClauseKey ck = it.next();
			String dstep = clauseD_tab.get(ck).toString();
			if (!dstep.equals("")) {
				int index = ck.getCindex();
				String sindex = index+"";
				if (index<10)
					sindex = "00" + index;
				else if (index<100)
					sindex = "0" + index;
				res += "Rule: " + sindex;  
				res += ck.getArgs();
				//res += "\n";
				res += dstep;
			}
		}
		res += "\nEps-RHS:\n";
		for(int i = 0 ; i < emptyRHS.size() ; i++){
			ClauseKey ckE = emptyRHS.get(i);
			int index = ckE.getCindex();
			String sindex = index+"";
			if (index<10)
				sindex = "00" + index;
			else if (index<100)
				sindex = "0" + index;
			res += "Rule: " + sindex + "(";  
			res += ckE.getArgs();
			res += "\n";
		}
		res += "\nAnswers:\n";
		for(int i = 0 ; i < answers.size() ; i++){
			ClauseKey ckE = answers.get(i);
			res += "Rule: " + ckE.getCindex() + "(";  
			res += ckE.getArgs();
			res += "\n";
		}
		return res;
	}
	
	/**
	 * Method used to remove partial derivation branches from the table (only keep solutions)
	 * To be used after parsing only (post-processing)!
	 */
	public void clean(){
		Set<ClauseKey> keys    = clauseD_tab.keySet();
		Iterator<ClauseKey> it = keys.iterator();
		while(it.hasNext()) {
			ClauseKey ck = it.next();
			String dstep = clauseD_tab.get(ck).toString();
			if (!dstep.equals("")) {
				derivation.put(ck, new DStep(clauseD_tab.get(ck).clean()));
			}
		}
	}

	/**
	 * Accessor for the table of clause instantiations (result of parsing)
	 * @return the tabulated clause instantiations (starting from the Start predicate, gives the forest)
	 */
	public Hashtable<ClauseKey, DStep> getParse(){
		this.clean();
		return derivation;
	}
	
	public List<ClauseKey> getEmptyRHS() {
		return emptyRHS;
	}

	public List<ClauseKey> getAnswers() {
		return answers;
	}

	/**
	 * just for testing, ambiguous copy language.
	 */
	public static void main(String[] args) throws Exception {
		//Grammar g = new RCG();
		//((RCG)g).lata();
		//((RCG)g).copylanguage();
		//((RCG) g).forestCheck();
		
		RCGReader rcggr  = new TextRCGReader(new File("/tmp/rcg6.txt"));
		RCG g = rcggr.getRCG();

		System.err.println(g.toString());
		System.err.println("--------------------");
		
		RCGParserBoullier2 p = new RCGParserBoullier2(true, g, null, new Hashtable<String, Integer>());
		BuiltinTokenizer tok = new BuiltinTokenizer();
		String s = "a a";
		//String s = "a a b a a b";
		//String s = "a a b";
		tok.setSentence(s);
		List<Word> l = tok.tokenize();
		System.err.println("Parse " + l.toString());
		long startTime = System.nanoTime();
		boolean res = p.parseSentence(true, l);
		long estimatedTime = System.nanoTime() - startTime;
		System.err.println("Result: " + Boolean.toString(res)+" computed in "+(estimatedTime)/(Math.pow(10, 9))+" sec.");
		
	}

	@Override
	public ForestExtractorInitializer getForestExtractorInitializer() {
		ForestExtractorInitializer ret = new ForestExtractorInitializer();
		ret.addField(getAnswers());
		ret.addField(getParse());
		return ret;
	}

}
