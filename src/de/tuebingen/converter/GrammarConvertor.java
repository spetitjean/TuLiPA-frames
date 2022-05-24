/*
 *  File GrammarConvertor.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 11:10:39 CEST 2007
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
package de.tuebingen.converter;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tuebingen.rcg.ArgContent;
import de.tuebingen.rcg.Argument;
import de.tuebingen.rcg.Clause;
import de.tuebingen.rcg.PredComplexLabel;
import de.tuebingen.rcg.PredLabel;
import de.tuebingen.rcg.Predicate;
import de.tuebingen.rcg.RCG;
import de.tuebingen.tag.TagNode;
import de.tuebingen.tag.TagTree;
import de.tuebingen.tag.Tuple;

/**
 * @author parmenti
 */
public class GrammarConvertor {

    private List<Tuple> ttgrammar; // the anchored tt-mctag grammar
    private RCG rcggrammar; // the corresponding rcg
    private LinkedList<PredLabel> agenda; // the list of predicates to be
    // processed
    private LinkedList<PredLabel> processed; // the list of predicates already
    // processed
    private Map<String, TagTree> treeDict; // the dictionary of trees (for
    // direct access)
    private boolean verbose; // with or without debugging info
    private Integer k_lpasize; // maximum size of the LPA (if any)
    private boolean auto_adj; // to authorize auto-adjunctions
    private Integer depth; // to limit the depth of the conversion (i.e. to stop
    // creating new clauses after a given limit)

    public GrammarConvertor(List<Tuple> tt, boolean b, List<String> in,
                            Map<String, TagTree> treeHash) {
        verbose = b;
        ttgrammar = tt;
        rcggrammar = new RCG();
        agenda = new LinkedList<PredLabel>();
        processed = new LinkedList<PredLabel>();
        treeDict = treeHash;
        k_lpasize = null;
        auto_adj = false;
        depth = null;
    }

    public GrammarConvertor(List<Tuple> tt, boolean b, List<String> in,
                            Map<String, TagTree> treeHash, boolean a) {
        this(tt, b, in, treeHash);
        auto_adj = a;
    }

    public GrammarConvertor(List<Tuple> tt, boolean b, List<String> in,
                            Map<String, TagTree> treeHash, boolean a, int k) {
        this(tt, b, in, treeHash, a);
        if (k >= 0)
            k_lpasize = new Integer(k);
    }

    public GrammarConvertor(List<Tuple> tt, boolean b, List<String> in,
                            Map<String, TagTree> treeHash, boolean a, int k, int limit) {
        this(tt, b, in, treeHash, a, k);
        if (limit >= 0)
            depth = new Integer(limit); // the depth should maybe be
        // proportional to the size of the
        // sentence ? e.g. (in.size() * 3)
    }

    public void buildAllClauses(String axiom) {
        /**
         * Method used to build the RCG clauses from a list of anchored tuples
         *
         * @param axiom
         *            the axiom (category) of the grammar
         */

        // a) we first need to compute the adjunction sets
        // ie the list of head trees that can adjoin at a given node.

        long adjTime = System.nanoTime();
        AdjunctionSets partition = new AdjunctionSets(ttgrammar);
        long estADJTime = System.nanoTime() - adjTime;
        if (verbose) {
            System.err.println("Adjunction sets computation time: "
                    + (estADJTime) / (Math.pow(10, 9)) + " sec.");
        }

        // System.err.println("Hadj:
        // "+partition.toString(partition.getAuxiliaries()));
        // System.err.println("Aadj:
        // "+partition.toString(partition.getAllaux()));
        // System.err.println("Subs:
        // "+partition.toString(partition.getInitials()));

        // b) we start the conversion with _initial_ head trees
        // whose root is the axiom
        LinkedList<Object> lt = AdjunctionSets.getList(partition.getInitials(),
                new CatPairs(axiom, ""));
        // if there is no axiom-rooted tree
        if (lt == null) {
            lt = new LinkedList<Object>();
        }

        // we define the start predicate:
        PredComplexLabel spred = new PredComplexLabel(PredComplexLabel.START,
                axiom);
        rcggrammar.setStartPredicate(spred);

        for (int i = 0; i < lt.size(); i++) {
            TagTree t = (TagTree) lt.get(i);

            // we produce the corresponding starting clause using the starting
            // predicate
            PredComplexLabel pred = new PredComplexLabel(PredComplexLabel.TREE,
                    t.getId());
            Predicate sLHS = new Predicate(spred);
            Predicate sRHS = new Predicate(pred);
            Argument x = new Argument(new ArgContent(ArgContent.VAR, "X"));
            sLHS.addArg(x);
            sRHS.addArg(x);
            Clause c = new Clause();
            c.setLhs(sLHS);
            c.addToRhs(sRHS);
            rcggrammar.addClause(c, null);

            if (verbose) {
                System.err.print("NEXT ONE ======");
            }
            if (verbose) {
                System.err.println("== " + pred.toString(treeDict));
            }
            // we build the clauses for the tree t
            // given the current state of the lpa (first empty)
            // t's arguments and the adjunction sets
            // NB: it also updates the agenda
            buildTreeClauses(0, t, new LinkedList<Object>(),
                    partition.getArguments(), partition.getAllaux());
        }

        // c) we process the agenda (until it is empty)
        while (!(agenda.isEmpty())) {
            PredLabel nextpred = agenda.poll();
            PredComplexLabel nplab = (PredComplexLabel) nextpred;
            if (verbose) {
                System.err.print("NEXT ONE ======");
            }
            if (verbose) {
                System.err.println("== " + nplab.toString(treeDict));
            }
            if (k_lpasize == null
                    || nplab.getLpa().size() <= k_lpasize.intValue()) {
                // System.err.println("LPA size: "+nplab.getLpa().size());
                if (verbose)
                    System.err
                            .println("Conversion's depth: " + nplab.getDepth());
                if (depth == null || nplab.getDepth() <= depth) {
                    switch (nplab.getType()) {
                        case 0: // tree clause
                            TagTree tt = treeDict.get(nplab.getTreeid());
                            buildTreeClauses(nplab.getDepth(), tt, nplab.getLpa(),
                                    partition.getArguments(),
                                    partition.getAllaux());
                            break;
                        case 1: // adj branching clause
                            buildAdjBranchingClauses(nplab, nplab.getLpa(),
                                    partition.getAuxiliaries());
                            break;
                        case 2: // sub branching clause
                            buildSubBranchingClauses(nplab,
                                    partition.getInitials());
                            break;
                        default: // skip
                    }
                }
            } else {
                if (verbose) {
                    System.err.println("oversized LPA -> predicate discarded ("
                            + nplab.toString(treeDict) + ")");
                }
            }
        }
    }

    /**
     * Method used to build clauses associated with elementary trees
     *
     * @param t, lpa, args, adjsets
     *           t - a tree identifier (String)
     *           lpa - the list of pending arguments (List<String>)
     *           args - the list of argument trees (List<Object>)
     *           adjsets - the list of adjunction sets (hashtable)
     */
    public boolean buildTreeClauses(int cur_depth, TagTree t, List<Object> lpa,
                                    Hashtable<Object, LinkedList<Object>> tuplesDict,
                                    Hashtable<Object, LinkedList<Object>> adjsets) {

        if (verbose) {
            System.err.println("ELEMENTARY");
        }

        // 1. The LHS is common to all currently computed clauses:
        PredComplexLabel plab = new PredComplexLabel(PredComplexLabel.TREE,
                t.getId());
        plab.setLpa(lpa);
        plab.setDepth(cur_depth + 1);
        Predicate p = new Predicate(plab);
        LinkedList<ArgContent> largs = new LinkedList<ArgContent>();
        t.buildDecorationString(t.getRoot(), largs);

        boolean hasSep = false;
        LinkedList<Argument> realArgs = new LinkedList<Argument>();
        int kk = 0;
        Argument a = new Argument();
        for (kk = 0; kk < largs.size()
                && largs.get(kk).getType() != ArgContent.SEPARATOR; kk++) {
            a.addArg(largs.get(kk));
        }
        realArgs.add(a);
        if (kk < largs.size()
                && largs.get(kk).getType() == ArgContent.SEPARATOR) {
            hasSep = true;
        }

        kk++; // for the SEPARATOR
        a = new Argument();
        while (kk < largs.size()) {
            a.addArg(largs.get(kk));
            kk++;
        }
        if (a.size() > 0) {
            realArgs.add(a);
        } else if (hasSep) {
            realArgs.add(new Argument(new ArgContent(ArgContent.EPSILON, "")));
        }
        p.setArgs(realArgs);

        // 2. We build LPA2 made of the current LPA + the ids of t's arguments
        // (if t is a head!)
        LinkedList<Object> args = null;
        if (t.getIsHead()) { // if t is a head, we gather its tuple's arguments
            args = AdjunctionSets.getList(tuplesDict, t.getTupleId());
        }

        LinkedList<String> lpa2 = new LinkedList<String>();
        for (int j = 0; j < lpa.size(); j++) {
            lpa2.add(new String((String) lpa.get(j)));
        }
        if (args != null) { // the tuple has arguments
            for (int j = 0; j < args.size(); j++) {
                lpa2.add(((TagTree) args.get(j)).getId());
            }
        }

        // we build psi which maps node names (positions)
        // to their "authorized adjunctions" sets (depending on lpa)
        Hashtable<Object, LinkedList<Object>> psi = new Hashtable<Object, LinkedList<Object>>();
        t.computePsi(auto_adj, t.getRoot(), psi, lpa2, lpa, adjsets);

        // 2-a. We gather the adjunction node ids
        LinkedList<String> adjnodes = new LinkedList<String>();
        Set<Object> knodes = psi.keySet();
        Iterator<Object> i = knodes.iterator();
        while (i.hasNext()) {
            String k = (String) i.next();
            adjnodes.add(k);
        }
        // 2-b. We compute the distributions of lpa2 (lpa2 := lpa U args)
        // We prepare and compute the constraints on integers
        Prepare prep = new Prepare(t.getId(), lpa2, adjnodes);
        prep.computeConstraints(psi);
        // System.out.println("PSI is size "+psi.size()+"");

        if (psi.size() > 0) {
            // System.out.println(prep.toString());
            prep.solveConstraints();

            // 2-c. We decode the distribution
            // We need the list of nodes that will appear on the RHS
            // That is, adjunction and substitution nodes
            prep.decodeSolutions();
            // System.out.println(prep.toString());

            LinkedList<Hashtable<String, PredLabel>> sols = prep
                    .getDecodedSols();
            // for each solution, we traverse the tree t and produce a clause
            // with the pre-computed lhs and a new rhs
            if (verbose) {
                System.err.println("+++++ Number of distributions "
                        + sols.size() + " +++++");
            }
            for (int j = 0; j < sols.size(); j++) {
                // we retrieve the predicates for the sol
                Hashtable<String, PredLabel> predlabs = sols.get(j);
                // we create the future RHS
                LinkedList<Predicate> preds = new LinkedList<Predicate>();
                // and compute it!
                t.buildRHS(t.getRoot(), predlabs, preds);
                // With both LHS and RHS, we create a new clause
                Clause c = new Clause(p, preds);
                c.setLhs(p);
                c.setRhs(preds);
                // we copy the depth from LHS to RHS before storing in the
                // agenda
                if (depth != null)
                    c.updateRhsDepth();

                // Finally, we update the agenda and the grammar
                addAll2agenda(c.getRhs());
                rcggrammar.addClause(c, null);
            }
        } else {
            // if there is no distribution, we just traverse the tree
            // to collect the RHS predicates (without any constraint on LPA)
            LinkedList<Predicate> preds = new LinkedList<Predicate>();
            t.buildRHS(t.getRoot(), new Hashtable<String, PredLabel>(), preds);
            // With both LHS and RHS, we create a new clause
            Clause c = new Clause(p, preds);
            c.setLhs(p);
            c.setRhs(preds);
            // we copy the depth from LHS to RHS before storing in the agenda
            if (depth != null)
                c.updateRhsDepth();

            // Finally, we update the agenda and the grammar
            addAll2agenda(c.getRhs());
            rcggrammar.addClause(c, null);
        }

        return true;
    }

    public boolean buildAdjBranchingClauses(PredComplexLabel nplab,
                                            List<Object> lpa, Hashtable<Object, LinkedList<Object>> hadjsets) {
        /**
         * Processing of adjunction branching predicates
         */
        if (verbose) {
            System.err.println("BRANCHING (ADJ)");
        }
        if (verbose) {
            System.err.println(nplab.toString(treeDict));
        }

        int cur_depth = nplab.getDepth();

        // we retrieve the head trees that can adjoin on
        // the node described in the branching predicate:
        LinkedList<Object> adjs = AdjunctionSets.getList(hadjsets,
                nplab.getNodecat());

        LinkedList<String> adjids = new LinkedList<String>();

        if (adjs != null) { // if there are adjoinable trees
            // we gather the ids of the adjoinable trees
            // the adjunction is restricted with respect to some
            // key features (num, gen, case, phrase) cf static method checkAdj
            for (int i = 0; i < adjs.size(); i++) {
                TagTree tr = (TagTree) adjs.get(i);
                if (this.checkAdjWord(nplab.getTreeid(), nplab.getTupleId(),
                        nplab.getTupleAncPos(), nplab.getZenode(),
                        nplab.getLexNode(), tr, lpa)
                        && TagTree.checkAdj(nplab.getZenode(), tr)) {
                    adjids.add(tr.getId());
                }
            }
        }
        // we complete this list with members of the lpa
        for (int j = 0; j < lpa.size(); j++) {
            if (!(adjids.contains(lpa.get(j)))) {
                TagTree tr = treeDict.get(lpa.get(j));
                if (this.checkAdjWord(nplab.getTreeid(), nplab.getTupleId(),
                        nplab.getTupleAncPos(), nplab.getZenode(),
                        nplab.getLexNode(), tr, null)
                        && TagTree.checkAdj(nplab.getZenode(), tr)) {
                    adjids.add(new String((String) lpa.get(j)));
                }
            }
        }

        // we now process the list and create new clauses
        // NB: the LHS is common to all branching
        Predicate pLHS = new Predicate();
        pLHS.setLabel(new PredComplexLabel(nplab));
        ArgContent left = new ArgContent(ArgContent.VAR, "L");
        ArgContent right = new ArgContent(ArgContent.VAR, "R");
        pLHS.addArg(new Argument(left));
        pLHS.addArg(new Argument(right));

        // depth update:
        if (depth != null)
            ((PredComplexLabel) pLHS.getLabel()).setDepth(cur_depth + 1);

        if (adjids != null) {
            for (int j = 0; j < adjids.size(); j++) {
                String treeid = adjids.get(j);
                boolean aadj = auto_adj || !(treeid.equals(nplab.getTreeid()));
                if (aadj) {
                    Predicate pRHS = new Predicate();
                    PredComplexLabel pRHSlabel = new PredComplexLabel(
                            PredComplexLabel.TREE, treeid);

                    LinkedList<Object> newlpa = new LinkedList<Object>();
                    // we check if the tree comes from the LPA and update it
                    for (int k = 0; k < lpa.size(); k++) {
                        String id = (String) lpa.get(k);
                        if (!(id.equals(treeid))) {
                            newlpa.add(new String(id));
                        }
                    }

                    pRHSlabel.setLpa(newlpa);
                    pRHS.setLabel(pRHSlabel);
                    pRHS.addArg(new Argument(left));
                    pRHS.addArg(new Argument(right));

                    // we build the clause
                    Clause c = new Clause();
                    c.setLhs(pLHS);
                    c.addToRhs(pRHS);
                    // we copy the depth from LHS to RHS before storing in the
                    // agenda
                    if (depth != null)
                        c.updateRhsDepth();

                    // we update the rcggrammar and the agenda
                    add2agenda(pRHS);
                    rcggrammar.addClause(c, null);
                }
            }
        }

        // Eventually, we add the clause for un-adjoined nodes (RHS equals
        // epsilon):
        Predicate nadjLHS = new Predicate();
        PredComplexLabel nadjPCL = new PredComplexLabel(PredComplexLabel.ADJ,
                nplab.getTreeid());
        nadjPCL.setNodecat(nplab.getNodecat());
        nadjPCL.setNodeid(nplab.getNodeid());
        nadjLHS.setLabel(nadjPCL);
        nadjLHS.addArg(new Argument(new ArgContent(ArgContent.EPSILON, "")));
        nadjLHS.addArg(new Argument(new ArgContent(ArgContent.EPSILON, "")));
        Clause cc = new Clause();
        cc.setLhs(nadjLHS);
        rcggrammar.addClause(cc, null);

        return true;
    }

    public boolean buildSubBranchingClauses(PredComplexLabel nplab,
                                            Hashtable<Object, LinkedList<Object>> substsets) {
        /**
         * Processing of substitution branching predicates
         */
        if (verbose) {
            System.err.println("BRANCHING (SUBST)");
        }
        if (verbose) {
            System.err.println(nplab.toString(treeDict));
        }

        int cur_depth = nplab.getDepth();

        // we retrieve the trees that can be substituted on
        // the node described in the branching predicate:
        LinkedList<Object> subst = AdjunctionSets.getList(substsets,
                nplab.getNodecat());

        LinkedList<String> substids = new LinkedList<String>();

        if (subst != null) { // if there are adjoinable trees
            // we gather the ids of the substitutable trees
            // the substitution is restricted with respect to some
            // key features (num, gen, case, phrase) cf static method checkSubst
            for (int i = 0; i < subst.size(); i++) {
                TagTree tr = (TagTree) subst.get(i);
                if (this.checkSubstWord(nplab.getTreeid(), nplab.getTupleId(),
                        nplab.getTupleAncPos(), nplab.getZenode(),
                        nplab.getLexNode(), tr)
                        && TagTree.checkSubst(nplab.getZenode(), tr)) {
                    substids.add(tr.getId());
                }
            }
        }

        if (substids != null) {
            // we now process the list and create new clauses
            // NB: the LHS is common to all branching
            Predicate pLHS = new Predicate();
            pLHS.setLabel(new PredComplexLabel(nplab));
            ArgContent xarg = new ArgContent(ArgContent.VAR, "X");
            pLHS.addArg(new Argument(xarg));

            // depth update:
            if (depth != null)
                ((PredComplexLabel) pLHS.getLabel()).setDepth(cur_depth + 1);

            for (int i = 0; i < substids.size(); i++) {
                String stree = substids.get(i);
                Predicate pRHS = new Predicate();
                pRHS.setLabel(
                        new PredComplexLabel(PredComplexLabel.TREE, stree));
                pRHS.addArg(new Argument(xarg));
                Clause c = new Clause();
                c.setLhs(pLHS);
                c.addToRhs(pRHS);
                // we copy the depth from LHS to RHS before storing in the
                // agenda
                if (depth != null)
                    c.updateRhsDepth();

                // we update the rcggrammar and the agenda
                add2agenda(pRHS);
                rcggrammar.addClause(c, null);
            }
        }

        return true;
    }

    public static String print(Hashtable<String, LinkedList<Object>> psi) {
        String res = "";
        Set<String> knodes = psi.keySet();
        Iterator<String> i = knodes.iterator();
        while (i.hasNext()) {
            String k = (String) i.next();
            res += "Node address " + k + " - trees: { ";
            LinkedList<Object> atrees = psi.get(k);
            for (int j = 0; j < atrees.size(); j++) {
                res += atrees.get(j) + " ";
            }
            res += "}\n";
        }
        return res;
    }

    public void addAll2agenda(List<Predicate> plist) {
        for (int i = 0; i < plist.size(); i++) {
            add2agenda(plist.get(i));
        }
    }

    public void add2agenda(Predicate p) {
        PredLabel plab = p.getLabel();
        if (!(processed.contains(plab))) {
            String plabs = plab.toString();
            if (plab instanceof PredComplexLabel)
                plabs = ((PredComplexLabel) plab).toString(treeDict);
            if (verbose) {
                System.err.println(" -> added in the agenda : " + plabs);
            }
            agenda.add(plab);
            processed.add(plab);
        }
    }

    public boolean checkAdjWord(String treeid, String tupleId,
                                String tupleAncPos, TagNode nn, TagNode lex, TagTree tr,
                                List<Object> lpa) {
        // return true if the adjunction is allowed, false otherwise
        boolean res = true;
        if (tupleAncPos != null && tr.getTupleAncPos() != null) {
            // if we try to adjoin the argument of another tuple anchored by the
            // same word, we return false
            if (tr.getTupleAncPos().equals(tupleAncPos)
                    && !(tr.getTupleId().equals(tupleId))) {
                res = false;
            } else {
                // if the tree is anchored, and we are on the spine
                if (lex != null && (nn.getAddress().equals("0")
                        || lex.getAddress().startsWith(nn.getAddress()))) {
                    if (tr.isLeftAdj() && tr.hasLex()) {
                        int posi2 = ((TagNode) tr.getLexAnc()).getWord()
                                .getEnd();
                        int posi = lex.getWord().getEnd();
                        res = (posi2 < posi);
                    } else if (tr.isRightAdj() && tr.hasLex()) {
                        int posi2 = ((TagNode) tr.getLexAnc()).getWord()
                                .getEnd();
                        int posi = lex.getWord().getEnd();
                        res = (posi < posi2);
                    }
                }
            }
        }
        // if the lpa contains a not-compatible tree, we also reject the tree to
        // adjoin!
        // NB: by not-compatible, we mean we cannot use two tuples of a given
        // input word within the same derivation
        if (lpa != null) {
            for (int i = 0; i < lpa.size(); i++) {
                TagTree lpaTree = treeDict.get((String) lpa.get(i));
                if (tr.getTupleAncPos().equals(lpaTree.getTupleAncPos())
                        && !(tr.getTupleId().equals(lpaTree.getTupleId()))) {
                    res = false;
                }
            }
        }
        if (verbose && !res) {
            System.err.println("\n ::: Adjunction of "
                    + treeDict.get(tr.getId()).getOriginalId() + " on "
                    + treeDict.get(treeid).getOriginalId() + " at node "
                    + nn.getAddress() + " discarded. ::: \n");
        }
        return res;
    }

    public boolean checkSubstWord(String treeid, String tupleId,
                                  String tupleAncPos, TagNode nn, TagNode lex, TagTree tr) {
        boolean res = true;
        // if we try to substitute the head of another tuple anchored by the
        // same word, we return false
        if (tupleAncPos != null && tr.getTupleAncPos().equals(tupleAncPos)
                && !(tr.getTupleId().equals(tupleId))) {
            res = false;
        } else {
            if (lex != null && tr.hasLex()) {
                // NB1: tr should always be lexicalised as an initial tree
                int posi2 = ((TagNode) tr.getLexAnc()).getWord().getEnd();
                int posi = lex.getWord().getEnd();
                if (nn.getAddress().compareTo(lex.getAddress()) < 0) { // "left
                    // substitution"
                    res = (posi2 < posi);
                } else { // "right substitution"
                    res = (posi < posi2);
                }
                // NB2: the test should never applies on the root
                // since a tree cannot both have a lex node and be a root!
            }
        }
        if (verbose && !res) {
            System.err.println("\n ::: Substitution of "
                    + treeDict.get(tr.getId()).getOriginalId() + " on "
                    + treeDict.get(treeid).getOriginalId()
                    + " discarded. ::: \n");
        }
        return res;
    }

    public List<Tuple> getTtgrammar() {
        return ttgrammar;
    }

    public void setTtgrammar(List<Tuple> ttgrammar) {
        this.ttgrammar = ttgrammar;
    }

    public RCG getRcggrammar() {
        return rcggrammar;
    }

    public void setRcggrammar(RCG rcggrammar) {
        this.rcggrammar = rcggrammar;
    }

    public LinkedList<PredLabel> getAgenda() {
        return agenda;
    }

    public void setAgenda(LinkedList<PredLabel> agenda) {
        this.agenda = agenda;
    }

    public LinkedList<PredLabel> getProcessed() {
        return processed;
    }

    public void setProcessed(LinkedList<PredLabel> processed) {
        this.processed = processed;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Map<String, TagTree> getTreeDict() {
        return treeDict;
    }

    public void setTreeDict(Hashtable<String, TagTree> treeDict) {
        this.treeDict = treeDict;
    }

    public Integer getK_lpasize() {
        return k_lpasize;
    }

    public void setK_lpasize(Integer k_lpasize) {
        this.k_lpasize = k_lpasize;
    }

    public boolean isAuto_adj() {
        return auto_adj;
    }

    public void setAuto_adj(boolean auto_adj) {
        this.auto_adj = auto_adj;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public String toString() {
        String res = "\nRCG:\n";
        res += rcggrammar.toString();
        return res;
    }

    // for pretty printing
    public String toString(Map<String, TagTree> dict) {
        String res = "\nRCG:\n";
        res += rcggrammar.toString(dict);
        return res;
    }

}
