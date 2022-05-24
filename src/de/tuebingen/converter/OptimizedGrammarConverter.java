package de.tuebingen.converter;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tuebingen.rcg.ArgContent;
import de.tuebingen.rcg.Argument;
import de.tuebingen.rcg.LPAInstructionClause;
import de.tuebingen.rcg.PredComplexLabel;
import de.tuebingen.rcg.PredLabel;
import de.tuebingen.rcg.Predicate;
import de.tuebingen.rcg.RCG;
import de.tuebingen.tag.TagNode;
import de.tuebingen.tag.TagTree;
import de.tuebingen.tag.Tuple;
import de.tuebingen.tree.Node;

/**
 * @author parmenti, jdellert
 */

public class OptimizedGrammarConverter {
    private List<Tuple> ttgrammar; // the anchored tt-mctag grammar
    private RCG rcggrammar; // the corresponding rcg
    private Map<String, TagTree> treeDict; // the dictionary of trees (for direct access)
    private boolean verbose; // with or without debugging info
    private boolean autoAdj;

    public OptimizedGrammarConverter(List<Tuple> tt, boolean b, List<String> in, Map<String, TagTree> treeHash, boolean auto_adj) {
        verbose = true;
        ttgrammar = tt;
        rcggrammar = new RCG();
        treeDict = treeHash;
        autoAdj = auto_adj;
    }

    public RCG getRcggrammar() {
        return rcggrammar;
    }

    public void buildAllClauses(String axiom) {
        /**
         * Method used to build the RCG clauses from a list of anchored tuples
         *
         * @param axiom
         *            the axiom (category) of the grammar
         */

        // compute adjunction set ( = head trees that can adjoin) for each node
        long adjTime = System.nanoTime();
        AdjunctionSets partition = new AdjunctionSets(ttgrammar);
        long estADJTime = System.nanoTime() - adjTime;
        if (verbose) {
            System.err.println("Adjunction sets computation time: " + (estADJTime) / (Math.pow(10, 9)) + " sec.");
        }

        // System.err.println("Hadj: "+partition.toString(partition.getAuxiliaries()));
        // System.err.println("Aadj: "+partition.toString(partition.getAllaux()));
        // System.err.println("Subs: "+partition.toString(partition.getInitials()));

        // collect all trees and all head trees
        List<TagTree> allTrees = new LinkedList<TagTree>();
        List<TagTree> allHeadTrees = new LinkedList<TagTree>();

        for (Tuple tpl : ttgrammar) {
            allTrees.add(tpl.getHead());
            allHeadTrees.add(tpl.getHead());
            if (tpl.getArguments() != null) {
                allTrees.addAll(tpl.getArguments());
            }
        }

        // CASE 1 : generate S(X) -> <a>(X) for each initial head tree a with axiom root

        LinkedList<Object> lt = AdjunctionSets.getList(partition.getInitials(), new CatPairs(axiom, ""));
        // if there is no axiom-rooted tree
        if (lt == null) {
            lt = new LinkedList<Object>();
        }

        // start predicate:
        PredComplexLabel spred = new PredComplexLabel(PredComplexLabel.START, axiom);
        rcggrammar.setStartPredicate(spred);

        for (int i = 0; i < lt.size(); i++) {
            TagTree t = (TagTree) lt.get(i);

            // we produce the corresponding starting clause using the starting predicate
            PredComplexLabel pred = new PredComplexLabel(PredComplexLabel.TREE, t.getId());
            Predicate sLHS = new Predicate(spred);
            Predicate sRHS = new Predicate(pred);
            Argument x = new Argument(new ArgContent(ArgContent.VAR, "X"));
            sLHS.addArg(x);
            sRHS.addArg(x);
            LPAInstructionClause c = new LPAInstructionClause();
            c.setLhs(sLHS);
            c.addToRhs(sRHS);
            rcggrammar.addClause(c, null);
        }

        // CASE 2: compute possible adjunctions and substitutions for each initial tree

        for (List<Object> initList : partition.getInitials().values()) {
            for (Object init : initList) {
                TagTree initTree = (TagTree) init;
                List<Object> args = partition.getArguments().get(initTree.getTupleId());
                if (args != null) {
                    if (verbose)
                        System.err.println("Found " + args.size() + " arguments for tuple ID " + initTree.getTupleId());
                    boolean[][] allowedAdjunction = computeAdjunctionMatrix(initTree, args, partition);
                    int n = args.size();
                    int m = initTree.numNodes(initTree.getRoot());
                    System.err.println("Testing " + n + " adjunctions at " + m + " nodes in inital tree.");
                    //numbers between 0 and m^n represent n arguments distributed into m LPAs
                    for (int c = 0; c < Math.pow(m, n); c++) {
                        int r = (int) Math.pow(m, n);
                        int i = n - 1;
                        //determine whether distribution with number c violates any constraints
                        //i + 1 is the number of arguments that are left for distribution over the LPAs
                        //r and q serve to decode the distribution c into single adjunctions that can be tested
                        while (i >= 0) {
                            r /= m;
                            int q = c / r;
                            System.err.println("Testing adjunction at [" + q + "][" + i + "]");
                            if (allowedAdjunction[q][i]) {
                                System.err.println("Adjunction allowed! New c: " + c % m);
                                c %= r;
                                i--;
                            } else {
                                System.err.println("Adjunction not allowed! Jumping over " + r + " alternatives, new c: " + (c + r));
                                //jump over all the possibilites with the prefix now excluded
                                c += r - 1;
                                break;
                            }
                        }
                        //successfully checked against all constraints (and distributed all arguments), build the corresponding clause
                        if (i == -1) {
                            System.err.println("Bulding case 2 LPA clauses!");

                            //BUILD CORRESPONDING CLAUSES HERE
                            //PredComplexLabel lhsLabel = new PredComplexLabel(PredComplexLabel.ADJ, initTree.getId());

                            Hashtable<String, PredLabel> mapping = new Hashtable<String, PredLabel>();
                            LinkedList<LinkedList<Object>> lpas = new LinkedList<LinkedList<Object>>();
                            //decode from c which pending arguments were added to which LPAs!
                            for (int k = 0; k < m; k++) {
                                lpas.add(new LinkedList<Object>());
                            }
                            r = (int) Math.pow(m, n);
                            i = n - 1;
                            while (i >= 0) {
                                r /= m;
                                int q = c / r;
                                lpas.get(q).add(args.get(i).toString());
                                c %= r;
                                i--;
                            }
                            //PROBLEM: were do we get the substitutions and adjunctions from?
                            //Did I just compute that, and the LPAs are still to be done?
                            //-reinvestigate the old conversion more closely and see how pieces fit together there
                            for (int k = 0; k < m; k++) {
                                //generate the different labs for the RHS to build clauses
                                PredComplexLabel lab = new PredComplexLabel(PredComplexLabel.ADJ, initTree.getId());
                                lab.setNodeid(initTree.getAllNodes().get(k).toString());
                                lab.setLpa(lpas.get(k));
                                mapping.put(lab.getNodeid(), lab);
                            }


                        }
                    }
                } else {
                    if (verbose) System.err.println("No arguments found for tuple ID " + initTree.getTupleId());
                }
            }
        }

        // CASE 3: compute possible adjunctions and substitutions for each auxiliary tree (uses LPA variables)

        // CASE 4: generate <adj,t,n,LPA>(L,R) -> <b,LPA>(L,R) for b in Adj(t,n) and b head tree
        // CASE 5: generate <adj,t,n,LPA>(L,R) -> <b,LPA\{b}>(L,R) for b in Adj(t,n)
        // CASE 6: generate <adj,t,n,{}>(e,e) -> e where n is no OA-node in t

        for (TagTree t : allTrees) {
            for (TagNode n : t.getAllNodes()) {
                if (n.getType() != TagNode.FOOT && n.getType() != TagNode.SUBST && n.getType() != TagNode.LEX) {
                    LinkedList<Object> adjs = AdjunctionSets.getList(partition.getAllaux(), new CatPairs(n.getAdjCategory(TagNode.TOP), n.getAdjCategory(TagNode.BOT)));
                    if (!(n.isNoadj()) && n.getType() != TagNode.NOADJ && adjs != null) { // if there are adjoinable trees
                        for (int i = 0; i < adjs.size(); i++) {
                            TagTree atree = (TagTree) adjs.get(i);
                            boolean aadj = autoAdj || (atree.getId() != t.getId());
                            if (aadj && checkAdjWord(t.getId(), t.getTupleId(), t.getTupleAncPos(), n, (TagNode) t.getLexAnc(), atree, null) && TagTree.checkAdj(n, atree)) {
                                // CASE 5
                                PredComplexLabel lhsLabel = new PredComplexLabel(PredComplexLabel.ADJ, t.getId(), n.getAddress());
                                Predicate lhs = new Predicate(lhsLabel);
                                Argument l = new Argument(new ArgContent(ArgContent.VAR, "L"));
                                Argument r = new Argument(new ArgContent(ArgContent.VAR, "R"));
                                lhs.addArg(l);
                                lhs.addArg(r);

                                PredComplexLabel rhsLabel = new PredComplexLabel(PredComplexLabel.TREE, atree.getId());
                                Predicate rhs = new Predicate(rhsLabel);
                                rhs.addArg(l);
                                rhs.addArg(r);

                                LPAInstructionClause c = new LPAInstructionClause();
                                c.setLhs(lhs);
                                c.addToRhs(rhs);
                                c.operationType = LPAInstructionClause.REMOVE_OPERATION;
                                c.removedTree = atree.getId();
                                rcggrammar.addClause(c, null);

                                // CASE 4
                                if (atree.getIsHead()) {
                                    LPAInstructionClause c2 = new LPAInstructionClause();
                                    c2.setLhs(lhs);
                                    c2.addToRhs(rhs);
                                    rcggrammar.addClause(c2, null);
                                }
                            }
                        }
                    }
                    // CASE 6
                    if (n.getAdjStatus() != TagNode.MADJ) {
                        PredComplexLabel lhsLabel = new PredComplexLabel(PredComplexLabel.ADJ, t.getId(), n.getAddress());
                        Predicate lhs = new Predicate(lhsLabel);
                        Argument l = new Argument(new ArgContent(ArgContent.EPSILON, ""));
                        Argument r = new Argument(new ArgContent(ArgContent.EPSILON, ""));
                        lhs.addArg(l);
                        lhs.addArg(r);

                        LPAInstructionClause c = new LPAInstructionClause();
                        c.setLhs(lhs);
                        c.operationType = LPAInstructionClause.EMPTY_LPA;
                        rcggrammar.addClause(c, null);
                    }
                }
            }
        }

        // CASE 7: generate <sub,t,n>(X) -> <t'>(X) for all t' that can be substituted into n in t

        for (TagTree t : allTrees) {
            for (Node node : t.getSubst()) {
                TagNode n = (TagNode) node;
                LinkedList<Object> subst = AdjunctionSets.getList(partition.getInitials(), new CatPairs(n.getSubstCategory(), ""));
                for (int i = 0; i < subst.size(); i++) {
                    TagTree stree = (TagTree) subst.get(i);
                    if (checkSubstWord(t.getId(), t.getTupleId(), t.getTupleAncPos(), n, (TagNode) t.getLexAnc(), stree) && TagTree.checkAdj(n, stree)) {

                        PredComplexLabel slabel = new PredComplexLabel(PredComplexLabel.SUB, t.getId(), n.getAddress());
                        Predicate sLHS = new Predicate(slabel);

                        PredComplexLabel pred = new PredComplexLabel(PredComplexLabel.TREE, stree.getId());
                        Predicate sRHS = new Predicate(pred);

                        Argument x = new Argument(new ArgContent(ArgContent.VAR, "X"));
                        sLHS.addArg(x);
                        sRHS.addArg(x);
                        LPAInstructionClause c = new LPAInstructionClause();
                        c.setLhs(sLHS);
                        c.addToRhs(sRHS);
                        rcggrammar.addClause(c, null);
                    }
                }
            }
        }
    }

    // additional checks from old grammar converter, most of which are NOT specified in the algorithm

    public boolean checkAdjWord(String treeid, String tupleId, String tupleAncPos, TagNode nn, TagNode lex, TagTree tr, List<Object> lpa) {
        // return true if the adjunction is allowed, false otherwise
        boolean res = true;
        if (tupleAncPos != null && tr.getTupleAncPos() != null) {
            // if we try to adjoin the argument of another tuple anchored by the same word, we return false
            if (tr.getTupleAncPos().equals(tupleAncPos) && !(tr.getTupleId().equals(tupleId))) {
                res = false;
            } else {
                // if the tree is anchored, and we are on the spine
                if (lex != null && (nn.getAddress().equals("0") || lex.getAddress().startsWith(nn.getAddress()))) {
                    if (tr.isLeftAdj() && tr.hasLex()) {
                        int posi2 = ((TagNode) tr.getLexAnc()).getWord().getEnd();
                        int posi = lex.getWord().getEnd();
                        res = (posi2 < posi);
                    } else if (tr.isRightAdj() && tr.hasLex()) {
                        int posi2 = ((TagNode) tr.getLexAnc()).getWord().getEnd();
                        int posi = lex.getWord().getEnd();
                        res = (posi < posi2);
                    }
                }
            }
        }
        // if the lpa contains a not-compatible tree, we also reject the tree to adjoin!
        // NB: by not-compatible, we mean we cannot use two tuples of a given input word within the same derivation
        if (lpa != null) {
            for (int i = 0; i < lpa.size(); i++) {
                TagTree lpaTree = treeDict.get((String) lpa.get(i));
                if (tr.getTupleAncPos().equals(lpaTree.getTupleAncPos()) && !(tr.getTupleId().equals(lpaTree.getTupleId()))) {
                    res = false;
                }
            }
        }
        if (verbose && !res) {
            System.err.println("\n ::: Adjunction of " + treeDict.get(tr.getId()).getOriginalId() + " on " + treeDict.get(treeid).getOriginalId() + " at node " + nn.getAddress()
                    + " discarded. ::: \n");
        }
        return res;
    }

    public boolean checkSubstWord(String treeid, String tupleId, String tupleAncPos, TagNode nn, TagNode lex, TagTree tr) {
        boolean res = true;
        // if we try to substitute the head of another tuple anchored by the same word, we return false
        if (tupleAncPos != null && tr.getTupleAncPos().equals(tupleAncPos) && !(tr.getTupleId().equals(tupleId))) {
            res = false;
        } else {
            if (lex != null && tr.hasLex()) {
                // NB1: tr should always be lexicalised as an initial tree
                int posi2 = ((TagNode) tr.getLexAnc()).getWord().getEnd();
                int posi = lex.getWord().getEnd();
                if (nn.getAddress().compareTo(lex.getAddress()) < 0) { // "left substitution"
                    res = (posi2 < posi);
                } else { // "right substitution"
                    res = (posi < posi2);
                }
                // NB2: the test should never applies on the root
                // since a tree cannot both have a lex node and be a root!
            }
        }
        if (verbose && !res) {
            System.err.println("\n ::: Substitution of " + treeDict.get(tr.getId()).getOriginalId() + " on " + treeDict.get(treeid).getOriginalId() + " discarded. ::: \n");
        }
        return res;
    }

    public boolean[][] computeAdjunctionMatrix(TagTree tree, List<Object> args, AdjunctionSets adjsets) {
        boolean[][] matrix = new boolean[tree.numNodes(tree.getRoot())][args.size()];
        //really hard to compute; involves everything the computation of psi involved!
        //matrix as planned now could also become prohibitively huge!
        List<TagNode> nodeList = tree.getAllNodes();
        for (int j = 0; j < nodeList.size(); j++) {
            TagNode n = nodeList.get(j);
            // if the node is neither a nadjanc, a foot, a subst, a no-adj, nor a lex node
            if (n.getType() != TagNode.FOOT && n.getType() != TagNode.SUBST && n.getType() != TagNode.LEX) {
                LinkedList<Object> adjs = AdjunctionSets.getList(adjsets.getAllaux(), new CatPairs(n.getAdjCategory(TagNode.TOP), n.getAdjCategory(TagNode.BOT)));
                if (!(n.isNoadj()) && n.getType() != TagNode.NOADJ && adjs != null) { // if there are adjoinable trees
                    // we gather the ids of the adjoinable trees
                    LinkedList<String> adjids = new LinkedList<String>();
                    for (int i = 0; i < adjs.size(); i++) {
                        TagTree atree = (TagTree) adjs.get(i);
                        boolean aadj = autoAdj || (atree.getId() != tree.getId());
                        if (aadj && TagTree.checkAdj(n, atree)) {
                            // we check possible adj-tree discardings because of features mismatches
                            adjids.add(atree.getId());
                            //System.err.println("::: tree " + atree.getId() + " added for " + this.getId());
                        }
                    }

                    for (int i = 0; i < args.size(); i++) {
                        String tid = ((TagTree) args.get(i)).getId();
                        if (adjids.contains(tid)) {
                            matrix[j][i] = true;
                        }
                    }
                }
            }
        }
        return matrix;
    }

}
