/*
 *  File XMLMorphReader.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:48:17 CEST 2007
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
package de.tuebingen.io;

import de.tuebingen.util.XMLUtilities;
import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.lexicon.Lemmaref;
import de.tuebingen.lexicon.MorphEntry;

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * 
 * @author parmenti
 *
 */
public class XMLMorphReader extends FileReader {

	private File morphFile;
	static Document morphDoc;	

	public XMLMorphReader(File morph) throws FileNotFoundException {
		/**
		 * Generate a parser for an XML file containing an XMG MC-TAG grammar
		 * 
		 * @param grammar
		 *            the XML file
		 */
		super(morph);
		this.morphFile = morph;
		morphDoc = XMLUtilities.parseXMLFile(morphFile, false);		
	}			

	public Map<String, List<MorphEntry>> getMorphs(){
		Map<String, List<MorphEntry>> morphs = new HashMap<String, List<MorphEntry>>();
	
		Element root = morphDoc.getDocumentElement();  		
		NodeList l = root.getElementsByTagName("morphs");
		Element e = (Element) l.item(0);
		NodeList ll = e.getChildNodes();
			
		for (int i=0; i < ll.getLength(); i++) {
			Node n = ll.item(i);			
			// according to the DTD, the XML chidlren
			// of tag is morph
			if (n.getNodeType() == Node.ELEMENT_NODE) {				
				Element el = (Element) n;
				if (el.getTagName().equals("morph")) {
					String lex = el.getAttribute("lex");
					MorphEntry mo = new MorphEntry(lex);
					mo.setLemmarefs(getLemmarefs(el));
					
					List<MorphEntry> lme = morphs.get(mo.getLex());
					if (lme == null) {
						lme = new LinkedList<MorphEntry>();
					}
					lme.add(mo);
					morphs.put(mo.getLex(), lme);
				}
			}
		}
		return morphs;
	}
		
	public static List<Lemmaref> getLemmarefs(Element e){
		List<Lemmaref> lrefs = new LinkedList<Lemmaref>();
		
		NodeList l = e.getChildNodes();
		
		for (int i=0 ; i < l.getLength() ; i++){
			Node n = l.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {				
				Element el = (Element) n;
				if (el.getTagName().equals("lemmaref")){
					String name = el.getAttribute("name");
					String cat  = el.getAttribute("cat");
					Lemmaref lref = new Lemmaref(name, cat);
					lref.setFeatures(XMLTTMCTAGReader.getNarg(el, XMLTTMCTAGReader.FROM_OTHER, new NameFactory()));
					lrefs.add(lref);
				}
			}					
		}
		return lrefs;
	}

}
