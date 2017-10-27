/*
 *  File TagTree.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:48:15 CEST 2007
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
package de.tuebingen.tag;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.converter.AdjunctionSets;
import de.tuebingen.converter.CatPairs;
import de.tuebingen.disambiguate.Polarities;
import de.tuebingen.lexicon.CoAnchor;
import de.tuebingen.lexicon.Equation;
import de.tuebingen.lexicon.Lemmaref;
import de.tuebingen.rcg.ArgContent;
import de.tuebingen.rcg.Argument;
import de.tuebingen.rcg.PredComplexLabel;
import de.tuebingen.rcg.PredLabel;
import de.tuebingen.rcg.Predicate;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.tree.Node;
import de.tuebingen.tree.Tree;
import de.tuebingen.util.Pair;

/**
 * @author wmaier, parmenti
 *
 */

public class TagTree implements Tree {

    // unique tree id:
    private String id;
    // original tree id (before renaming during parsing because of lexical
    // ambiguity)
    private String originalId;
    // boolean used to mark the head
    private boolean isHead;
    // pointer to the root node
    private Node root;
    // pointer to the anchor node (added to facilitate anchoring)
    private Node anchor;
    // pointer to the lexical item after anchoring
    private Node lexAnc;
    // pointer to the foot node (null if it is no auxiliary tree)
    private Node foot;
    // the trace corresponds to the MG classes used to build the tree:
    private List<String> trace;
    // the interface is a FS (used for instance for anchoring):
    private Fs iface;
    // the tree belong to a family name (MG's value instruction):
    private String family;
    // the tuple id, the tree belong to (for argument selection)
    private String tupleId;
    // the original tuple id, the tree belong to (before polarity computation)
    private String originalTupleId;
    // the anchor and position of the tuple's head
    private String tupleAncPos;
    // semantics
    private List<SemLit> sem;
    // frames
    private List<Fs> frames;
    // substitution nodes (for lexical disambiguation)
    private List<Node> subst;
    // lexical items (hard-coded, i.e. lexical nodes, for lexical
    // disambiguation)
    private List<Node> lexNodes;
    // co-anchors (for lexical disambiguation)
    private List<Node> coAnchors;

    /**
     * 
     * @param i
     *            the id of the TagTree
     */
    public TagTree(String i) {
        id = i;
        isHead = false;
        subst = new LinkedList<Node>();
        lexNodes = new LinkedList<Node>();
        coAnchors = new LinkedList<Node>();
    }

    public TagTree(TagTree t, NameFactory nf) {
        root = new TagNode((TagNode) t.getRoot(), nf);
        id = new String(t.getId());
        originalId = t.getOriginalId();
        isHead = t.getIsHead();
        trace = new LinkedList<String>();
        for (int i = 0; i < t.getTrace().size(); i++) {
            trace.add(t.getTrace().get(i));
        }
        iface = new Fs(t.getIface(), nf);
        family = t.getFamily();
        tupleId = t.getTupleId();
        originalTupleId = t.getOriginalTupleId();

        // instantiate semantics
        sem = new LinkedList<SemLit>();

        for (int i = 0; i < t.getSem().size(); i++) {
            SemLit semlit = t.getSem().get(i);

            if (semlit instanceof SemPred)
                sem.add(new SemPred((SemPred) semlit, nf));
            else if (semlit instanceof SemDom)
                sem.add(new SemDom((SemDom) semlit, nf));
            else if (semlit instanceof Value)
                sem.add(new Value((Value) semlit, nf));
        }

        // instantiate frames
        if (t.getFrames() != null) {
            List<Fs> oldFrames = t.getFrames();
            List<Fs> newFrames = new ArrayList<Fs>();
            for (Fs oldFrame : oldFrames) {
                //System.err.println("Recreating frame, before: " + oldFrame);

                Fs newFrame = new Fs(oldFrame, nf);
                newFrames.add(newFrame);
                //System.err.println("After: " + newFrame);

            }
            this.frames = newFrames;
        }

        // to update subst, foot and anchor:
        subst = new LinkedList<Node>();
        lexNodes = new LinkedList<Node>();
        coAnchors = new LinkedList<Node>();
        this.findMarks(root, "0");
        if (t.getLexAnc() != null) {
            lexAnc = new TagNode((TagNode) t.getLexAnc(), nf);
        }
        tupleAncPos = t.getTupleAncPos();
    }

    public TagTree(Node root) {
        this.root = root;
    }

    public TagTree() {
        // TODO Auto-generated constructor stub
    }

    public void add2sem(List<SemLit> s) {
        if (sem == null)
            sem = new LinkedList<SemLit>();
        sem.addAll(s);
    }

    public void add2Trace(String s) {
        if (trace == null)
            trace = new LinkedList<String>();
        trace.add(s);
    }

    public boolean hasAnchor() {
        return (anchor != null);
    }

    /**
     * aka isAuxiliary!
     * 
     * @return
     */
    public boolean hasFoot() {
        return (foot != null);
    }

    /**
     * ie there is an anc or coanc that can helps defining constraints on word
     * order
     */
    public boolean hasLex() {
        return (lexAnc != null);
    }

    public boolean isLeftAdj() {
        boolean res = false;
        if (hasFoot() && hasLex()) {
            res = ((TagNode) lexAnc).getAddress()
                    .compareTo(((TagNode) foot).getAddress()) < 0 ? true
                            : false;
        }
        return res;
    }

    public boolean isRightAdj() {
        boolean res = false;
        if (hasFoot() && hasLex()) {
            res = ((TagNode) foot).getAddress()
                    .compareTo(((TagNode) lexAnc).getAddress()) < 0 ? true
                            : false;
        }
        return res;
    }

    public static void traverse(Node node) {
    }

    /**
     * Method used to solve a node equation (lookup of the node and
     * unification between FS)
     * NB: this build a new tree
     */
    public boolean solveEquation(Node n, Equation eq, Environment env)
            throws UnifyException {
        boolean res = false;
        String nodeid = eq.getNode_id();

        if (n.getName() != null && n.getName().equals(nodeid)) {
            // node found
            Fs eqfs = new Fs(1);
            eqfs.setFeat(eq.getType(), new Value(eq.getFeatures()));
            Fs label = ((TagNode) n).getLabel();
            Fs fs = Fs.unify(label, eqfs, env);
            ((TagNode) n).setLabel(fs);
            res = true;
        } else { // node not found
            if (n.getChildren() != null) {
                for (int i = 0; i < n.getChildren().size(); i++) {
                    res |= solveEquation(n.getChildren().get(i), eq, env);
                }
            }
        }
        return res;
    }

    /**
     * Method used to add the lexical anchor and unify the morphological
     * features
     */
    @SuppressWarnings("unused")
    public void anchor(Word lex, Lemmaref l, Environment env)
            throws UnifyException {
        Fs ancLabel = ((TagNode) anchor).getLabel();
        if (l != null) { // the anchor node has features
            // System.out.println("---"+l.toString());
            Fs lexfs = l.getFeatures();
            Fs fs = new Fs(ancLabel.getSize() + 1);
            fs.setFeat("bot", new Value(lexfs));
            if (ancLabel != null) {
                Fs resLabel = Fs.unify(ancLabel, fs, env);
                ((TagNode) anchor).setLabel(resLabel);
            } else
                ((TagNode) anchor).setLabel(fs);
        }
        List<Node> ch = new LinkedList<Node>();
        TagNode anc = new TagNode();
        anc.setType(TagNode.LEX); // lex node
        anc.setCategory(lex.getWord()); // the lexical item
        anc.setWord(lex); // the word
        anc.setAncLex(true); // to distinguish lex nodes corresponding to main
                             // anchors
        anc.setAddress(((TagNode) anchor).getAddress() + ".1"); // Gorn address
        lexAnc = anc;
        ch.add(anc);
        ((TagNode) anchor).setChildren(ch);
    }

    /**
     * returns true if the coanchor has been processed false otherwise
     * 
     * @param n
     * @param ca
     * @return
     */
    public boolean coanchor(Node n, CoAnchor ca) {
        boolean res = false;
        List<Node> ln = n.getChildren();
        if (ln == null) {
            if (((TagNode) n).getType() == TagNode.COANCHOR) {
                if (n.getName() != null) { // it has a name
                    if (n.getName().equals(ca.getNode_id())) {
                        TagNode tn = (TagNode) n;
                        List<Node> ch = new LinkedList<Node>();
                        TagNode coanc = new TagNode();
                        coanc.setType(TagNode.LEX); // lex node
                        coanc.setCategory(ca.getLex().get(0)); // the word
                        coanc.setAddress(tn.getAddress() + ".1"); // Gorn
                                                                  // address
                        ch.add(coanc);
                        tn.setChildren(ch);
                        res = true;
                    } else {
                        res = false;
                    }
                } else {
                    // System.err.println(((TagNode) n).toString());
                    System.err.println(
                            "Anonymous coanchor found during co-anchoring, expected node name: "
                                    + ca.getNode_id() + ", lexical item: "
                                    + ca.getLex());
                }
            }
        } else {
            for (int i = 0; i < ln.size(); i++) {
                res |= coanchor(ln.get(i), ca);
            }
        }
        return res;
    }

    /**
     * Method used to update the features labeling the nodes of a tree
     * called after application of node's equations
     */
    public void updateFS(Node n, Environment env, boolean finalUpdate)
            throws UnifyException {
        Fs label = ((TagNode) n).getLabel();
        // if the node has a FS, we update it
        if (label != null) {
            ((TagNode) n).setLabel(Fs.updateFS(label, env, finalUpdate));
        }
        // if the node has children, we update them
        if (n.getChildren() != null) {
            for (int j = 0; j < n.getChildren().size(); j++) {
                updateFS(n.getChildren().get(j), env, finalUpdate);
            }
        }
    }

    /**
     * Method used to find both anchor and foot node (if any)
     * and to add Gorn addresses to nodes (and to store subst nodes)
     * and we check if there are null-adj or mandatory-adj nodes
     */
    public void findMarks(Node n, String gorn) {
        ((TagNode) n).setAddress(gorn);
        ((TagNode) n).updateAdjStatus();
        if (((TagNode) n).getType() == TagNode.ANCHOR) { // anchor node
            anchor = n;
        } else if (((TagNode) n).getType() == TagNode.FOOT) { // foot node
            foot = n;
        } else if (((TagNode) n).getType() == TagNode.SUBST) { // subst node
            subst.add(n);
        } else if (((TagNode) n).getType() == TagNode.LEX) { // lexical node
            lexNodes.add(n);
	    System.out.println("Added "+n+" to LexNodes");
        } else if (((TagNode) n).getType() == TagNode.COANCHOR) { // coanchor
                                                                  // node
            coAnchors.add(n);
        }
        // in all cases we have to get through the children
        if (n.getChildren() != null) {
            LinkedList<Node> l = (LinkedList<Node>) n.getChildren();
            for (int i = 0; i < l.size(); i++) {
                if (gorn.equals("0")) { // root node
                    findMarks(l.get(i), "" + (i + 1));
                } else { // not the root
                    findMarks(l.get(i), gorn + "." + (i + 1));
                }
            }
        }
    }

    /**
     * Method used to find a node according to its Gorn address
     */
    public void findNode(Node n, String address, List<Node> ln) {
        TagNode nn = (TagNode) n;
        if (nn.getAddress().equals(address)) {
            ln.add(n);
        } else {
            if (n.getChildren() != null) {
                LinkedList<Node> l = (LinkedList<Node>) n.getChildren();
                for (int i = 0; i < l.size(); i++) {
                    findNode(l.get(i), address, ln);
                }
            }
        }
    }

    /**
     * Method used to compute the mapping between a lex node and its (Gorn
     * address, cat)
     * (used at dependencies building)
     */
    public void findAllLex(Node n, Map<String, Pair> lexAdd) {
        TagNode nn = (TagNode) n;
        if (nn.getChildren() != null && ((TagNode) nn.getChildren().get(0))
                .getType() == TagNode.LEX) {
            lexAdd.put(((TagNode) nn.getChildren().get(0)).getCategory(),
                    new Pair(((TagNode) nn.getChildren().get(0)).getAddress(),
                            nn.getCategory()));
        } else {
            if (n.getChildren() != null) {
                LinkedList<Node> l = (LinkedList<Node>) n.getChildren();
                for (int i = 0; i < l.size(); i++) {
                    findAllLex(l.get(i), lexAdd);
                }
            }
        }
    }

    /**
     * Method used to gather the lex items (for forest checking)
     * 
     */
    public void lookupLex(Node n, List<String> lex) {
        TagNode nn = (TagNode) n;
        if (nn.getChildren() == null && nn.getType() == TagNode.LEX) {
            // NB: we wants duplicates in case of duplicated words
            lex.add(nn.getCategory());
        } else {
            if (n.getChildren() != null) {
                LinkedList<Node> l = (LinkedList<Node>) n.getChildren();
                for (int i = 0; i < l.size(); i++) {
                    lookupLex(l.get(i), lex);
                }
            }
        }
    }

    /**
     * Method used to count the number of nodes in the tree
     */
    public int numNodes(Node n) {
        int res = 1;
        if (n.getChildren() != null) {
            LinkedList<Node> l = (LinkedList<Node>) n.getChildren();
            for (int i = 0; i < l.size(); i++) {
                res += numNodes(l.get(i));
            }
        }
        return res;
    }

    /**
     * Method checking the gen, num, case and phrase features
     * for adjunction on a given node
     */
    public static boolean checkSubst(TagNode nn, TagTree t) {
        boolean res = true;
        TagNode adjRoot = (TagNode) t.getRoot();
        // gen feat
        res &= checkFeat("gen", TagNode.TOP, nn, adjRoot);
        // num feat
        res &= checkFeat("num", TagNode.TOP, nn, adjRoot);
        // case feat
        res &= checkFeat("case", TagNode.TOP, nn, adjRoot);
        // phrase feat
        res &= checkFeat("phrase", TagNode.TOP, nn, adjRoot);
        return res;
    }

    /**
     * Method checking the gen, num, case and phrase features
     * for adjunction on a given node
     * CHANGELOG: bottom feature check commented out because it might be too
     * restrictive (says Laura)
     */
    public static boolean checkAdj(TagNode nn, TagTree t) {
        boolean res = true;
        TagNode adjRoot = (TagNode) t.getRoot();
        // TagNode adjFoot = (TagNode) t.getFoot();

        // gen feat
        res &= checkFeat("gen", TagNode.TOP, nn, adjRoot);
        // res &= checkFeat("gen", TagNode.BOT, nn, adjFoot);
        // num feat
        res &= checkFeat("num", TagNode.TOP, nn, adjRoot);
        // res &= checkFeat("num", TagNode.BOT, nn, adjFoot);
        // case feat
        res &= checkFeat("case", TagNode.TOP, nn, adjRoot);
        // res &= checkFeat("case", TagNode.BOT, nn, adjFoot);
        // phrase feat
        res &= checkFeat("phrase", TagNode.TOP, nn, adjRoot);
        // res &= checkFeat("phrase", TagNode.BOT, nn, adjFoot);
        // if (!res) {
        // System.err.println("\n :::::: Features mismatch at convertion: \n" +
        // nn.toString() + " and \n" + adjRoot.toString() + " - " + t.getId() +
        // "\n :::::: \n");
        // }
        return res;
    }

    public static boolean checkFeat(String f, int fs, TagNode loc,
            TagNode adj) {
        boolean res = true;
        String localF = loc.getFeatVal(f, fs);
        String adjF = adj.getFeatVal(f, fs);
        if (localF != null && adjF != null && !(localF.equals(adjF))) {
            res = false;
        }
        return res;
    }

    public void computePsi(boolean auto_adj, Node n,
            Hashtable<Object, LinkedList<Object>> psi, List<String> lpa2,
            List<Object> lpa, Hashtable<Object, LinkedList<Object>> adjsets) {
        /**
         * Computes the psi hashtable mapping nodes with adjoinable trees
         */
        TagNode nn = (TagNode) n;
        String nodeid = nn.getAddress();

        // if the node is neither a nadjanc, a foot, a subst, a no-adj, nor a
        // lex node
        if (nn.getType() != TagNode.FOOT && nn.getType() != TagNode.SUBST
                && nn.getType() != TagNode.LEX) {
            LinkedList<Object> adjs = AdjunctionSets.getList(adjsets,
                    new CatPairs(nn.getAdjCategory(TagNode.TOP),
                            nn.getAdjCategory(TagNode.BOT)));
            if (!(nn.isNoadj()) && nn.getType() != TagNode.NOADJ
                    && adjs != null) { // if there are adjoinable trees

                // we gather the ids of the adjoinable trees
                LinkedList<String> adjids = new LinkedList<String>();
                for (int i = 0; i < adjs.size(); i++) {
                    TagTree atree = (TagTree) adjs.get(i);
                    boolean aadj = auto_adj || (atree.getId() != id);
                    if (aadj && checkAdj(nn, atree)) {
                        // we check possible adj-tree discardings because of
                        // features mismatches
                        adjids.add(atree.getId());
                        // System.err.println("::: tree " + atree.getId() + "
                        // added for " + this.getId());
                    }
                }

                for (int i = 0; i < lpa2.size(); i++) {
                    String tid = lpa2.get(i);

                    if (adjids.contains(tid)) {
                        if (lpa.contains(tid)) {
                            if (nodeid.equals("0")) { // t is in the lpa =>
                                                      // attaches to the root
                                                      // node
                                AdjunctionSets.update(psi, tid, nodeid);
                            }
                        } else { // t is not in the lpa => attaches everywhere
                            AdjunctionSets.update(psi, tid, nodeid);
                        }
                    }
                }
            }
            // we update psi for the children
            if (n.getChildren() != null) {
                LinkedList<Node> l = (LinkedList<Node>) n.getChildren();
                for (int i = 0; i < l.size(); i++) {
                    computePsi(auto_adj, l.get(i), psi, lpa2, lpa, adjsets);
                }
            }
        }
    }

    public void buildDecorationString(Node n, LinkedList<ArgContent> largs) {
        /**
         * Build the decoration string of the tree
         */
        TagNode nn = (TagNode) n;

        if (nn.getChildren() != null) {
            if (!(nn.isNoadj())) {
                // Left range
                nn.setLRange(
                        new ArgContent(ArgContent.VAR, "X" + nn.getAddress(),
                                nn.getAdjStatus(), nn.getCategory()));
                largs.add(nn.getLRange());
            }
            LinkedList<Node> l = (LinkedList<Node>) nn.getChildren();
            for (int i = 0; i < l.size(); i++) {
                // Daughter nodes
                buildDecorationString(l.get(i), largs);
            }
            if (!(nn.isNoadj())) {
                // Right range
                nn.setRRange(
                        new ArgContent(ArgContent.VAR, "Y" + nn.getAddress(),
                                nn.getAdjStatus(), nn.getCategory()));
                largs.add(nn.getRRange());
            }
        } else {
            switch (nn.getType()) {
            case TagNode.FOOT: // foot node
                nn.setCRange(new ArgContent(ArgContent.SEPARATOR, ","));
                largs.add(nn.getCRange());
                break;
            case TagNode.SUBST: // subst node
                nn.setSRange(new ArgContent(ArgContent.VAR,
                        "S" + nn.getAddress(), ArgContent.SUBST_RANGE));
                largs.add(nn.getSRange());
                break;
            case TagNode.LEX: // lex node
                if (nn.getCategory() != null
                        && !(nn.getCategory().equals(""))) {
                    nn.setCRange(
                            new ArgContent(ArgContent.TERM, nn.getCategory()));
                    largs.add(nn.getCRange());
                }
                break;
            case TagNode.STD:
                nn.setLRange(
                        new ArgContent(ArgContent.VAR, "X" + nn.getAddress(),
                                nn.getAdjStatus(), nn.getCategory()));
                largs.add(nn.getLRange());
                nn.setRRange(
                        new ArgContent(ArgContent.VAR, "Y" + nn.getAddress(),
                                nn.getAdjStatus(), nn.getCategory()));
                largs.add(nn.getRRange());
                break;
            // NB: after anchoring, anchor and coanchor should have children!
            // for robustness, the cases are added
            case TagNode.COANCHOR: // coanchor node
                // if it hasn't been anchored, it means its daughter is an empty
                // node
                nn.setLRange(
                        new ArgContent(ArgContent.VAR, "X" + nn.getAddress(),
                                nn.getAdjStatus(), nn.getCategory()));
                largs.add(nn.getLRange());
                nn.setRRange(
                        new ArgContent(ArgContent.VAR, "Y" + nn.getAddress(),
                                nn.getAdjStatus(), nn.getCategory()));
                largs.add(nn.getRRange());
                break;
            case TagNode.ANCHOR:
                nn.setLRange(
                        new ArgContent(ArgContent.VAR, "X" + nn.getAddress(),
                                nn.getAdjStatus(), nn.getCategory()));
                largs.add(nn.getLRange());
                nn.setRRange(
                        new ArgContent(ArgContent.VAR, "Y" + nn.getAddress(),
                                nn.getAdjStatus(), nn.getCategory()));
                largs.add(nn.getRRange());
                break;
            default: // skip (no-adj frontier nodes)
            }
        }
    }

    /**
     * @param predlabs
     *            predlabs is an hashtable associating a node id with a complex
     *            predicate
     */
    public void buildRHS(Node n, Hashtable<String, PredLabel> predlabs,
            LinkedList<Predicate> preds) {
        TagNode nn = (TagNode) n;

        if (predlabs.containsKey(nn.getAddress())) {
            // the node receive adjunctions
            PredComplexLabel plab = (PredComplexLabel) predlabs
                    .get(nn.getAddress());
            plab.setNodecat(new CatPairs(nn.getAdjCategory(TagNode.TOP),
                    nn.getAdjCategory(TagNode.BOT)));
            plab.setZenode(new TagNode(nn));
            plab.setTupleId(this.tupleId);
            plab.setTupleAncPos(this.tupleAncPos);
            if (this.hasLex()) {
                plab.setLexNode(new TagNode(((TagNode) this.getLexAnc())));
            }
            Predicate p = new Predicate(plab);
            for (int i = 0; i < nn.giveArgs().size(); i++) {
                p.addArg(new Argument(nn.giveArgs().get(i)));
            }
            preds.add(p);
        } else {
            // the node do not receive adjunctions
            // if the node is neither a foot nor a no-adj nor a lex node
            if (!(nn.isNoadj()) && nn.getType() != TagNode.FOOT
                    && nn.getType() != TagNode.NOADJ
                    && nn.getType() != TagNode.LEX) {
                // then it should raise a predicate
                if (nn.getType() != TagNode.SUBST) {
                    // adjunction node
                    PredComplexLabel plab = new PredComplexLabel(
                            PredComplexLabel.ADJ, id, nn.getAddress());
                    plab.setNodecat(new CatPairs(nn.getAdjCategory(TagNode.TOP),
                            nn.getAdjCategory(TagNode.BOT)));
                    plab.setZenode(new TagNode(nn));
                    plab.setTupleId(this.tupleId);
                    plab.setTupleAncPos(this.tupleAncPos);
                    if (this.hasLex()) {
                        plab.setLexNode(
                                new TagNode(((TagNode) this.getLexAnc())));
                    }
                    Predicate p = new Predicate(plab);
                    for (int i = 0; i < nn.giveArgs().size(); i++) {
                        p.addArg(new Argument(nn.giveArgs().get(i)));
                    }
                    preds.add(p);
                } else { // subst node
                    PredComplexLabel plab = new PredComplexLabel(
                            PredComplexLabel.SUB, id, nn.getAddress());
                    plab.setNodecat(new CatPairs(nn.getSubstCategory(), ""));
                    plab.setZenode(new TagNode(nn));
                    plab.setTupleId(this.tupleId);
                    plab.setTupleAncPos(this.tupleAncPos);
                    if (this.hasLex()) {
                        plab.setLexNode(
                                new TagNode(((TagNode) this.getLexAnc())));
                    }
                    Predicate p = new Predicate(plab);
                    for (int i = 0; i < nn.giveArgs().size(); i++) {
                        p.addArg(new Argument(nn.giveArgs().get(i)));
                    }
                    preds.add(p);
                }
            } // skip foot, no-adj and lex nodes
        }
        // recursive traversal
        if (nn.getChildren() != null) {
            LinkedList<Node> l = (LinkedList<Node>) nn.getChildren();
            for (int i = 0; i < l.size(); i++) {
                buildRHS(l.get(i), predlabs, preds);
            }
        }
    }

    public void getFeatures(Node n, String nodeId, LinkedList<Fs> res) {
        /**
         * Retrieves a given label and store it in the 3rd parameter
         */
        TagNode nn = (TagNode) n;
        if (nn.getAddress() != null && nn.getAddress().equals(nodeId)) {
            // node found
            res.addFirst(((TagNode) n).getLabel()); // pointer to the Fs
                                                    // labelling the node
        } else { // node not found
            if (n.getChildren() != null) {
                for (int i = 0; i < n.getChildren().size(); i++) {
                    getFeatures(n.getChildren().get(i), nodeId, res);
                }
            }
        }
    }

    public void getPolarities(Polarities p) {
        TagNode rootNode = (TagNode) this.getRoot();
        String rootCat = rootNode.getCategory();
        TagNode footNode = (TagNode) this.getFoot();
        String footCat = footNode != null ? footNode.getCategory() : null;
        p.setPol(rootCat, rootCat, Polarities.PLUS);
        if (footCat != null) {
            p.setPol(footCat, footCat, Polarities.MINUS);
        }
        for (Node n : subst) {
            TagNode tn = (TagNode) n;
            String nCat = tn.getCategory();
            p.setPol(nCat, nCat, Polarities.MINUS);
        }
        for (Node n : lexNodes) {
            TagNode tn = (TagNode) n;
            String nCat = tn.getCategory(); // NB: lexical items are stored as
                                            // cat values
            if (!nCat.equals(""))
                p.setPol(nCat, nCat, Polarities.MINUS);
        }
        for (Node n : coAnchors) {
            TagNode tn = (TagNode) n;
            String nCat = tn.getCategory();
            p.setPol(nCat, nCat, Polarities.MINUS);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public boolean getIsHead() {
        return isHead;
    }

    public void setIsHead(boolean isHead) {
        this.isHead = isHead;
    }

    public Node getLexAnc() {
        return lexAnc;
    }

    public void setLexAnc(Node lexAnc) {
        this.lexAnc = lexAnc;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node node) {
        this.root = node;
    }

    public Node getAnchor() {
        return anchor;
    }

    public void setAnchor(Node anchor) {
        this.anchor = anchor;
    }

    public Node getFoot() {
        return foot;
    }

    public void setFoot(Node foot) {
        this.foot = foot;
    }

    public List<String> getTrace() {
        return trace;
    }

    public void setTrace(List<String> trace) {
        this.trace = trace;
    }

    public Fs getIface() {
        return iface;
    }

    public void setIface(Fs iface) {
        this.iface = iface;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getOriginalTupleId() {
        return originalTupleId;
    }

    public void setOriginalTupleId(String originalTupleId) {
        this.originalTupleId = originalTupleId;
    }

    public String getTupleId() {
        return tupleId;
    }

    public void setTupleId(String tupleId) {
        this.tupleId = tupleId;
    }

    public String getTupleAncPos() {
        return tupleAncPos;
    }

    public void setTupleAncPos(String tupleAncPos) {
        this.tupleAncPos = tupleAncPos;
    }

    public List<SemLit> getSem() {
        return sem;
    }

    public void setSem(List<SemLit> sem) {
        this.sem = sem;
    }

    public void setFrames(List<Fs> framerepr) {
        this.frames = framerepr;
    }

    public void concatFrames(Fs frame) {
        if (this.frames == null) {
            frames = new LinkedList<Fs>();
        }
        frames.add(frame);
    }

    public List<Fs> getFrames() {
        return frames;
    }

    public List<Node> getSubst() {
        return subst;
    }

    public List<Node> getLexNodes() {
        return lexNodes;
    }

    public List<TagNode> getAllNodes() {
        LinkedList<TagNode> nodes = new LinkedList<TagNode>();
        nodes.add((TagNode) root);
        int pos = 0;
        while (pos < nodes.size()) {
            List<Node> children = nodes.get(pos).getChildren();
            if (children != null) {
                for (Node n : children) {
                    nodes.add((TagNode) n);
                }
            }
            pos++;
        }
        return nodes;
    }

    public List<Node> getCoAnchors() {
        return coAnchors;
    }

    public List<String> getLexItems() {
        List<String> lex = new LinkedList<String>();
        for (Node n : lexNodes) {
            TagNode tn = (TagNode) n;
            String nCat = tn.getCategory(); // NB: lexical items are stored as
                                            // cat values
            lex.add(nCat);
        }
        return lex;
    }

    public List<String> getCoAnchorsCat() {
        List<String> coac = new LinkedList<String>();
        for (Node n : coAnchors) {
            TagNode tn = (TagNode) n;
            String nCat = tn.getCategory();
            coac.add(nCat);
        }
        return coac;
    }

    public String toString(String space) {
        String s = "";
        for (int i = 0; i < trace.size(); i++) {
            s += trace.get(i) + "-";
        }
        s = s.equals("") ? s : s.substring(0, (s.length() - 1));
        String semantics = "";
        for (int i = 0; i < sem.size(); i++) {
            semantics += "  " + sem.get(i).toString() + "\n";
        }
        return ("\n Tree " + id + "\n  Original Id : " + originalId
                + "\n  Family : " + family + "\n  Tuple id : " + tupleId
                + "\n  Trace : " + s + "\n  Interface : [" + iface.toString()
                + "]\n  Syn : \n" + space
                + ((TagNode) root).toString(space + "  ") + "\n  Sem : \n"
                + semantics);
    }

}
