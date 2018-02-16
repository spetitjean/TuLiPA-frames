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

public class SimpleRRGParseChart implements ParseChart {

    /**
     * map tree -> node -> i -> j -> ws? -> List of Gaps
     */
    private HashMap<String, HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<Boolean, HashSet<Gap>>>>>> chart;

    public SimpleRRGParseChart() {
        chart = new HashMap<String, HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<Boolean, HashSet<Gap>>>>>>();
    }

    public boolean containsItem(ParseItem item) {
        return false;
    }

    /**
     * adds an item to the chart if it is not already in there.
     */
    public void addItem(ParseItem item) {
    }

    /**
     * represents the gaps in wrapping substitution (see deductino rules)
     * 
     * @author david
     *
     */
    private class Gap {
        int start;
        int end;
        String nonterminal;
    }
}
