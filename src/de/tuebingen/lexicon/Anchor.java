/*
 *  File Anchor.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:06:46 CEST 2007
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
package de.tuebingen.lexicon;

import java.util.LinkedList;
import java.util.List;

import de.duesseldorf.frames.Fs;

public class Anchor {

    private String tree_id;
    private List<CoAnchor> coanchors;
    private List<Equation> equations;
    private Fs filter;
    private List<LexSem> semantics;

    public Anchor(String t) {
        tree_id = t;
        coanchors = null;
        equations = null;
        filter = null;
        semantics = null;
    }

    public Anchor(Anchor anc) {
        tree_id = anc.getTree_id();
        coanchors = new LinkedList<CoAnchor>();
        for (int i = 0; anc.getCoanchors() != null && i < anc.getCoanchors().size(); i++) {
            coanchors.add(new CoAnchor(anc.getCoanchors().get(i)));
        }
        equations = new LinkedList<Equation>();
        for (int i = 0; anc.getEquations() != null && i < anc.getEquations().size(); i++) {
            equations.add(new Equation(anc.getEquations().get(i)));
        }
        filter = new Fs(anc.getFilter());
        semantics = new LinkedList<LexSem>();
        if (anc.getSemantics() != null) {
            for (int i = 0; i < anc.getSemantics().size(); i++) {
                semantics.add(new LexSem(anc.getSemantics().get(i)));
            }
        }
    }

    public void addEquation(Equation eq) {
        if (equations == null)
            equations = new LinkedList<Equation>();
        equations.add(eq);
    }

    public void addSem(LexSem ls) {
        if (semantics == null)
            semantics = new LinkedList<LexSem>();
        semantics.add(ls);
    }

    public void addCoAnchor(CoAnchor ca) {
        if (coanchors == null)
            coanchors = new LinkedList<CoAnchor>();
        coanchors.add(ca);
    }

    public Fs getFilter() {
        return filter;
    }

    public void setFilter(Fs filter) {
        this.filter = filter;
    }

    public String getTree_id() {
        return tree_id;
    }

    public void setTree_id(String tree_id) {
        this.tree_id = tree_id;
    }

    public List<CoAnchor> getCoanchors() {
        return coanchors;
    }

    public void setCoanchors(List<CoAnchor> coanchors) {
        this.coanchors = coanchors;
    }

    public List<Equation> getEquations() {
        return equations;
    }

    public void setEquations(List<Equation> equations) {
        this.equations = equations;
    }

    public List<LexSem> getSemantics() {
        return semantics;
    }

    public void setSemantics(List<LexSem> semantics) {
        this.semantics = semantics;
    }

    public String toString() {
        String res = "";
        String res1 = "";
        String res2 = "";
        for (int i = 0; coanchors != null && i < coanchors.size(); i++) {
            res += coanchors.get(i).toString();
        }
        for (int i = 0; equations != null && i < equations.size(); i++) {
            res1 += equations.get(i).toString();
        }
        if (semantics != null) {
            for (int i = 0; i < semantics.size(); i++) {
                res2 += semantics.get(i).toString();
            }
        }
        return "  anchor : " + tree_id + "\n  coanchors : " + res + "\n  equations : " + res1 + "\n  filter : [" + filter.toString() + "]\n semantics : " + res2 + "\n";
    }

}
