/**
 * File TypeHierarchy.java
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.tuebingen.tag.Environment;

/**
 * 
 * @author david
 *
 */
public class TypeHierarchy {

    private HashMap<Integer, Set<Type>> tyHi;

    /**
     * given a Collection of Types, create a type hierarchy.
     * 
     * @param l
     */
    public TypeHierarchy(Iterable<Type> l) {
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
    public Type leastSpecificSubtype(Type a, Type b, Environment env)
            throws UnifyException {
        // System.out.println("Find least specific subtype of a and b");
        // System.out.println(a);
        // System.out.println(b);
        Type union = a.union(b, env);
        Value resvar = ValueTools.unify(a.getVar(), b.getVar(), env, this);
        int max = Collections.max(tyHi.keySet());
        if (union.getSpec() <= max) {
            for (int i = union.getSpec(); i <= max; i++) {
                for (Type type : tyHi.get(i)) {
                    if (union.subsumes(type)) {
                        return new Type(type.getElementaryTypes(), resvar,
                                type.getTypeConstraints());
                    }
                }
            }
        }
        throw new UnifyException("Types " + a + " and " + b
                + " are incompatible in the environment " + env);
        // System.err.println("Unification of types failed: ");
        // System.err.println(a.toString() + "\n" + b.toString());
        // return null;
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
