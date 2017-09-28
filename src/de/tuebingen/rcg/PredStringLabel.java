/*
 *  File PredStringLabel.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:55:36 CEST 2007
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
package de.tuebingen.rcg;

/**
 * A simple (RCG) predicate label, just labeled with a String 
 * 
 * @author wmaier
 *
 */
public class PredStringLabel implements PredLabel {

	private String label;

	public PredStringLabel(String label) {
		this.label = label;
	}
	
	public PredStringLabel(PredStringLabel l) {
		this.label = new String(l.label);
	}

	public String getName() {
		return label;
	}

	public void setName(String name) {
		this.label = name;
	}


	public int hashCode() {
	    return label.hashCode();
	}

	public boolean equals(Object o) {
		return o.hashCode() == this.hashCode();
	}
	
	public String toString() {
		return label;
	}

	@Override
	public Object clone() {
		return new PredStringLabel(new String(label));
	}

}
