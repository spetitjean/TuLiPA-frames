/*
 *  File SemDom.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Tue Jan 29 10:42:11 CET 2009
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
package de.tuebingen.tag;

import de.tuebingen.anchoring.NameFactory;

public class SemDom implements SemLit {
	
	public static final int SCOPEOVER = 0;
	
	private Value arg1;
	private Value arg2;
	private int constr;
	
	public SemDom(){
		constr = SCOPEOVER;
	}
	
	public SemDom(SemDom sd) {
		arg1   = new Value(sd.getArg1());
		arg2   = new Value(sd.getArg2());
		constr = sd.getConstr();		
	}
	
	public SemDom(SemDom sd, NameFactory nf) {
		arg1   = new Value(sd.getArg1(), nf);
		arg2   = new Value(sd.getArg2(), nf);
		constr = sd.getConstr();
	}
	
	public void update(Environment env, boolean finalUpdate) {
		arg1.update(env, finalUpdate);
		arg2.update(env, finalUpdate);
	}

	public Value getArg1() {
		return arg1;
	}
	
	public void setArg1(Value arg1) {
		this.arg1 = arg1;
	}
	
	public Value getArg2() {
		return arg2;
	}
	
	public void setArg2(Value arg2) {
		this.arg2 = arg2;
	}
	
	public int getConstr() {
		return constr;
	}

	public void setConstr(int constr) {
		this.constr = constr;
	}

	public String toString() {
		String res = "";
		res += arg2.toString() + " has_scope_over " + arg1.toString();
		return res;
	}

}
