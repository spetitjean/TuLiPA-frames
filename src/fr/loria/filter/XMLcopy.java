/*
 *  File XMLcopy.java
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

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.tuebingen.util.MyEntityResolver;

public class XMLcopy {

    public static void produceOutXML(List<String> f, String uri, boolean verbose, String output) {
        try {
            XMLReader saxReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            TreeFilter tf = new TreeFilter(f);
            tf.setParent(saxReader);
            tf.setEntityResolver(new MyEntityResolver());
            tf.setContentHandler(new CopyContentHandler());
            System.setOut(new PrintStream(output, "utf8"));
            tf.parse(uri);
        } catch (SAXException e) {
            System.err.println("   " + e.getMessage());
        } catch (IOException e) {
            System.err.println("   " + e.getMessage());
        } catch (Exception e) {
            System.err.println("   " + e.getMessage());
        }
    }
}
