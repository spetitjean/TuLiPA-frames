/*
 * @(#)Codec.java created 25.01.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.codec.domgraph;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.saar.chorus.domgraph.codec.CodecMetadata;
import de.saar.chorus.domgraph.codec.InputCodec;
import de.saar.chorus.domgraph.codec.MalformedDomgraphException;
import de.saar.chorus.domgraph.codec.ParserException;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.EdgeData;
import de.saar.chorus.domgraph.graph.EdgeType;
import de.saar.chorus.domgraph.graph.NodeData;
import de.saar.chorus.domgraph.graph.NodeLabels;
import de.saar.chorus.domgraph.graph.NodeType;


/**
 * An input codec for weakly normal dominance graphs in GXL syntax.
 * <a href="http://www.gupro.de/GXL/">GXL</a> is a standard format
 * for representing graphs based on XML. Inputs of this codec specify
 * the nodes of the dominance graph, plus edges of type "dominance"
 * or "solid" connecting them.<p>
 * 
 * An example input looks as follows:<p>
 * {@code <gxl xmlns:xlink="http://www.w3.org/1999/xlink">}<br/>
 *   {@code <graph id="testgraph" edgeids="true" hypergraph="false" edgemode="directed">}<br/>
 *     {@code <node id="x">}<br/>
 *       {@code <type xlink:href="root" />}<br/>
 *       {@code <attr name="label"><string>f</string></attr>}<br/>
 *     {@code </node>}<br/>
 *     {@code <node id="y">}<br/>
 *       {@code <type xlink:href="hole" />}<br/>
 *     {@code </node>}<br/>
 *     {@code <node id="z">}<br/>
 *       {@code <type xlink:href="leaf" />}<br/>
 *       {@code <attr name="label"><string>a</string></attr>}<br/>
 *     {@code </node>}<br/>
 *     {@code <edge from="x" to="y" id="edge1">}<br/>
 *       {@code <type xlink:href="solid" />}<br/>
 *     {@code </edge>}<br/>
 *     {@code <edge from="y" to="z" id="edge2">}<br/>
 *       {@code <type xlink:href="dominance" />}<br/>
 *     {@code </edge>}<br/>
 *   {@code </graph>}<br/>
 * {@code </gxl>}
 * <p>
 * 
 *       
 * @author Alexander Koller
 *
 */
@CodecMetadata(name="domgraph-gxl", extension=".dg.xml")
public class DomgraphGxlInputCodec extends InputCodec {
 
    /**
     * Reads a GXL description of a dominance graph from a file and writes it
     * into a JDomGraph object. Any previous contents of the JDomGraph object
     * are deleted in the process. 
     * 
     * @param inputStream the stream from which we read the GXL document.
     * @param graph the graph into which we write the dominance graph we read.
     * @throws ParserConfigurationException if there was an internal error in the parser configuration.
     * @throws IOException if an error occurred while reading from the stream.
     * @throws SAXException if the input wasn't well-formed XML.
     */
    public void decode(Reader inputStream, DomGraph graph, NodeLabels labels)
            throws IOException, ParserException, MalformedDomgraphException {
        // set up XML parser
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc;
        
        try {
            db = dbf.newDocumentBuilder();

            // Parse the input file to get a Document object
            doc = db.parse(new InputSource(inputStream));
        } catch (Exception e) {
            throw new ParserException(e);
        }
        
        
        graph.clear();
        labels.clear();
        
        Element gxl = doc.getDocumentElement(); // First gxl element

        NodeList graph_list = gxl.getChildNodes();
        if (graph_list.getLength() == 0) {
            return;
        }

        for (int graph_index = 0; graph_index < graph_list.getLength(); graph_index++) {
            Node graph_node = graph_list.item(graph_index);
            if (graph_node.getNodeName().equals("graph")) {
                // the "graph" element in the XML file
                Element graph_elem = (Element) graph_node;
                
                /*
                // set graph id
                String graphId = getAttribute(graph_elem, "id");
                if( graphId != null )
                    graph.setName(graphId);
                    */
                
                NodeList list = graph_elem.getChildNodes();

                // Loop over all nodes
                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);  // a "node" or "edge" element
                    String id = getAttribute(node, "id");
                    String edgeOrNode = node.getNodeName();
                    String type = getType(node);
                    Map<String,String> attrs = getStringAttributes(node);

                    // TODO: check for unique ID
                    
                    if( edgeOrNode.equals("node") ) {
                        NodeData data;
                        
                        if( type.equals("hole") ) {
                            data = new NodeData(NodeType.UNLABELLED);
                        } else {
                            data = new NodeData(NodeType.LABELLED);
                            labels.addLabel(id, attrs.get("label"));
                        }

                        graph.addNode(id, data);

                        /*
                        // add popup menu
                        NodeList children = node.getChildNodes();
                        for (int j = 0; j < children.getLength(); j++) {
                            Node popupNode = children.item(j); 
                            if( popupNode.getNodeName().equals("popup") ) {
                                data.addMenuItem(getAttribute(popupNode, "id"),
                                                 getAttribute(popupNode, "label"));
                            }
                        }
                        */

                    }
                }
                
                // Loop over all edges
                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);  // a "node" or "edge" element
                    //String id = getAttribute(node, "id");
                    String edgeOrNode = node.getNodeName();
                    String type = getType(node);
                    //Map<String,String> attrs = getStringAttributes(node);
                        
                        
                    if( edgeOrNode.equals("edge")) {
                        EdgeData data;
                        
                        if( type.equals("solid")) {
                            data = new EdgeData(EdgeType.TREE);
                        } else {
                            data = new EdgeData(EdgeType.DOMINANCE);
                        }
                        
                        /*
                        // add popup menu
                        NodeList children = node.getChildNodes();
                        for (int j = 0; j < children.getLength(); j++) {
                            Node popupNode = children.item(j); 
                            if( popupNode.getNodeName().equals("popup") ) {
                                data.addMenuItem(getAttribute(popupNode, "id"),
                                                 getAttribute(popupNode, "label"));
                            }
                        }
                        */

                        String src = getAttribute(node, "from");
                        String tgt = getAttribute(node, "to");
                        
                        if( (src != null) && (tgt != null)) {
                            graph.addEdge(src, tgt, data);
                        }
                
                    }
                }
            }
        } // end of document loop
        
     //   graph.computeAdjacency();
    }

    /**
     * Retrieve the attribute with name "attr" from the XML node.
     * 
     * @param node a node in the XML document
     * @param attr the attribute whose value we want to retrieve
     * @return the attribute's value, or null if it is undefined.
     */
    private static String getAttribute(Node node, String attr) {
        if( node != null )
            if( node.getAttributes() != null )
                if( node.getAttributes().getNamedItem(attr) != null )
                    return node.getAttributes().getNamedItem(attr).getNodeValue();
                
        return null;
    }
    
    /**
     * Read the "type" attribute from a GXL node.
     * 
     * @param node the DOM node whose type attribute we want to read.
     * @return the node's type attribute, or null if it doesn't have one.
     */
    private static String getType(Node node) {
        NodeList children = node.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            Node type = children.item(j); 
            if( type.getNodeName().equals("type") ) {
                return getAttribute(type, "xlink:href");
            }
        }
        
        return null;        
    }
    
    /**
     * Read the string-valued attributes of a GXL node into a Map.
     * The resulting map will have one key/value pair for each attribute/value
     * pair in the GXL representation. The method will ignore all attributes
     * whose value types aren't "string".
     * 
     * @param node the DOM node whose attributes we want to read. 
     * @return a Map of attribute/value pairs.
     */
    private static Map<String,String> getStringAttributes(Node node) {
        Map<String,String> ret = new HashMap<String,String>();
        
        NodeList children = node.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            Node attr = children.item(j); // an "attr" element which is a child
                                          // of the node
            if( attr.getNodeName().equals("attr") ) {
                String key = getAttribute(attr, "name");
                NodeList values = attr.getChildNodes(); // a "string" element
                                                        // which is a child of
                                                        // attr
                for (int k = 0; k < values.getLength(); k++) {
                    if (values.item(k).getNodeName().equals("string")) {
                        Node labelNode = values.item(k).getFirstChild();
                        if (labelNode != null)
                            ret.put(key, labelNode.getNodeValue());
                    }
                }
            }
        }
        
        return ret;
    }

    
    

}
