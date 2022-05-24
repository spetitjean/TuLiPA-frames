/*
 *  File Item.java
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

import de.tuebingen.rcg.Clause;
import de.tuebingen.rcg.PredLabel;
import de.tuebingen.rcg.Predicate;

public class Item {

    private boolean active;
    private RangeConstraintVector rcv;


    // active
    private Clause cl;
    private int dotpos;

    // passive
    private boolean completed;
    private PredLabel pl;
    private boolean isterminal;

    public Item() {
        active = false;
        completed = false;
        setIsterminal(false);
        cl = null;
        setPredLabel(null);
        setRcv(null);
        dotpos = 0;
    }

    public void setRcv(RangeConstraintVector rcv) {
        this.rcv = rcv;
    }

    public RangeConstraintVector getRcv() {
        return rcv;
    }

    public void setPredLabel(PredLabel pl) {
        this.pl = pl;
    }

    public PredLabel getPredLabel() {
        return pl;
    }

    public Predicate getPredicateAtDot() {
        Predicate ret = null;
        if (!isDotAtEnd()) {
            ret = cl.getRhs().get(dotpos);
        }
        return ret;
    }

    public boolean isDotAtEnd() {
        return cl != null && cl.getRhs().size() > 0 && dotpos >= cl.getRhs().size();
    }

    public int getDotpos() {
        return dotpos;
    }

    public void setDotpos(int dotpos) {
        this.dotpos = dotpos;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setClause(Clause cl) {
        this.cl = cl;
    }

    public Clause getClause() {
        return cl;
    }

    public void setIsterminal(boolean isterminal) {
        this.isterminal = isterminal;
    }

    public boolean isIsterminal() {
        return isterminal;
    }

    public String toString() {
        String ret = "[";
        if (active) {
            ret += getClause() + ", ";
            ret += dotpos + ", ";
        } else {
            ret += getPredLabel() + ", ";
            ret += "com:" + completed + ", ";
        }
        ret += getRcv();
        ret += "]";
        return ret;
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public boolean equals(Object o) {
        return (o instanceof Item && o.toString().equals(this.toString()));
    }


}
