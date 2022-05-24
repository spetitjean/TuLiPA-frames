/*
 *  File Lemmaref.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:08:43 CEST 2007
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

import de.duesseldorf.frames.Fs;

public class Lemmaref {

    private String name;
    private String cat;
    private Fs features;

    public Lemmaref(String n, String c) {
        name = n;
        cat = c;
        features = new Fs();
    }

    public Lemmaref(Lemmaref other) {
        name = other.getName();
        cat = other.getCat();
        features = new Fs(other.getFeatures());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public Fs getFeatures() {
        return features;
    }

    public void setFeatures(Fs features) {
        this.features = features;
    }

    public String toString() {
        return "  lemmaref : " + name + " - cat : " + cat + " - features : ["
                + features.toString() + "]\n";
    }

}
