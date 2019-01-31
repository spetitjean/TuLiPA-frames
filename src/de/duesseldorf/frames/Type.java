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
import java.util.Objects;
import java.util.Set;

import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.tag.Environment;
import de.tuebingen.tag.Value;

/**
 * 
 * @author david
 * 
 * 
 *         A class representing a type as a set of elementary type or a
 *         Variable.
 *
 */
public final class Type {

    private Set<String> elemTypes;
    private Value var;
    private Boolean truevar;

    public Type(Set<String> elementaryTypes, Value variable) {
        this.elemTypes = elementaryTypes;
        this.var = variable;
	this.truevar = true;
    }

    public Type(Set<String> elementaryTypes) {
        this.elemTypes = elementaryTypes;
        this.var = new Value(Value.VAR, new NameFactory().getUniqueName());
	this.truevar = false;
    }

    /**
     * Attention: If t is of Kind Variable, then the same variable String is
     * assigned to the new type
     * 
     * @param t
     */
    public Type(Type t) {
        this.elemTypes = t.getElementaryTypes();
        this.var = new Value(t.getVar(), new NameFactory());
	this.truevar = false;
    }

    public Value getVar() {
        return var;
    }

    /**
     * 
     * @param t
     * @return this type unified with t
     */
    public Type union(Type t, Environment env) {
        Type result = null;
        // if (kind == Kind.ELTYPES && t.getKind() == Kind.ELTYPES) {
        Set<String> s = t.getElementaryTypes();
        s.addAll(elemTypes);
        result = new Type(s);
        return result;
    }

    /**
     * 
     * @return Is this a type containing no elementary types?
     */
    public boolean isEmpty() {
        return this.elemTypes.isEmpty()&&!this.truevar;
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
    public boolean equals(Object o) {
        boolean res = false;
        if (o instanceof Type) {
            // old version:
            // Type that = (Type) o;
            // if (this.subsumes(that) && that.subsumes(this)) {
            // res = true;
            // }
            res = o.hashCode() == this.hashCode();
        }
        return res;
    }

    @Override
    public int hashCode() {
        return Objects.hash(elemTypes, var);
    }

    public String toStringWithoutVariable() {
        String s = "";
        s = "[";
        for (String string : elemTypes) {
            s = s + string + "-";
        }

        // remove the last -
        int last = s.length() - 1;
        if (last > 0) {
            s = s.substring(0, last);
        }
        s += "]";
        return s;
    }

    /**
     * @return A string representation of this type in the format
     *         [elemtype1-elemtype2-...] Value
     *         Example:
     *         [sleep-activity-event] Value
     */
    @Override
    public String toString() {
        String s = toStringWithoutVariable();
        s += " " + var.toString();
        return s;
    }
}
