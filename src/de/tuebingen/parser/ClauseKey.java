/*
 *  File ClauseKey.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:59:44 CEST 2007
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

import java.util.ArrayList;
import java.util.List;

import de.tuebingen.rcg.Argument;
import de.tuebingen.rcg.RCG;

public class ClauseKey implements Comparable<Object> {
	
	private int             cindex;
	private List<Argument> arglist;

	public ClauseKey(int i, List<Argument> la){
		cindex  = i;
		arglist = la;
	}
	
	public ClauseKey(ClauseKey c){
		cindex = c.getCindex();
		arglist = new ArrayList<Argument>();
		for(int i = 0 ; i < c.getArglist().size() ; i++){
			arglist.add(c.getArglist().get(i));
		}
	}

	public int getCindex() {
		return cindex;
	}

	public void setCindex(int cindex) {
		this.cindex = cindex;
	}

	public List<Argument> getArglist() {
		return arglist;
	}

	public void setArglist(List<Argument> arglist) {
		this.arglist = arglist;
	}
	
	public String getArgs() {
		String res = "";
		res += "(";
		for(int i = 0 ; i < arglist.size() ; i++){
			res += arglist.get(i)+" ";
		}
		res += " )";
		return res;
	}

	public int hashCode() {
		return toString().hashCode();
	}
	
	public boolean equals(Object o) {
		return this.hashCode() == o.hashCode();
	}
	
	public int compareTo(Object o) {
		if (o instanceof ClauseKey) {
			int cindex2 = ((ClauseKey) o).cindex;
			if (cindex2 > cindex) return -1;
			if (cindex2 < cindex) return 1;
		}
		return 0;
	}
	
	public String toString(){
		String res="";
		res += "Clause number "+ cindex +" instantiated with ( ";
		for(int i = 0 ; i < arglist.size() ; i++){
			res += arglist.get(i)+" ";
		}
		res += " )";
		return res;
	}
	
	public String toString(RCG g){
		String res="";
		res += "Clause "+ g.getClause(cindex) +" instantiated with ( ";
		for(int i = 0 ; i < arglist.size() ; i++){
			res += arglist.get(i)+" ";
		}
		res += " )";
		return res;
		
	}
	
}
