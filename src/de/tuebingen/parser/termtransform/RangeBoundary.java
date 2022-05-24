/*
 *  File RangeBoundary.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2008
 *
 *  Last modified:
 *     Mi 8. Okt 10:21:32 CET 2008
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


package de.tuebingen.parser.termtransform;

public class RangeBoundary {
    private int id;
    private int val;

    public RangeBoundary(int name) {
        this(name, -1);
    }

    public RangeBoundary(int name, int val) {
        this.id = name;
        this.val = val;
    }

    public RangeBoundary(RangeBoundary r) {
        this.id = r.id;
        this.val = r.val;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addToId(int n) {
        id += n;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public String toString() {
        String ret = "V" + String.valueOf(id);
        if (val > -1) {
            ret += ":" + val;
        }
        return ret;
    }

}
