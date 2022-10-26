
package de.duesseldorf.rrg.parser;

import java.util.Objects;
import java.util.Set;

import de.duesseldorf.factorizer.EqClassBot;

/*
 *  File RRGParseItem.java
 *
 *  Authors:
 *     David Arps <david.arps@hhu.de
 *
 *  Copyright:
 *     David Arps, 2018
 *
 *  This file is part of the TuLiPA system
 *     https://github.com/spetitjean/TuLiPA-frames
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
public class RRGParseItem implements Comparable<RRGParseItem> {


    private final EqClassBot eqClass;
    private final int start;
    private final int end;
    private final Set<Gap> gaps;
    private final boolean ws;
    private final RRGParseItem genwrappingjumpback;

    private final NodePos nodePos;

    private RRGParseItem(EqClassBot eqClass, int start,
                         int end, Set<Gap> gaps, boolean ws, RRGParseItem genwrappingjumpback) {
        this.eqClass = eqClass;
        this.start = start;
        this.end = end;
        this.gaps = gaps;
        this.ws = ws;
        this.genwrappingjumpback = genwrappingjumpback;
        if(eqClass != null) {        this.nodePos =  eqClass.isTopClass() ? NodePos.TOP : NodePos.BOT;
        } else {nodePos = null;}
    }



    public NodePos getNodePos() {
        return this.nodePos;
    }

    public EqClassBot getEqClass() {
        return this.eqClass;
    }

    public int startPos() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public boolean getwsflag() {
        return ws;
    }

    public Set<Gap> getGaps() {
        return gaps;
    }

    public RRGParseItem getGenwrappingjumpback() {
        return genwrappingjumpback;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, eqClass, gaps, ws, genwrappingjumpback);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RRGParseItem) {
            return o.hashCode() == this.hashCode();
        }
        return false;
    }

    /**
     * @return
     */
    public String toString() {
        String gapstr = "[";
        for (Gap gap : gaps) {
            gapstr += gap.toString();
        }
        gapstr += "]";
        String itemstr = "[" + this.eqClass.getId() + ", "
                + this.eqClass.toString() + ", "
                + this.start + ", " + this.end + ", " + gapstr + ", " + ws;
        if (genwrappingjumpback != null) {
            itemstr += ", " + genwrappingjumpback.toString();
        }
        itemstr += "]";
        return itemstr;
    }

    public int compareTo(RRGParseItem o) {
        if (!this.getEqClass().getId().equals(o.getEqClass().getId())) {
            return getEqClass().getId().compareTo(o.getEqClass().getId());
        }
        if (this.start != o.start) {
            return this.start - o.start;
        }
        if (this.end != o.end) {
            return this.end - o.end;
        }
        return this.hashCode() - o.hashCode();
    }

    /**
     * Do we look at everything below this node ({@code BOT}), or do we also
     * look at the left sister ({@code TOP})?
     *
     * @author david
     */
    public enum NodePos {
        TOP, BOT;
    }

    public static class Builder {
        private EqClassBot eqClass = null;
        private int start = -2;
        private int end = -2;
        private Set<Gap> gaps = null;
        private Boolean ws;
        private RRGParseItem genwrappingjumpback = null;


        public Builder() {
        }

        public Builder eqClass(EqClassBot eqClass){
            this.eqClass = eqClass;
            return this;
        }

        public Builder start(int start) {
            this.start = start;
            return this;
        }

        public Builder end(int end) {
            this.end = end;
            return this;
        }

        public Builder gaps(Set<Gap> gaps) {
            this.gaps = gaps;
            return this;
        }

        public Builder ws(boolean ws) {
            this.ws = ws;
            return this;
        }

        public Builder genwrappingjumpback(RRGParseItem genwrappingjumpback) {
            this.genwrappingjumpback = genwrappingjumpback;
            return this;
        }

        public RRGParseItem build() {
            /*
             * System.out.println();
             * System.out.println();
             * // System.out.println(tree.toString());
             * System.out.println();
             * System.out.println();
             */
            return new RRGParseItem(eqClass, start, end, gaps, ws, genwrappingjumpback);
        }
    }
}
