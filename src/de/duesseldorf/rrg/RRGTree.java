package de.duesseldorf.rrg;

import java.util.HashMap;
import java.util.Map;

import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.tuebingen.tree.Node;

public class RRGTree {

    // representation of the syntactic tree
    private Node root;
    private Map<RRGNode, String> lexNodes; // all lexical nodes

    public RRGTree(Node root) {
        this.root = root;
        retrieveLexNodes();
    }

    private void retrieveLexNodes() {

        this.lexNodes = new HashMap<RRGNode, String>();
        retrieveLexNodes((RRGNode) root);
    }

    private void retrieveLexNodes(RRGNode root) {
        if (root.getType().equals(RRGNodeType.LEX)) {
            lexNodes.put(root, root.getCategory());
        }
        for (Node daughter : root.getChildren()) {
            retrieveLexNodes((RRGNode) daughter);
        }
    }

    /**
     * 
     * @return a map with all the lexical nodes and their lexical elements
     */
    public Map<RRGNode, String> getLexNodes() {
        return lexNodes;
    }

    @Override
    public String toString() {
        return RRGTreeTools.recursivelyPrintNode(root);
    }

}
