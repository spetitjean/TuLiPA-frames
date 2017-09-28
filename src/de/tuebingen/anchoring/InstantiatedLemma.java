/*
 *  File InstantiatedLemma.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 11:11:54 CEST 2007
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

import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.Lemmaref;

public class InstantiatedLemma extends Lemma {
	/**
	 * An instantiated lemma is a lemma associated with a morphological item
	 * 
	 * @author parmenti
	 */	
	private InstantiatedMorph lexItem; 
	private Lemmaref   lref;
	
	public InstantiatedLemma(InstantiatedLemma other) {
		super((Lemma) other);
		lref    = new Lemmaref(other.getLref());
		lexItem = new InstantiatedMorph(other.getLexItem());
	}
	
	public InstantiatedLemma(){
		super();
	}
	
	public InstantiatedLemma(Lemma l){
		super(l);
	}
	
	public InstantiatedLemma(Lemma l, InstantiatedMorph m){
		super(l);
		this.lexItem = new InstantiatedMorph(m);
	}
	
	public InstantiatedLemma(Lemma l, InstantiatedMorph m, Lemmaref le){
		super(l);
		this.lexItem = new InstantiatedMorph(m);
		this.lref    = new Lemmaref(le);
	}
	
	public Lemmaref getLref() {
		return lref;
	}

	public void setLref(Lemmaref lref) {
		this.lref = lref;
	}

	public InstantiatedMorph getLexItem() {
		return lexItem;
	}

	public void setLexItem(InstantiatedMorph lexItem) {
		this.lexItem = lexItem;
	}
	
	public String toString(){
		String r1  = super.toString();
		String res = lexItem.getLex()+"\n "; 
		res += lexItem.toString();
		return "Instantiated Lemma : "+res+" "+r1+"\n";
	}
	
}
