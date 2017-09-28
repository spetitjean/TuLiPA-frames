/*
	 *  File CanonicalNameFactory.java
	 *
	 *  Authors:
	 *     Yannick Parmentier  <parmenti@loria.fr>
	 *     
	 *  Copyright:
	 *     Yannick Parmentier, 2007
	 *
	 *  Last modified:
	 *     Tue Nov 25 16:03:44 CET 2008
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
package de.tuebingen.parserconstraints;

import java.util.*;

public class CanonicalNameFactory {
	
	private int index;
	private Map<String, String> dictionary;
	private final char[] roots = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 
			'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 
			'W', 'X', 'Y', 'Z'};
	
	public CanonicalNameFactory(){
		index = 0;
		dictionary = new Hashtable<String, String>();
	}
	
	public String getName(String in){
		String out = "";
		if (dictionary.containsKey(in)) {
			out = dictionary.get(in);
		} else {
			int which = index;
			int suffix = 0;
			if (index >= 26) {
				which = index % 26;
				suffix = index / 26;
			}
			out = roots[which]+""+suffix;
			index++;
			dictionary.put(in, out);
		}
		//System.err.println(" ---> old name: " + in + " ---> new name: " + out);
		return out;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Map<String, String> getDictionary() {
		return dictionary;
	}

	public void setDictionary(Map<String, String> dictionary) {
		this.dictionary = dictionary;
	}
	
	public String toString(){
		String res = "";
		Set<String> keys = dictionary.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String k = it.next();
			res += " key: " + k + " - value: " + dictionary.get(k);
		}
		return res;
	}
}
