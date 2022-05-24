/*
 *  File TAG.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:45:07 CEST 2007
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

import java.util.List;

import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.MorphEntry;

/**
 * A class representing a tree-adjoining grammar.
 *
 * @author wmaier
 */
public class TAG {

    private List<TagTree> trees;
    private List<Lemma> lemmas;
    private List<MorphEntry> morphEntries;

    public List<TagTree> getTrees() {
        return trees;
    }

    public void setTrees(List<TagTree> trees) {
        this.trees = trees;
    }

    public void addTree(TagTree tree) {
        trees.add(tree);
    }

    public List<Lemma> getLemmas() {
        return lemmas;
    }

    public void setLemmas(List<Lemma> lemmas) {
        this.lemmas = lemmas;
    }

    public void addLemma(Lemma l) {
        lemmas.add(l);
    }

    public List<MorphEntry> getMorphEntries() {
        return morphEntries;
    }

    public void setMorphEntries(List<MorphEntry> morphEntries) {
        this.morphEntries = morphEntries;
    }

    public void addMorphEntry(MorphEntry m) {
        morphEntries.add(m);
    }


}
