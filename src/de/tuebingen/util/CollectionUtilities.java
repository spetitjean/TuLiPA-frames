/*
 *  File CollectionUtilities.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:24:45 CEST 2007
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map;

import de.tuebingen.anchoring.InstantiatedTagTree;

/**
 * @author wmaier
 *
 */
public class CollectionUtilities {

	public static Object deepCopy(Object original) throws Exception {
		Object ret = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject(original);
		out.flush();
		out.close();
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
		ret = in.readObject();
		return ret;
	}
	
	public static void addToValueSet(Map<Object,Set<Object>> m, Object key, Object o) {
		Set<Object> c = null;
		if (m.containsKey(key)) {
			c = m.get(key);
		} else {
			c = new HashSet<Object>();
			m.put(key, c);
		}
		c.add(o);
	}
	
	public static Object getFirstCollectionMember(Set<Object> s) {
		if (s == null || s.size() == 0) {
			return null;
		} 
		return s.iterator().next();
	}

	public static boolean arrayContains(Object o, Object[] a) {
		for (int i = 0; i < a.length; ++i) {
			if (a[i].equals(o)) {
				return true;
			}
		}
		return false;
	}

	public static long computeCartesianCard(Map<String, List<InstantiatedTagTree>> sets, List<String> tokens) {
		// computes the product of lexical entries per token of the sentence to parse
		long res = 1;
		for(String t : tokens) {
			List<InstantiatedTagTree> ls = sets.get(t);
			if (ls != null)
				res *= ls.size();
		}
		return res;
	}
	
	public static long computeCartesianCard(List<List<List<String>>> sets, List<String> tokens) {
		long res = 0;
		for(List<List<String>> llo : sets) {
			long tmp = 1;
			for (List<String> lo : llo) {
				tmp *= lo.size();
			}
			res += tmp;
		}
		return res;
	}

	public static List<String> computeSubGrammar(List<List<List<String>>> sets) {
		List<String> grammar = new LinkedList<String>(); 
		for(List<List<String>> llo : sets) {
			for (List<String> lo : llo) {
				for (String s : lo) {
					if (!grammar.contains(s))
						grammar.add(s);
				}
			}
		}		
		return grammar;
	}
	
	public static long computeAmbig(List<List<String>> subg, List<String> tokens) {
		long res = 1;
		Map<Integer, List<String>> lset = new HashMap<Integer, List<String>>();
		for (List<String> asub : subg) {
			for (int i = 0 ; i < asub.size() ; i++) {
				List<String> tokset = lset.get(i);
				if (tokset == null) {
					tokset = new LinkedList<String>();
					lset.put(i, tokset);
				}
				String tid = asub.get(i);
				if (!tokset.contains(tid))
					tokset.add(tid);
			}
		}
		for (int i = 0 ; i < tokens.size() ; i++) {
			List<String> tids = lset.get(i);
			if (tids != null)	
				res *= tids.size();
		}
		return res;
	}
}
