/*
 * @(#)ExampleManager.java created 22.10.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * An example manager for facilitating access to example input files. 
 * This class allows the user to register example files and get
 * Reader objects for reading from them conveniently.<p>
 *
 * TODO: Right now, the example manager's behaviour is undefined
 * when there is more than one example with the same filename.
 *
 * @author Alexander Koller
 *
 */
public class ExampleManager extends DefaultHandler {
    private List<String> exampleNames;
    private Map<String,String> exampleDirectories;
    private Map<String,String> descriptions;
    private String currentDirectory;
    
    
    public ExampleManager() {
        exampleNames = new ArrayList<String>();
        descriptions = new HashMap<String,String>();
        exampleDirectories = new HashMap<String,String>();
    }
    
    
    /**
     * Returns the list of all registered example names.
     * 
     * @return
     */
    public List<String> getExampleNames() {
        return exampleNames;
    }
    
    /**
     * Returns the description for the example of the given name.
     * 
     * @param example
     * @return
     */
    public String getDescriptionForExample(String example) {
        return descriptions.get(example);
    }
    
    /**
     * Returns a Reader for the specified example file. This reader
     * looks for the file at its original pathname relative to the
     * classpath. It is robust towards packaging in a Jar or the use
     * of alternative class loaders, and provides the preferred way
     * of accessing an example file.
     * 
     * @param name
     * @return
     */
    public Reader getExampleReader(String name) {
        InputStream s = getExampleStream(name, exampleDirectories.get(name));
        
        if( s == null ) {
            return null;
        } else {
            return new InputStreamReader(s);
        }
    }
    
    private InputStream getExampleStream(String name, String directory) {    
    	return Thread.currentThread().getContextClassLoader().getResourceAsStream(directory + "/" + name);
    }
    

    /**
     * Adds all examples from a given example specification file. An 
     * example specification file is an XML file with the name "examples.xml"
     * in the given directory. It is assumed to have the following form:
     * <blockquote>
     *   &lt;examples&gt;
     *     &lt;example filename="..." description="..." /&gt;
     *   &lt;/examples&gt;
     * </blockquote>
     * 
     * Each <code>example</code> element specifies a filename and a description
     * for one example file. The filename is interpreted relative to the
     * directory which contains the "examples.xml" file.
     * 
     * @param directory a directory containing a file with the name "examples.xml".
     * @throws ParserException if a parsing error occurred while reading
     * the XML file.
     */
    public void addAllExamples(String directory) throws ParserException {
        // strip off trailing /
        while( directory.charAt(directory.length()-1) == '/' ) {
            directory = directory.substring(0, directory.length()-1);
        }
        
        currentDirectory = directory;
        
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            InputSource insrc = new InputSource(getExampleStream("examples.xml", directory));
        
            parser.parse(insrc, this);   
        } catch(IOException e) {

            // ignore -- this probably means that the examples.xml didn't exist here
        } catch(SAXException e) {
            throw new ParserException(e);
        } catch(ParserConfigurationException e) {
            throw new ParserException(e);
        }
    }
    
    /**
     * Adds an example to the registry.
     * 
     * @param filename 
     * @param directory 
     * @param description
     */
    public void addExample(String filename, String directory, String description) {
        if( getExampleStream(filename, directory) != null ) {
        	
            exampleNames.add(filename);
            descriptions.put(filename, description);
            exampleDirectories.put(filename, directory);
        } 
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if( "example".equals(qName)) {
            addExample(attributes.getValue("filename"), currentDirectory, attributes.getValue("description"));
           
        }
    }

    
    /**
     * A parser exception that occurred while reading an examples
     * specification file.
     * 
     * @author Alexander Koller
     *
     */
    public static class ParserException extends Exception {

        
		private static final long serialVersionUID = 2728280775395157375L;

		public ParserException() {
            super();
            // TODO Auto-generated constructor stub
        }

        public ParserException(String arg0, Throwable arg1) {
            super(arg0, arg1);
            // TODO Auto-generated constructor stub
        }

        public ParserException(String arg0) {
            super(arg0);
            // TODO Auto-generated constructor stub
        }

        public ParserException(Throwable arg0) {
            super(arg0);
            // TODO Auto-generated constructor stub
        }
        
        
    }

}
