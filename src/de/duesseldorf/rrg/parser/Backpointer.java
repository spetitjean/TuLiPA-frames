package de.duesseldorf.rrg.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/*
 *  File Backpointer.java
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
public class Backpointer {
    private Map<Operation, Set<Set<RRGParseItem>>> store;

    public Backpointer() {
        store = new HashMap<Operation, Set<Set<RRGParseItem>>>();
    }

    /**
     * Given that the antecedents created the item that this Backpointer object
     * belongs to, add the information that this item was created using the
     * antecedents and the Operation {@code op}.
     *
     * @param op
     * @param antecedents
     */
    public void addToBackpointer(Operation op, Set<RRGParseItem> antecedents) {
        if (store.containsKey(op)) {
            store.get(op).add(antecedents);
        } else {
            Set<Set<RRGParseItem>> value = new HashSet<Set<RRGParseItem>>();
            value.add(antecedents);
            store.put(op, value);
        }
    }

    /**
     * @param op
     * @return the set with all sets of antecedents that created the item using
     * the {@code Operation} {@code op}.
     */
    public Set<Set<RRGParseItem>> getAntecedents(Operation op) {
        Set<Set<RRGParseItem>> res = store.get(op);

        return (res != null) ? res : new HashSet<Set<RRGParseItem>>();
    }

    public Set<Set<RRGParseItem>> getAntecedents(Collection<Operation> ops) {
        Set<Set<RRGParseItem>> result = new HashSet<Set<RRGParseItem>>();
        for (Operation op : ops) {
            result.addAll(getAntecedents(op));
        }
        return result;
    }

    public int size() {
        int size = 0;
        for (Set<Set<RRGParseItem>> e : store.values()) {
            size += e.size();
        }
        return size;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Entry<Operation, Set<Set<RRGParseItem>>> storeEntry : store
                .entrySet()) {
            for (Set<RRGParseItem> backpointerSet : storeEntry.getValue()) {
                sb.append(storeEntry.getKey() + " : ");
                sb.append(backpointerSet);
                sb.append("\n\t");
            }

        }
        return sb.substring(0, sb.length() - 2);
    }
}
