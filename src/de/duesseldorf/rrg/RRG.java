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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.MorphEntry;
import de.tuebingen.tag.Tuple;
import de.tuebingen.tree.Grammar;

public class RRG implements Grammar {

    // What is in the xml input grammars: an entry has a family, trace, frame,
    // tree, interface

    Set<RRGTree> trees;
    boolean isLexicalised;

    public RRG() {
        trees = new HashSet<RRGTree>();
        isLexicalised = lookForLexicalisation();
    }

    public RRG(Set<RRGTree> trees) {
        this.trees = trees;
        isLexicalised = lookForLexicalisation();
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

    // public void addTree(RRGTree tree) {
    // trees.add(tree);
    // }

    public Set<RRGTree> getTrees() {
        return this.trees;
    }

    //////// interface methods that are not used:
    public boolean needsAnchoring() {
        return false;
    }

    public void setNeedsAnchoring(boolean b) {
        // TODO Auto-generated method stub
    }

    public void setMorphEntries(Map<String, List<MorphEntry>> morphEntries) {
        // TODO Auto-generated method stub
    }

    public void setLemmas(Map<String, List<Lemma>> lemmas) {
        // TODO Auto-generated method stub
    }

    public Map<String, List<Lemma>> getLemmas() {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, List<MorphEntry>> getMorphEntries() {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, List<Tuple>> getGrammar() {
        // TODO Auto-generated method stub
        return null;
    }

}
