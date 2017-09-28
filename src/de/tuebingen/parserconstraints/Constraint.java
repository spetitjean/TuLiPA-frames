/*
 *  File Constraint.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@loria.fr>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Thu Dec 18 11:42:01 CET 2008
 *
 *  This file is part of the TuLiPA system
 *     http://sourcesup.cru.fr/tulipa
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

import java.util.List;
import java.util.Map;

public class Constraint {
	
	public static final int EQUALS = 0;
	public static final int LE_EQ  = 1;
	
	public static final String LEFT = ".l";
	public static final String RIGHT= ".r";
	public static final String VAL  = "";
	
	private int  type;
	private String arg1; // arg1 or arg2 is "" when processing a constant!
	private String arg2;
	private int modif1;
	private int modif2;
	
	public Constraint(int t, String a1, String a2) {
		type = t;
		arg1 = a1;
		arg2 = a2;
		modif1 = 0;
		modif2 = 0;
	}
	
	public Constraint(int t, String a1, String a2, int m1, int m2) {
		this(t, a1, a2);
		modif1 = m1;
		modif2 = m2;
	}
	
	public Constraint(Constraint c) {
		type = c.getType();
		arg1 = new String(c.getArg1());
		arg2 = new String(c.getArg2());
		modif1 = c.getModif1();
		modif2 = c.getModif2();
	}
	
	public void order() {
		// in case of equality, this method makes sure the arguments are lexicographically ordered
		// (canonical form) to facilitate comparisons between constraints
		if (type == Constraint.EQUALS && arg1.compareTo(arg2) > 0) {
			String tmp = arg1;
			int    tmpp= modif1;
			arg1   = arg2;
			modif1 = modif2;
			arg2   = tmp;
			modif2 = tmpp;
		}
	}
	
	public Integer getConst(String bound) {
		Integer c = null;
		if (arg1.equals(bound) && arg2.equals(Constraint.VAL) && modif1 == 0) {
			c = Integer.valueOf(modif2);
		}
		if (arg2.equals(bound) && arg1.equals(Constraint.VAL) && modif2 == 0) {
			c = Integer.valueOf(modif1);
		}		
		return c;
	}
	
	public Constraint select(String left, String right, Map<String, String> newNames) {
		Constraint c = null;
		// LEFT AND VAL
		if (arg1.equals(left) && arg2.equals(Constraint.VAL)) {
			c = new Constraint(this);
			c.setArg1(newNames.get(left));
		}
		if (arg2.equals(left) && arg1.equals(Constraint.VAL)) {
			c = new Constraint(this);
			c.setArg2(newNames.get(left));
		}
		// LEFT AND RIGHT
		if (arg1.equals(left) && arg2.equals(right)) {
			c = new Constraint(this);
			c.setArg1(newNames.get(left));
			c.setArg2(newNames.get(right));
		}
		if (arg2.equals(left) && arg1.equals(right)) {
			c = new Constraint(this);
			c.setArg2(newNames.get(left));
			c.setArg1(newNames.get(right));
		}
		// RIGHT AND VAL
		if (arg1.equals(right) && arg2.equals(Constraint.VAL)) {
			c = new Constraint(this);
			c.setArg1(newNames.get(right));
		}
		if (arg2.equals(right) && arg1.equals(Constraint.VAL)) {
			c = new Constraint(this);
			c.setArg2(newNames.get(left));
		}
		return c;
	}
	
	public Constraint update(List<String> olDs, List<String> neWs) {
		// returns null if the constraint is not modified, the new constraint otherwise
		Constraint c = new Constraint(this);
		String name1 = c.getArg1();
		String name2 = c.getArg2();
		boolean modified1 = false || name1.equals(Constraint.VAL);
		boolean modified2 = false || name2.equals(Constraint.VAL);
		boolean verb = false;
		
		if (olDs.contains(name1)) {
			if (verb)
				System.err.println("Old: " + name1);
			int i1 = olDs.indexOf(name1);
			name1 = neWs.get(i1);
			modified1 = true;
			if (verb)
				System.err.println("New: " + name1);
		}
		if (olDs.contains(name2)) {
			if (verb)
				System.err.println("Old: " + name2);
			int i2 = olDs.indexOf(name2);
			name2 = neWs.get(i2);
			modified2 = true;
			if (verb)
				System.err.println("New: " + name2);
		}
		c.setArg1(name1);
		c.setArg2(name2);
		if (modified1 && modified2 && verb) {
			System.err.println("### updated " + c.toString());
		}
		if (modified1 && modified2) {
			return c;
		} else
			return null;
	}
	
	public String getName(int i) {
		String res ="";
		switch (i) {
		case 1: res = arg1.length() == 0 ? "" : arg1.substring(0, arg1.length() -2); // to remove :l or :r
			break;
		case 2: res = arg2.length() == 0 ? "" : arg2.substring(0, arg2.length() -2); // to remove :l or :r
			break;
		default://skip
		}
		return res;
	}
	
	public String getType(int i) {
		String res ="";
		switch (i) {
		case 1: res = arg1.length() == 0 ? "" : arg1.substring(arg1.length()-3, arg1.length());
			break;
		case 2: res = arg2.length() == 0 ? "" : arg2.substring(arg2.length()-3, arg1.length());
			break;
		default://skip
		}
		return res;
	}
		
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getArg1() {
		return arg1;
	}

	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

	public String getArg2() {
		return arg2;
	}

	public void setArg2(String arg2) {
		this.arg2 = arg2;
	}

	public int getModif1() {
		return modif1;
	}

	public void setModif1(int modif1) {
		this.modif1 = modif1;
	}

	public int getModif2() {
		return modif2;
	}

	public void setModif2(int modif2) {
		this.modif2 = modif2;
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	public boolean equals(Object o) {
		return this.hashCode() == o.hashCode();
	}
	
	public String toShortString() {
		StringBuffer res = new StringBuffer();
		String a1 = (arg1.equals(Constraint.VAL) || !arg1.matches("_")) ? arg1 : arg1.split("_")[1];
		String a2 = (arg2.equals(Constraint.VAL) || !arg1.matches("_")) ? arg2 : arg2.split("_")[1];
		Constraint c = new Constraint(type, a1, a2, modif1, modif2);
		c.order();
		String name1 = c.getArg1();
		String name2 = c.getArg2();
		int m1 = c.getModif1();
		int m2 = c.getModif2();
		switch (type) {
		case EQUALS:
			res.append(name1);
			if (m1 != 0 || name1.length() == 0) {
				res.append("+");
				res.append(m1);
			}
			res.append("=");
			res.append(name2);
			if (m2 != 0 || name2.length() == 0) {
				res.append("+");
				res.append(m2);
			}		
			break;
		case LE_EQ: //skip
			break;
		}
		return res.toString();
	}
	
	public String toStringRenamed(Map<String, String> names) {
		StringBuffer res = new StringBuffer();
		String name1 = names.containsKey(arg1) ? names.get(arg1) : arg1;
		String name2 = names.containsKey(arg2) ? names.get(arg2) : arg2;
		int    m1 = modif1;
		int    m2 = modif2;
		//--------------------------------------------------------
		// when displaying the constraint, we impose a canonical
		// order to facilitate the comparison between constraints
		Constraint c = new Constraint(type, name1, name2, m1, m2);
		c.order();
		name1 = c.getArg1();
		name2 = c.getArg2();
		m1 = c.getModif1();
		m2 = c.getModif2();
		//---------------------------------------------------------
		res.append(name1);
		if (m1 != 0 || name1.length() == 0) {
			res.append("+(");
			res.append(m1);
			res.append(")");
		}
		switch (type) {
		case EQUALS: res.append("=");break;
		case LE_EQ: res.append("<=");break;
		}
		res.append(name2);
		if (m2 != 0 || name2.length() == 0) {
			res.append("+(");
			res.append(m2);
			res.append(")");			
		}
		return res.toString();
	}

	public String toString() {
		StringBuffer res = new StringBuffer();
		res.append(arg1);
		if (modif1 != 0 || arg1.length() == 0) {res.append("+("+modif1+")");}
		switch (type) {
		case EQUALS: res.append("=");break;
		case LE_EQ: res.append("<=");break;
		}
		res.append(arg2);
		if (modif2 != 0 || arg2.length() == 0) {res.append("+("+modif2+")");}
		return res.toString();
	}

}
