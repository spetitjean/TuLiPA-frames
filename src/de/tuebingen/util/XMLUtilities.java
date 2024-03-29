/*
 *  File XMLUtilities.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     Yannick  Parmentier <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:28:56 CEST 2007
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLUtilities {

    /**
     * parse an XML file
     *
     * @param f          the XML file
     * @param validating toggle validation
     * @return the parsed document
     */
    public static Document parseXMLFile(File f, boolean validating) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            factory.setValidating(validating);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new MyEntityResolver());
            Document ldoc = builder.parse(f);
            ldoc.normalize();
            return ldoc;
        } catch (SAXParseException spe) {
            // Error generated by the parser
            System.err.println("\n** Parsing error" + ", line "
                    + spe.getLineNumber() + ", uri " + spe.getSystemId());
            System.err.println("   " + spe.getMessage());

        } catch (SAXException sxe) {
            // Error generated during parsing
            System.err.println("   " + sxe.getMessage());
        } catch (ParserConfigurationException e) {
            // Parser with specified options can't be built
            System.err.println("   " + e.getMessage());
        } catch (IOException e) {
            // I/O error
            System.err.println("   " + e.getMessage());
            // e.printStackTrace();
        }
        return null;
    }

    /**
     * If outfile is "stdout", print to stdout.
     *
     * @param document
     * @param outfile
     * @param dtd
     * @param system
     */
    public static void writeXML(Document document, String outfile, String dtd,
                                boolean system) {
        try {
            DOMSource domSource = new DOMSource(document);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            // tf.setAttribute("indent-number", new Integer(2)); //no more
            // available since using xalan

            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            // Now using xalan, the indent number is defined the following way:
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "2");
            if (system) {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dtd);
            } else {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, dtd);
            }
            transformer.transform(domSource, result);
            String stringResult = writer.toString();

            if (outfile.equals("stdout")) {
                System.out.println(stringResult);
            } else {
                FileWriter fw = new FileWriter(outfile);
                fw.write(stringResult);
                fw.close();
            }

        } catch (TransformerException e) {
            System.err.println("   " + e.getMessage());
        } catch (IOException e) {
            // I/O error
            System.err.println("   " + e.getMessage());
        } catch (Exception e) {
            System.err.println("   " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reference:
     * https://stackoverflow.com/questions/2325388/what-is-the-shortest-way-to-pretty-print-a-org-w3c-dom-document-to-stdout
     *
     * @param rootNode
     * @param spacer
     */
    public static void printNode(Node rootNode, String spacer) {
        System.out.println(spacer + rootNode.getNodeName() + " -> "
                + rootNode.getNodeValue());
        NodeList nl = rootNode.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++)
            printNode(nl.item(i), spacer + "   ");
    }

}
