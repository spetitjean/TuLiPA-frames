/*
 *  File InstantiatedTagTree.java
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

import de.tuebingen.lexicon.Anchor;
import de.tuebingen.tokenizer.Word;
import fr.loria.disambiguation.Polarities;

public class InstantiatedTagTree {

    private InstantiatedLemma lemma;
    private Anchor iAnchor;
    private String treeName;
    private Polarities polarities;
    private Word word;

    public InstantiatedTagTree(String tn, Polarities pol) {
        this.polarities = pol;
        this.treeName = tn;
    }

    public InstantiatedTagTree(String tn, InstantiatedLemma l) {
        this.treeName = tn;
        this.lemma = new InstantiatedLemma(l);
    }

    public InstantiatedTagTree(Anchor a, String tn, InstantiatedLemma l) {
        this(tn, l);
        this.iAnchor = new Anchor(a);
    }

    public InstantiatedTagTree(InstantiatedTagTree it) {
        lemma = new InstantiatedLemma(it.getLemma());
        iAnchor = new Anchor(it.getIAnchor());
    }

    public Anchor getIAnchor() {
        return iAnchor;
    }

    public void setAnchor(Anchor anchor) {
        this.iAnchor = anchor;
    }

    public InstantiatedLemma getLemma() {
        return lemma;
    }

    public void setLemma(InstantiatedLemma lemma) {
        this.lemma = lemma;
    }

    public Polarities getPolarities() {
        return polarities;
    }

    public void setPolarities(Polarities polarities) {
        this.polarities = polarities;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public String getTreeName() {
        return treeName;
    }

    public void setTreeName(String treeName) {
        this.treeName = treeName;
    }

    public String toString() {
        String res = "";
        res += treeName + "-" + word + "\n";
        res += polarities.toString();
        return res;
    }

}
