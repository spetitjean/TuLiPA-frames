/*
 *  File InstantiatedTuple.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 11:12:47 CEST 2007
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
package de.tuebingen.anchoring;

import java.util.List;

import de.tuebingen.lexicon.Anchor;
import de.tuebingen.tag.Tuple;

public class InstantiatedTuple extends Tuple {

    private InstantiatedLemma lemma;
    private Anchor anchor;
    private List<String> semLabels;

    public InstantiatedTuple() {
        super();
    }

    public InstantiatedTuple(InstantiatedTuple it) {
        super((Tuple) it);
        lemma = new InstantiatedLemma(it.getLemma());
        anchor = new Anchor(it.getAnchor());
    }

    public InstantiatedTuple(Tuple t, InstantiatedLemma l) {
        super(t);
        this.lemma = new InstantiatedLemma(l);
    }

    public InstantiatedTuple(Anchor a, Tuple t, InstantiatedLemma l) {
        super(t);
        this.anchor = new Anchor(a);
        this.lemma = new InstantiatedLemma(l);
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    public InstantiatedLemma getLemma() {
        return lemma;
    }

    public void setLemma(InstantiatedLemma lemma) {
        this.lemma = lemma;
    }

    public List<String> getSemLabels() {
        return semLabels;
    }

    public void setSemLabels(List<String> semLabels) {
        this.semLabels = semLabels;
    }

}
