/*
 *  File Rule.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fr 18. Jan 19:44:44 CET 2008
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
package de.tuebingen.forest;

public class Rule {
	
	private Tidentifier lhs;
	private Combination rhs;
	
	public Rule(Tidentifier ti){
		lhs = ti;
		rhs = new Combination();
	}
	
	public Tidentifier getLhs() {
		return lhs;
	}
	public void setLhs(Tidentifier lhs) {
		this.lhs = lhs;
	}
	public Combination getRhs() {
		return rhs;
	}
	public void setRhs(Combination rhs) {
		this.rhs = rhs;
	}
	
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	public boolean equals(Object o) {
		return this.hashCode()==o.hashCode();
	}
	
	public String toString(){
		String res = "";
		res += rhs.toString() + "\n";
		return res;
	}
}
