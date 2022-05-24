/*
 *  File ItemKey.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@loria.fr>
 *     Laura Kallmeyer  <kallmeyer@sfs.uni-tuebingen.de>
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2009
 *     Laura Kallmeyer, 2009
 *     Wolfgang Maier, 2009
 *
 *  Last modified:
 *     Wed Jan 21 09:52:13 CET 2009
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
package de.tuebingen.parserconstraints;

import java.util.*;

public class ItemKey {

    // to distinguish between active and passive items
    public static final int ACTIVE = 1;
    public static final int PASSIVE = 2;

    private int typeMap;
    private Map<Object, List<ConstraintItem>> itemMap;

    public ItemKey(int type) {
        typeMap = type;
        itemMap = new HashMap<Object, List<ConstraintItem>>();
    }

    public int add2map(ConstraintItem ci) {
        int res = 0;
        switch (typeMap) {
            case (ACTIVE):
                int dot = ci.getDotpos();
                res = this.update(new Integer(dot), ci);
                break;
            case (PASSIVE):
                int fbound = ci.getVect().getFirstBoundValue();
                // fbound = -1 means unknown left bound
                res = this.update(new Integer(fbound), ci);
                break;
        }
        return res;
    }

    public int update(Object o, ConstraintItem ci) {
        int res = 0;
        List<ConstraintItem> lci = itemMap.get(o);
        if (lci == null) {
            lci = new LinkedList<ConstraintItem>();
            itemMap.put(o, lci);
        }
        if (lci.contains(ci))
            res = 1; // res != 0 means update not possible
        else
            lci.add(ci);
        return res;
    }
}
