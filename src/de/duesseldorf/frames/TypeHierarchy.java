package de.duesseldorf.frames;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeHierarchy {

    private HashMap<Integer, Set<Type>> tyHi;

    public TypeHierarchy(List<Type> l) {
        this.tyHi = new HashMap<Integer, Set<Type>>();

        for (Type type : l) {
            int spec = type.getSpec();
            if (!tyHi.containsKey(spec)) {
                tyHi.put(spec, new HashSet<Type>());
            }
            tyHi.get(type.getSpec()).add(new Type(type));
        }
    }

    /**
     * 
     * @return the type which
     *         - is in the type hierarchy
     *         - consists of the fewest elementary types
     *         - is subsumed by the union of types a and b
     *         - if there is no such type in the type hierarchy, return null
     */
    public Type leastSpecificSubtype(Type a, Type b) {
        Type union = a.union(b);
        int max = Collections.max(tyHi.keySet());

        if (union.getSpec() <= max) {
            for (int i = union.getSpec(); i <= max; i++) {
                for (Type type : tyHi.get(i)) {
                    if (union.subsumes(type)) {
                        return new Type(type);
                    }
                }
            }
        }
        System.err.println("Unification of types failed: ");
        System.err.println(a.toString() + "\n" + b.toString());
        return null;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i <= Collections.max(tyHi.keySet()); i++) {
            System.out.println("spec: " + i);
            for (Type type : tyHi.get(i)) {
                System.out.println(type.toString());
            }
            System.out.println();
        }
        return sb.toString();
    }
}
