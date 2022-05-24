/*
 *  File IncrementalEarleyGoalItem.java
 *
 *  Authors:
 *     Johannes Dellert
 *
 *  Copyright:
 *     Johannes Dellert, 2009
 *
 *  Last modified:
 *     Do 16. Apr 09:55:36 CEST 2009
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
package de.tuebingen.parser.simple;

import de.tuebingen.rcg.Clause;

public class IncrementalEarleyGoalItem extends IncrementalEarleyItem {
    public IncrementalEarleyGoalItem(Clause cl) {
        super(cl);
    }

    public boolean equals(Object obj) {
        if (obj instanceof IncrementalEarleyItem) {
            //System.err.println("eq " + this + " = " + obj);
            IncrementalEarleyItem o = (IncrementalEarleyItem) obj;
            if (!o.cl.equals(cl)) return false;
            if (o.i != i) return false;
            if (o.j != j) return false;
            if (o.pos != pos) return false;
            return true;
        }
        return false;
    }

    public String mainString() {
        return "[" + cl.toString() + "," + pos + ",<" + i + "," + j + ">=" + el + "]";
    }
}
