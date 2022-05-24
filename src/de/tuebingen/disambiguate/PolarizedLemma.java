/*
 *  File PolarizedLemma.java
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

import java.util.*;

public class PolarizedLemma {

    private String lemmaID;
    private Map<String, PolarizedTuple> tuples;
    private List<String> lexicals;

    public PolarizedLemma(String l) {
        lemmaID = l;
        tuples = new HashMap<String, PolarizedTuple>();
        lexicals = new LinkedList<String>();
    }

    public void addTuple(PolarizedTuple t) {
        // No suffix needed since lemma + tupleID (i.e. family) is a unique key
        if (!(tuples.containsKey(t.getTupleID()))) {
            tuples.put(t.getTupleID(), t);
            lexicals.addAll(t.getLexicals());
        } else
            System.err.println("*** Polarized tuple already encountered. ***");
    }

    public Iterator<String> iterator() {
        return tuples.keySet().iterator();
    }

    public Map<String, Map<String, Integer>> getCharges() {
        Map<String, Map<String, Integer>> res = new HashMap<String, Map<String, Integer>>();
        Iterator<String> it = tuples.keySet().iterator();
        while (it.hasNext()) {
            String nexttuple = it.next();
            Map<String, Integer> polarities = tuples.get(nexttuple).getPol().getCharges();
            res.put(nexttuple, polarities);
        }
        return res;
    }

    public String getLemmaID() {
        return lemmaID;
    }

    public void setLemmaID(String lemmaID) {
        this.lemmaID = lemmaID;
    }

    public Map<String, PolarizedTuple> getTuples() {
        return tuples;
    }

    public void setTuples(Map<String, PolarizedTuple> tuples) {
        this.tuples = tuples;
    }

    public List<String> getLexicals() {
        return lexicals;
    }

    public String toString() {
        String res = "";
        res += "Lemma " + lemmaID + "\n";
        res += "Tuples : \n";
        Set<String> keys = tuples.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String next = it.next();
            res += tuples.get(next).toString();
        }
        return res;
    }

}
