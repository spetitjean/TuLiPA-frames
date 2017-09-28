/*
 *  File MyEntityResolver.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:26:34 CEST 2007
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
package de.tuebingen.util;

import java.io.InputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MyEntityResolver implements EntityResolver {
	
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException { 
		//System.out.println("DTD: "+systemId);
		if (systemId.endsWith("xmg-mctag.dtd,xml")) {
			InputStream is = getClass().getResourceAsStream("xmg-mctag.dtd,xml");
			return new InputSource(is);
		} else if (systemId.endsWith("xmg-tag.dtd,xml")) {
			InputStream is = getClass().getResourceAsStream("xmg-tag.dtd,xml");
			return new InputSource(is);
		} else if (systemId.endsWith("tulipa-forest.dtd,xml")) {
			InputStream is = getClass().getResourceAsStream("tulipa-forest.dtd,xml");
			return new InputSource(is);
		} else if (systemId.endsWith("rcg.dtd,xml")) {
			InputStream is = getClass().getResourceAsStream("rcg.dtd,xml");
			return new InputSource(is);
		} else if (systemId.endsWith("polarities.dtd,xml")) {
			InputStream is = getClass().getResourceAsStream("polarities.dtd,xml");
			return new InputSource(is);
		} else {
			return null;
		}
	}

}