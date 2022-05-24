/*
 *  File RCGParserBoullier.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni.tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:05:09 CEST 2007
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
import java.util.List;
import java.util.Set;

import de.tuebingen.rcg.*;
import de.tuebingen.tokenizer.BuiltinTokenizer;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.tree.Grammar;

/**
 * A parser for Range Concatenation Grammar (RCG), following
 * Boullier (2000).
 * <p>
 * Pierre Boullier (2000). <em>Range Concatenation Grammars</em>. In: Proceedings
 * of IWPT 2000.
 *
 * @author wmaier, parmenti
 */
public class RCGParserBoullier extends RCGParser {

    private Hashtable<PredLabelKey, Boolean> prdct_tab; // the map of unsuccessful predicates
    private Hashtable<ClauseKey, Boolean> clause_tab;
    private Hashtable<ClauseKey, Derivations> success_tab;
    private Derivations steps;
    private boolean verbose;

    /**
     * Constructor.
     *
     * @param grammar
     */
    public RCGParserBoullier(boolean v, Grammar grammar) {
        super(grammar);
        prdct_tab = new Hashtable<PredLabelKey, Boolean>();
        clause_tab = new Hashtable<ClauseKey, Boolean>();
        success_tab = new Hashtable<ClauseKey, Derivations>();
        verbose = v;
    }

    public boolean parse(List<Word> input) {
        // parsing is performed by the recognizeWrapper method
        // which stores the successful instantiation in
        // a dedicated list (derivation attribute)
        return (this.recognize(input));
    }

    public boolean parseSentence(boolean verbose, List<Word> sentence) {
        boolean bres = false;
        if ((bres = parse(sentence)) && verbose) {
            printDeriv(steps);
        }
        return bres;
    }

    public boolean recognize(List<Word> input) {
        Argument rl = Argument.argFromWord(input);
        List<Argument> al = new ArrayList<Argument>();
        al.add(rl);
        Derivations derivations = new Derivations();
        boolean res = prdct(((RCG) getGrammar()).getStartPredicateLabel(), al, new HashSet<PredLabel>(), derivations);
        // store the successful clauses in the steps attribute
        steps = derivations;
        // return results of parsing (and recognizing)
        return res;
    }

    /**
     * As in Boullier (2000) - treat predicates. True iff there exists a p-clause
     * in the grammar with which the given Argument can be reduced to epsilon.
     *
     * @param p  - a predicate label
     * @param al - a List of Argument
     * @return true if p can be reduced to epsilon given r.
     */
    public boolean prdct(PredLabel p, List<Argument> al, Set<PredLabel> trace, Derivations derivations) {
        //System.err.println("Trying predicate " + p.toString() + " with " + al.toString());

        boolean ret = false;
        //System.err.println(" === TRYING PREDLABEL: " + p.toString() + "\n");

        // Predicate memoization activated only for unsuccessful predicates
        // (to allow for parsing vs recognition)
        if (prdct_tab.containsKey(new PredLabelKey(p, al))) {
            //System.err.println(" ===> Predicate already computed : "+p.toString()+ " " + al.toString() + " result " + prdct_tab.get(new PredLabelKey(p, al)));
            return prdct_tab.get(new PredLabelKey(p, al));
        }

        int pclausesnum = ((RCG) getGrammar()).getClausesForLabelNum(p);
        // loop through all p-clauses
        for (int i = 0; i < pclausesnum; ++i) {
            Derivations dd = new Derivations();
            //System.err.println(" == NEXT CLAUSE: " + getGrammar().getClauseForLabel(p, i) + "( " + (i+1) + " / " + pclausesnum + " ) \n");
            ret |= clause(p, i, al, trace, dd);
            if (ret) {
                derivations.addSteps(null, dd, (RCG) getGrammar());
            }
        }

        if (!ret) {
            prdct_tab.put(new PredLabelKey(p, al), new Boolean(ret));
        }
        return ret;
    }

    /**
     * As in Boullier (2000) - treat clauses
     *
     * @param clabel - the label of the clause we check
     * @param cind   - an index in the list of all predicate labels
     * @param al     - a List of Argument
     * @return
     */
    @SuppressWarnings("unused")
    public boolean clause(PredLabel clabel, int cind, List<Argument> al, Set<PredLabel> trace, Derivations derivations) {

        System.err.println("Obsolete - don't call this");
        System.exit(255);

        // default value
        boolean ret = false;

        // fetch the clause
        Clause c = ((RCG) getGrammar()).getClauseForLabel(clabel, cind);
        if (verbose) {
            System.err.println(" -- TRYING CLAUSE " + c.toString() + " with " + al.toString());
        }

        // lookup in the tabulation
        if (clause_tab.containsKey(new ClauseKey(c.getCindex(), al))) {
            // if it is a success clause, we retrieve its derivation steps
            if (clause_tab.get(new ClauseKey(c.getCindex(), al))) {
                derivations.addSteps(null, success_tab.get(new ClauseKey(c.getCindex(), al)), (RCG) getGrammar());
            }
            //System.err.println(" -- RETURNING CLAUSE " + c.toString() + " with " + al.toString() + " res: " + ret);
            return clause_tab.get(new ClauseKey(c.getCindex(), al));
        }

        // this shouldn't happen.
        if (c == null) {
            return false;
        }

        // avoid cycles
        if (c.getRhs().size() == 1) {
            List<Argument> lhsargs = c.getLhs().getArgs();
            List<Argument> rhsargs = c.getRhs().get(0).getArgs();
            if (lhsargs.size() == rhsargs.size()) {
                int i = 0;
                for (; i < lhsargs.size() && lhsargs.get(i).equals(rhsargs.get(i)); ++i) ;
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

        Encode enc = new Encode(al, c.getLhs().getArgs(), new Hashtable<String, Integer>());
        try {
            bs = enc.buildAllBindings(verbose);
        } catch (RCGInstantiationException e) {
            if (verbose) {
                System.err.println(e);
            }
            //e.printStackTrace();
            // if the binding computation fails, we quit
            clause_tab.put(new ClauseKey(c.getCindex(), al), new Boolean(ret));
            return ret;
        }

        if (verbose) {
            System.err.println("Bindings : " + bs.toString() + "\n");
        }

        Iterator<Binding> it = bs.iterator();

        bindingsNext:
        while (it.hasNext()) {
            // a binding with respect to a clause
            Binding b = (Binding) it.next();

            boolean rhspRes = true;

            // new derivations collection
            Derivations lc = new Derivations();

            // loop through all RHS predicates
            Iterator<Predicate> rhsit = c.getRhs().iterator();
            while (rhsit.hasNext()) {
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

                // call prdct for this instantiation
                rhspRes &= prdct(rhsp.getLabel(), rhsp.getArgs(), trace, lc);
            }

            if (rhspRes) {
                //System.err.println("Adding success instantiation: " + c.toString() + " - bindings: " + b.toString() + "\n");

                derivations.addOne(new ClauseKey(c.getCindex(), al), (RCG) getGrammar());
                derivations.addSteps(null, lc, (RCG) getGrammar());
                success_tab.put(new ClauseKey(c.getCindex(), al), derivations);
            }

            ret |= rhspRes;
        }

        clause_tab.put(new ClauseKey(c.getCindex(), al), new Boolean(ret));

        //System.err.println(" -- RETURNING CLAUSE " + c.toString() + " with " + al.toString() + " res: " + ret);
        return ret;
    }

    public void printDeriv(Derivations derivations) {

        System.err.println("\nSuccessful clauses : ");
        for (int u = 0; u < derivations.size(); u++) {
            System.err.print(derivations.get(u).getId() + " ");
            Clause cc = ((RCG) getGrammar()).getClause(derivations.get(u).getCk().getCindex());
            System.err.println(cc.toString() + " <=> " + derivations.get(u).getCk().getArglist().toString());
        }
    }

    public Derivations getSteps() {
        return steps;
    }

    public Hashtable<ClauseKey, Derivations> getSuccess_tab() {
        return success_tab;
    }

    /*
     * just for testing
     */
    public static void main(String[] args) throws Exception {
/*		Grammar g = new RCG();
		((RCG)g).copylanguage();*/
        de.tuebingen.io.RCGReader r = new de.tuebingen.io.TextRCGReader(new File("/home/wmaier/workspace/TuebingenParser/test/rcg/rcg-prob.txt"));
        Grammar g = r.getRCG();

        System.err.println(g.toString());
        System.err.println("--------------------");

        RCGParserBoullier p = new RCGParserBoullier(false, g);
        BuiltinTokenizer tok = new BuiltinTokenizer();
        String s = " sieht hans mit_dem_teleskop";
        tok.setSentence(s);
        List<Word> l = tok.tokenize();
        System.err.println("Parse " + l.toString());
        long startTime = System.nanoTime();
        boolean res = p.parseSentence(true, l);
        long estimatedTime = System.nanoTime() - startTime;
        System.err.println("Result: " + Boolean.toString(res) + " computed in " + (estimatedTime) / (Math.pow(10, 9)) + " sec.");

    }


    // dummy

    public List<ClauseKey> getAnswers() {
        return null;
    }

    public List<ClauseKey> getEmptyRHS() {
        return null;
    }

    public Hashtable<ClauseKey, DStep> getParse() {
        return null;
    }

    public String printForest() {
        return null;
    }

    public ForestExtractorInitializer getForestExtractorInitializer() {
        return null;
    }

}
