/*
 *  File RangeConstraintVector.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.tuebingen.util.CollectionUtilities;

public class RangeConstraintVector {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    private List<Object> map;
    List<RangeBoundary> bound;
    Map<Integer, List<Integer>> rmap;

    public RangeConstraintVector(int size) {
        rmap = new HashMap<Integer, List<Integer>>();
        map = new ArrayList<Object>(size);
        bound = new ArrayList<RangeBoundary>(size * 2);
        for (int i = 0; i < size * 2; ++i) {
            if (i < size) {
                map.add(i);
            }
            bound.add(new RangeBoundary(i));
            rmap.put(i, new ArrayList<Integer>());
            rmap.get(i).add(i);
        }
    }

    @SuppressWarnings("unchecked")
    public RangeConstraintVector(RangeConstraintVector r) {
        // deep copy
        this.map = new ArrayList<Object>(r.map);
        this.bound = new ArrayList<RangeBoundary>(r.bound.size());
        for (int i = 0; i < r.bound.size(); ++i) {
            this.bound.add(i, new RangeBoundary(r.bound.get(i)));
        }
        try {
            this.rmap = (Map<Integer, List<Integer>>) CollectionUtilities.deepCopy(r.rmap);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public int size() {
        return map.size();
    }

    public void setKey(int index, Object key) {
        map.set(index, key);
    }

    public Object getKey(int i) {
        return map.get(i);
    }

    public List<Object> getKeys() {
        return map;
    }

    public void setKeys(List<Object> keys) {
        if (keys.size() == this.map.size()) {
            map = keys;
        }
    }

    public RangeBoundary left(Object o) {
        RangeBoundary ret = null;
        int i = map.indexOf(o);
        if (i > -1) {
            ret = bound.get(i * 2);
        }
        return ret;
    }

    public RangeBoundary right(Object o) {
        RangeBoundary ret = null;
        int i = map.indexOf(o);
        if (i > -1) {
            ret = bound.get(i * 2 + 1);
        }
        return ret;
    }


    public void updateNoCheck(List<RangeBoundary> l) {
        if (l.size() == bound.size()) {
            bound = new ArrayList<RangeBoundary>(l);
        }
        for (int i = 0; i < bound.size(); ++i) {
            if (!rmap.containsKey(bound.get(i).getId())) {
                rmap.put(bound.get(i).getId(), new ArrayList<Integer>());
            }
            rmap.get(bound.get(i).getId()).add(i);
        }
    }

    public boolean update(RangeBoundary b, RangeBoundary v) {
        if (b.getVal() > -1 && v.getVal() > -1 && b.getVal() != v.getVal()) {
            return false;
        }
        boolean ret = false;
        for (int i = 0; i < bound.size(); ++i) {
            RangeBoundary bd = bound.get(i);
            if (bd.getId() == b.getId()) {
                bound.set(i, new RangeBoundary(v));
                ret = true;
            }
        }/*
		List<Integer> blist = rmap.get(b.getId());
		for (int i = 0; i < blist.size(); ++i) {
			RangeBoundary rb = new RangeBoundary(v);
			bound.set(blist.get(i), rb);
			ret = true;
		}
		rmap.get(b.getId()).addAll(rmap.get(v.getId()));*/
        return ret;
    }

    public boolean update(RangeBoundary b, int v) {
        boolean ret = true;
        //System.err.println("updating " + this + ", b:" + b + ", v:" + v);
        for (int i = 0; i < bound.size(); ++i) {
            RangeBoundary bd = bound.get(i);
            if (bd.getId() == b.getId()) {
                boolean boundok = false;
                boolean isleft = i % 2 == 0;
                if (isleft) {
                    int rbound = bound.get(i + 1).getVal();
                    boundok = rbound == -1 || v <= rbound;
                } else {
                    int lbound = bound.get(i - 1).getVal();
                    boundok = v >= lbound;
                }
                if ((bd.getVal() == -1 || bd.getVal() == v) && boundok) {
                    bound.get(i).setVal(v);
                    ret &= true;
                } else if (bd.getVal() != v) {
                    ret &= false;
                }
            }
        }
        //System.err.println("updated " + this + ", res:" + ret);
        return ret;
    }

    public void resetNumbering() {
        // map
        Map<Integer, Integer> imap = new HashMap<Integer, Integer>();
        // rename
        int highest = 0;
        //System.err.println("bound:" + bound + "\nmap:" + imap);
        for (int i = 0; i < bound.size(); ++i) {
            //System.err.print(bound.get(i).getId() + " ");
            if (!imap.containsKey(bound.get(i).getId())) {
                imap.put(bound.get(i).getId(), highest++);
            }
            bound.get(i).setId(imap.get(bound.get(i).getId()));
        }
    }

    public String toString() {
        String ret = "(";
        Iterator<Object> keyit = map.iterator();
        while (keyit.hasNext()) {
            Object o = keyit.next();
            ret += o + ":<" + left(o) + "," + right(o) + ">";
            if (keyit.hasNext()) {
                ret += ", ";
            }
        }
        ret += ")";
        return ret;
    }

}
