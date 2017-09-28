/*
 * @(#)Chain.java created 26.01.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.codec.basic;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

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
 * An input codec that generates pure chains. Pure chains
 * are normal dominance graphs in which upper and lower fragments
 * alternate in a zig-zag shape (see e.g. <a href="http://www.coli.uni-saarland.de/~koller/showpaper.php?id=thesis">
 * Koller's Ph.D. thesis</a>, Chapter 6.4). They correspond to
 * the predicate-argument structure of (possibly nested) NPs
 * and (possibly transitive) verbs, and as such are a convenient
 * type of graphs for benchmarks.<p>
 * 
 * 
 * @author Alexander Koller
 *
 */
@CodecMetadata(name="chain", extension="")
public class Chain extends InputCodec {
    public static int ERROR_CHAIN_LENGTH = 1;
    
    public void decode(Reader inputStream, DomGraph graph, NodeLabels labels) 
    throws IOException, ParserException, MalformedDomgraphException {
        CharBuffer buf = CharBuffer.allocate(10);
        int chainLength = -1;
        String lengthAsString;
        
        inputStream.read(buf);
        
        buf.limit(buf.position());
        buf.rewind();
        lengthAsString = buf.toString();
                
        try {
            chainLength = Integer.parseInt(lengthAsString);
        } catch(NumberFormatException e) {
            throw new ParserException(e);
        }

        if( chainLength < 1 ) {
            throw new MalformedDomgraphException("You must specify a numeric chain length of at least 1!", ERROR_CHAIN_LENGTH);
        }
        
        makeChain(chainLength, graph, labels);
    }
    
    /** 
     * Determines a <code>Reader</code> from which the USR specified
     * by the <code>spec</code> will be read. In the context of the
     * <code>Chain</code> input codec, the <code>spec</code> must be
     * a string representing a number, which is taken to specify
     * the length of the chain to generate. (This is different than
     * the implementation in {@link de.saar.chorus.domgraph.codec.InputCodec},
     * where <code>spec</code> is read as a filename.)
     * 
     * @param spec a string specifying the length of the chain
     * @return a <code>StringReader</code> for the given string
     */
    public Reader getReaderForSpecification(String spec) {
        return new StringReader(spec);
    }

    private void makeChain(int length, DomGraph graph, NodeLabels labels) {
    	String upper_root, upper_lefthole, upper_righthole;
    	String lower;
        
        
        graph.clear();
    	
    	lower = "y0";
    	graph.addNode("y0", new NodeData(NodeType.LABELLED));
    	labels.addLabel("y0", "a0");
    	
    	for( int i = 1; i <= length; i++ ) {
    		// upper fragment
    		upper_root = "x" + i;
    		upper_lefthole = "xl" + i;
    		upper_righthole = "xr" + i;
    		
    		graph.addNode(upper_root, new NodeData(NodeType.LABELLED));
    		labels.addLabel(upper_root, "f" + i);
    		
    		graph.addNode(upper_lefthole, new NodeData(NodeType.UNLABELLED));
    		graph.addNode(upper_righthole, new NodeData(NodeType.UNLABELLED));
    		
    		graph.addEdge(upper_root, upper_lefthole, new EdgeData(EdgeType.TREE));
    		graph.addEdge(upper_root, upper_righthole, new EdgeData(EdgeType.TREE));
    		
    		// dominance edge to previous lower fragment
    		graph.addEdge(upper_lefthole, lower, new EdgeData(EdgeType.DOMINANCE));
    		
    		// lower fragment
    		lower = "y" + i;
    		graph.addNode(lower, new NodeData(NodeType.LABELLED));
        	labels.addLabel(lower, "a" + i);
    		
    		// dominance edge to new lower fragment
        	graph.addEdge(upper_righthole, lower, new EdgeData(EdgeType.DOMINANCE));
    	}
    }
    
}
