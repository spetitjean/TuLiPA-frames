/**
 * File TypeHierarchyReader.java
 * 
 * Authors:
 * David Arps <david.arps@hhu.de>
 * Simon Petitjean <petitjean@phil.hhu.de>
 * 
 * Copyright
 * David Arps, 2017
 * Simon Petitjean, 2017
 * 
 * This file is part of the TuLiPA-frames system
 * https://github.com/spetitjean/TuLiPA-frames
 * 
 * 
 * TuLiPA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * TuLiPA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package de.duesseldorf.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.duesseldorf.frames.Type;
import de.duesseldorf.frames.TypeHierarchy;
import de.tuebingen.util.XMLUtilities;

/**
 * Parse the file. Then walk through the parsed Type Hierarchy Document
 * according to its
 * specifications. Collect all the elementary types belonging to each type.
 * 
 * @author david
 */
public class XMLTypeHierarchyReader extends FileReader {

    private File tyHiFile;
    Document tyHiDoc;

    public XMLTypeHierarchyReader(File typeHierarchy)
            throws FileNotFoundException {
        super(typeHierarchy);
        this.tyHiFile = typeHierarchy;
        tyHiDoc = XMLUtilities.parseXMLFile(tyHiFile, false);
    }

    public TypeHierarchy getTypeHierarchy() {

        Element root = tyHiDoc.getDocumentElement();
        NodeList l = root.getElementsByTagName("hierarchy");
        Element e = (Element) l.item(0);
        NodeList entries = e.getElementsByTagName("entry");
        List<Type> typeCollector = getTypesfromNL(entries);

        return new TypeHierarchy(typeCollector);
    }

    private List<Type> getTypesfromNL(NodeList entries) {
        List<Type> typeCollector = new LinkedList<Type>();
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            NodeList entryChildren = entry.getElementsByTagName("ctype");

            Element cType = (Element) entryChildren.item(0);
            NodeList types = cType.getElementsByTagName("type");

            Set<String> eltypes = new HashSet<String>();
            for (int j = 0; j < types.getLength(); j++) {
                Element elType = (Element) types.item(j);
                String elt = elType.getAttribute("val");
                eltypes.add(elt);
            }
            Type t = new Type(eltypes);
            typeCollector.add(t);
        }
        return typeCollector;
    }

    /**
     * only testing...
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        File typeHierarchy = new File(
                "../../resources/verbs_frames_split_mg/more.mac");
        XMLTypeHierarchyReader reader = new XMLTypeHierarchyReader(
                typeHierarchy);

        System.out.println("File: " + typeHierarchy);
        System.out.println("type hierarchy XML file parsed");

        TypeHierarchy th = reader.getTypeHierarchy();
        reader.close();
        System.out.println(th.toString());

        System.out.println("Least specific subtypes: ");

        Set<String> s = new HashSet<String>();

        s.add("love");
        Type a = new Type(new HashSet<String>(s));
        // s.remove("love");
        s.add("causation");
        s.add("bla");
        Type b = new Type(new HashSet<String>(s));

        System.out.println("Type a: " + a.toString());
        System.out.println("Type b: " + b.toString());
        System.out
                .println("result: " + th.leastSpecificSubtype(a, b).toString());
    }
}
