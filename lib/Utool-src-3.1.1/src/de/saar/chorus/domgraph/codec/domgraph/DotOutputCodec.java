/*
 * @(#)DotOutputCodec.java created 04.04.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.codec.domgraph;

import java.io.IOException;
import java.io.Writer;

import org._3pq.jgrapht.Edge;

import de.saar.chorus.domgraph.codec.CodecMetadata;
import de.saar.chorus.domgraph.codec.CodecOption;
import de.saar.chorus.domgraph.codec.MalformedDomgraphException;
import de.saar.chorus.domgraph.codec.OutputCodec;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.NodeLabels;

/**
 * An output codec that represents a dominance graph using the Dot
 * graph description language. <a href="http://www.graphviz.org/">Dot</a> 
 * is a standard language which is supported by many tools.<p>
 * 
 * <strong>Note:</strong> This codec is not suitable for printing
 * a list of graphs, e.g. in the context of printing the solved
 * forms computed by the <code>solve</code> command.<p>
 * 
 * The name of this codec is <code>domgraph-dot</code>, and its
 * associated filename extension is <code>.dg.dot</code>.
 * 
 * @author Alexander Koller
 *
 */
@CodecMetadata(name="domgraph-dot", extension=".dg.dot")
public class DotOutputCodec extends OutputCodec {
	private boolean enforceEdgeOrder;
	
	public DotOutputCodec(@CodecOption(name="enforceEdgeOrder", defaultValue="false") boolean enf) {
		enforceEdgeOrder = enf;
	}
	
    @Override
    public void encode(DomGraph graph, NodeLabels labels, Writer writer)
            throws IOException, MalformedDomgraphException {
        
        writer.write("digraph domgraph {\n");
        
        if( enforceEdgeOrder ) {
        	writer.write("ordering=out;\n");
        }
        
        for( String node : graph.getAllNodes() ) {
            switch(graph.getData(node).getType()) {
            case LABELLED:
            	writer.write("node [shape=plaintext];\n");
                writer.write("   " + node + " [label=\"" + labels.getLabel(node) + "\"];\n");
                break;
                
            case UNLABELLED:
            	writer.write("node [shape=point, label=\"\"];\n");
                writer.write("   " + node + ";\n");
                break;
            }
        }
        
        for( Edge edge : graph.getAllEdges() ) {
            switch(graph.getData(edge).getType() ) {
            case TREE:
                writer.write("   " + edge.getSource() + " -> " + edge.getTarget() + " [arrowhead=nonenone];\n");
                break;
                
            case DOMINANCE:
                writer.write("   " + edge.getSource() + " -> " + edge.getTarget() + " [arrowhead=nonenone, style=dotted];\n");
                break;
            }
        }
        
        writer.write("}\n");
        writer.flush();
    }

    @Override
    public void print_header(Writer writer) throws IOException {
    }

    @Override
    public void print_footer(Writer writer) throws IOException {
    }


}
