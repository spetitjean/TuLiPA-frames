package de.duesseldorf.rrg.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Backpointer {
    private Map<Operation, Set<List<ParseItem>>> store;

    public Backpointer() {
        store = new HashMap<Operation, Set<List<ParseItem>>>();
    }

    /**
     * Given that the antecedents created the item that this Backpointer object
     * belongs to, add the information that this item was created using the
     * antecedents and the Operation {@code op}.
     * 
     * @param op
     * @param antecedents
     */
    public void addToBackpointer(Operation op, List<ParseItem> antecedents) {
        if (store.containsKey(op)) {
            store.get(op).add(antecedents);
        } else {
            Set<List<ParseItem>> value = new HashSet<List<ParseItem>>();
            value.add(antecedents);
            store.put(op, value);
        }
    }

    /**
     * 
     * @param op
     * @return the set with all sets of antecedents that created the item using
     *         the {@code Operation} {@code op}.
     */
    public Set<List<ParseItem>> getAntecedents(Operation op) {
        Set<List<ParseItem>> res = store.get(op);

        return (res != null) ? res : new HashSet<List<ParseItem>>();
    }

    public Set<List<ParseItem>> getAntecedents(Collection<Operation> ops) {
        Set<List<ParseItem>> result = new HashSet<List<ParseItem>>();
        for (Operation op : ops) {
            result.addAll(getAntecedents(op));
        }
        return result;
    }

    public int size() {
        int size = 0;
        for (Set<List<ParseItem>> e : store.values()) {
            size += e.size();
        }
        return size;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Entry<Operation, Set<List<ParseItem>>> storeEntry : store
                .entrySet()) {
            for (List<ParseItem> backpointerSet : storeEntry.getValue()) {
                sb.append(storeEntry.getKey() + " : ");
                sb.append(backpointerSet);
                sb.append("\n\t");
            }

        }
        return sb.substring(0, sb.length() - 2);
    }
}
