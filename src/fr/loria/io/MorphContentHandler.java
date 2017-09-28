/*
 *  File MorphContentHandler.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@loria.fr>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fri May 16 16:03:14 CEST 2008
 *
 *  This file is part of the Polarity Filter
 *
 *  The Polarity Filter is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The Polarity Filter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package fr.loria.io;

import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

import de.tuebingen.tag.Fs;
import de.tuebingen.tag.Value;
import de.tuebingen.lexicon.Lemmaref;
import de.tuebingen.lexicon.MorphEntry;
import de.tuebingen.anchoring.NameFactory;


public class MorphContentHandler implements ContentHandler {

	private Locator locator;
	private boolean verbose = false;
	private Map<String, List<MorphEntry>> morphs;
	private MorphEntry        currentMorph;
	private Lemmaref            currentLem;
	private List<Fs>          currentFeats;
	private List<String>       currentFeat;
	private LinkedList<Value>  currentADisj = null;
	
	private NameFactory nf;
	
	public MorphContentHandler(boolean v){
		super();     
		locator = new LocatorImpl();
		verbose = v;
		morphs = new HashMap<String, List<MorphEntry>>();
	}
	
	public void add2morph(String key, MorphEntry value) {
		List<MorphEntry> lm = morphs.get(key);
		if (lm == null) {
			lm = new LinkedList<MorphEntry>();
			morphs.put(key, lm);
		}
		lm.add(value);
	}
	
	/**
     * @param locator to use
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator value) {
    	locator =  value;
    }
    
	/**
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
	}

	/**
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
	}

	/**
	 * @param chosen namespace prefix
	 * @param URI of the name-space
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
	 */
	public void startPrefixMapping(String prefix, String URI) throws SAXException {
	}

	/**
	 * @param chose namespace prefix
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	public void endPrefixMapping(String prefix) throws SAXException {
	}

	/**
	 * @param nameSpaceURI.
	 * @param localName.
	 * @param rawName for version 1.0 <code>nameSpaceURI + ":" + localName</code>
	 * @throws SAXException (error such as not DTD compliant) 
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String nameSpaceURI, String localName, String rawName, Attributes attributs) throws SAXException {
		
		if ( ! "".equals(nameSpaceURI) && verbose) { 
			System.err.println(" Namespace detected : "  + nameSpaceURI);
		}
		
		if (localName.equals("mcgrammar")) {
			// nothing to do
		} 
		else if (localName.equals("morphs")) {
			// nothing to do
		}
		else if (localName.equals("morph")) {
			String lex = attributs.getValue("lex");
			currentMorph = new MorphEntry(lex);
			nf = new NameFactory();
			this.add2morph(lex, currentMorph);
		}
		else if (localName.equals("lemmaref")) {
			String cat  = attributs.getValue("cat");
			String name = attributs.getValue("name");
			currentLem = new Lemmaref(name, cat);
			currentMorph.addLemmaref(currentLem);
			currentFeats = new LinkedList<Fs>();
			currentFeat  = new LinkedList<String>();
		}
		else if (localName.equals("fs")) {
			Fs fs = new Fs(10);
			currentFeats.add(fs);
		}
		else if (localName.equals("f")) {
			String name = attributs.getValue("name");
			currentFeat.add(name);
		}
		else if (localName.equals("sym")) {
			Value val = null;
			if (attributs.getIndex("varname") != -1) {
				String varname = attributs.getValue("varname");
				String realVar = nf.getName(varname);
				val = new Value(Value.VAR, realVar);
			}
			else if (attributs.getIndex("value") != -1) {
				String cste = attributs.getValue("value");
				val = new Value(Value.VAL, cste);
			}
			if (currentADisj == null) {
				Fs feats = currentFeats.get(currentFeats.size() - 1);
				feats.setFeat(currentFeat.get(currentFeat.size() - 1), val);
			}
			else 
				currentADisj.add(val);				
		}
		else if (localName.equals("vAlt")) {
			currentADisj = new LinkedList<Value>();
		}
	}

	/**
	 * Evenement recu a chaque fermeture de balise.
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String nameSpaceURI, String localName, String rawName) throws SAXException {
		if ( ! "".equals(nameSpaceURI)) { // name space non null
			System.err.print(" Namespace detected : " + localName);
		}
		
		if (localName.equals("vAlt")) {
			Value val = new Value(currentADisj);
			Fs feats = currentFeats.get(currentFeats.size() - 1);
			feats.setFeat(currentFeat.get(currentFeat.size() - 1), val);
			currentADisj = null;
		}
		else if (localName.equals("fs")) {
			if (currentFeats.size() == 1) { // extern fs -> attaches to a lemmaref
				currentLem.setFeatures(currentFeats.get(currentFeats.size() - 1));
				currentFeats.remove(currentFeats.size() - 1);
			}
			else { // intern fs -> is a feature value
				Fs feats = currentFeats.get(currentFeats.size() - 1);
				String f = currentFeat.get(currentFeat.size() - 1);
				currentFeats.remove(currentFeats.size() - 1);
				currentFeats.get(currentFeats.size() - 1).setFeat(f, new Value(feats));
			}
		}
		else if (localName.equals("f")) {
			currentFeat.remove(currentFeat.size() - 1);
		}
	}

	/**
	 * for DATA
	 * @param ch characters
	 * @param start 1st character to process
	 * @param end last character to process
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int end) throws SAXException {
		//if (verbose)
			//System.err.println("#PCDATA : " + new String(ch, start, end));
	}

	/**
	 * @param ch characters
	 * @param start 
	 * @param end 
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] ch, int start, int end) throws SAXException {
		//if (verbose)
			//System.err.println(" useless white spaces : ..." + new String(ch, start, end) +  "...");
	}

	/**
	 * @param target 
	 * @param data 
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
	 */
	public void processingInstruction(String target, String data) throws SAXException {
	}

	/**
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	public void skippedEntity(String arg0) throws SAXException {
	}

	public Locator getLocator() {
		return locator;
	}

	public Map<String, List<MorphEntry>> getMorphs() {
		return morphs;
	}
	
}
