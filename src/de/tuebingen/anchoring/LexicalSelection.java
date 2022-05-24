/*
 *  File LexicalSelection.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@loria.fr>
 *
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fri May 16 16:03:14 CEST 2008
 *
 *  This file is part of the Polarity Filter
 *
 *  The Polarity Filter is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The Polarity Filter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.tuebingen.anchoring;

import java.util.*;

import de.tuebingen.lexicon.*;
import fr.loria.disambiguation.Polarities;
import de.tuebingen.tokenizer.Word;

public class LexicalSelection {

    private boolean verbose;
    private List<Word> tokens;  // tokens (as Words)
    private Map<String, List<InstantiatedTagTree>> selected;  // anchored tuples
    private Map<String, Polarities> polarities;
    private Map<String, Integer> ambiguity;

    public LexicalSelection(Map<String, Polarities> pol, List<Word> w, boolean v) {
        verbose = v;
        tokens = w;
        polarities = pol;
        selected = new HashMap<String, List<InstantiatedTagTree>>();
        ambiguity = new HashMap<String, Integer>();
    }

    public void retrieve(Map<String, List<MorphEntry>> lm, Map<String, List<Lemma>> ll, Map<String, List<String>> lt) {
        // lm is a mapping between a morph name and morph entries
        // ll is a mapping between a lemma name and lemma entries
        // lt is a mapping between a family name and trees
        // slabels is where to store the semantic labels
        for (int i = 0; i < tokens.size(); i++) {
            String s = tokens.get(i).getWord();
            ambiguity.put(s, 0); //init
            if (lm.containsKey(s)) {
                if (verbose)
                    System.err.println("Processed token: " + s);
                List<MorphEntry> lme = lm.get(s);
                for (int j = 0; j < lme.size(); j++) {
                    retrieveLemma(tokens.get(i), new InstantiatedMorph(lme.get(j), tokens.get(i)), ll, lt);
                }
            } else {
                if (verbose) {
                    System.err.println("Unknown token (not in the morph lexicon): " + s);
                }
            }
        }
    }

    public void retrieveLemma(Word w, InstantiatedMorph m, Map<String, List<Lemma>> ll, Map<String, List<String>> lt) {
        // m is a morph entry coupled with a lexical item from the input sentence
        // ll is a mapping between a lemma name and lemma entries
        // lt is a mapping between a family name and trees

        // for each reference to a lemma included in the morph entry:
        List<Lemmaref> lm = m.getLemmarefs();
        for (int k = 0; k < lm.size(); k++) {
            // we retrieve the name and cat of the lemma reference
            String lem = lm.get(k).getName();
            String cat = lm.get(k).getCat();

            if (ll.containsKey(lem)) {
                if (verbose)
                    System.err.println("Processed lemma: " + lem);
                // we retrieve the lemmas matching the reference name
                List<Lemma> listlemma = ll.get(lem);
                for (int l = 0; l < listlemma.size(); l++) {
                    // if both the lemma and the cat match the reference, we instantiate the lemma entry
                    if (listlemma.get(l).getCat().equals(cat)) {
                        InstantiatedLemma il = new InstantiatedLemma(listlemma.get(l), m, lm.get(k));
                        retrieveTrees(w, il, lt);
                    } else {
                        if (verbose)
                            System.err.println("Rejected " + listlemma.get(l).toString() + " expected cat: " + cat);
                    }
                }
            } else {
                if (verbose)
                    System.err.println("Unknown lemma (not in the lemma lexicon): " + lem);
            }
        }
    }

    public void retrieveTrees(Word w, InstantiatedLemma il, Map<String, List<String>> lt) {
        // il is a lemma entry coupled with the lemma reference from the input morph
        // lt is a mapping between a family name and trees
        // slabels is where to store the semantic labels

        int cpt_tmp = 0; // for counting ambiguity

        // for each anchoring scheme defined in the lemma entry
        List<Anchor> la = il.getAnchors();
        for (int k = 0; k < la.size(); k++) {
            // we retrieve the tuple name the scheme contains
            String family = la.get(k).getTree_id();
            if (lt.containsKey(family)) {
                if (verbose)
                    System.err.println("Processed family: " + family);
                // for each matching tuple
                for (int l = 0; l < lt.get(family).size(); l++) {
                    cpt_tmp++;
                    String treeID = lt.get(family).get(l);
                    InstantiatedTagTree it = new InstantiatedTagTree(la.get(k), treeID, il);
                    String word = w.getWord();
                    it.setWord(w);
                    Polarities treePol = polarities.get(treeID);
                    if (treePol == null)
                        System.err.println("*** WARNING *** \n Polarities not found for tree " + treeID);
                    else
                        it.setPolarities(treePol);
                    List<InstantiatedTagTree> lit = selected.get(word);
                    if (lit == null) { // first selection of a tree for the current word
                        lit = new LinkedList<InstantiatedTagTree>();
                        selected.put(word, lit);
                    }
                    if (verbose)
                        System.err.println("Added tree: " + it.toString());
                    lit.add(it);
                }
            } else {
                if (verbose) {
                    System.err.println("Unknown family (not in the tuple lexicon): " + family);
                }
            }
        }
        // ambiguity computation:
        String word = il.getLexItem().getLex();
        //System.err.println("Ambig for word " + word + " : " + cpt_tmp);
        int amb = ambiguity.get(word);
        amb += cpt_tmp;
        ambiguity.put(word, amb);
    }

    public Map<String, List<InstantiatedTagTree>> getSelected() {
        return selected;
    }

    public long getambig() {
        long res = 1;
        Iterator<String> it = ambiguity.keySet().iterator();
        while (it.hasNext()) {
            String tok = it.next();
            int num = ambiguity.get(tok);
            //System.err.println(tok + " , " + num);
            if (num != 0) //for lexical nodes
                res *= num;
        }
        return res;
    }
}
