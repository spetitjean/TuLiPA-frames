/*
 *  File TreeFilter.java
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

import java.util.*;

import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class TreeFilter extends XMLFilterImpl {

    private List<String> toFilter; // list of trees to consider
    private boolean eraseMode;

    public TreeFilter(List<String> subgrammar) {
        toFilter = subgrammar;
        eraseMode = false;
    }

    public void startElement(String namespaceUri, String localName, String qualifiedName, Attributes attributes) throws SAXException {
        if (!eraseMode) {
            if (localName.equals("entry")) {
                String treeId = attributes.getValue("name");
                if (toFilter.contains(treeId)) {
                    super.startElement(namespaceUri, localName, qualifiedName, attributes);
                } else
                    eraseMode = !eraseMode;  // eraseMode switched on
            } else
                super.startElement(namespaceUri, localName, qualifiedName, attributes);
        }
        // no else since in erase mode, we do not keep anything
    }

    public void endElement(String namespaceUri, String localName, String qualifiedName) throws SAXException {
        if (localName.equals("entry")) {
            if (!eraseMode)
                super.endElement(namespaceUri, localName, qualifiedName);
            else
                eraseMode = !eraseMode; // eraseMode switched off
        } else {
            // we only keep end tags in non erase mode
            if (!eraseMode)
                super.endElement(namespaceUri, localName, qualifiedName);
        }
    }

    public void setToFilter(List<String> toFilter) {
        this.toFilter = toFilter;
    }

}
