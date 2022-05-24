/*
 *  File TreeOp.java
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

import de.tuebingen.rcg.PredComplexLabel;

public class TreeOp {

    private Tidentifier id;
    private int type;  // ADJ or SUB (i.e. 1 and 2 resp., cf PredComplexLabel)
    private TreeOp or;  // for ambiguity

    public TreeOp() {
    }

    public TreeOp(Tidentifier tid) {
        id = tid;
    }

    public TreeOp(Tidentifier tree, int t) {
        id = tree;
        type = t;
    }

    public TreeOp(TreeOp top) {
        id = new Tidentifier(top.getId());
        type = top.getType();
        if (top.getOr() != null)
            or = new TreeOp(top.getOr());
    }

    public boolean isBlank() {
        return (id == null);
    }

    public boolean isDisj() {
        return (or != null);
    }

    public Tidentifier getId() {
        return id;
    }

    public void setId(Tidentifier id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public TreeOp getOr() {
        return or;
    }

    public void setOr(TreeOp or) {
        this.or = or;
    }

    public String toString() {
        String res = (id == null) ? "" : id.getClauseId() + "";
        res += "[" + id.getBinding() + "]";
        switch (type) {
            case PredComplexLabel.ADJ:
                res += ".a" + "[" + id.getNodeId() + "]";
                break;
            case PredComplexLabel.SUB:
                res += ".s" + "[" + id.getNodeId() + "]";
                break;
            default:
                res += "._" + type;
        }
        if (or != null)
            res += " | " + or.toString();
        return res;
    }
}
