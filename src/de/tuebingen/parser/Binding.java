/*
 *  File Binding.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:58:05 CEST 2007
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
package de.tuebingen.parser;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tuebingen.rcg.ArgContent;
import de.tuebingen.rcg.RCGInstantiationException;

/**
 * Environment for range variable bindings
 * 
 * @author wmaier, parmenti
 *
 */
public class Binding implements Iterable<ArgContent> {

	private Hashtable<ArgContent,ArgContent> table;

	public Binding() {
		table = new Hashtable<ArgContent,ArgContent>();
	}
	
	public Binding(Binding bndg) {
		table = new Hashtable<ArgContent,ArgContent>(bndg.table);
	}

	public ArgContent deref(ArgContent r) {
		return table.get(r);
	}
	
	public void bind(boolean verbose, ArgContent var, ArgContent r) throws RCGInstantiationException {
		// this put in the table a variable (key) and a content (value)
		// the content can be a var, a const, a list, or epsilon
		if (verbose) {System.err.println("  -> binding "+var.toString()+" with "+r.toString());}

		switch(var.getType()){
		case ArgContent.VAR:
			ArgContent x = table.get(var);
			if (x == null) {
				table.put(var, r);
			} else {
				if (!(x.equals(r))) {
					throw new RCGInstantiationException("Cannot bind variable "+var.toString()+" with "+x.toString()+" and "+r.toString());
				}
			}
			break;
		case ArgContent.TERM:
			if (r.getType() == ArgContent.LIST) {
				throw new RCGInstantiationException("Cannot bind constant "+var.toString()+" with list "+r.toString());
			}
			else if (r.getType() != ArgContent.TERM || !(r.getName().equals(var.getName()))) {
				throw new RCGInstantiationException("Cannot bind constant "+var.toString()+" with "+r.toString());
			}
			break;
		case ArgContent.EPSILON:
			if (r.getType() != ArgContent.EPSILON) {
				throw new RCGInstantiationException("Cannot bind Eps with "+r.toString());
			}
			break;
		case ArgContent.LIST:
			if (r.getType() != ArgContent.LIST) {
				throw new RCGInstantiationException("Cannot bind "+var.toString()+" with "+r.toString());
			}
			break;
		default://skip
		}
	}
	
	public static Binding merge(Binding b1, Binding b2) throws RCGInstantiationException {
		Binding res = new Binding(b2);
		Set<ArgContent> keys = b1.getTable().keySet();
		Iterator<ArgContent> ia = keys.iterator();
		while(ia.hasNext()){
			ArgContent ac = (ArgContent) ia.next();
			res.bind(false, ac, b1.deref(ac));
		}
		return res;
	}
	
	public static List<Binding> listMerge(List<Binding> l1, List<Binding> l2) throws RCGInstantiationException {
		
		if (l1.isEmpty()) {
			return l2;
		} else if (l2.isEmpty()) {
			return l1;
		} else {
			LinkedList<Binding> res = new LinkedList<Binding>();
			for(int i = 0 ; i < l1.size() ; i++){
				for(int j = 0 ; j < l2.size() ; j++){
					res.add(merge(l1.get(i), l2.get(j)));
				}
			}
			return res;
		}
	}
	
	public Hashtable<ArgContent, ArgContent> getTable() {
		return table;
	}

	public void setTable(Hashtable<ArgContent, ArgContent> table) {
		this.table = table;
	}

	public int hashCode() {
		return toString().hashCode();
	}
	
	public boolean equals(Object o) {
		return this.hashCode() == o.hashCode();
	}
	
	public String toString() {
		String ret = "[";
		Set<ArgContent> keyset = table.keySet();
		Iterator<ArgContent> it = keyset.iterator();
		while (it.hasNext()) {
			ArgContent key = (ArgContent) it.next();
			ArgContent val = table.get(key);
			ret += "(" + key.toString() + " ==> " + val.toString() + ")"; 
		}
		return ret + "]";
	}
	
	public Iterator<ArgContent> iterator() {
		return table.keySet().iterator(); 
	}

}
