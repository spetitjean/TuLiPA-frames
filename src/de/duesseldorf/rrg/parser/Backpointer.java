package de.duesseldorf.rrg.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Backpointer {
    private Map<Operation, Set<Set<ParseItem>>> store;

    public Backpointer() {
        store = new HashMap<Operation, Set<Set<ParseItem>>>();
    }

    /**
     * Given that the antecedents created the item that this Backpointer object
     * belongs to, add the information that this item was created using the
     * antecedents and the Operation {@code op}.
     * 
     * @param op
     * @param antecedents
     */
    public void addToBackpointer(Operation op, Set<ParseItem> antecedents) {
        if (store.containsKey(op)) {
            store.get(op).add(antecedents);
        } else {
            Set<Set<ParseItem>> value = new HashSet<Set<ParseItem>>();
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
    public Set<Set<ParseItem>> getBackpointers(Operation op) {
        return store.get(op);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Entry<Operation, Set<Set<ParseItem>>> storeEntry : store
                .entrySet()) {
            for (Set backpointerSet : storeEntry.getValue()) {
                sb.append(storeEntry.getKey() + " : ");
                sb.append(backpointerSet);
                sb.append("\n\t");
            }

        }
        return sb.substring(0, sb.length() - 2);
    }
}
