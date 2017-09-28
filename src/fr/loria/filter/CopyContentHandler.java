/*
 *  File CopyContentHandler.java
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
package fr.loria.filter;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class CopyContentHandler extends DefaultHandler {
	
	private String space = "  ";
	private int level = 0;
	private boolean ok = false;

	public void startDocument() {
		String tmp = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
		tmp += "<!DOCTYPE grammar SYSTEM \"xmg-tag.dtd,xml\">\n";
		System.out.print(tmp);
	}

	public void endDocument() {
	}
		  
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		this.printSpaces();
		if (localName.equals("entry"))
			ok = true;
		String tmp = "<" + localName;
		for (int i = 0; i < attributes.getLength(); i++) {
			tmp += " " + attributes.getLocalName(i) + "=\"" + attributes.getValue(i) + "\"";
		}
		if (localName.equals("family"))
			tmp += ">";
		else if (localName.equals("class"))
			tmp += ">";
		else
			tmp += ">\n";
		System.out.print(tmp);
		level+=2;
	}
		  
	public void endElement(String uri, String localName, String qName) {
		level-=2;
		if (!localName.equals("family") && !localName.equals("class")) 
			this.printSpaces();
		String tmp = "</" + localName + ">\n";
		System.out.print(tmp);
		if (localName.equals("entry"))
			ok = false;
	}

	public void characters(char[] ch, int start, int length) {
		if (ok) {
			String tmp = new String(ch, start, length);
			System.out.print(tmp);
		}
	}

	public void ignorableWhitespace(char[] ch, int start, int length) {
		//System.out.print(new String(ch, start, length));
	}
	
	private void printSpaces() {
		// method used for indentation
		String res = "";
		for (int i = 0 ; i < level ; i++) {
			res += space;
		}
		System.out.print(res);
	}
	
}
