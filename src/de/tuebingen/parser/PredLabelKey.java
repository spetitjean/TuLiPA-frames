/*
 *  File PredLabelKey.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:03:18 CEST 2007
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
package de.tuebingen.parser;

import java.util.List;
import java.util.Map;

import de.tuebingen.rcg.Argument;
import de.tuebingen.rcg.PredComplexLabel;
import de.tuebingen.rcg.PredLabel;
import de.tuebingen.tag.TagTree;

public class PredLabelKey {

    private PredLabel pl;
    private List<Argument> al;

    public PredLabelKey(PredLabel p, List<Argument> a) {
        pl = p;
        al = a;
    }

    public PredLabel getPl() {
        return pl;
    }

    public void setPl(PredLabel pl) {
        this.pl = pl;
    }

    public List<Argument> getAl() {
        return al;
    }

    public void setAl(List<Argument> al) {
        this.al = al;
    }


    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    public String toString() {
        String res = "";
        res += pl.toString() + " ( ";
        for (int i = 0; i < al.size(); i++) {
            res += al.get(i) + " ";
        }
        res += " )";
        return res;
    }

    // for pretty printing
    public String toString(Map<String, TagTree> dict) {
        String res = "";
        if (pl instanceof PredComplexLabel)
            res += ((PredComplexLabel) pl).toString(dict) + " ( ";
        else
            res += pl.toString() + " ( ";
        for (int i = 0; i < al.size(); i++) {
            res += al.get(i) + " ";
        }
        res += " )";
        return res;
    }
}
