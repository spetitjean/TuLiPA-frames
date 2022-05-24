/*
 *  File Polarities.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Wed Feb 27 15:53:52 CET 2008
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
package de.tuebingen.disambiguate;

import de.tuebingen.util.Pair;

import java.util.*;

public class Polarities {

    public static final int MINUS = -1;
    public static final int NEUTRAL = 0;
    public static final int PLUS = 1;

    private Map<String, List<Pair>> polarities;
    private Map<String, Integer> charges;

    public Polarities() {
        polarities = new HashMap<String, List<Pair>>();
        charges = new HashMap<String, Integer>();
    }

    public Polarities(String label, String v, int polarity) {
        this();
        setPol(label, v, polarity);
    }

    public void setPol(String label, String v, int polarity) {
        Pair p = new Pair(v, new Integer(polarity));
        List<Pair> lp = null;
        int globalPol = 0;
        if (polarities.containsKey(label)) {
            lp = polarities.get(label);
            globalPol = charges.get(label);
        } else {
            lp = new LinkedList<Pair>();
        }
        lp.add(p);
        polarities.put(label, lp);
        charges.put(label, globalPol + polarity);
    }

    public List<Pair> retrievePol(String label) {
        return polarities.get(label);
    }

    public int retrieveCharge(String label) {
        int res = 0;
        if (polarities.containsKey(label)) {
            for (Pair p : polarities.get(label)) {
                res += (Integer) p.getValue();
            }
        }
        return res;
    }

    public Map<String, Integer> getCharges() {
        return charges;
    }

    public Iterator<String> iterator() {
        return polarities.keySet().iterator();
    }

    public Map<String, List<Pair>> getPolarities() {
        return polarities;
    }

    public static Polarities add(Polarities p1, Polarities p2) {
        Polarities addedPolarities = new Polarities();
        for (String s : p1.charges.keySet()) {
            addedPolarities.charges.put(s, p1.charges.get(s));
            addedPolarities.polarities.put(s, p1.getPolarities().get(s));
        }
        for (String s : p2.charges.keySet()) {
            if (!(addedPolarities.charges.containsKey(s))) {
                addedPolarities.charges.put(s, p2.charges.get(s));
                addedPolarities.polarities.put(s, p2.getPolarities().get(s));
            } else {
                addedPolarities.charges.put(s, addedPolarities.charges.get(s) + p2.charges.get(s));
                addedPolarities.getPolarities().get(s).addAll(p2.getPolarities().get(s));
            }
        }
        return addedPolarities;
    }

    public String toString() {
        String res = "";
        Set<String> keys = polarities.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String label = it.next();
            // printing polarities:
            res += "< " + label + ": ";
            List<Pair> pol = polarities.get(label);
            for (int i = 0; i < pol.size(); i++) {
                if (i > 0)
                    res += ", ";
                String v = (String) pol.get(i).getKey();
                Integer w = (Integer) pol.get(i).getValue();
                switch (w.intValue()) {
                    case PLUS:
                        res += "+";
                        break;
                    case NEUTRAL:
                        res += "";
                        break;
                    case MINUS:
                        res += "-";
                        break;
                    default: //skip
                }
                res += v;
            }
            res += ">\n";
            // idem for charges:
            res += "< " + label + ": ";
            Integer c = charges.get(label);
            res += c;
            res += ">\n";
        }
        return res;
    }

}
