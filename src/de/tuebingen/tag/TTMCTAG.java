/*
 *  File TTMCTAG.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:49:03 CEST 2007
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

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.MorphEntry;
import de.tuebingen.tree.Grammar;

public class TTMCTAG implements Grammar {
	
	private Map<String, List<Tuple>>           grammar;
	private Map<String, List<Lemma>>            lemmas;
	private Map<String, List<MorphEntry>> morphEntries;
	private boolean                     needsAnchoring;
	
	public TTMCTAG() {
		grammar = new HashMap<String,List<Tuple>>();
		needsAnchoring = false;
	}
	
	public void add2family(String f, Tuple t) {
		if (grammar.containsKey(f)) {
			List<Tuple> lt = grammar.get(f);
			lt.add(t);
		} else { 
			List<Tuple> lt = new LinkedList<Tuple>();
			lt.add(t);
			grammar.put(f, lt);
		}
	}
	
	public TTMCTAG(Map<String, List<Tuple>> l){
		grammar = l;
		needsAnchoring = false;
	}
	
	public Map<String, List<Tuple>> getGrammar() {
		return grammar;
	}

	public boolean needsAnchoring() {
		return needsAnchoring;
	}

	public void setNeedsAnchoring(boolean needsAnchoring) {
		this.needsAnchoring = needsAnchoring;
	}

	public void setGrammar(Map<String, List<Tuple>> grammar) {
		this.grammar = grammar;
	}

	public Map<String, List<Lemma>> getLemmas() {
		return lemmas;
	}

	public void setLemmas(Map<String, List<Lemma>> lemmas) {
		this.lemmas = lemmas;
	}

	public Map<String, List<MorphEntry>> getMorphEntries() {
		return morphEntries;
	}

	public void setMorphEntries(Map<String, List<MorphEntry>> morphEntries) {
		this.morphEntries = morphEntries;
	}

	public String toString(){
		String res="";
		Set<String> keys = grammar.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String k = it.next();
		
			for (int i=0;i<grammar.get(k).size(); i++) {
				res+=grammar.get(k).get(i).toString();
			}
		}
		String res1="";
		String res2="";
		if (needsAnchoring) {
			keys = lemmas.keySet();
			it   = keys.iterator();
			while(it.hasNext()){
				String k = it.next();
				
				for (int i=0;i<lemmas.get(k).size(); i++){
					res1+=lemmas.get(k).get(i).toString();
				}
			}
			
			keys = morphEntries.keySet();
			it   = keys.iterator();
			while(it.hasNext()){
				String k = it.next();
			
					for (int i=0;i<morphEntries.get(k).size();i++){
						res2+=morphEntries.get(k).get(i).toString();
					}
			}
		}
		return res+"\n"+res1+"\n"+res2;
	}
}
