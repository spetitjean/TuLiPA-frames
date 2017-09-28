/*
 *  File Lemma.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:08:10 CEST 2007
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

public class Lemma {
	
	private String name;
	private String cat;
	private List<Anchor> anchors;
	
	public Lemma() {
		name    = null;
		cat     = null;
		anchors = null;
	}
	
	public Lemma(Lemma l){
		name    = l.getName();
		cat     = l.getCat();
		anchors = new LinkedList<Anchor>(); 
		for(int i = 0 ; i < l.getAnchors().size() ; i++) {
			anchors.add(new Anchor(l.getAnchors().get(i)));
		}
	}
	
	public Lemma (String n, String c) {
		name = n;
		cat  = c;
		anchors = null;
	}
	
	public void addAnchor(Anchor a) {
		if (anchors == null)
			anchors = new LinkedList<Anchor>();
		anchors.add(a);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public List<Anchor> getAnchors() {
		return anchors;
	}

	public void setAnchors(List<Anchor> anchors) {
		this.anchors = anchors;
	}
	
	public List<CoAnchor> getCoAnchors() {
		List<CoAnchor> lca = new LinkedList<CoAnchor>();
		for(Anchor a : anchors) {
			lca.addAll(a.getCoanchors());
		}
		return lca;
	}
	
	public String toString(){
		String res = "";
		for (int i=0 ; i < anchors.size() ; i++){
			res += res + anchors.get(i).toString();
		}
		return "Lemma : "+name+" - cat : "+cat+" - anchors : \n"+res;
	}

}
