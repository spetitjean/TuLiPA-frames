/*
 *  File RRG.java
 *
 *  Authors:
 *     David Arps <david.arps@hhu.de
 *     
 *  Copyright:
 *     David Arps, 2018
 *
 *  This file is part of the TuLiPA system
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
package de.duesseldorf.rrg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.MorphEntry;
import de.tuebingen.tag.Tuple;
import de.tuebingen.tree.Grammar;

public class RRG implements Grammar {

    // What is in the xml input grammars: an entry has a family, trace, frame,
    // tree, interface

    private Set<RRGTree> trees;
    private boolean isLexicalised;
    private boolean needsAnchoring;
    private Map<String, List<MorphEntry>> morphEntries;
    private Map<String, List<Lemma>> lemmas;
    private Set<RRGTree> anchoredTrees;

    public RRG() {
        trees = new HashSet<RRGTree>();
        anchoredTrees = new HashSet<RRGTree>();
        isLexicalised = lookForLexicalisation();
        needsAnchoring = lookForAnchors();
    }

    public RRG(Set<RRGTree> trees) {
        this.trees = trees;
        this.anchoredTrees = new HashSet<RRGTree>();
        isLexicalised = lookForLexicalisation();
        needsAnchoring = lookForAnchors();
    }

    /**
     * 
     * @return true iff at least one tree in the grammar has an anchor node
     */
    private boolean lookForAnchors() {
        boolean thereAreAnchors = trees.parallelStream()
                .anyMatch(tree -> tree.getAnchorNode() != null);
        return thereAreAnchors;
    }

    /**
     * 
     * @return true iff each and every tree in the set of trees has at least one
     *         lexical node, false otherwise
     */
    private boolean lookForLexicalisation() {
        boolean res = trees.parallelStream()
                .noneMatch(tree -> tree.getLexNodes().isEmpty());
        if (!res) {
            System.out.println("creating unlexicalized RRG grammar");
            return false;
        } else {
            System.out.println("creating lexicalized RRG grammar");
            return true;
        }
    }

    public boolean isLexicalised() {
        return isLexicalised;
    }

    public Set<RRGTree> getTrees() {
        return this.trees;
    }

    //////// interface methods that are not used:
    public boolean needsAnchoring() {
        return needsAnchoring;
    }

    public void setNeedsAnchoring(boolean b) {
        needsAnchoring = b;
    }

    public void setMorphEntries(Map<String, List<MorphEntry>> morphEntries) {
        this.morphEntries = morphEntries;
    }

    public void setLemmas(Map<String, List<Lemma>> lemmas) {
        this.lemmas = lemmas;
    }

    public Map<String, List<Lemma>> getLemmas() {
        return lemmas;
    }

    public Map<String, List<MorphEntry>> getMorphEntries() {
        return morphEntries;
    }

    public Map<String, List<Tuple>> getGrammar() {
        // TODO Auto-generated method stub
        return new HashMap<String, List<Tuple>>();
    }

    public Set<RRGTree> getTreesByFamily(String family) {
        return trees.parallelStream()
                .filter(tree -> tree.getFamily().equals(family))
                .collect(Collectors.toSet());
    }

    public void addAnchoredTree(RRGTree anchoredTree) {
        this.anchoredTrees.add(anchoredTree);
    }

    public Set<RRGTree> getAnchoredTrees() {
        if (needsAnchoring) {
            return this.anchoredTrees;
        } else {
            return this.trees;
        }

    }
}
