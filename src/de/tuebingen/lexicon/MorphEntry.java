/*
 *  File MorphEntry.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:09:15 CEST 2007
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

import java.util.LinkedList;
import java.util.List;

public class MorphEntry {
	
	private String lex;
	private List<Lemmaref> lemmarefs;
	
	public MorphEntry(String l){
		lex = l;
		lemmarefs = null;
	}
	
	public MorphEntry(MorphEntry m){
		lex       = m.getLex();
		lemmarefs = new LinkedList<Lemmaref>();
		for(int i = 0 ; i < m.getLemmarefs().size() ; i++) {
			lemmarefs.add(new Lemmaref(m.getLemmarefs().get(i)));
		}
	}
	
	public void addLemmaref(Lemmaref l) {
		if (lemmarefs == null)
			lemmarefs = new LinkedList<Lemmaref>();
		lemmarefs.add(l);
	}

	public String getLex() {
		return lex;
	}

	public void setLex(String lex) {
		this.lex = lex;
	}

	public List<Lemmaref> getLemmarefs() {
		return lemmarefs;
	}

	public void setLemmarefs(List<Lemmaref> lemmarefs) {
		this.lemmarefs = lemmarefs;
	}
	
	public String toString(){
		String res="";
		for (int i=0; i< lemmarefs.size(); i++){
			res += lemmarefs.get(i);
		}
		return "Morph : "+lex+"\n  lemmarefs : \n"+res;
	}
	
}
