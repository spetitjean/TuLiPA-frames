/*
 *  File InstantiatedMorph.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 11:12:18 CEST 2007
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
package de.tuebingen.anchoring;

import de.tuebingen.lexicon.MorphEntry;
import de.tuebingen.tokenizer.Word;

public class InstantiatedMorph extends MorphEntry {
	
	private Word       inToken;
	
	public InstantiatedMorph(InstantiatedMorph other) {
		super((MorphEntry) other);
		inToken = other.getInToken();
	}
	
	public InstantiatedMorph(String lex){
		super(lex);
		inToken = null;
	}
	
	public InstantiatedMorph(String lex, Word w){
		super(lex);
		inToken = w;
	}
	
	public InstantiatedMorph(Word w){
		super(w.getWord());
		inToken = w;
	}
	
	public InstantiatedMorph(MorphEntry m, Word w){
		super(m);
		inToken = w;
	}

	public Word getInToken() {
		return inToken;
	}

	public void setInToken(Word inToken) {
		this.inToken = inToken;
	}

}
