/*
 * @(#)XmlParser.java created 12.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.utool.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sun.security.pkcs.ParsingException;
import de.saar.basic.Logger;
import de.saar.basic.LoggingReader;
import de.saar.basic.XmlDecodingException;
import de.saar.basic.XmlEntities;
import de.saar.chorus.domgraph.GlobalDomgraphProperties;
import de.saar.chorus.domgraph.codec.CodecManager;
import de.saar.chorus.domgraph.codec.InputCodec;
import de.saar.chorus.domgraph.codec.MalformedDomgraphException;
import de.saar.chorus.domgraph.codec.OutputCodec;
import de.saar.chorus.domgraph.codec.ParserException;
import de.saar.chorus.domgraph.equivalence.EquationSystem;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.NodeLabels;
import de.saar.chorus.domgraph.utool.AbstractOptions;
import de.saar.chorus.domgraph.utool.AbstractOptionsParsingException;
import de.saar.chorus.domgraph.utool.ExitCodes;
import de.saar.chorus.domgraph.utool.AbstractOptions.Operation;

class XmlParser extends DefaultHandler {
    private CodecManager codecManager;
    private AbstractOptions options;
    private Logger logger;

    private final static String EOF_MESSAGE = "successfully finished parsing one utool element";

    public XmlParser(Logger logger) {
        super();
        
        this.logger = logger;
        
        codecManager = new CodecManager();
        codecManager.setAllowExperimentalCodecs(GlobalDomgraphProperties.allowExperimentalCodecs());
        registerAllCodecs(codecManager);
    }


    public AbstractOptions parse(BufferedReader xmlSource)
    throws AbstractOptionsParsingException {
        SAXParser saxParser;
        LoggingReader reader = null;
        
        options = new AbstractOptions();
        
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
            reader = new LoggingReader(new NonClosingBufferedReader(xmlSource), logger, "Received: ");
            saxParser.parse( new InputSource(reader), this );
        } catch (ParserConfigurationException e) {
            throw new AbstractOptionsParsingException("An error occurred while initialising the XML parser!", e, ExitCodes.PARSER_CONFIGURATION_ERROR);
        } catch (SAXException e) {
            if( EOF_MESSAGE.equals(e.getMessage())) {
                // Parsing finished successfully. This is an abuse of exceptions
                // to abort parsing once one complete UTOOL element has been read.
                
                // NOP
                
                reader.flushLog();
                
                
            } else if( (e.getException() != null) && (e.getException() instanceof AbstractOptionsParsingException) ) {
                throw (AbstractOptionsParsingException) e.getException();
            } else {
                throw new AbstractOptionsParsingException("An error occurred while parsing the input!", e, ExitCodes.PARSING_ERROR_INPUT_GRAPH);
            }
        } catch (IOException e) {
            throw new AbstractOptionsParsingException("An error occurred while reading the input!", e, ExitCodes.IO_ERROR);
        }
        
        if( options.getOperation().requiresInput && (options.getGraph() == null) ) {
            throw new AbstractOptionsParsingException("You must specify an input graph!", ExitCodes.NO_INPUT);
        }
        
        return options;
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        /* log the opening tag
        StringBuffer buf = new StringBuffer("<" + qName );
        for( int i = 0; i < attributes.getLength(); i++ ) {
            buf.append(" " + attributes.getQName(i) + "='" + attributes.getValue(i) + "'");
        }
        buf.append(">");
        logger.log(buf.toString());
        */
        
        if( qName.equals("utool") ) {
            String cmd = attributes.getValue("cmd");
    
            if( cmd != null ) {
                Operation op = resolveOperation(cmd);
                if( op == null ) {
                    if( "display-codecs".equals(cmd) ) {
                        options.setOperation(Operation._displayCodecs);
                        return;
                    } else if( "version".equals(cmd) ) {
                        options.setOperation(Operation._version);
                        return;
                    } else {
                        throw new SAXException(new AbstractOptionsParsingException("Unknown command: " + cmd, ExitCodes.NO_SUCH_COMMAND));                    
                    }
                }
                
                options.setOperation(op);
                
                if( op.requiresOutput ) {
                    if( attributes.getValue("output-codec") != null ) {
                        String outputCodecOptions = mydecode(attributes.getValue("output-codec-options"));
                        OutputCodec codec = codecManager.getOutputCodecForName(attributes.getValue("output-codec"), outputCodecOptions);
                        
                        if( codec == null ) {
                            throw new SAXException(new AbstractOptionsParsingException("Unknown output codec: " + attributes.getValue("output-codec"), ExitCodes.NO_SUCH_OUTPUT_CODEC));
                        } else {
                            options.setOutputCodec(codec);
                            if( outputCodecOptions != null ) {
                                options.setOutputCodecOptions(outputCodecOptions);
                            }
                        }
                    } else {
                        options.setOptionNoOutput(true);
                    }
                }
                
                if( "true".equalsIgnoreCase(attributes.getValue("nochart")) ) {
                    options.setOptionNochart(true);
                }
                
                if( op == Operation.help ) {
                    options.setHelpArgument(resolveOperation(attributes.getValue("on")));
                }
            } else {
                throw new SAXException(new AbstractOptionsParsingException("You must specify a command!", ExitCodes.NO_SUCH_COMMAND));
            }
            
            
        } else if( qName.equals("usr")) {
            if( attributes.getValue("codec") == null ) {
                throw new SAXException(new AbstractOptionsParsingException("You must specify an input codec for the USR!", ExitCodes.NO_INPUT_CODEC_SPECIFIED));
            }
            
            if( attributes.getValue("string") == null ) {
            	throw new SAXException(new AbstractOptionsParsingException("You must specify an USR!", ExitCodes.NO_INPUT));
            }
            
            
            // obtain input codec
            String inputCodecOptions = mydecode(attributes.getValue("codec-options"));
            String codecName = attributes.getValue("codec");
            InputCodec codec = codecManager.getInputCodecForName(codecName, inputCodecOptions);
            
            if( codec == null ) {
                throw new SAXException(new AbstractOptionsParsingException("Unknown input codec: " + codecName, ExitCodes.NO_SUCH_INPUT_CODEC));
            }
            
            if( inputCodecOptions != null ) {
                options.setInputCodecOptions(inputCodecOptions);
            }
            
            // obtain input graph
            DomGraph graph = new DomGraph();
            NodeLabels labels = new NodeLabels();
            try {
                String usr = mydecode(attributes.getValue("string"));
                //System.err.println("----- [USR] -----\n" + usr + "\n---------------");
                codec.decode(new StringReader(usr), graph, labels);
            } catch(MalformedDomgraphException e) {
                throw new SAXException(new AbstractOptionsParsingException("A semantic error occurred while decoding the graph.", 
                        e, ExitCodes.MALFORMED_DOMGRAPH_BASE_INPUT + e.getExitcode()));
            } catch (IOException e) {
                throw new SAXException(new AbstractOptionsParsingException("An I/O error occurred while reading the input.",
                        e, ExitCodes.IO_ERROR));
            } catch (ParserException e) {
                throw new SAXException(new AbstractOptionsParsingException("A parsing error occurred while reading the input.",
                        e, ExitCodes.PARSING_ERROR_INPUT_GRAPH));
            }
            
            if( attributes.getValue("name") != null ) {
                options.setInputName(attributes.getValue("name"));
            } else {
                options.setInputName("(graph from server)"); 
            }
            
            options.setGraph(graph);
            options.setLabels(labels);
        
        
        } else if( qName.equals("eliminate")) {
            try {
                EquationSystem eqs = new EquationSystem();
                eqs.read(new StringReader(XmlEntities.decode(attributes.getValue("equations"))));
                options.setOptionEliminateEquivalence(true);
                options.setEquations(eqs);
            } catch(Exception e) {
                throw new SAXException(new AbstractOptionsParsingException("An error occurred while reading the equivalences file!", e, ExitCodes.EQUIVALENCE_READING_ERROR));
            }
        }
    }
    
    
    @Override
    public void endElement(String uri, String localName, String qname) throws SAXException {
        //logger.log("</" + qname + ">");

        if( "utool".equals(qname)) {
            throw new SAXException(EOF_MESSAGE);
        }
    }


    private String mydecode(String x) throws SAXException {
        try {
            return XmlEntities.decode(x);
        } catch (XmlDecodingException e) {
            throw new SAXException(new AbstractOptionsParsingException("An XML entity could not be resolved.",
                    new ParsingException(e.getMessage()), ExitCodes.PARSING_ERROR_INPUT_GRAPH));
        }
    }


    private static Operation resolveOperation(String opstring) {
        if( opstring == null ) {
            return null;
        }
        
        for( Operation op : Operation.values() ) {
            String name = op.toString();
            
            if( !name.startsWith("_") && name.equals(opstring)) {
                return op;
            }
        }
        
        return null;
    }

    /*** codec management ***/
    
    private void registerAllCodecs(CodecManager codecManager) {
        try {
            codecManager.registerAllDeclaredCodecs();
        } catch(Exception e) {
            System.err.println("An error occurred trying to register a codec.");
            System.err.println(e + " (cause: " + e.getCause() + ")");

            System.exit(ExitCodes.CODEC_REGISTRATION_ERROR);
        }
        
    }










    public CodecManager getCodecManager() {
        return codecManager;
    }

    
    
    private static class NonClosingBufferedReader extends BufferedReader
    {
        public NonClosingBufferedReader(Reader i) {
            super(new BufferedReader(i));
        }

        public void close() {
        }
    }

}


/*
 * Unit tests:
 * - wrong element names
 * - missing attributes
 */
