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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.tag.Environment;

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
    private Set<TypeConstraint> typeConstraints;
    private Boolean truevar;

    public Type(Collection<String> elementaryTypes) {
        this.typeConstraints = new HashSet<TypeConstraint>();
        this.elemTypes = new HashSet<String>(elementaryTypes);
        this.var = new Value(Value.Kind.VAR, new NameFactory().getUniqueName());
        this.truevar = false;
    }

    public Type(Collection<String> elementaryTypes, Value variable) {
        this.elemTypes = new HashSet<String>(elementaryTypes);
        this.var = variable;
        this.typeConstraints = new HashSet<TypeConstraint>();
        this.truevar = true;
    }

    public Type(Collection<String> elementaryTypes, Value variable,
            Collection<TypeConstraint> typeConstraints) {
        this.elemTypes = new HashSet<String>(elementaryTypes);
        this.var = variable;
        this.typeConstraints = new HashSet<TypeConstraint>(typeConstraints);
        this.truevar = true;
    }

    public Type(Collection<String> elementaryTypes,
            Collection<TypeConstraint> typeConstraints) {
        this.typeConstraints = new HashSet<TypeConstraint>(typeConstraints);
        this.elemTypes = new HashSet<String>(elementaryTypes);
        this.var = new Value(Value.Kind.VAR, new NameFactory().getUniqueName());
        this.truevar = false;
    }

    /**
     * Attention: If t is of Kind Variable, then the same variable String is
     * assigned to the new type. Or is it?
     * 
     * @param t
     */
    public Type(Type t) {
	if(t==null)
	    return;
	if(t.getElementaryTypes()!=null) 
	    this.elemTypes = t.getElementaryTypes();
	else
	    this.elemTypes = new HashSet<String>();
        this.var = new Value(t.getVar(), new NameFactory());
        this.typeConstraints = t.getTypeConstraints();
        this.truevar = false;
    }

    public Value getVar() {
        return var;
    }

    public void setVar(Value v) {
        this.var = v;
    }

    /**
     * 
     * @param t
     * @return this type unified with t
     */
    public Type union(Type t, Environment env) {
        Type result = null;
        // if (kind == Kind.ELTYPES && t.getKind() == Kind.ELTYPES) {
        Set<String> resultingElementaryTypes = t.getElementaryTypes();
        resultingElementaryTypes.addAll(elemTypes);

        Set<TypeConstraint> resultingTypeConstraints = t.getTypeConstraints();
        resultingTypeConstraints.addAll(typeConstraints);
        result = new Type(resultingElementaryTypes, resultingTypeConstraints);
        return result;
    }

    /**
     * 
     * @return Is this a type containing no elementary types?
     */
    public boolean isEmpty() {
        return this.elemTypes.isEmpty() && !this.truevar;
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

    public Set<TypeConstraint> getTypeConstraints() {
        return this.typeConstraints;
    }

    public Set<String> getElementaryTypes() {
	if(elemTypes != null){
	    Set<String> e = new HashSet<String>(elemTypes);
	    return e;
	}
	else
	    return new HashSet<String>();	    
        
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
        return Objects.hash(elemTypes, var, typeConstraints);
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
        // if (!typeConstraints.isEmpty()) {
        //     s += "\nConstraints:";
        //     for (TypeConstraint constraint : typeConstraints) {
        //         s += "\n" + constraint;
        //     }
        // }
        return s;
    }
}
