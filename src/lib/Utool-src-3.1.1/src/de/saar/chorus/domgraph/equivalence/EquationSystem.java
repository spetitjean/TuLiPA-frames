/*
 * @(#)Equations.java created 06.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.equivalence;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A representation for a term equation system. Such an equation
 * system can be used as input for the redundancy elimination
 * algorithm.
 * 
 * @author Alexander Koller
 *
 */
public class EquationSystem extends DefaultHandler {
    private Collection<Equation> equations;
    private Collection<FragmentWithHole> wildcards;
    
    // for XML parsing
    private List<FragmentWithHole> currentEquivalenceGroup;
    private FragmentWithHole currentEquivalencePartner;
    
    public EquationSystem() {
        super();
        
        equations = new HashSet<Equation>();
        wildcards = new HashSet<FragmentWithHole>();
        
        currentEquivalenceGroup = null;
        currentEquivalencePartner = null;
        
    }
    
    /**
     * Add an equation between two label-hole pairs.
     * 
     * @param fh1 a label-hole pair
     * @param fh2 another label-hole pair
     */
    public void add(FragmentWithHole fh1, FragmentWithHole fh2) {
        equations.add(new Equation(fh1,fh2));
    }
    
    /**
     * Add equations between any two members of a collection
     * of label-hole pairs.
     * 
     * @param fhs a collection of label-hole pairs
     */
    public void addEquivalenceClass(Collection<FragmentWithHole> fhs) {
        for( FragmentWithHole fh1 : fhs ) {
            for( FragmentWithHole fh2 : fhs) {
                add(fh1, fh2);
            }
        }
    }
    
    /**
     * Remove all equations from this equation system.
     */
    public void clear() {
        equations.clear();
    }
    
    /**
     * Checks whether a given equation is contained in the
     * equation system. 
     * 
     * @param eq an equation
     * @return true iff this equation is contained in this equation system.
     */
    public boolean contains(Equation eq) {
        return wildcards.contains(eq.getQ1())
        || wildcards.contains(eq.getQ2())
        || equations.contains(eq);
    }
    
    /**
     * Returns the number of equations. 
     * 
     * @return the number of equations
     */
    public int size() {
        return equations.size();
    }
    
    /**
     * Reads an equation system from an XML specification. The specification
     * can use the following constructions:
     * <ul>
     * <li> Define a group of label-hole pairs that are equivalent with 
     *      each other:<br/>
     *      {@code <equivalencegroup>}<br/>
     *       &nbsp; {@code <quantifier label="a" hole="0" />} <br/>
     *       &nbsp; {@code <quantifier label="a" hole="1" />} <br/>
     *      {@code </equivalencegroup>}
     * <li> Define a single label-hole pair that is equivalent with
     * <i>everything</i> (useful for e.g. proper names):<br/>
     *      {@code <permutesWithEverything label="proper_q" hole="1" />}
     * </ul>
     * 
     * 
     * @param reader a reader from which the specification is read
     * @throws ParserConfigurationException if an error occurred while
     * configuring the XML parser
     * @throws SAXException if an error occurred while parsing
     * @throws IOException if an I/O error occurred while reading
     * from the reader.
     */
    public void read(Reader reader) 
    throws ParserConfigurationException, SAXException, IOException {
        SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        saxParser.parse( new InputSource(reader), this );
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if( qName.equals("equivalencegroup")) {
            currentEquivalenceGroup = new ArrayList<FragmentWithHole>();
        } else if( qName.equals("equivalencepartners")) {
            int hole = Integer.parseInt(attributes.getValue("hole"));
            currentEquivalencePartner =
                new FragmentWithHole(attributes.getValue("label"), hole);
        } else if( qName.equals("quantifier") ) {
            int hole = Integer.parseInt(attributes.getValue("hole"));
            FragmentWithHole fh =
                new FragmentWithHole(attributes.getValue("label"), hole);
            
            if( currentEquivalencePartner != null ) {
                add(currentEquivalencePartner, fh);
            } else if( currentEquivalenceGroup != null ) {
                currentEquivalenceGroup.add(fh);
            }
        } else if( qName.equals("permutesWithEverything")) {
            FragmentWithHole frag = 
                new FragmentWithHole(
                        attributes.getValue("label"),
                        Integer.parseInt(attributes.getValue("hole")));
            wildcards.add(frag);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if( qName.equals("equivalencegroup")) {
            addEquivalenceClass(currentEquivalenceGroup);
            currentEquivalenceGroup = null;
        } else if( qName.equals("equivalencepartners")) {
            currentEquivalencePartner = null;
        }
    }

    /**
     * Returns a string representation of this equation system.
     * 
     * @return a string representation
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for( Equation eq : equations ) {
            buf.append("  " + eq + "\n");
        }
        return buf.toString();
    }
    
}
