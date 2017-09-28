/*
 *  File DStep.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fr 18. Jan 10:21:32 CET 2008
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DStep implements Iterable<String> {
	
	private Map<String, DNode[]> cinstantiation; 
	
	public DStep() {
		cinstantiation = new HashMap<String, DNode[]>();
	}
	
	public DStep(Map<String, DNode[]> cinst) {
		cinstantiation = cinst;
	}
	
	public void create(Binding b, int n) {
		String key = b.toString();
		if (!cinstantiation.containsKey(key)) {
			// test needed for symmetric bindings
			DNode[] rhs = new DNode[n]; 
			Arrays.fill(rhs, null);
			cinstantiation.put(key, rhs);
		}
	}
	
	public void put(ClauseKey called, int motherRHSpos, Binding b) {
		String key = b.toString();
		DNode[] mother = cinstantiation.get(key);		
		if (mother[motherRHSpos] == null)
			mother[motherRHSpos] =  new DNode(called);
		else 
			// before inserting the derivation step, we have to check that it is not already there
			// i.e. same bindings, same clause number, same RHS position
			// see the DNode.addDNode method
			// disjunction
			mother[motherRHSpos].addDNode(called);
	}
	
	/**
	 * Method used to remove the partial clause instantiations (keep only the successful instantiations)
	 */
	public Map<String, DNode[]> clean(){
		Map<String, DNode[]> cinst = new HashMap<String, DNode[]>();
		Set<String> keys   = cinstantiation.keySet();
		Iterator<String> it= keys.iterator();
		while (it.hasNext()) {
			String next = it.next();
			DNode[] instantiations = cinstantiation.get(next);
			boolean full = (instantiations.length > 0);
			for(int i = 0 ; i < instantiations.length ; i++) {
				full &= (instantiations[i] != null);
			}
			if (full) {
				cinst.put(next, instantiations);
			}
		}
		return cinst;
	}
	
	public ClauseKey getFirstCk() {
		Iterator<String> it = this.iterator();
		DNode[] next = cinstantiation.get(it.next());
		return next[0].getInstantiation();
	}
	
	public List<ClauseKey> getAllFirstCk() {
		List<ClauseKey> ckl = new LinkedList<ClauseKey>();
		Iterator<String> it = this.iterator();
		DNode[] next = cinstantiation.get(it.next());
		DNode first  = next[0];
		ckl.add(first.getInstantiation());
		boolean go_on = first.isAmbiguous();
		while (go_on) {
			first = first.getDerivStep();
			ckl.add(first.getInstantiation());
			go_on = first.isAmbiguous();
		}
		return ckl;
	}
	
	public DNode getFirstDNode(String binding) {
		DNode[] dnode = cinstantiation.get(binding);
		return dnode[0];
	}
	
	public Iterator<String> iterator() {
		return cinstantiation.keySet().iterator();
	}

	public int size() {
		return cinstantiation.size();
	}

	public Map<String, DNode[]> getCinstantiation() {
		return cinstantiation;
	}

	public String toString() {
		String res = "";
		Set<String> keys   = cinstantiation.keySet();
		Iterator<String> it= keys.iterator();
		int j = 1;
		while (it.hasNext()) {
			String next = it.next();
			DNode[] instantiations = cinstantiation.get(next);
			boolean show = (instantiations.length > 0);
			for(int i = 0 ; i < instantiations.length ; i++) {
				show &= (instantiations[i] != null);
			}
			if (show) {
				if (j > 1)
					res += " || ";
				res += next.toString() + " ";

				res += "{<";
			
				for(int i = 0 ; i < instantiations.length ; i++) {
					res += i + ":: " ;
					if (instantiations[i] == null) // should not occur since we check it, c.f. show
						res += "null DNode ";
					else
						res += instantiations[i].toString();
					res += "::";
					if (i < instantiations.length -1)
						res += " ; ";
				}
				res += ">}";
				res += "\n";
				j++;
			}
		}
		return res;
	}

}
