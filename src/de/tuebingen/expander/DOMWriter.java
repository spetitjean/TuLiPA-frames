/*
 *  File DOMWriter.java
 *
 *  Authors:
 *     Johannes Dellert  <johannes.dellert@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Johannes Dellert, 2008
 *
 *  Last modified:
 *     Wed Jan 30 11:49:58 CET 2008
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
package de.tuebingen.expander;
/**
 * A collection of quick and functional methods to print out parts of DOM models as XML
 *
 * @author Johannes Dellert
 */

import org.w3c.dom.*;

public class DOMWriter {
    public static String documentToString(Document doc) {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n";
        xml += elementToString(doc.getDocumentElement(), 0);
        return xml;
    }

    public static String elementToString(Node rootNode, int depth) {
        if (rootNode.getNodeName().equals("narg")) {
            return "";
        }
        String xml = spacing(depth) + "<" + rootNode.getNodeName();
        if (rootNode.getAttributes() != null) xml += attributesToString(rootNode.getAttributes());
        if (rootNode.getChildNodes().getLength() == 0) {
            xml += "/>\n";
        } else {
            xml += ">\n";
            NodeList childList = rootNode.getChildNodes();
            for (int i = 0; i < childList.getLength(); i++) {
                Node child = childList.item(i);
                if (child instanceof Element) {
                    xml += elementToString(child, depth + 1);
                }
            }
            xml += spacing(depth) + "</" + rootNode.getNodeName() + ">\n";
        }
        return xml;
    }

    public static String attributesToString(NamedNodeMap attrs) {
        String xml = "";
        for (int i = 0; i < attrs.getLength(); i++) {
            xml += " " + attributeToString((Attr) attrs.item(i));
        }
        return xml;
    }

    public static String attributeToString(Attr attrNode) {
        return attrNode.getName() + "=\"" + attrNode.getValue() + "\"";
    }

    public static String spacing(int depth) {
        String tabs = "";
        for (int i = 0; i < depth; i++) {
            tabs += "  ";
        }
        return tabs;
    }
}
