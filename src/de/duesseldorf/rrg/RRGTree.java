package de.duesseldorf.rrg;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.tree.Node;

public class RRGTree {

    // representation of the syntactic tree
    private Node root;
    private Map<RRGNode, String> lexNodes; // all lexical nodes

    public RRGTree(Node root) {
        this.root = root;
        retrieveLexNodes();
    }

    public Node getRoot() {
        return root;
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
     * Look for a gorn address in a tree.
     * 
     * @param address
     * @return The RRGNode that you have been looking for, or {@code null} if it
     *         is not in the tree.
     */
    public RRGNode findNode(GornAddress address) {

        try {
            // System.out.println("addi: " + address);
            Iterator<Integer> it = address.addressIterator();
            RRGNode nextNode = (RRGNode) root;
            while (it.hasNext()) {
                int nexti = it.next();
                nextNode = (RRGNode) nextNode.getChildren().get(nexti);
            }
            return nextNode;
        } catch (Exception e) {
            // debug
            // System.out.println("no node " + address + " found for mother "
            // + nextNode.getGornaddress() + " in\n" + toString());
            return null;
        }
    }

    /**
     * 
     * @return a map with all the lexical nodes and their lexical elements
     */
    public Map<RRGNode, String> getLexNodes() {
        return lexNodes;
    }

    /**
     * prints the syntax tree from the root downwards
     */
    @Override
    public String toString() {
        return RRGTreeTools.recursivelyPrintNode(root);
    }

}
