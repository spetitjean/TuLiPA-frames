/*
 *  File TagContentHandler.java
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

// Class inspired by http://smeric.developpez.com/java/cours/xml/sax/

import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.LocatorImpl;

import fr.loria.disambiguation.Polarities;

public class TagContentHandler implements ContentHandler {

	private Locator locator;
	private Map<String, Polarities> polarities;
	private Map<String, List<String>> families;
	private boolean mode    = false; //LEFT := true or GLOBAL := false
	private boolean verbose = false;
	private String currentTree = null;
	private List<String> lexNodes;
	private int inGrammarSize = 0;
	
	public TagContentHandler(boolean v) {
		super();     
		locator = new LocatorImpl();
		verbose = v;
		polarities = new HashMap<String, Polarities>();
		lexNodes   = new LinkedList<String>();
		families   = new HashMap<String, List<String>>();
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
		
		if (localName.equals("polarities")) {
			// nothing to do
		} 
		else if (localName.equals("entry")) {
			inGrammarSize+=1;
			Polarities pol = new Polarities();
			String name = attributs.getValue("name");
			currentTree = name;
			polarities.put(name, pol);
		} 
		else if (localName.equals("family")) {
		} 
		else if (localName.equals("global")) {
			mode = !mode;
		} 
		else if (localName.equals("plus")) {
			String name = attributs.getValue("name");
			Polarities pol = polarities.get(currentTree);
			pol.setPol(name, name, Polarities.PLUS);
			if (mode)
				pol.setLeftPol(name, name, Polarities.PLUS);
		} 
		else if (localName.equals("minus")) {
			String name = attributs.getValue("name");
			Polarities pol = polarities.get(currentTree);
			pol.setPol(name, name, Polarities.MINUS);
			if (mode)
				pol.setLeftPol(name, name, Polarities.MINUS);
		} 
		else if (localName.equals("lex")) {
			String name = attributs.getValue("name");
			Polarities pol = polarities.get(currentTree);
			pol.setPol(name, name, Polarities.MINUS);
			lexNodes.add(name);
			if (mode)
				pol.setLeftPol(name, name, Polarities.MINUS);
		} 
		else if (localName.equals("anchor")) {
			mode = !mode;
		} 
		else if (localName.equals("coanchor")) {
			String name = attributs.getValue("name");
			Polarities pol = polarities.get(currentTree);
			pol.setPol(name, name, Polarities.MINUS);
			if (mode)
				pol.setLeftPol(name, name, Polarities.MINUS);			
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
		String data = new String(ch, start, end);
		List<String> trees = families.get(data);
		if (trees == null) {
			trees = new LinkedList<String>();
			families.put(data, trees);
		}
		trees.add(currentTree);
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

	public Map<String, Polarities> getPolarities() {
		return polarities;
	}

	public List<String> getLexNodes() {
		return lexNodes;
	}

	public Map<String, List<String>> getFamilies() {
		return families;
	}

	public int getInGrammarSize() {
		return inGrammarSize;
	}

}
