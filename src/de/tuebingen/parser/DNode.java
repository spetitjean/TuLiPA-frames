/*
 *  File DNode.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fr 18. Jan 10:21:32 CET 2008
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

public class DNode {

    private ClauseKey instantiation;
    private DNode other;

    public DNode(ClauseKey ck) {
        instantiation = ck;
    }

    public void addDNode(ClauseKey ck) {
        if (!instantiation.equals(ck)) {
            if (other == null)
                other = new DNode(ck);
            else
                other.addDNode(ck);
        }
    }

    public boolean equals(DNode another) {
        boolean res = instantiation.equals(another.getInstantiation());
        if (other != null)
            res &= other.equals(another);
        return res;
    }

    public boolean isAmbiguous() {
        return (other != null);
    }

    public ClauseKey getInstantiation() {
        return instantiation;
    }

    public void setInstantiation(ClauseKey instantiation) {
        this.instantiation = instantiation;
    }

    public DNode getDerivStep() {
        return other;
    }

    public void setDerivStep(DNode derivStep) {
        this.other = derivStep;
    }

    public String toString() {
        String res = "";
        res += instantiation.getCindex();
        res += instantiation.getArgs();
        if (other != null)
            res += " | " + other.toString();
        return res;
    }

}
