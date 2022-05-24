/*
 *  File TransformPolarity.java
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.tuebingen.util.MyEntityResolver;


public class TransformPolarity {

    public void xsltprocess(String[] args) throws TransformerException, TransformerConfigurationException, FileNotFoundException, IOException {
        // 1. Instantiate a TransformerFactory.
        SAXTransformerFactory tFactory = (SAXTransformerFactory) TransformerFactory.newInstance();

        // 2. Use the TransformerFactory to process the stylesheet Source and
        //    generate a Transformer.
        InputStream is = getClass().getResourceAsStream("xmg2pol.xsl");
        Transformer transformer = tFactory.newTransformer(new StreamSource(is));
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "polarities.dtd,xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");

        // 3. Use the Transformer to transform an XML Source and send the
        //    output to a Result object.
        try {
            String input = args[0];
            String output = args[1];
            SAXSource saxs = new SAXSource(new InputSource(input));
            XMLReader saxReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            saxReader.setEntityResolver(new MyEntityResolver());
            saxs.setXMLReader(saxReader);
            transformer.transform(saxs, new StreamResult(new OutputStreamWriter(new FileOutputStream(output), "utf-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
