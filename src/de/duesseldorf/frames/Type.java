/**
 * File Type.java
 * 
 * Authors:
 * David Arps <david.arps@hhu.de>
 * Simon Petitjean <petitjean@phil.hhu.de>
 * 
 * Copyright
 * David Arps, 2017
 * Simon Petitjean, 2017
 * 
 * This file is part of the TuLiPA-frames system
 * https://github.com/spetitjean/TuLiPA-frames
 * 
 * 
 * TuLiPA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * TuLiPA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

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
     * Given two types a and b, a subsumes b iff b contains all the elementary
     * types in b
     * 
     * @param t
     * @return true if this type subsumes the type t.
     * 
     */
    public boolean subsumes(Type t) {
        Set<String> ttypes = t.getElementaryTypes();
        return ttypes.containsAll(elemTypes);
    }

    public Set<String> getElementaryTypes() {
        Set<String> e = new HashSet<String>(elemTypes);
        return e;
    }

    /**
     * 
     * @return The number of elementary types that this type consists of
     */
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

    /**
     * @return A string representation of this type in the format
     *         [elemtype1-elemtype2-...]
     *         Example:
     *         [sleep-activity-event]
     */
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
