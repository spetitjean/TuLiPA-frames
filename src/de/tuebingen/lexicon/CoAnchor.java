/*
 *  File CoAnchor.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:07:13 CEST 2007
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

public class CoAnchor {

    private String node_id;
    private String cat;
    private List<String> lex;

    public CoAnchor(String n) {
        node_id = n;
        lex = null;
    }

    public CoAnchor(CoAnchor other) {
        node_id = other.getNode_id();
        lex = new LinkedList<String>();
        for (int i = 0; i < other.getLex().size(); i++) {
            lex.add(other.getLex().get(i));
        }
    }

    public void addLex(String l) {
        if (lex == null)
            lex = new LinkedList<String>();
        lex.add(l);
    }

    public String getNode_id() {
        return node_id;
    }

    public void setNode_id(String node_id) {
        this.node_id = node_id;
    }

    public List<String> getLex() {
        return lex;
    }

    public void setLex(List<String> lex) {
        this.lex = lex;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String toString() {
        String res = "";
        for (int i = 0; i < lex.size(); i++) {
            res += " " + lex.get(i);
        }
        return "    coanchor : " + node_id + " - lex : " + res + " (cat: " + cat + ")\n";
    }
}
