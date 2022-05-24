/*
 *  File PolarityAutomatonState.java
 *
 *  Authors:
 *     Johannes Dellert  <jdellert@sfs.uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@loria.fr>
 *
 *  Copyright:
 *     Johannes Dellert, 2008
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fri May 16 16:03:14 CEST 2008
 *
 *  This file is part of the Polarity Filter
 *
 *  Polarity Filter is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The Polarity Filter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package fr.loria.disambiguation;

import java.util.*;

public class PolarityAutomatonState {
    int stateId;
    Polarities polarities;
    //automata are deterministic: edge labels are assigned the IDs of the states they lead to
    HashMap<Integer, List<String>> edges;  // NB: outgoing edges!

    public PolarityAutomatonState(Polarities polarities, int i) {
        this.stateId = i;
        this.polarities = polarities;
        this.edges = new HashMap<Integer, List<String>>();
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public String toString() {
        String res = "";
        res += "State n°" + stateId + "\n";
        res += "Polarities: " + polarities.toString();
        Iterator<Integer> it = edges.keySet().iterator();
        while (it.hasNext()) {
            Integer next = it.next();
            res += "To " + next + " " + edges.get(next).toString() + "\n";
        }
        return res;
    }

}
