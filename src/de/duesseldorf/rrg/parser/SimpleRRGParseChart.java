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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimpleRRGParseChart implements ParseChart {

    /**
     * probably to much:
     * map tree -> node -> i -> j -> ws? -> List of Gaps
     */
    // private HashMap<RRGTree, HashMap<RRGNode, HashMap<Integer,
    // HashMap<Integer, HashMap<Boolean, HashSet<Gap>>>>>> chart;

    private Map<Integer, Set<ParseItem>> chart;

    public SimpleRRGParseChart(int size) {
        // chart = new HashMap<RRGTree, HashMap<RRGNode, HashMap<Integer,
        // HashMap<Integer, HashMap<Boolean, HashSet<Gap>>>>>>();
        chart = new HashMap<Integer, Set<ParseItem>>(size);
        for (int i = 0; i < size; i++) {
            chart.put(i, new HashSet<ParseItem>());
        }
    }

    public boolean containsItem(ParseItem item) {
        return false;
    }

    /**
     * adds an item to the chart if it is not already in there.
     * 
     * @return true if the ParseItem was not already in the chart
     */
    public boolean addItem(ParseItem item, ParseItem... antecedents) {
        int startpos = item.startPos();
        return chart.get(startpos).add(item);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("Printing chart\n");
        for (Integer i = 0; i < chart.size(); i++) {
            if (i < chart.size()) {
                sb.append("\nstart index " + i + "\n");
            }
            for (ParseItem item : chart.get(i)) {
                sb.append(item);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
