/*
 *  File PolarizedTuple.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Wed Feb 27 15:53:52 CET 2008
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
package de.tuebingen.disambiguate;

import java.util.*;

public class PolarizedTuple {
	
	private String tupleID;
	private String originalID;
	private Polarities pol;
	private List<String> lexicals;
	
	public PolarizedTuple(String id) {
		tupleID = id;
		lexicals= new LinkedList<String>();
	}
	
	public PolarizedTuple(String id, Polarities p) {
		tupleID = id;
		pol     =  p;
		lexicals= new LinkedList<String>();
	}
	
	public void addLexicals(List<String> lex) {
		lexicals.addAll(lex);
	}
	
	public String getTupleID() {
		return tupleID;
	}
	
	public void setTupleID(String tupleID) {
		this.tupleID = tupleID;
	}
	
	public Polarities getPol() {
		return pol;
	}
	
	public void setPol(Polarities pol) {
		this.pol = pol;
	}
	
	public String getOriginalID() {
		return originalID;
	}

	public void setOriginalID(String originalID) {
		this.originalID = originalID;
	}
	
	public List<String> getLexicals() {
		return lexicals;
	}

	public String toString() {
		String res = "";
		res += "Tuple " + tupleID + " (" + originalID + "):\n";
		res += pol.toString() + "\n";
		return res;
	}
	
}
