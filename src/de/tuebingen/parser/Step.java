/*
 *  File Step.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:05:47 CEST 2007
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

import de.tuebingen.rcg.Clause;

public class Step {

	private int       id;
	private ClauseKey ck;
	private Clause    cl;
	private float   prob;
	
	public Step(ClauseKey c, int i, Clause cc){
		this(c,i,cc,-1);
	}
	
	public Step(ClauseKey ck, int id, Clause cl, float prob) {
		this.ck = ck;
		this.id = id;
		this.cl = cl;
		this.prob = prob;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ClauseKey getCk() {
		return ck;
	}

	public void setCk(ClauseKey ck) {
		this.ck = ck;
	}
	
	public Clause getCl() {
		return cl;
	}

	public void setCl(Clause cl) {
		this.cl = cl;
	}

	public float getProb() {
		return prob;
	}

	public void setProb(float prob) {
		this.prob = prob;
	}

	public String toString(){
		return "step num " + id + ", clause " + cl.toString() + ", instantiation " + ck.getArglist().toString();
	}
	
}
