/**
 * File TypeHierarchyReader.java
 * <p>
 * Authors:
 * David Arps <david.arps@hhu.de>
 * Simon Petitjean <petitjean@phil.hhu.de>
 * <p>
 * Copyright
 * David Arps, 2017
 * Simon Petitjean, 2017
 * <p>
 * This file is part of the TuLiPA-frames system
 * https://github.com/spetitjean/TuLiPA-frames
 * <p>
 * <p>
 * TuLiPA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * TuLiPA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import org.w3c.dom.Node;

import de.duesseldorf.frames.Type;
import de.duesseldorf.frames.TypeConstraint;
import de.duesseldorf.frames.TypeHierarchy;
import de.duesseldorf.frames.UnifyException;
import de.duesseldorf.frames.Value;
import de.duesseldorf.frames.HierarchyConstraint;
import de.duesseldorf.frames.ConstraintLiteral;
import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.tag.Environment;
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
	NodeList hc = root.getElementsByTagName("type_constraints");
	NodeList hc_entries = hc.item(0).getChildNodes();
        List<HierarchyConstraint> hierarchyConstraints = getHierarchyConstraintsfromNL(hc_entries);
        TypeHierarchy result = new TypeHierarchy(typeCollector, hierarchyConstraints);
        return result;
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
            // if (!constraints.isEmpty()) {
            // System.out.println("created type constraints: " + constraints);
            // }
            Type t = new Type(eltypes, constraints);
            // if (!constraints.isEmpty()) {
            // System.out.println("created type : " + t);
            // }
            typeCollector.add(t);
        }
	//System.out.println(typeCollector);
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

            List<String> attrsInPathParsed = new LinkedList<String>();
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
            Value.Kind valType = valString.startsWith("@") ? Value.Kind.VAR
                    : Value.Kind.VAL;
            Value val = new Value(valType, nf.getName(valString));

            // find the type
            String typeString = ((Element) constraint
                    .getElementsByTagName("type").item(0)).getAttribute("val");
            Value.Kind typeType = typeString.startsWith("@") ? Value.Kind.VAR
                    : Value.Kind.VAL;
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
	//System.out.println("typeConstraints:");
	//System.out.println(typeConstraints);
        return typeConstraints;
    }

    private List<String> getPath(Element litteral){
	Element path = litteral;
	if (path.getTagName() != "path")
	    path = (Element) litteral.getElementsByTagName("path")
		.item(0);
	List<String> attrsInPathParsed = new LinkedList<String>();
	NodeList attrsInPath = path.getElementsByTagName("attr");
	for (int j = 0; j < attrsInPath.getLength(); j++) {
	    attrsInPathParsed.add(
				  ((Element) path.getElementsByTagName("attr")
				   .item(j)).getAttribute("val"));
	}
	return attrsInPathParsed;
    }

    private List<String> getCType(Element litteral){
	Element path = litteral;
	if (path.getTagName() != "ctype"){
	    if(litteral.getElementsByTagName("ctype").getLength() > 0){
		path = (Element) litteral.getElementsByTagName("ctype")
		    .item(0);
	    }
	    else return null;
	}
	List<String> attrsInPathParsed = new LinkedList<String>();
	NodeList attrsInPath = path.getElementsByTagName("type");
	for (int j = 0; j < attrsInPath.getLength(); j++) {
	    attrsInPathParsed.add(
				  ((Element) path.getElementsByTagName("type")
				   .item(j)).getAttribute("val"));
	}
	return attrsInPathParsed;
    }

    private List<List<String>> getPaths(Element litteral){
        NodeList paths = litteral.getElementsByTagName("path");
	List<List<String>> result = new LinkedList<List<String>>();
	if(paths == null)
	    return result;
	for (int i = 0 ; i < paths.getLength(); i++){
	    result.add(getPath((Element) (paths.item(i))));
	}
	return result;
    }

    private List<ConstraintLiteral> getConstraintLiterals(NodeList literals){
	List<ConstraintLiteral> result = new LinkedList<ConstraintLiteral>();
	for (int i = 0; i < literals.getLength(); i++){
	    if (literals.item(i).getNodeType() == Node.ELEMENT_NODE){
		Element current = (Element)literals.item(i);
		String constraintType = current.getNodeName();
		ConstraintLiteral new_literal = null;
		// create the literal depending on its type (type_constraint, )
		if (constraintType == "type_constraint"){
		    new_literal = new ConstraintLiteral(getCType(current));
		}
		else if (constraintType == "path_identity"){
		    new_literal = new ConstraintLiteral(getPaths(current).get(0), getPaths(current).get(1));
		}
		else if (constraintType == "attr_type"){
		    new_literal = new ConstraintLiteral(getPath(current), getCType(current), null);
		}
		else{
		    System.out.println("Constraint literal type not supported: "+constraintType);
		}
		if (new_literal != null){
		    result.add(new_literal);
		}
	    }
	}
	return result; 
    }
    
    private List<HierarchyConstraint> getHierarchyConstraintsfromNL(NodeList entries) {
	List<HierarchyConstraint> constraints = new LinkedList<HierarchyConstraint>();
	for (int i = 0; i < entries.getLength(); i++) {
	    if (entries.item(i).getNodeType() != Node.ELEMENT_NODE)
		continue;
	    Element elem = (Element)entries.item(i);
	    String constraintType = elem.getTagName();
	    Element antecedent = (Element)(elem
					   .getElementsByTagName("antecedent").item(0));
	    Element consequent = (Element)(elem
					   .getElementsByTagName("consequent").item(0));
	    NodeList antecedents = antecedent.getChildNodes();
	    NodeList consequents = consequent.getChildNodes();
	    
	   
	    HierarchyConstraint newConstraint = new HierarchyConstraint(getConstraintLiterals(antecedents), getConstraintLiterals(consequents));
	    constraints.add(newConstraint);
	    System.out.println(newConstraint);
	    // for (int j = 0; j<antecedents.getLength(); j++){
	    // 	if (antecedents.item(j).getNodeType() == Node.ELEMENT_NODE){
	    // 	    Node current = antecedents.item(j);
		    
	    // 	    System.out.println((antecedents.item(j).getNodeName()));
	    // 	}
	    // }
	    // if(constraintType == "type_constraint"){
	    // 	ConstraintLiteral left = new ConstraintLiteral(getCType(antecedent));
	    // 	ConstraintLiteral right = new ConstraintLiteral(getCType(consequent));
	    // 	constraints.add(new HierarchyConstraint(left, right));
	    // 	//System.out.println(new HierarchyConstraint(left, right));
	    // }
	    // else if (constraintType == "type_to_path_constraint"){
	    // 	ConstraintLiteral left = new ConstraintLiteral(getCType(antecedent));
	    // 	ConstraintLiteral right = new ConstraintLiteral(getPaths(consequent).get(0), getPaths(consequent).get(1));
	    // 	constraints.add(new HierarchyConstraint(left, right));
	    // 	//System.out.println(new HierarchyConstraint(left, right));
	       
	    // }
	    // else if (constraintType == "type_to_attr_constraint"){
	    // 	ConstraintLiteral left = new ConstraintLiteral(getCType(antecedent));
	    // 	ConstraintLiteral right = new ConstraintLiteral(getPath(consequent), getCType(consequent), null);
	    // 	constraints.add(new HierarchyConstraint(left, right));
	    // 	//System.out.println(new HierarchyConstraint(left, right));

	    // }
	    // else if (constraintType == "attr_to_path_constraint"){
	    // 	ConstraintLiteral left = new ConstraintLiteral(getPath(antecedent), getCType(antecedent), null);
	    // 	ConstraintLiteral right = new ConstraintLiteral(getPaths(consequent).get(0), getPaths(consequent).get(1));
	    // 	constraints.add(new HierarchyConstraint(left, right));
	    // 	//System.out.println(new HierarchyConstraint(left, right));
	    // }
	    // else if (constraintType == "attr_to_attr_constraint"){
	    // 	ConstraintLiteral left = new ConstraintLiteral(getPath(antecedent), getCType(antecedent), null);
	    // 	ConstraintLiteral right = new ConstraintLiteral(getPath(consequent), getCType(consequent), null);
	    // 	constraints.add(new HierarchyConstraint(left, right));
	    // 	//System.out.println(new HierarchyConstraint(left, right));

	    // }
	    // else{
	    // 	System.out.println("Unsupported constraint:");
	    // 	System.out.println(elem.getTagName() );
	    // }
	     
	    
	}
	System.out.println("Finished!");
	return constraints;
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
