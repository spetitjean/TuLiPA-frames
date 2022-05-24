/*
 *  File PrettyNameFactory.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Thu Jan 31 15:31:02 CET 2008
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
package de.tuebingen.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PrettyNameFactory {

    private int index;
    private Map<String, String> dictionary;
    private String[] availableNames = {"A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z", "A1", "B1", "C1", "D1", "E1", "F1", "G1",
            "H1", "I1", "J1", "K1", "L1", "M1", "N1", "O1", "P1", "Q1", "R1",
            "S1", "T1", "U1", "V1", "W1", "X1", "Y1", "Z1"};

    public PrettyNameFactory() {
        index = 0;
        dictionary = new Hashtable<String, String>();
    }

    public String getName(String in) {
        // System.out.println("Getting name (in util) for "+in);
        String out = "";
        if (dictionary.containsKey(in)) {
            out = dictionary.get(in);
        } else {
            out = getNextName();
            dictionary.put(in, out);
        }
        return out;
    }

    public String getNextName() {
        String res = "";
        if (index > availableNames.length) {
            System.err.println(
                    "Debug info: Too many variables for pretty printing (variable names may be wrong)");
            res = null;
        } else {
            res = availableNames[index];
            index++;
        }
        return res;
    }

    public Map<String, String> getDictionary() {
        return dictionary;
    }

    public void setDictionary(Map<String, String> dictionary) {
        this.dictionary = dictionary;
    }

    public String toString() {
        String res = "";
        Set<String> keys = dictionary.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String k = it.next();
            res += " key: " + k + " - value: " + dictionary.get(k);
        }
        return res;
    }
}
