/*
 * @(#)DomconUdrawOutputCodec.java created 03.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.codec.domgraph;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org._3pq.jgrapht.Edge;

import de.saar.chorus.domgraph.codec.CodecMetadata;
import de.saar.chorus.domgraph.codec.CodecOption;
import de.saar.chorus.domgraph.codec.MalformedDomgraphException;
import de.saar.chorus.domgraph.codec.OutputCodec;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.EdgeType;
import de.saar.chorus.domgraph.graph.NodeLabels;
import de.saar.chorus.domgraph.graph.NodeType;

/**
 * An output codec that represents a dominance graph in the uDraw(Graph)
 * format. 
 * <a href="http://www.informatik.uni-bremen.de/uDrawGraph/en/index.html">uDraw(Graph)</a> 
 * is a tool for displaying and editing graphs which
 * was developed at the University of Bremen. 
 * <p>
 * 
 * This codec can be run into two modes. By default, it computes an uDraw
 * representation which is suitable for loading into uDraw(Graph) via
 * the File/Open menu. Alternatively, you can pass the boolean option "pipe"
 * to it. If "pipe" is set to true, the codec will compute a uDraw
 * graph drawing command which can be fed to uDraw(Graph) via a pipe, e.g.
 * <blockquote><code>utool convert -O domgraph-udraw --output-codec-options pipe | uDraw -pipe</code></blockquote>
 * <p>
 * 
 * <strong>Note:</strong> This codec is not suitable for printing
 * a list of graphs, e.g. in the context of printing the solved
 * forms computed by the <code>solve</code> command.<p>
 * 
 * The name of this codec is <code>domgraph-udraw</code>, and its associated
 * file extension is <code>.dg.udg</code>.
 * 
 * @author Alexander Koller
 *
 */
@CodecMetadata(name="domgraph-udraw", extension=".dg.udg")
public class DomgraphUdrawOutputCodec extends OutputCodec {
	private boolean pipe = false;
	
    public DomgraphUdrawOutputCodec(
            @CodecOption(name="pipe", defaultValue="false") boolean pipe) {
        this.pipe = pipe;
    }

    @Override
	public void encode(DomGraph graph, NodeLabels labels, Writer writer) throws IOException, MalformedDomgraphException 
	{
		Set<String> indeg0 = new HashSet<String>();
		
		for (String node : graph.getAllRoots()) {
			if (graph.indeg(node) == 0)
				indeg0.add(node);
		}
		
		encodeNodes(indeg0, graph, labels, writer);
		writer.flush();
	}
	
	private void encodeNodes(Set<String> roots, DomGraph graph, NodeLabels labels, Writer writer) throws IOException
	{
		boolean first = true;
		Set<String> refp = new HashSet<String>();
		
		for( String root : roots ) {
			if( !first ) {
				writer.write(",");
			} else {
				first = false;
			}
			encodeNode(root, graph, labels, refp, writer);
		}
	}
	
	private void encodeNode(String root, DomGraph graph, NodeLabels labels, Set<String> refp, Writer writer) throws IOException
	{
		if( !refp.add(root)) {
			writer.write("r(\"" + root + "\")");
		} else {
			writer.write("l(\"" + root + "\",n(\"\",[a(\"OBJECT\",\"" + root);
			
			if( graph.getData(root).getType() == NodeType.LABELLED ) {
				writer.write(":" + labels.getLabel(root));
			}
			writer.write("\")],[");
			encodeEdges(graph.getOutEdges(root, null), graph, labels, refp, writer);
			writer.write("]))");
		}
	}
	
	private void encodeEdges(List<Edge> edges, DomGraph graph, NodeLabels labels, Set<String> refp, Writer writer) throws IOException 
	{
		boolean first = true;
		
		for( Edge edge : edges) {
			if( !first ) {
				writer.write(",");
			} else {
				first = false;
			}
			
			encodeEdge(edge, graph, labels, refp, writer);
		}
	}
	
	private void encodeEdge(Edge edge, DomGraph graph, NodeLabels labels, Set<String> refp, Writer writer) throws IOException
	{
		if( graph.getData(edge).getType() == EdgeType.TREE ) {
			writer.append("e(\"\",[a(\"EDGEPATTERN\",\"solid\")],");
		} else {
			writer.write("e(\"\",[a(\"EDGEPATTERN\",\"dotted\")],");
		}
		encodeNode((String) edge.getTarget(), graph, labels, refp, writer);
		writer.write(")");
	}
	
	@Override
	public void print_header(Writer writer) throws IOException
	{
		if (pipe) {
			writer.write("graph(new([");
		} else {
			writer.write("[");
		}
	}
	
	@Override
	public void print_footer(Writer writer) throws IOException
	{
		if (pipe) {
			writer.write("]))\n");
		} else {
			writer.write("]\n");
		}
		writer.flush();
	}
	
}
