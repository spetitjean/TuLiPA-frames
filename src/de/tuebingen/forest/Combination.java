/*
 *  File Combination.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fr 18. Jan 19:44:44 CET 2008
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
package de.tuebingen.forest;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Combination implements Iterable<TreeOp> {

    private List<TreeOp> operations;

    public Combination() {
        operations = new LinkedList<TreeOp>();
    }

    //ADDED_BY_TS
    public Combination(Combination toCopy) {

        operations = new LinkedList<TreeOp>(toCopy.getOperations());
    }
    //END_ADDED_BY_TS

    public void addOp(TreeOp to) {
        operations.add(to);
    }

    public int size() {
        return operations.size();
    }

    public List<TreeOp> getOperations() {
        return operations;
    }

    public void setOperations(List<TreeOp> operations) {
        this.operations = operations;
    }

    public Iterator<TreeOp> iterator() {
        return operations.iterator();
    }

    public String toString() {
        String res = "";
        for (int i = 0; i < operations.size(); i++) {
            if (i > 0)
                res += " ; ";
            res += "(" + operations.get(i).toString() + ")";
        }
        return res;
    }

}
