/*
 *  File Equation.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:07:43 CEST 2007
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
package de.tuebingen.lexicon;

import de.tuebingen.tag.Fs;

public class Equation {

	private String type;    // top or bot
	private String node_id;
	private Fs features;
	
	public Equation(String t, String n){
		type = t;
		node_id = n;
		features = null;
	}
	
	public Equation(Equation eq) {
		type = eq.getType();
		node_id = eq.getNode_id();
		features = new Fs(eq.getFeatures());
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNode_id() {
		return node_id;
	}

	public void setNode_id(String node_id) {
		this.node_id = node_id;
	}

	public Fs getFeatures() {
		return features;
	}

	public void setFeatures(Fs features) {
		this.features = features;
	}
	
	public String toString(){
		return "\n\t"+node_id+" -> "+type+"."+features.toString(); 
	}
}
