/*
 *  File SimpleRRGParseChart.java
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
package de.duesseldorf.rrg.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SimpleRRGParseChart implements ParseChart {

    /**
     * probably to much:
     * map tree -> node -> i -> j -> ws? -> List of Gaps
     */
    // private HashMap<RRGTree, HashMap<RRGNode, HashMap<Integer,
    // HashMap<Integer, HashMap<Boolean, HashSet<Gap>>>>>> chart;

    // map start index to Parse Items to their backpointers
    private Map<Integer, Map<ParseItem, Set<Set<ParseItem>>>> chart;

    public SimpleRRGParseChart(int size) {
        // chart = new HashMap<RRGTree, HashMap<RRGNode, HashMap<Integer,
        // HashMap<Integer, HashMap<Boolean, HashSet<Gap>>>>>>();
        chart = new HashMap<Integer, Map<ParseItem, Set<Set<ParseItem>>>>(size);
        for (int i = 0; i < size; i++) {
            chart.put(i, new HashMap<ParseItem, Set<Set<ParseItem>>>());
        }
    }

    public boolean containsItem(ParseItem item) {
        int startpos = item.startPos();
        System.out.println(
                "Simple...chart.containsItem() is not tested yet and might be wrong in some cases");
        return chart.get(startpos).containsKey(item);
    }

    /**
     * adds an item to the chart if it is not already in there.
     * 
     * @param consequent
     *            the item that should be added to the chart.
     * @param antecedents
     *            the antecedents from which this item was created.
     * @return true if the ParseItem was not already in the chart
     */
    public boolean addItem(ParseItem consequent, ParseItem... antecedents) {
        Set<ParseItem> antes;
        if (antecedents.length > 0) {
            antes = new HashSet<ParseItem>(Arrays.asList(antecedents));
        } else {
            antes = new HashSet<ParseItem>();

        }
        int startpos = consequent.startPos();

        // was the item in the chart before?
        boolean alreadythere = (chart.get(startpos)).containsKey(consequent);
        if (alreadythere) {
            // just put the additional backpointers
            chart.get(startpos).get(consequent).add(antes);
        } else {
            // add the consequent and a fresh set of backpointers
            Set<Set<ParseItem>> backpointers = new HashSet<Set<ParseItem>>();
            backpointers.add(antes);
            chart.get(startpos).put(consequent, backpointers);
        }
        return !alreadythere;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("Printing chart\n");
        for (Integer i = 0; i < chart.size(); i++) {
            if (i < chart.size()) {
                sb.append("\nstart index " + i + "\n");
            }
            // print the items
            for (Entry<ParseItem, Set<Set<ParseItem>>> chartEntry : chart.get(i)
                    .entrySet()) {
                sb.append(chartEntry.getKey().toString());

                // and their backpointers
                sb.append(" : {");
                for (Set<ParseItem> bpset : chartEntry.getValue()) {
                    if (bpset.size() > 0) {
                        sb.append("{");
                        for (ParseItem parseItem : bpset) {
                            sb.append(parseItem);
                        }
                        sb.append("}, ");
                    }
                }
                sb.append("}\n");
            }
        }
        return sb.toString();
    }
}