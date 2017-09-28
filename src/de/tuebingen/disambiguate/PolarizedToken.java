/*
 *  File PolarizedToken.java
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.tuebingen.anchoring.NameFactory;


public class PolarizedToken {
	
	private String                       token;
	private int                       position;
	private Map<String, PolarizedLemma> lemmas;
		
	public PolarizedToken (String t, int p) {
		token    = t;
		position = p;
		lemmas   = new HashMap<String, PolarizedLemma>();
	}
	
	public void addLemma(PolarizedLemma lemma) {
		String suffix = new NameFactory().getName(token);
		// the suffix is used to deal with morphological ambiguity (token + lemma is not a unique key!)
		lemmas.put(lemma.getLemmaID() + "--" + suffix, lemma);
	}
	
	public Iterator<String> iterator() {
		return lemmas.keySet().iterator();
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Map<String, PolarizedLemma> getLemmas() {
		return lemmas;
	}

	public void setLemmas(Map<String, PolarizedLemma> lemmas) {
		this.lemmas = lemmas;
	}
	
	public String toString() {
		String res = "";
		res += "Token " + token + "\n";
		res += "Lemmas : \n";
		Set<String> keys = lemmas.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String next = it.next();
			res += lemmas.get(next).toString();
		}
		return res;
	}

}
