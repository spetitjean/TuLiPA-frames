/*
 *  File PowerSet.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:27:53 CEST 2007
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
package de.tuebingen.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class PowerSet {
	/**
	 * Implements a simple power set computer
	 * (the sets are made of objects)
	 */

	public static LinkedList<LinkedList<Object>> computePowerSet(LinkedList<Object> v) {
		/**
		 * Computes the power set of a set (represented as a list of objects)
		 */
		LinkedList<LinkedList<Object>> res = new LinkedList<LinkedList<Object>>();
		if (v.isEmpty()) {
			res.add(new LinkedList<Object>());			
		}	
		else {
			Object x = v.poll();
			LinkedList<LinkedList<Object>> y = computePowerSet(v);

			Iterator<LinkedList<Object>> i = y.iterator();
			while (i.hasNext()) {
				LinkedList<Object> lo = i.next();
				// to be checked: copy constructor:
				LinkedList<Object> lo2= new LinkedList<Object>(lo);
				lo2.add(x);
				res.add(lo);
				res.add(lo2);
			}
		}
		return res;
	}

	public static ArrayList<LinkedList<LinkedList<Object>>> computeCartProd(LinkedList<LinkedList<Object>> v) {
		/**
		 * Computes the cartesian product from a list of sets (each one represented as a list of objects)
		 * represented as: 
		 * 			<Set1-Set2-...-SetN>
		 * and outputs an arraylist like:
		 * 			[PowerSet1-PowerSet2-...-PowerSetN]
		 */
		ArrayList<LinkedList<LinkedList<Object>>> res = new ArrayList<LinkedList<LinkedList<Object>>>();
		for (int i = 0 ; i < v.size() ; i++) {
			LinkedList<Object> set = v.get(i);
			LinkedList<LinkedList<Object>> pset = PowerSet.computePowerSet(set); 
			res.add(i, pset);
		}
		return res;
	}
}