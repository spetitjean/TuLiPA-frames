/*
 *  File CVectorKey.java
 *
 *  Authors:
 *     Yannick Parmentier
 *     
 *  Copyright:
 *     Yannick Parmentier, 2009
 *
 *  Last modified:
 *     Do 16. Apr 09:55:36 CEST 2009
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

public class CVectorKey {
	
	private Map<String, Integer> range2index;
	private Map<Integer, String> index2range;
	int[][]                         equality;
	int[][]                       inequality;
	int                             nbRanges;
	int                                 next;
	
	public CVectorKey(int n) {
		range2index = new HashMap<String, Integer>();
		index2range = new HashMap<Integer,String>();
		nbRanges    = n;
		equality    = new int[2*n+1][2*n+1];
		inequality  = new int[2*n+1][2*n+1];
		for (int i = 0 ; i < equality.length ; i++) {
			for (int j = 0 ; j < equality[0].length ; j++) {
				equality[i][j]   = -1;
				inequality[i][j] = -1;
			}
		}
		next        = 1; // for the line 0 and the column 0 are reserved for constants
	}
	
	private int getNext() {
		int n = next;
		next++;
		return n;
	}

	public int addConst(Constraint c) {
		System.err.println("Storing: " + c.toString());
		int res = 0;  // 0 means constraint added, 
		              // 1 duplicated constraint and -1 error (inconsistency)
		String name1 = c.getArg1();
		String name2 = c.getArg2();
		// the range is stored to a table to later get its id
		if (!name1.equals(Constraint.VAL) && !range2index.containsKey(name1)) {
			int n = this.getNext();
			range2index.put(name1, n);
			index2range.put(n, name1);
		}
		if (!name2.equals(Constraint.VAL) && !range2index.containsKey(name2)) {
			int n = this.getNext();
			range2index.put(name2, n);
			index2range.put(n, name2);
		}
		int m2 = c.getModif2();
		int type = c.getType();
		switch(type) {
		case Constraint.EQUALS:
			int x1 = name1.equals(Constraint.VAL) ? 0 : range2index.get(name1);
			int y1 = name2.equals(Constraint.VAL) ? 0 : range2index.get(name2);
			int n1 = equality[x1][y1];
			if (n1 >= 0) {
				if (n1 != m2)
					res = -1;
				else
					res = 1;
			} else
				equality[x1][y1] = m2;
			break;
		case Constraint.LE_EQ:
			int x2 = name1.equals(Constraint.VAL) ? 0 : range2index.get(name1);
			int y2 = name2.equals(Constraint.VAL) ? 0 : range2index.get(name2);
			int n2 = inequality[x2][y2];
			if (n2 >= 0) {
				if (n2 != m2)
					res = -1;
				else
					res = 1;
			} else
				inequality[x2][y2] = m2;
			break;
		default: //skip
		}
		return res;
	}
	
	public int getHash() {
		int res = 0;
		StringBuffer sb = new StringBuffer();
		for (int[] line : equality) {
			for (int num : line) {
				sb.append(String.valueOf(num+1));
			}
		}
		res = Integer.valueOf(sb.toString());
		return res;
	}
	
	public String print() {
		String res = "Equality:\n    ";
		for (int k = 1 ; k < equality[0].length ; k++) {
			res += " " + index2range.get(k);
		}
		res += "\n  ";
		for (int i = 0 ; i < equality.length ; i++) {
			for (int j = 1 ; j < equality[0].length ; j++) {
				res += " " + equality[i][j];
			}
		}
		res = "Inequality:\n    ";
		for (int k = 1 ; k < inequality[0].length ; k++) {
			res += " " + index2range.get(k);
		}
		res += "\n  ";
		for (int i = 0 ; i < inequality.length ; i++) {
			for (int j = 1 ; j < inequality[0].length ; j++) {
				res += " " + inequality[i][j];
			}
		}
		return res;
	}
	
}
