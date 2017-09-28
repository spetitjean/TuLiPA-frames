/*
 *  File LexSem.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fri Jan 25 13:39:50 CET 2008
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

import de.tuebingen.tag.Fs;

public class LexSem {

    private String semclass;
    private Fs args;

    public LexSem(String name) {
        semclass = name;
    }

    public LexSem(String sc, Fs a) {
        semclass = sc;
        args = a;
    }

    public LexSem(LexSem other) {
        semclass = other.getSemclass();
        args = new Fs(other.getArgs());
    }

    public String getSemclass() {
        return semclass;
    }

    public void setSemclass(String semclass) {
        this.semclass = semclass;
    }

    public Fs getArgs() {
        return args;
    }

    public void setArgs(Fs args) {
        this.args = args;
    }

    public String toString() {
        String res = "";
        res += semclass + "[" + args.toString() + "]\n";
        return res;
    }

}
