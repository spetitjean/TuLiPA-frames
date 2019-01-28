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
import de.duesseldorf.frames.TypeConstraint;
import de.duesseldorf.frames.TypeHierarchy;
import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.tag.Environment;
import de.tuebingen.tag.UnifyException;
import de.tuebingen.tag.Value;
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

            // retrieve the elementary types
            NodeList cTypesForEntry = entry.getElementsByTagName("ctype");

            Element cType = (Element) cTypesForEntry.item(0);
            NodeList types = cType.getElementsByTagName("type");

            Set<String> eltypes = new HashSet<String>();
            for (int j = 0; j < types.getLength(); j++) {
                Element elType = (Element) types.item(j);
                String elt = elType.getAttribute("val");
                eltypes.add(elt);
            }

            // retrieve the constraints
            NodeList constraintsForEntry = entry
                    .getElementsByTagName("constraints");
            Set<TypeConstraint> constraints = retrieveConstraints(
                    constraintsForEntry);
            if (!constraints.isEmpty()) {
                System.out.println("created type constraints: " + constraints);
            }
            Type t = new Type(eltypes, constraints);
            typeCollector.add(t);
        }
        return typeCollector;
    }

    /**
     * a type constraint can look like this
     * <constraint>
     * <attr val="theme"/>
     * <type val="@1"/>
     * <val val="@0"/>
     * </constraint>
     * 
     * The attribute val in the type element can also be a single elementary
     * type.
     * The attribute val in the val element cal also be a String.
     * If the constraint considers more than one attribute, these are collected
     * under a path element like so:
     * 
     * <constraint>
     * <path>
     * <attr val="initial_state"/>
     * <attr val="theme"/>
     * </path>
     * ...
     * 
     * @param constraintsForEntry
     * @return
     */
    private Set<TypeConstraint> retrieveConstraints(
            NodeList constraintsForEntry) {
        Set<TypeConstraint> typeConstraints = new HashSet<TypeConstraint>();
        NodeList constraints = ((Element) constraintsForEntry.item(0))
                .getElementsByTagName("constraint");
        NameFactory nf = new NameFactory();
        for (int i = 0; i < constraints.getLength(); i++) {
            Element constraint = (Element) constraints.item(i);

            Set<String> attrsInPathParsed = new HashSet<String>();
            // find the attr or path
            // the path set also holds the single attr at the moment
            if (constraint.getElementsByTagName("path").getLength() > 0) {

                Element path = (Element) constraint.getElementsByTagName("path")
                        .item(0);
                NodeList attrsInPath = path.getElementsByTagName("attr");
                for (int j = 0; j < attrsInPath.getLength(); j++) {
                    attrsInPathParsed.add(
                            ((Element) constraint.getElementsByTagName("attr")
                                    .item(j)).getAttribute("val"));
                }
            } else {
                String attr = ((Element) constraint.getElementsByTagName("attr")
                        .item(0)).getAttribute("val");
                attrsInPathParsed.add(attr);
            }
            // find the val
            String valString = ((Element) constraint.getElementsByTagName("val")
                    .item(0)).getAttribute("val");
            int valType = valString.startsWith("@") ? Value.VAR : Value.VAL;
            Value val = new Value(valType, nf.getName(valString));

            // find the type
            String typeString = ((Element) constraint
                    .getElementsByTagName("type").item(0)).getAttribute("val");
            int typeType = typeString.startsWith("@") ? Value.VAR : Value.VAL;
            Value typeVal = typeString.startsWith("@")
                    ? new Value(typeType, nf.getName(typeString))
                    : new Value(typeType, nf.getUniqueName());

            Set<String> elemTypes = new HashSet<String>();
            if (!typeString.startsWith("@")) {
                elemTypes.add(typeString);
            }
            Type type = new Type(elemTypes, typeVal);

            typeConstraints
                    .add(new TypeConstraint(attrsInPathParsed, type, val));
        }
        return typeConstraints;
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
        try {
            System.out.println("result: " + th
                    .leastSpecificSubtype(a, b, new Environment(4)).toString());
        } catch (UnifyException e) {
            e.printStackTrace();
        }
    }
}
