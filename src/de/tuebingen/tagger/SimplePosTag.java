/*
 *  File SimplePosTag.java
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
 *     Mi 12. Dez 11:58:40 CET 2007
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
package de.tuebingen.tagger;

/**
 * @author wmaier, parmenti
 */
public class SimplePosTag implements PosTag {

    private String flex;  // the lexical item from the input string
    private String tag;   // the cat assigned by the tagger
    private String lemma; // the lemma assigned by the tagger

    public SimplePosTag() {
    }

    public SimplePosTag(String f, String t, String l) {
        flex = f;
        tag = t;
        lemma = l;
    }

    public String getFlex() {
        return flex;
    }

    public void setFlex(String flex) {
        this.flex = flex;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setTag(Object tag) {
        this.tag = tag.toString();
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public String toString() {
        String res = " ";
        if (flex != null && tag != null && lemma != null) {
            res += "(" + flex + ", ";
            res += "pos: " + tag + ", ";
            res += "lemma: " + lemma + ") ";
        }
        return res;
    }
}
