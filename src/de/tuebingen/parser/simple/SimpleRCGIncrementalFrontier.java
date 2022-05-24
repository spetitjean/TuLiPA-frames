/*
 *  File SimpleRCGIncrementalFrontier.java
 *
 *  Authors:
 *     Johannes Dellert
 *
 *  Copyright:
 *     Johannes Dellert, 2009
 *
 *  Last modified:
 *     Do 16. Apr 09:55:36 CEST 2009
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
package de.tuebingen.parser.simple;

import java.util.*;

public class SimpleRCGIncrementalFrontier {
    HashMap<Integer, List<IncrementalEarleyItem>> frontier;
    int size;
    int currentPos;

    public SimpleRCGIncrementalFrontier() {
        frontier = new HashMap<Integer, List<IncrementalEarleyItem>>();
        size = 0;
        currentPos = 0;
    }

    public void add(IncrementalEarleyItem it) {
        if (frontier.get(it.pos) == null) {
            frontier.put(it.pos, new LinkedList<IncrementalEarleyItem>());
        }
        frontier.get(it.pos).add(it);
        size++;
    }

    public int size() {
        return size;
    }

    public IncrementalEarleyItem next() {
        if (frontier.get(currentPos).size() > 0) {
            size--;
            return frontier.get(currentPos).remove(0);
        } else {
            currentPos++;
            return next();
        }
    }

}
