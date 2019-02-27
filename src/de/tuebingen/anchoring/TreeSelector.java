/*
 *  File TreeSelector.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     David Arps <david.arps@hhu.de>
 *     Simon Petitjean <petitjean@phil.hhu.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *     David Arps, 2017
 *     Simon Petitjean, 2017
 *
 *  Last modified:
 *     2017
 *
 *  This file is part of the TuLiPA-frames system
 *     https://github.com/spetitjean/TuLiPA-frames
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
package de.tuebingen.anchoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.duesseldorf.frames.Frame;
import de.duesseldorf.frames.Fs;
import de.duesseldorf.frames.FsTools;
import de.duesseldorf.frames.Relation;
import de.duesseldorf.frames.Situation;
import de.duesseldorf.frames.UnifyException;
import de.duesseldorf.frames.Value;
import de.tuebingen.derive.ElementaryTree;
import de.tuebingen.disambiguate.Polarities;
import de.tuebingen.disambiguate.PolarizedLemma;
import de.tuebingen.disambiguate.PolarizedToken;
import de.tuebingen.disambiguate.PolarizedTuple;
import de.tuebingen.lexicon.Anchor;
import de.tuebingen.lexicon.CoAnchor;
import de.tuebingen.lexicon.Equation;
import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.Lemmaref;
import de.tuebingen.lexicon.LexSem;
import de.tuebingen.lexicon.MorphEntry;
import de.tuebingen.tag.Environment;
import de.tuebingen.tag.SemLit;
import de.tuebingen.tag.SemPred;
import de.tuebingen.tag.TagNode;
import de.tuebingen.tag.TagTree;
import de.tuebingen.tag.Tuple;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.tree.Node;

public class TreeSelector {

    private boolean verbose;
    private List<Word> tokens; // tokens (as Words)
    private List<Tuple> anctuples; // anchored tuples
    private Map<String, TagTree> treeHash; // dictionary to retrieve anchored
                                           // trees at forest processing time
    private Map<String, List<String>> tupleHash; // dictionary mapping a tuple
                                                 // id with tree ids
    private List<PolarizedToken> ptokens; // polarized tokens (for lexical
                                          // disambiguation)
    private List<String> lexNodes; // lexical items in the trees (for polarity
                                   // computation)
    private Map<String, List<String>> coancNodes; // coanchors in the trees (for
                                                  // polarity computation)
    private Map<String, Integer> ambiguity;

    /**
     * 
     * @param w
     *            list of words
     * @param v
     *            verbose mode?
     */
    public TreeSelector(List<Word> w, boolean v) {
        verbose = v;
        tokens = w;
        anctuples = new LinkedList<Tuple>(); // the anchored tuples
        treeHash = new Hashtable<String, TagTree>();// the trees dictionary
        tupleHash = new HashMap<String, List<String>>();
        ptokens = new LinkedList<PolarizedToken>();
        lexNodes = new LinkedList<String>();
        coancNodes = new HashMap<String, List<String>>();
        ambiguity = new HashMap<String, Integer>();
    }

    public void retrieve(List<String> slabels) {
        if (Situation.getFrameGrammar() != null) {
            Map<String, List<Tuple>> g = Situation.getFrameGrammar()
                    .getGrammar();

            for (Entry<String, List<Tuple>> e : g.entrySet()) {
                // System.out.println("Key: " + e.getKey());
                // System.out.println(e.getValue() == null); // f
                if (e.getValue() != null) {
                    int size = e.getValue().size(); // 1
                    // System.out.println("Size of the list<Tuple>: " + size);
                    // if (size > 0) {
                    // System.out.print("Tuple null: ");
                    // System.out.println(e.getValue().get(0) == null);
                    // if (e.getValue().get(0) != null) {
                    // System.out.println(e.getValue().get(0));
                    // }
                    // }
                }
            }
        }
        retrieve(Situation.getGrammar().getMorphEntries(),
                Situation.getGrammar().getLemmas(),
                Situation.getGrammar().getGrammar(), slabels);
    }

    /**
     * 
     * @param lm
     *            lm is a mapping between a morph name and morph entries
     * @param ll
     *            is a mapping between a tuple name and tuple entries
     * @param lt
     *            is where to store the semantic labels
     * @param slabels
     */
    public void retrieve(Map<String, List<MorphEntry>> lm,
            Map<String, List<Lemma>> ll, Map<String, List<Tuple>> lt,
            List<String> slabels) {
        for (int i = 0; i < tokens.size(); i++) {
            String s = tokens.get(i).getWord();
            ambiguity.put(s, 0); // init
            PolarizedToken ptk = new PolarizedToken(s, tokens.get(i).getEnd());
            if (lm.containsKey(s)) {
                List<MorphEntry> lme = lm.get(s);
                for (int j = 0; j < lme.size(); j++) {
                    retrieveLemma(ptk,
                            new InstantiatedMorph(lme.get(j), tokens.get(i)),
                            ll, lt, slabels);
                }
            } else {
                if (verbose) {
                    System.err.println(
                            "Unknown token (not in the morph lexicon): " + s);
                }
            }
            ptokens.add(ptk);
        }
    }

    public void retrieveLemma(PolarizedToken ptk, InstantiatedMorph m,
            Map<String, List<Lemma>> ll, Map<String, List<Tuple>> lt,
            List<String> slabels) {

        // m is a morph entry coupled with a lexical item from the input
        // sentence
        // ll is a mapping between a lemma name and lemma entries
        // lt is a mapping between a tuple name and tuple entries

        // if the tokens have POS tags, we check these as well:
        // lemcats is a mapping Lemma,Cat for the current token
        Map<String, String> lemcats = m.getInToken().getTagAsAMap();

        // for each reference to a lemma included in the morph entry:
        List<Lemmaref> lm = m.getLemmarefs();
        for (int k = 0; k < lm.size(); k++) {
            // we retrieve the name and cat of the lemma reference
            String lem = lm.get(k).getName();
            String cat = lm.get(k).getCat();
            // we check that either there is no POS-tagger
            // or that both the lemma and the cat of the MorphEntry match the
            // tagged lemma and cat
            if (lemcats == null || (lemcats.containsKey(lem)
                    && lemcats.get(lem).equals(cat))) {
                if (ll.containsKey(lem)) {
                    // we retrieve the lemmas matching the reference name
                    List<Lemma> listlemma = ll.get(lem);
                    for (int l = 0; l < listlemma.size(); l++) {
                        // if both the lemma and the cat match the reference, we
                        // instantiate the lemma entry
                        if (listlemma.get(l).getCat().equals(cat)) {
                            PolarizedLemma plm = new PolarizedLemma(
                                    listlemma.get(l).getName());
                            InstantiatedLemma il = new InstantiatedLemma(
                                    listlemma.get(l), m, lm.get(k));
                            retrieveTuples(plm, il, lt, slabels);
                            updateCoAnchors(listlemma.get(l));
                            lexNodes.addAll(plm.getLexicals());
                            ptk.addLemma(plm);
                        } else {
                            if (verbose)
                                System.err.println("Rejected "
                                        + listlemma.get(l).toString()
                                        + " expected cat: " + cat);
                        }
                    }
                } else {
                    if (verbose)
                        System.err.println(
                                "Unknown lemma (not in the lemma lexicon): "
                                        + lem);
                }
            } else {
                if (verbose)
                    System.err.println("*** Lemma \"" + lem
                            + "\" and category \"" + cat + "\" ignored.");
            }
        }

    }

    public void retrieveTuples(PolarizedLemma plm, InstantiatedLemma il,
            Map<String, List<Tuple>> lt, List<String> slabels) {

        // il is a lemma entry coupled with the lemma reference from the input
        // morph
        // lt is a mapping between a tuple name and tuple entries
        // slabels is where to store the semantic labels

        int cpt_tmp = 0; // for counting ambiguity

        // for each anchoring scheme defined in the lemma entry
        List<Anchor> la = il.getAnchors();
        for (int k = 0; k < la.size(); k++) {
            // we retrieve the tuple name the scheme contains
            String family = la.get(k).getTree_id();
            if (lt.containsKey(family)) {
                // for each matching tuple
                for (int l = 0; l < lt.get(family).size(); l++) {
                    cpt_tmp++;
                    // System.err.println("Going in a loop (TreeSelector): " +
                    // l);
                    TagTree head = lt.get(family).get(l).getHead();
                    // if the tuple's head's category match the anchoring
                    // scheme, we keep it for anchoring
                    // if (il.getCat().equals(((TagNode)
                    // head.getAnchor()).getCategory())) {
                    // IT IS NO LONGER IDENTITY THAT IS USED FOR SELECTION BUT
                    // UNIFICATION:
                    boolean match = true;
                    try {
                        Fs morphAncFS = new Fs();
                        morphAncFS.setFeat("cat",
                                new Value(Value.Kind.VAL, il.getCat()));
                        Fs treeAncFS = ((TagNode) head.getAnchor()).getLabel();
                        Fs anchorFS = FsTools.unify(morphAncFS, treeAncFS,
                                new Environment(5));
                        ((TagNode) head.getAnchor()).getLabel()
                                .removeCategory();
                        ((TagNode) head.getAnchor()).getLabel().setFeat("cat",
                                new Value(Value.Kind.VAL,
                                        anchorFS.getCategory()));
                        ((TagNode) head.getAnchor()).findCategory();
                        // System.out.print("ts.270: ");
                        // System.out.println(
                        // ((TagNode) head.getAnchor()).getLabel());
                    } catch (UnifyException e) {
                        match = false;
                    }
                    if (match) {
                        InstantiatedTuple it = new InstantiatedTuple(la.get(k),
                                lt.get(family).get(l), il);

                        // System.out.println("[1-] ");

                        // In case we have several entries for
                        // frames associated to the lemma, we need
                        // several instantiated tuples

                        List<LexSem> lemmaSem = it.getAnchor().getSemantics();
                        List<Tuple> tlist = new LinkedList<Tuple>();

                        if (lemmaSem.size() > 1) {
                            System.out.println(
                                    "TODO: create a loop in TreeSelector.546!");
                        }
                        if (Situation.getFrameGrammar() != null) {
                            if (lemmaSem.size() > 0) {
                                tlist = Situation.getFrameGrammar().getGrammar()
                                        .get(lemmaSem.get(0).getSemclass());
                            } else {
                                System.err.println("No semantics for lemma "
                                        + it.getLemma().getName()
                                        + "\nCreating empty semantics.");
                                tlist = new LinkedList<Tuple>();
                                tlist.add(new Tuple());
                            }
                            // System.out.println("Size of the frame list
                            // for this entry: "+tlist.size());
                        }
                        for (int iframe = 0; iframe < tlist.size(); iframe++) {
                            try {
                                InstantiatedTuple x = this.anchor(plm, it, l,
                                        slabels, iframe);
                                anctuples.add(x);
                            } catch (AnchoringException e) {
                                System.err.println("Tuple non-anchored: "
                                        + it.getId() + " with frame "
                                        + tlist.get(iframe));
                            }
                        }
                    } else {
                        if (verbose) {
                            System.err.println("Rejected " + head.getId()
                                    + " (categories do not match)");
                        }
                    }
                }
            } else {
                if (verbose) {
                    System.err.println(
                            "Unknown family (not in the tuple lexicon): "
                                    + family);
                }
            }
        }
        // ambiguity computation:
        // System.err.println("Starting ambiguity computation (TreeSelector) ");

        String word = il.getLexItem().getLex();
        // System.err.println("Ambig for word " + word + " : " + cpt_tmp);
        int amb = ambiguity.get(word);
        amb += cpt_tmp;
        ambiguity.put(word, amb);
    }

    /**
     * Method used to update the NameFactory with the semantic labels name
     * prior to traverse the instantiated tree (c.f. feature sharing)
     */
    public List<String> processSemLabels(List<SemLit> lsl, NameFactory nf) {
        List<String> slabels = new LinkedList<String>();
        for (SemLit sl : lsl) {
            if (sl instanceof SemPred) {
                Value label = ((SemPred) sl).getLabel();
                // NB: labels have to be values (i.e. either constants or
                // variables !)
                String newLabel = null;
                if (label.getSVal() != null)
                    newLabel = nf.getName(label.getSVal());
                else if (label.getVarVal() != null)
                    newLabel = nf.getName(label.getVarVal());
                // thus the name factory is initialized with the labels to
                // rename
                if (!slabels.contains(newLabel))
                    slabels.add(newLabel);
            }
        }
        return slabels;
    }

    public InstantiatedTuple anchor(PolarizedLemma plm, InstantiatedTuple t,
            int i, List<String> slabels, int frameid)
            throws AnchoringException {
        /**
         * Method processing each instantiated tuples of the selector
         * in order to anchor it (unification between FS and lexical anchoring
         * of the head)
         * frameid is the number of the frame we are considering
         */

        if (verbose)
            System.err.println("\nAnchoring ... " + t.getId() + "\n");
        NameFactory nf = new NameFactory();

        // First, we get the semantic labels and rename them using the name
        // factory
        List<SemLit> lsl = t.getHead().getSem();
        List<String> semlabels = this.processSemLabels(lsl, nf);

        // we retrieve the tuple's head (and use a duplicate!)
        TagTree hd = t.getHead();
        // System.err.println("[0] ");

        TagTree tt = new TagTree(hd, nf);
        // System.err.println("[1] ");

        // we build unique tree and tuple identifiers
        String newTreeId = tt.getId() + "--"
                + (t.getLemma().getLexItem().getLex())
                + (t.getLemma().getLexItem().getInToken().getEnd()) + "--" + i;
        String newTupleId = tt.getTupleId() + "--"
                + (t.getLemma().getLexItem().getLex())
                + (t.getLemma().getLexItem().getInToken().getEnd()) + "--" + i;
        tt.setId(nf.getName(newTreeId));
        tt.setPosition(t.getLemma().getLexItem().getInToken().getEnd());

        tt.setOriginalId(newTreeId);
        tt.setTupleId(nf.getName(newTupleId));

        tt.setOriginalTupleId(newTupleId);
        tt.setIsHead(true);

        tt.setTupleAncPos((t.getLemma().getLexItem().getLex())
                + (t.getLemma().getLexItem().getInToken().getEnd()));

        // we retrieve the tuple's arguments (and use duplicates!)
        List<TagTree> args = t.getArguments();
        LinkedList<TagTree> tl = null;
        if (args != null) {
            tl = new LinkedList<TagTree>();
            for (int k = 0; k < args.size(); k++) {
                TagTree targ = new TagTree(args.get(k), nf);
                // we build unique tree and tuple identifiers for arguments
                targ.setId(nf.getName(targ.getId() + "--"
                        + (t.getLemma().getLexItem().getLex())
                        + (t.getLemma().getLexItem().getInToken().getEnd())
                        + "--" + i));
                targ.setOriginalId(args.get(k).getId() + "--"
                        + (t.getLemma().getLexItem().getLex())
                        + (t.getLemma().getLexItem().getInToken().getEnd())
                        + "--" + i);
                targ.setTupleId(nf.getName(newTupleId));
                targ.setOriginalTupleId(newTupleId);
                targ.setTupleAncPos(tt.getTupleAncPos());
                tl.add(targ);
                // we do not update the tree dictionary yet since we do not
                // know
                // whether the anchoring will succeed
                // treeHash.put(targ.getId(), targ);
            }
        }
        // System.err.println("Tree "+hd.getId());

        Fs ancfs = t.getAnchor().getFilter();
        Fs iface = tt.getIface();
        // the default size of the environment is 10 (trade-off)
        Environment env = new Environment(10);

        // a) unify the head tree interface with the filter
        Fs uniface = null;
        if (ancfs != null && iface != null) {
            try {
                uniface = FsTools.unify(ancfs, iface, env);

            } catch (UnifyException e) {
                System.err.println("Interface unification failed on tree "
                        + tt.getId() + " for filter " + ancfs.toString());
                System.err.println(e);
                throw new AnchoringException(); // we withdraw the current
                                                // anchoring
            }
        } else if (ancfs == null) {
            uniface = iface;
        } else {
            uniface = ancfs;
        }
        tt.setIface(uniface);

        // System.err.println("\n (after iface processing)");
        // System.err.println(env.toString());

        // b) tree traversal to apply node equations
        // node equations concern both the head and argument trees
        // if an equation fails, the tuple is discarded (not anchored)
        List<Equation> leq = t.getAnchor().getEquations();
        for (int j = 0; j < leq.size(); j++) {
            boolean equationOk = false;
            Equation eq = leq.get(j);
            try {
                equationOk |= tt.solveEquation(tt.getRoot(), eq, env);
            } catch (UnifyException e) {
                System.err.println(
                        "Equation solving failed on tree " + tt.getOriginalId()
                                + " for equation " + eq.toString());
                System.err.println(e);
                throw new AnchoringException(); // we withdraw the current
                                                // anchoring
            }
            if (tl != null) {
                for (int k = 0; k < tl.size(); k++) {
                    TagTree targ = tl.get(k);
                    try {
                        equationOk |= targ.solveEquation(targ.getRoot(), eq,
                                env);
                    } catch (UnifyException e) {
                        System.err.println("Equation solving failed on tree "
                                + tt.getOriginalId() + " for equation "
                                + eq.toString());
                        System.err.println(e);
                        throw new AnchoringException(); // we withdraw the
                                                        // current anchoring
                    }
                }
            }
            if (!equationOk) {
                System.err.println("Equation solving failed on tree "
                        + tt.getOriginalId() + " for equation " + eq.toString()
                        + " (node not found)");
                throw new AnchoringException(); // we withdraw the current
                                                // anchoring
            }
        }
        // System.err.println(" (after equation processing)");
        // System.err.println(env.toString());

        // c) add the new lexical item under the anchor node
        // comes with unification of morph features
        try {
            tt.anchor(t.getLemma().getLexItem().getInToken(),
                    t.getLemma().getLref(), env);
        } catch (UnifyException e) {
            System.err.println("Anchoring failed on tree " + tt.getOriginalId()
                    + " for lexical item "
                    + t.getLemma().getLexItem().getLex());
            System.err.println(e);
            throw new AnchoringException(); // we withdraw the current anchoring
        }
        // System.err.println(" (after anchoring)");
        // System.err.println(env.toString());

        // d) we perform the co-anchor equations
        List<CoAnchor> lca = t.getAnchor().getCoanchors();

        if (lca != null) {
            for (int j = 0; j < lca.size(); j++) {
                boolean coaOk = false;
                // try to co-anchor the head
                int k = 0;
                coaOk = tt.coanchor(tt.getRoot(), lca.get(j));
                // try to co-anchor the arguments
                if (tl != null) {
                    while (k < tl.size() && !coaOk) {
                        // System.out.println("Looking for coanchor
                        // "+lca.get(j)+" in "+tt.getRoot());
                        coaOk = tl.get(k).coanchor(tl.get(k).getRoot(),
                                lca.get(j));
                        k++;
                    }
                }
                if (!coaOk) {
                    // if the co-anchor has not been found, do not use the tuple
                    System.err.println("Co-anchor not found : "
                            + lca.get(j).getNode_id() + " (tuple "
                            + tt.getOriginalId() + " discarded).");
                    throw new AnchoringException();
                }
            }
        }
        // e) we instantiate the semantic information
        // we unify the tree interface with the semantic class call from the
        // lemma,
        // provided the semantic class is part of the trace!
        // This gives values to instantiate the semantic information of the tree
        List<LexSem> lemmaSem = t.getAnchor().getSemantics();
        if (lemmaSem.size() > 1) {
            System.out.println("TODO: create a loop in TreeSelector.546!");
        }

        if (Situation.getFrameGrammar() != null && lemmaSem.size() > 0) {
            ;
            ;
            ;
            List<Tuple> tlist = Situation.getFrameGrammar().getGrammar()
                    .get(lemmaSem.get(0).getSemclass());

            Fs frameInterface = new Fs();

            // if (tt.getFrames() != null) {
            // List<Fs> frames = tt.getFrames();
            // if (tlist != null) {
            // if (tlist.get(frameid) != null) {
            // // Looking for the interface of the frame
            // frameInterface = tlist.get(frameid).getHead()
            // .getIface();
            // frames.addAll(tlist.get(frameid).getHead().getFrames());
            // }
            // }
            // }
            // if (tt.getFrames() == null) {
            // if (tlist != null) {
            // if (tlist.get(frameid) != null) {
            // // Looking for the interface of the frame
            // frameInterface = tlist.get(frameid).getHead()
            // .getIface();
            // tt.setFrames(tlist.get(frameid).getHead().getFrames());
            // }
            // }
            // }
            if (tt.getFrameSem() != null) {
                Frame frameSem = tt.getFrameSem();
                if (tlist != null && tlist.get(frameid) != null) {
                    frameInterface = tlist.get(frameid).getHead().getIface();
                    frameSem.addOtherFrame(
                            tlist.get(frameid).getHead().getFrameSem());
                    // tt.setFrameSem(frameSem);
                }
            }
            if (tt.getFrameSem() == null) {
                if (tlist != null) {
                    if (tlist.get(frameid) != null) {
                        // Looking for the interface of the frame
                        frameInterface = tlist.get(frameid).getHead()
                                .getIface();
                        tt.setFrameSem(
                                tlist.get(frameid).getHead().getFrameSem());
                    }
                }
            }
            // Why does this happen?
            // DA:Because the tlist and/or tlist.get(frameid) might be null,
            // too?
            // if (tt.getFrames() == null) {
            // tt.setFrames(new ArrayList<Fs>());
            // }
            if (tt.getFrameSem() == null) {
                tt.setFrameSem(new Frame());
            }

            try {
                tt.setIface(FsTools.unify(frameInterface, tt.getIface(), env));
                // tt.setFrames(ElementaryTree.updateFrames(tt.getFrames(), env,
                // false));
                // List<Fs> newFrames = tt.getFrames();

                // for (int ii = 0; ii < newFrames.size() - 1; ii++) {
                // for (int jj = ii + 1; jj < newFrames.size(); jj++) {
                // if (newFrames.get(ii).getCoref()
                // .equals(newFrames.get(jj).getCoref())) {
                // Fs res = Fs.unify(newFrames.get(ii),
                // newFrames.get(jj), env,
                // situation.getTypeHierarchy());
                // // newFrames.set(ii,res);
                // // newFrames.set(jj,res);
                // // System.out.println("Unified frames by
                // // coreference");
                // }
                // }
                // }
                // tt.setFrames(newFrames);

                // DA do the same thing to the FrameSem
                tt.setFrameSem(ElementaryTree.updateFrameSem(tt.getFrameSem(),
                        env, false));

                List<Fs> newFrames = tt.getFrameSem().getFeatureStructures();

                for (int ii = 0; ii < newFrames.size() - 1; ii++) {
                    for (int jj = ii + 1; jj < newFrames.size(); jj++) {
                        if (newFrames.get(ii).getCoref()
                                .equals(newFrames.get(jj).getCoref())) {
                            Fs res = FsTools.unify(newFrames.get(ii),
                                    newFrames.get(jj), env);
                            // newFrames.set(ii,res);
                            // newFrames.set(jj,res);
                            // System.out.println("Unified frames by
                            // coreference");
                        }
                    }
                }
                Set<Relation> newRelations = new HashSet<Relation>();
                for (Relation oldRel : tt.getFrameSem().getRelations()) {
                    List<Value> newArgs = new LinkedList<Value>();
                    for (Value oldVal : oldRel.getArguments()) {
                        Value oldCopy = new Value(oldVal);
                        oldCopy.update(env, true);
                        // Value newVal = env.deref(oldVal);
                        newArgs.add(oldCopy);
                    }
                    newRelations.add(new Relation(oldRel.getName(), newArgs));
                }
                tt.setFrameSem(new Frame(newFrames, newRelations));

                // System.out.println("treeselector framesem: " + tt+
                // tt.getFrameSem());
                // END DA

                // System.out.println("Frames after processing:");
                // for(Fs ttframe: tt.getFrames()){
                // System.out.println(ttframe);
                // }
            } catch (UnifyException e) {
                System.err.println(
                        "Semantic features unification failed on tree ");
                System.err.println(e);
                // This exception should be raised, but not cancel the whole
                // anchoring
                // it might just be one of the frames given by the lexicon which
                // raises it

                // throw new AnchoringException(); // we withdraw the
                // current anchoring
            }
        }
        List<String> treeTrace = tt.getTrace();
        List<SemLit> treeSem = tt.getSem();

        if (lemmaSem != null) {
            for (int k = 0; k < lemmaSem.size(); k++) {
                // if (treeTrace.contains(lemmaSem.get(k).getSemclass())) {
                if (true) {
                    // if the called semantic class has been used in the tree
                    // (cf trace)
                    // we unify the interface and semantic arguments
                    Fs semFs = new Fs(lemmaSem.get(k).getArgs());
                    try {
                        // System.out.println("Unifying: " + semFs);
                        // System.out.println(" with : " + tt.getIface());

                        tt.setIface(FsTools.unify(semFs, tt.getIface(), env));
                        tt.setFrameSem(ElementaryTree
                                .updateFrameSem(tt.getFrameSem(), env, false));
                        // System.out.println(
                        // "Result in FrameSem: " + tt.getFrameSem());
                        // the environment now contains the bindings for
                        // semantic variables
                        // we can update the tree semantics
                        for (int ksem = 0; ksem < treeSem.size(); ksem++) {
                            treeSem.get(ksem).update(env, false);
                        }
                    } catch (UnifyException e) {
                        System.err.println(
                                "Semantic features unification failed on tree "
                                        + hd.getOriginalId()
                                        + " for semantic class "
                                        + lemmaSem.get(k).toString());
                        System.err.println(e);
                        throw new AnchoringException(); // we withdraw the
                                                        // current anchoring
                    }
                }
            }
        }

        // System.err.println(" (after semantic anchoring)");
        // System.err.println(env.toString());

        // e') we update all the FS in the trees according to the env state
        try {
            tt.updateFS(tt.getRoot(), env, false);
        } catch (UnifyException e) {
            System.err
                    .println("FS update failed on tree " + tt.getOriginalId());
            System.err.println(e);
            throw new AnchoringException(); // we withdraw the current anchoring
        }
        if (tl != null) {

            for (int j = 0; j < tl.size(); j++) {
                try {
                    tl.get(j).updateFS(tl.get(j).getRoot(), env, false);
                } catch (UnifyException e) {
                    System.err.println("FS update failed on tree "
                            + tl.get(j).getOriginalId());
                    System.err.println(e);
                    throw new AnchoringException(); // we withdraw the current
                                                    // anchoring
                }
            }
        }

        // Updating the variables in the frames
        // try {
        // List<Fs> newFrames = new ArrayList<Fs>();
        // if (tt.getFrames() != null)
        // for (Fs f : tt.getFrames()) {
        // // System.out.println(
        // // "Updating variables in a frame, with environment "
        // // + env);
        //
        // // System.out.println("Before: " + f);
        //
        // newFrames.add(Fs.updateFS(f, env, false));
        // // System.out.println("After: " + f);
        //
        // }
        // tt.setFrames(newFrames);
        // } catch (UnifyException e) {
        // System.err
        // .println("FS update failed on tree " + tt.getOriginalId());
        // System.err.println(e);
        // throw new AnchoringException(); // we withdraw the current anchoring
        // }

        // System.err.println(" (after FS updating)");
        // System.err.println(env.toString());

        // f) We store the result
        InstantiatedTuple x = new InstantiatedTuple();
        List<String> xTrees = new LinkedList<String>();
        String tupleId = nf.getName(t.getId());
        x.setId(tupleId);
        x.setOriginalId(t.getId());
        x.setFamily(t.getFamily());
        x.setLemma(t.getLemma());
        x.setSemLabels(semlabels);
        slabels.addAll(semlabels);
        // we initialize the tuple's polarities
        Polarities p = new Polarities();
        PolarizedTuple ptl = new PolarizedTuple(x.getId());
        ptl.setOriginalID(x.getOriginalId());
        // we add the head's polarities
        tt.getPolarities(p);
        x.setHead(tt);
        // System.out.println(tt.getLexItems());
        ptl.addLexicals(tt.getLexItems());
        // we update the tree dictionary
        // -------------------------------
        Map<String, List<MorphEntry>> lm = Situation.getGrammar()
                .getMorphEntries();

        List<TagTree> ttlist = new ArrayList<TagTree>();
        // for (Node CoAnchor:tt.getCoAnchors()){
        for (int CoAnchorIndex = 0; CoAnchorIndex < tt.getCoAnchors()
                .size(); CoAnchorIndex++) {
            // System.out.println(CoAnchor);
            Node CoAnchor = tt.getCoAnchors().get(CoAnchorIndex);
            // System.out.println(CoAnchor);
            TagNode CoAnc = (TagNode) CoAnchor;
            // Somehow the LexItem does not always get created before
            if (CoAnc.getChildren() == null) {
                List<Node> ch = new LinkedList<Node>();
                TagNode lex = new TagNode();
                lex.setType(TagNode.LEX); // lex node
                lex.setCategory(CoAnc.getCategory()); // the word
                lex.setAddress(CoAnc.getAddress() + ".1"); // Gorn
                                                           // address
                ch.add(lex);
                CoAnc.setChildren(ch);
            }
            TagNode LexItem = (TagNode) CoAnc.getChildren().get(0);
            // If we can find the item in the lexicon, we add as many
            // trees as we can find items (if more than one)
            if (lm.containsKey(LexItem.getCategory())) {
                List<MorphEntry> lme = lm.get(LexItem.getCategory());
                for (int j = 0; j < lme.size(); j++) {
                    // try{
                    // //TagTree ttt=new TagTree(tt.getRoot());
                    // //ttt.setId(nf.getUniqueName());
                    // //System.out.println("created new TagTree with ID:
                    // "+ttt.getId());
                    // System.out.println(LexItem);
                    // System.out.println("Unifying
                    // "+lme.get(j).getLemmarefs().get(0).getFeatures()+" and
                    // "+CoAnc.getLabel());
                    // Environment E=new Environment(5);
                    // Fs
                    // Unify=Fs.unify(lme.get(j).getLemmarefs().get(0).getFeatures(),CoAnc.getLabel(),E);
                    // // we also unify this Fs with top
                    // if(CoAnc.getLabel().hasFeat("top")){
                    // Fs Top=CoAnc.getLabel().getFeat("top").getAvmVal();
                    // Fs New=new Fs(0);
                    // New.setFeat("top",new
                    // Value(lme.get(j).getLemmarefs().get(0).getFeatures()));
                    // Unify=Fs.unify(Unify,New,E);
                    // }
                    // CoAnc.setLabel(Unify);
                    // //ttlist.add(ttt);
                    // }
                    // catch (UnifyException e) {
                    // System.err.println(
                    // "Features unification failed on tree ");
                    // System.err.println(e);
                    // throw new AnchoringException(); // we withdraw the
                    // // current coanchoring
                    // }

                }
            }
        }
        if (ttlist.size() == 0) {
            ttlist.add(tt);
        }
        // System.out.println("Adding elements: "+ttlist.size());
        for (TagTree one_tt : ttlist) {
            // System.out.println("Current id: "+one_tt.getId());
            treeHash.put(one_tt.getId(), one_tt);
            // and the tuple:
            xTrees.add(one_tt.getId());
        }
        // System.out.println("Added elements");

        // System.err.println("Added " + tt.getOriginalId());
        // System.err.println(tt.getRoot().toString());
        if (tl != null) {
            for (int l = 0; l < tl.size(); l++) {
                treeHash.put(tl.get(l).getId(), tl.get(l));
                xTrees.add(tl.get(l).getId());
                tl.get(l).getPolarities(p);
                // System.out.println(tt.getLexItems());
                ptl.addLexicals(tl.get(l).getLexItems());
            }
            x.setArguments(tl);
        }

        // -------------------------------
        tupleHash.put(x.getId(), xTrees);
        ptl.setPol(p);
        plm.addTuple(ptl);
        return x;
    }

    public void store(Map<String, List<Tuple>> grammar) {
        /**
         * Method used when no anchoring is needed, the tree selector
         * then only stores the trees to the dictionary and lists.
         */
        Set<String> keys = grammar.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String tupleId = it.next();
            List<Tuple> tup = grammar.get(tupleId);
            for (int i = 0; i < tup.size(); i++) {
                Tuple tu = tup.get(i);
                anctuples.add(tu);
                tu.getHead().setOriginalId(tu.getHead().getId());
                treeHash.put(tu.getHead().getId(), tu.getHead());
                List<TagTree> args = tu.getArguments();
                if (args != null) {
                    for (int j = 0; j < args.size(); j++) {
                        TagTree ttree = args.get(j);
                        ttree.setOriginalId(ttree.getId());
                        treeHash.put(ttree.getId(), ttree);
                    }
                }
            }
        }
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public List<Word> getTokens() {
        return tokens;
    }

    public void setTokens(List<Word> tokens) {
        this.tokens = tokens;
    }

    public List<Tuple> getAnctuples() {
        return anctuples;
    }

    public void setAnctuples(List<Tuple> anctuples) {
        this.anctuples = anctuples;
    }

    public Map<String, TagTree> getTreeHash() {
        return treeHash;
    }

    public Map<String, List<String>> getTupleHash() {
        return tupleHash;
    }

    public List<PolarizedToken> getPtokens() {
        return ptokens;
    }

    public List<String> getLexNodes() {
        return lexNodes;
    }

    public Map<String, List<String>> getCoancNodes() {
        return coancNodes;
    }

    public String toString() {
        String res = "";
        res += "\n Retrieved tuples : \n\t";
        for (int i = 0; i < anctuples.size(); i++) {
            InstantiatedTuple t = (InstantiatedTuple) anctuples.get(i);
            res += t.getOriginalId() + "[" + t.getId() + "]("
                    + t.getLemma().getName() + "-"
                    + t.getLemma().getLexItem().getLex() + ") ";
        }
        res += "\n Anchored tuples : \n";
        for (int i = 0; i < anctuples.size(); i++) {
            Tuple t = anctuples.get(i);
            res += "\n Tuple " + (i + 1) + ": \n";
            TagTree anchored = t.getHead();
            res += anchored.toString("  ");
            if (t.getArguments() != null) {
                for (int j = 0; j < t.getArguments().size(); j++) {
                    TagTree arg = t.getArguments().get(j);
                    res += arg.toString("  ");
                }
            }
        }
        return res;
    }

    public void addCoanchor(String word, String cat) {
        List<String> cats = null;
        if (coancNodes.containsKey(word))
            cats = coancNodes.get(word);
        else
            cats = new LinkedList<String>();
        if (!cats.contains(cat))
            cats.add(cat);
        coancNodes.put(word, cats);
    }

    public void updateCoAnchors(Lemma l) {
        List<CoAnchor> lca = l.getCoAnchors();
        for (CoAnchor a : lca) {
            List<String> words = a.getLex();
            for (String s : words) {
                this.addCoanchor(s, a.getCat());
            }
        }
    }

    public long getambig() {
        long res = 1;
        Iterator<String> it = ambiguity.keySet().iterator();
        while (it.hasNext()) {
            String tok = it.next();
            int num = ambiguity.get(tok);
            // System.err.println(tok + " , " + num);
            if (num != 0) // for lexical nodes
                res *= num;
        }
        return res;
    }

}
