package de.duesseldorf.frames;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author david
 * 
 * 
 *         A class representing a type as a set of elementary type.
 *
 */
public final class Type {

    private Set<String> elemTypes;

    public Type(Set<String> elementaryTypes) {
        this.elemTypes = elementaryTypes;
    }

    public Type(Type t) {
        this.elemTypes = t.getElementaryTypes();
    }

    /**
     * 
     * @param t
     * @return this type unified with t
     */
    public Type union(Type t) {
        Set<String> s = t.getElementaryTypes();
        s.addAll(elemTypes);
        return new Type(s);
    }

    /**
     * 
     * @return Is this a type containing no elementary types?
     */
    public boolean isEmpty() {
        return this.elemTypes.isEmpty();
    }

    /**
     * 
     * @param t
     * @return true if this type subsumes the type t
     */
    public boolean subsumes(Type t) {
        Set<String> ttypes = t.getElementaryTypes();
        return ttypes.containsAll(elemTypes);
    }

    public Set<String> getElementaryTypes() {
        Set<String> e = new HashSet<String>(elemTypes);
        return e;
    }

    public int getSpec() {
        return elemTypes.size();
    }

    @Override
    public boolean equals(Object obj) {
        boolean res = false;
        if (obj instanceof Type) {
            Type that = (Type) obj;
            if (this.subsumes(that) && that.subsumes(this)) {
                res = true;
            }
        }
        return res;
    }

    @Override
    public int hashCode() {
        return (41 * (41 + this.elemTypes.hashCode()));
    }

    @Override
    public String toString() {
        String s = "[";
        for (String string : elemTypes) {
            s = s + string + "-";
        }

        // remove the last -
        int last = s.length() - 1;
        if (last > 0) {
            s = s.substring(0, last);
        }
        return s + "]";
    }
}
