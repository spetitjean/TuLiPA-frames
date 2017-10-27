/*
 *  File RCG.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:56:02 CEST 2007
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
package de.tuebingen.rcg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.MorphEntry;
import de.tuebingen.tag.TagNode;
import de.tuebingen.tag.TagTree;
import de.tuebingen.tag.Tuple;
import de.tuebingen.tree.Grammar;
import de.tuebingen.util.MathUtilities;

/**
 * Represents a Range Concatenation Grammar (Boullier 2000)
 * 
 * @author wmaier
 *
 */
public class RCG implements Grammar {

    private int k;
    private List<Clause> clauses;
    private Map<PredLabel, List<Clause>> clausesByLhsLabel;
    private Map<PredLabel, Integer> arityByLhsLabel;
    private PredLabel startPredicate;
    private boolean needsAnchoring;
    private Map<String, Integer> categories; // compute adjunction restrictions
                                             // wrt the modifiers in the
                                             // subgrammar
    private boolean termtransformed = false;

    public RCG() {
        clauses = new LinkedList<Clause>();
        clausesByLhsLabel = new Hashtable<PredLabel, List<Clause>>();
        arityByLhsLabel = new Hashtable<PredLabel, Integer>();
        startPredicate = null;
        k = 0;
        needsAnchoring = false;
        categories = new Hashtable<String, Integer>();
    }

    public int getK() {
        return k;
    }

    public int getCurIdx() {
        return clauses.size();
    }

    public boolean needsAnchoring() {
        return needsAnchoring;
    }

    public Map<String, Integer> getCategories() {
        return categories;
    }

    public void setNeedsAnchoring(boolean needsAnchoring) {
        this.needsAnchoring = needsAnchoring;
    }

    public boolean addClause(Clause c, Map<String, TagTree> dict) {
        // can't add a clause without lhs
        Predicate p = c.getLhs();
        if (p == null) {
            return false;
        }
        PredLabel pl = (PredLabel) p.getLabel();
        // can't add a clause without lhs label
        if (pl == null) {
            return false;
        }
        // will return false if we have the same predicate with another arity
        if (!setArity(pl, p.getArity())) {
            return false;
        }
        // can't add the same clause twice
        if (clauses.contains(c)) {
            // System.err.println("Duplicate clause: " + c.toString());
            return false;
        }
        List<Clause> cl = clausesByLhsLabel.get(pl);
        if (cl == null) {
            cl = new LinkedList<Clause>();
            clausesByLhsLabel.put(pl, cl);
        }
        c.setCindex(this.getCurIdx());
        cl.add(c);
        clauses.add(c);
        k = MathUtilities.max(k, p.getArity());
        // for local optimisation wrt de facto null-adjunction in a subgrammar
        if (dict != null && c.getLhs().getLabel() instanceof PredComplexLabel) {
            PredComplexLabel plabel = ((PredComplexLabel) c.getLhs()
                    .getLabel());
            if (plabel.getType() == PredComplexLabel.TREE) {
                TagNode foot = ((TagNode) dict.get(plabel.getTreeid())
                        .getFoot());
                if (foot != null) {
                    String footCat = foot.getCategory();
                    if (categories.containsKey(footCat))
                        categories.put(footCat, categories.get(footCat) + 1);
                    else
                        categories.put(footCat, 1);
                }
            }
        }
        return true;
    }

    public boolean addGrammar(RCG other, Map<String, TagTree> dict) {
        boolean res = true;
        // note that the start predicates must match!
        if (startPredicate == null)
            startPredicate = other.getStartPredicateLabel();
        List<Clause> otherClauses = other.getClauses();
        for (int i = 0; i < otherClauses.size(); i++) {
            Clause c = otherClauses.get(i);
            this.addClause(c, dict);
        }
        return res;
    }

    public int getClauseNum() {
        return clauses.size();
    }

    public List<Clause> getClauses() {
        return clauses;
    }

    public Clause getClause(int ind) {
        return clauses.get(ind);
    }

    public List<Clause> getClausesForLabel(PredLabel label) {
        return clausesByLhsLabel.get(label);
    }

    public Clause getClauseForLabel(PredLabel label, int ind) {
        Clause ret = null;
        List<Clause> l = clausesByLhsLabel.get(label);
        if (l != null) {
            ret = l.get(ind);
        }
        return ret;
    }

    // number of clauses with a given label
    public int getClausesForLabelNum(PredLabel label) {
        int ret = -1;
        List<?> l = clausesByLhsLabel.get(label);
        if (l != null) {
            ret = l.size();
        }
        return ret;
    }

    private boolean setArity(PredLabel label, int arity) {
        int oldArity = getArity(label);
        if (oldArity == 0) {
            arityByLhsLabel.put(label, Integer.valueOf(arity));
            return true;
        }
        if (oldArity == arity) {
            return true;
        }
        return false;
    }

    public int getArity(PredLabel label) {
        Integer arity = arityByLhsLabel.get(label);
        if (arity == null) {
            return 0;
        }
        return arity.intValue();
    }

    public boolean startPredicateDefined() {
        return startPredicate != null;
    }

    public PredLabel getStartPredicateLabel() {
        return startPredicate;
    }

    public void setStartPredicate(PredLabel startPredicate) {
        this.startPredicate = startPredicate;
    }

    /**
     * transforms the grammar such all clauses have only variables
     * as their arguments or are of the form
     * T(a) -> eps
     * or
     * T(eps) -> eps
     * 
     * Preliminary, since based on pred-string labels.
     */
    public void termtransform() {
        // grammar-wide list for terminal clauses
        HashMap<String, Clause> terminalcls = new HashMap<String, Clause>();
        for (Clause cl : clauses) {
            Map<ArgContent, List<Predicate>> psort = new HashMap<ArgContent, List<Predicate>>();

            // FIXME: not yet implemented: A(X^1_1\ldotsX^1_k, \ldots,
            // X^1_m\ldots X^k_m) \to \epsilon results in undefined behavior
            // clause-wide list for additional terminal predicates with counter
            // per terminal to ensure unique names
            Map<String, Integer> terms = new HashMap<String, Integer>();
            // do transformation
            List<Predicate> preds = new ArrayList<Predicate>(cl.getRhs());
            preds.add(cl.getLhs());
            // change terminals in arguments to variables and (globally) create
            // the corresponding clauses
            for (Predicate pred : preds) {
                for (Argument arg : pred.getArgs()) {
                    for (ArgContent argc : arg) {
                        if (argc.getType() == ArgContent.TERM
                                || argc.getType() == ArgContent.EPSILON) {
                            if (!terminalcls.containsKey(argc.getName())) {
                                Argument tlhsarg = new Argument();
                                // it's again a CONST (terminal) or an EPS
                                tlhsarg.addArg(new ArgContent(argc.getType(),
                                        argc.getName()));
                                Predicate tlhs = new Predicate(
                                        new PredStringLabel(
                                                "T_" + argc.getName()));
                                tlhs.addArg(tlhsarg);
                                Clause terminalcl = new Clause();
                                terminalcl.setLhs(tlhs);
                                terminalcls.put(argc.getName(), terminalcl);
                            }
                            if (!terms.containsKey(argc.getName())) {
                                terms.put(argc.getName(), 0);
                            } else {
                                terms.put(argc.getName(),
                                        terms.get(argc.getName()) + 1);
                            }
                            argc.setType(ArgContent.VAR);
                            argc.setName(argc.getName()
                                    + terms.get(argc.getName()).toString()
                                    + "_t");
                        }
                    }
                }

                if (!pred.equals(cl.getLhs())) {
                    if (!psort.containsKey(pred.getFirst())) {
                        psort.put(pred.getFirst(), new LinkedList<Predicate>());
                    }
                    psort.get(pred.getFirst()).add(pred);
                }

            }
            // create the predicates for the clause
            for (String termname : terms.keySet()) {
                for (int i = terms.get(termname); i >= 0; --i) {
                    String term = termname + String.valueOf(i);
                    Argument tlhsarg = new Argument();
                    tlhsarg.addArg(new ArgContent(ArgContent.VAR, term + "_t"));
                    Predicate pred = new Predicate(
                            new PredStringLabel("T_" + termname));
                    pred.addArg(tlhsarg);
                    if (!psort.containsKey(pred.getFirst())) {
                        psort.put(pred.getFirst(), new LinkedList<Predicate>());
                    }
                    psort.get(pred.getFirst()).add(pred);
                }
            }

            List<Predicate> newrhs = new ArrayList<Predicate>();
            Set<ArgContent> added = new HashSet<ArgContent>();
            for (ArgContent argc : cl.getArgcList()) {
                if (psort.containsKey(argc)) {
                    newrhs.addAll(psort.get(argc));
                    added.add(argc);
                }
            }

            // for the stuff that only appears on the RHS
            psort.keySet().removeAll(added);
            for (ArgContent argc : psort.keySet()) {
                newrhs.addAll(psort.get(argc));
            }

            cl.setRhs(newrhs);

            /*
             * ArrayList<Predicate> newrhs = new ArrayList<Predicate>();
             * newrhs.add(pred);
             * newrhs.addAll(rhs);
             * cl.setRhs(newrhs);
             */

            cl.calcRangeConstraintVector();

        }
        for (Clause cl : terminalcls.values()) {
            cl.calcRangeConstraintVector();
            this.addClause(cl, null);
        }

        termtransformed = true;
    }

    public boolean isTermtransformed() {
        return termtransformed;
    }

    public void calcAllRangeConstraintVectors() {
        for (Clause cl : clauses) {
            cl.calcRangeConstraintVector();
            // we do no longer pre-compute the vector for we want unique names:
            // cl.createVect(new NameFactory());
        }
    }

    public String print() {
        String ret = k + "-RCG:\n";
        ret += "Start predicate: " + getStartPredicateLabel().toString() + "\n";
        Set<PredLabel> hlabels = clausesByLhsLabel.keySet();
        Iterator<PredLabel> it = hlabels.iterator();
        while (it.hasNext()) {
            PredLabel label = (PredLabel) it.next();
            List<Clause> clauseset = clausesByLhsLabel.get(label);
            Iterator<Clause> clauseit = clauseset.iterator();
            while (clauseit.hasNext()) {
                Clause clause = (Clause) clauseit.next();
                int index = clause.getCindex();
                String ind = index + "";
                if (index < 10)
                    ind = "00" + ind;
                else if (index < 100)
                    ind = "0" + ind;
                ret += "(C_" + ind + ") ";
                ret += clause.print() + "\n";
            }
        }
        ret = ret.trim();
        return ret;
    }

    public String toString() {
        String ret = k + "-RCG:\n";
        ret += "Start predicate: " + getStartPredicateLabel().toString() + "\n";
        Set<PredLabel> hlabels = clausesByLhsLabel.keySet();
        Iterator<PredLabel> it = hlabels.iterator();
        while (it.hasNext()) {
            PredLabel label = (PredLabel) it.next();
            List<Clause> clauseset = clausesByLhsLabel.get(label);
            Iterator<Clause> clauseit = clauseset.iterator();
            while (clauseit.hasNext()) {
                Clause clause = (Clause) clauseit.next();
                int index = clause.getCindex();
                String ind = index + "";
                if (index < 10)
                    ind = "00" + ind;
                else if (index < 100)
                    ind = "0" + ind;
                ret += "(C_" + ind + ") ";
                ret += clause.toString() + "\n";
            }
        }
        ret = ret.trim();
        return ret;
    }

    // for pretty printing
    public String toString(Map<String, TagTree> dict) {
        String ret = k + "-RCG:\n";
        ret += "Start predicate: " + getStartPredicateLabel().toString() + "\n";
        Set<PredLabel> hlabels = clausesByLhsLabel.keySet();
        Iterator<PredLabel> it = hlabels.iterator();
        while (it.hasNext()) {
            PredLabel label = (PredLabel) it.next();
            List<Clause> clauseset = clausesByLhsLabel.get(label);
            Iterator<Clause> clauseit = clauseset.iterator();
            while (clauseit.hasNext()) {
                Clause clause = (Clause) clauseit.next();
                int index = clause.getCindex();
                String ind = index + "";
                if (index < 10)
                    ind = "00" + ind;
                else if (index < 100)
                    ind = "0" + ind;
                ret += "(C_" + ind + ") ";
                ret += clause.toString(dict) + "\n";
            }
        }
        ret = ret.trim();
        return ret;
    }

    /*
     * Load a grammar G with L(G) = { {a,b}^n | n%2 = 0}
     */
    public void copylanguage() {
        // S(XY) -> a(X,Y) ****************************
        Clause start = new Clause();
        // S(XY)
        Predicate startp = new Predicate();
        PredLabel spl = new PredStringLabel("S");
        startp.setLabel(spl);
        setStartPredicate(spl);
        // XY
        Argument myrl = new Argument();
        ArgContent myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        start.setLhs(startp);
        // a(X,Y)
        startp = new Predicate();
        startp.setLabel(new PredStringLabel("a"));
        LinkedList<Argument> myrl2 = new LinkedList<Argument>();
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl2.add(new Argument(myrange));
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl2.add(new Argument(myrange));
        startp.setArgs(myrl2);
        start.addToRhs(startp);

        // a(aX,aY) -> a(X,Y) ***************************
        // a(aX,aY)
        Clause aclause = new Clause();
        startp = new Predicate();
        PredStringLabel z = new PredStringLabel("a");
        startp.setLabel(z);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "a");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "a");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        aclause.setLhs(startp);
        // a(X,Y)
        startp = new Predicate();
        startp.setLabel(new PredStringLabel("a"));
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        aclause.addToRhs(startp);

        // a(bX,bY) -> a(X,Y) ***************************
        // a(bX,bY)
        Clause bclause = new Clause();
        startp = new Predicate();
        z = new PredStringLabel("a");
        startp.setLabel(z);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "b");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "b");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        bclause.setLhs(startp);
        // a(X,Y)
        startp = new Predicate();
        startp.setLabel(new PredStringLabel("a"));
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        bclause.addToRhs(startp);

        // a(EPS,EPS) -> EPS
        Clause epsilonclause = new Clause();
        startp = new Predicate();
        z = new PredStringLabel("a");
        startp.setLabel(z);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.EPSILON, "Eps");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.EPSILON, "Eps");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        epsilonclause.setLhs(startp);

        addClause(start, null);
        addClause(aclause, null);
        addClause(bclause, null);
        addClause(epsilonclause, null);

    }

    public void lata() {
        // S(XY) -> T(X)U(Y) ****************************
        Clause start = new Clause();
        // S(XY)
        Predicate startp = new Predicate();
        PredLabel spl = new PredStringLabel("S");
        startp.setLabel(spl);
        setStartPredicate(spl);
        // XY
        Argument myrl = new Argument();
        ArgContent myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        start.setLhs(startp);
        // T(X)
        startp = new Predicate();
        startp.setLabel(new PredStringLabel("T"));
        LinkedList<Argument> myrl2 = new LinkedList<Argument>();
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl2.add(new Argument(myrange));
        startp.setArgs(myrl2);
        start.addToRhs(startp);
        // U(Y)
        startp = new Predicate();
        startp.setLabel(new PredStringLabel("U"));
        myrl2 = new LinkedList<Argument>();
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl2.add(new Argument(myrange));
        startp.setArgs(myrl2);
        start.addToRhs(startp);

        // T(a) -> epsilon ***************************
        // T(a)
        Clause aclause = new Clause();
        startp = new Predicate();
        PredStringLabel z = new PredStringLabel("T");
        startp.setLabel(z);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "a");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        aclause.setLhs(startp);

        // T(eps) -> eps ***************************
        // T(eps)
        Clause bclause = new Clause();
        startp = new Predicate();
        z = new PredStringLabel("T");
        startp.setLabel(z);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.EPSILON, "Eps");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        bclause.setLhs(startp);

        // U(a) -> eps ***************************
        // U(a)
        Clause bbclause = new Clause();
        startp = new Predicate();
        z = new PredStringLabel("U");
        startp.setLabel(z);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "a");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        bbclause.setLhs(startp);

        addClause(start, null);
        addClause(aclause, null);
        addClause(bclause, null);
        addClause(bbclause, null);

    }

    public void anbn() {
        // S(XY) -> a(X,Y) ****************************
        Clause start = new Clause();
        // S(XY)
        Predicate startp = new Predicate();
        PredLabel spl = new PredStringLabel("S");
        startp.setLabel(spl);
        setStartPredicate(spl);
        // XY
        Argument myrl = new Argument();
        ArgContent myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        start.setLhs(startp);
        // a(X,Y)
        startp = new Predicate();
        startp.setLabel(new PredStringLabel("a"));
        LinkedList<Argument> myrl2 = new LinkedList<Argument>();
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl2.add(new Argument(myrange));
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl2.add(new Argument(myrange));
        startp.setArgs(myrl2);
        start.addToRhs(startp);

        // a(bX,bY) -> a(X,Y) ***************************
        // a(aX,bY)
        Clause abclause = new Clause();
        startp = new Predicate();
        PredLabel z = new PredStringLabel("a");
        startp.setLabel(z);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "a");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "b");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        abclause.setLhs(startp);
        // a(X,Y)
        startp = new Predicate();
        startp.setLabel(new PredStringLabel("a"));
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        abclause.addToRhs(startp);

        // a(EPS,EPS) -> EPS
        Clause epsilonclause = new Clause();
        startp = new Predicate();
        z = new PredStringLabel("a");
        startp.setLabel(z);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.EPSILON, "Eps");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.EPSILON, "Eps");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        epsilonclause.setLhs(startp);

        addClause(start, null);
        addClause(abclause, null);
        addClause(epsilonclause, null);
    }

    public void forestCheck() {
        // S(XYZ) -> a(X,Y)b(Z) ****************************
        Clause start = new Clause();
        // S(XY)
        Predicate startp = new Predicate();
        PredLabel spl = new PredStringLabel("S");
        startp.setLabel(spl);
        setStartPredicate(spl);
        // XYZ
        Argument myrl = new Argument();
        ArgContent myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Z");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        start.setLhs(startp);
        // a(X,Y)
        startp = new Predicate();
        startp.setLabel(new PredStringLabel("a"));
        LinkedList<Argument> myrl2 = new LinkedList<Argument>();
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl2.add(new Argument(myrange));
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl2.add(new Argument(myrange));
        startp.setArgs(myrl2);
        start.addToRhs(startp);
        // b(Z)
        startp = new Predicate();
        startp.setLabel(new PredStringLabel("b"));
        myrl2 = new LinkedList<Argument>();
        myrange = new ArgContent(ArgContent.VAR, "Z");
        myrl2.add(new Argument(myrange));
        startp.setArgs(myrl2);
        start.addToRhs(startp);

        // a(aX,aY) -> a(X,Y) ***************************
        // a(aX,aY)
        Clause aclause = new Clause();
        startp = new Predicate();
        PredStringLabel z = new PredStringLabel("a");
        startp.setLabel(z);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "a");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "a");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        aclause.setLhs(startp);
        // a(X,Y)
        startp = new Predicate();
        startp.setLabel(new PredStringLabel("a"));
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        aclause.addToRhs(startp);

        // a(aX,aY) -> a(bX,bY) ***************************
        // a(aX,aY)
        Clause bbclause = new Clause();
        startp = new Predicate();
        z = new PredStringLabel("a");
        startp.setLabel(z);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "a");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "a");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        bbclause.setLhs(startp);
        // a(bX,bY)
        startp = new Predicate();
        z = new PredStringLabel("a");
        startp.setLabel(z);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "b");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "b");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        bbclause.addToRhs(startp);

        // a(bX,bY) -> a(X,Y)a(Eps,Eps) ***************************
        // a(bX,bY)
        Clause bclause = new Clause();
        startp = new Predicate();
        z = new PredStringLabel("a");
        startp.setLabel(z);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "b");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "b");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        bclause.setLhs(startp);
        // a(X,Y)
        startp = new Predicate();
        startp.setLabel(new PredStringLabel("a"));
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.VAR, "X");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.VAR, "Y");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        bclause.addToRhs(startp);
        // a(Eps,Eps)
        startp = new Predicate();
        startp.setLabel(new PredStringLabel("a"));
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.EPSILON, "Eps");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.EPSILON, "Eps");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        bclause.addToRhs(startp);

        // b(bZ) -> a(bZ,bZ) **************
        // b(bZ)
        Clause cclause = new Clause();
        startp = new Predicate();
        z = new PredStringLabel("b");
        startp.setLabel(z);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "b");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Z");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        cclause.setLhs(startp);
        // a(bZ,bZ)
        startp = new Predicate();
        startp.setLabel(new PredStringLabel("a"));
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "b");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Z");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.TERM, "b");
        myrl.addArg(myrange);
        myrange = new ArgContent(ArgContent.VAR, "Z");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        cclause.addToRhs(startp);

        // a(EPS,EPS) -> EPS ******************
        Clause epsilonclause = new Clause();
        startp = new Predicate();
        z = new PredStringLabel("a");
        startp.setLabel(z);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.EPSILON, "Eps");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        myrl = new Argument();
        myrange = new ArgContent(ArgContent.EPSILON, "Eps");
        myrl.addArg(myrange);
        startp.addArg(myrl);
        epsilonclause.setLhs(startp);

        addClause(start, null);
        addClause(aclause, null);
        addClause(bbclause, null);
        addClause(bclause, null);
        addClause(cclause, null);
        addClause(epsilonclause, null);

    }

    // Inherited abstract methods
    public Map<String, List<Tuple>> getGrammar() {
        return null;
    }

    public Map<String, List<Lemma>> getLemmas() {
        return null;
    }

    public Map<String, List<MorphEntry>> getMorphEntries() {
        return null;
    }

    public void setLemmas(Map<String, List<Lemma>> lemmas) {
    }

    public void setMorphEntries(Map<String, List<MorphEntry>> morphEntries) {
    }

}
