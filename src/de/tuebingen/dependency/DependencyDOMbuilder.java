/*
 *  File DependencyDOMBuilder.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 11:08:08 CEST 2007
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
package de.tuebingen.dependency;

import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.tuebingen.tokenizer.Word;

public class DependencyDOMbuilder {
	
	private static Document dependency;
	
	public static Document buildAllDep(List<Word> tok, String sentence, List<Map<Integer, Dependency>> deps){
		Map<Integer, String> words = new HashMap<Integer, String>();
		for(int i = 0 ; i < tok.size() ; i++){
			words.put(tok.get(i).getEnd(), tok.get(i).getWord());
		}
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder constructor    = factory.newDocumentBuilder();
			dependency                     = constructor.newDocument();
			dependency.setXmlVersion("1.0");
			dependency.setXmlStandalone(true);
			
			Element root = dependency.createElement("dependencies");
			root.setAttribute("sentence", sentence);
			
			// for each derivation tree
			for(int j = 0 ; j < deps.size() ; j++) {
				Map<Integer, Dependency> dep = deps.get(j);
				Element e = buildDepDOM(words, dep, j);
				root.appendChild(e);
			}
			
			// finally we do not forget the root
			dependency.appendChild(root);
			return dependency;

		} catch (ParserConfigurationException e) {
			System.err.println(e);
			//System.err.println(e.getStackTrace());
			return null;
		}
	}
	
	public static Element buildDepDOM(Map<Integer, String> words, Map<Integer, Dependency> dep, int j) {

		Element oneDep = dependency.createElement("sentence");
		oneDep.setAttribute("id", "_"+j);
		
		for(int i = 0 ; i < words.size() ; i++) {
			int id   = i+1;
			if (dep.containsKey(id)) { // the word is an anchor
				int head   = dep.get(id).getHead();
				String cat = dep.get(id).getCat();
				Element w = dependency.createElement("word");
				w.setAttribute("id",   id+"");
				w.setAttribute("head", head+"");
				w.setAttribute("form", words.get(id));
				w.setAttribute("deprel", cat);
				oneDep.appendChild(w);
			} //otherwise it is not an anchor, we just ignore it
		}
			
		return oneDep;
	}

}
