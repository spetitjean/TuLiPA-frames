/*
 * @(#)TermOutputCodec.java created 25.01.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.codec.term;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org._3pq.jgrapht.Edge;

import de.saar.chorus.domgraph.codec.CodecTools;
import de.saar.chorus.domgraph.codec.MalformedDomgraphException;
import de.saar.chorus.domgraph.codec.MultiOutputCodec;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.EdgeType;
import de.saar.chorus.domgraph.graph.NodeLabels;
import de.saar.chorus.domgraph.graph.NodeType;

/**
 * A generic output codec for terms. This codec assumes that the
 * graph that is to be output is 
 * <ul>
 * <li> normal;
 * <li> leaf-labelled;
 * <li> a simple solved form.
 * </ul>
 * 
 * That is, the graph must be a forest, and a node has an outgoing dominance
 * edge iff it is a hole (and <i>exactly</i> one dominance edge in this case).
 * Such solved forms can be output as terms; this is what this codec does.<p>
 * 
 * This codec can be used to compute terms both in Oz and in Prolog syntax.
 * The difference between these two concrete syntaxes is that Prolog inserts
 * a comma between subterms, whereas Oz uses whitespace for the same purpose.
 *  
 *  
 * @author Alexander Koller
 *
 */
public class TermOutputCodec extends MultiOutputCodec {
    public static final int ERROR_NOT_SIMPLE_SOLVED_FORM = 1;
    
    protected String separator;
    
    /**
     * Construct a new term output codec with the given subterm
     * separator (e.g. "," for Prolog, " " for Oz).
     *  
     * @param separator the subterm separator
     */
    public TermOutputCodec(String separator) {
        super();
        this.separator = separator;
    }

    @Override
    public void encode(DomGraph graph, NodeLabels labels, Writer writer)
            throws IOException, MalformedDomgraphException {
        Map<String,String> domEdges = new HashMap<String,String>(); // top -> bottom
        List<String> terms = new ArrayList<String>();
        boolean first = true;
        
        // check whether graph is in simple solved form
        if( !graph.isSimpleSolvedForm() || !graph.isLeafLabelled() || !graph.isNormal() ) {
            throw new MalformedDomgraphException("Graph must be a leaf-labelled simple normal solved form",
                    ERROR_NOT_SIMPLE_SOLVED_FORM);
        }

        // build dom-edge map
        for( Edge e : graph.getAllEdges() ) {
            if( graph.getData(e).getType() == EdgeType.DOMINANCE ) {
                domEdges.put((String) e.getSource(), (String) e.getTarget());
            }
        }
        
        // compute top nodes
        for( String node : graph.getAllNodes() ) {
            if( graph.indeg(node) == 0 ) {
                terms.add(computeTerm(node, graph, labels, domEdges));
            }
        }
        
        // output the whole term
        if( terms.size() == 1 ) {
            //System.err.println("write: " + terms.get(0));
            //System.err.println("writer = " + writer);
            //writer.write("hallo\n");
            writer.write(terms.get(0));
        } else {
            writer.write("top" + terms.size() + "(");
            for( String str : terms ) {
                if( first ) {
                    first = false;
                } else {
                    writer.write(separator);
                }
                
                writer.write(str);
            }
            writer.write(")");
        }

	writer.flush();
    }

    protected String computeTerm(String node, DomGraph graph, NodeLabels labels, Map<String, String> domEdges) {
        boolean first = true;
        
        if( graph.getData(node).getType() == NodeType.UNLABELLED ) {
            return computeTerm(domEdges.get(node), graph, labels, domEdges);
        } else {
            String label = labels.getLabel(node);
            StringBuilder ret = new StringBuilder(CodecTools.atomify(label));
            
            if( graph.outdeg(node) > 0 ) {
                ret.append("(");
                for( Edge e : graph.getOutEdges(node, EdgeType.TREE) ) {
                    if( first ) {
                        first = false;
                    } else {
                        ret.append(separator);
                    }
                    
                    ret.append(computeTerm((String) e.getTarget(), graph, labels, domEdges));
                }
                ret.append(")");
            }
            
            return ret.toString();
        }
    }

    @Override
    public void print_header(Writer writer) {
    }

    @Override
    public void print_footer(Writer writer) {
    }

    @Override
    public void print_start_list(Writer writer) throws IOException {
    	writer.write("[");
    }

    @Override
    public void print_end_list(Writer writer) throws IOException {
    	writer.write("]");
    }

    @Override
    public void print_list_separator(Writer writer) throws IOException {
        writer.write(separator + "\n");
    }

}
