package de.duesseldorf.rrg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.tree.Node;

public class RRGTree {

    // representation of the syntactic tree
    private Node root;
    private Map<String, Set<RRGNode>> lexNodes; // all lexical nodes
    private Map<String, Set<RRGNode>> substNodes; // all substitution nodes
    private RRGNode ddaughter; // only one ddaughter is allowed!
    private String id;

    public RRGTree(Node root, String id) {
        this.root = root;
        this.id = id;
        retrieveSpecialNodes();
    }

    /**
     * When creating a {@code RRGTree }object, store pointers to lex and subst
     * nodes and the ddaughter in order to access them later
     */
    private void retrieveSpecialNodes() {
        // inits
        this.lexNodes = new HashMap<String, Set<RRGNode>>();
        this.substNodes = new HashMap<String, Set<RRGNode>>();

        retrieveSpecialNodes((RRGNode) root);

    }

    /**
     * recursive retrieval, use {@code retrieveSpecialNodes()} without
     * parameters to trigger retrieval
     * 
     * @param root
     */
    private void retrieveSpecialNodes(RRGNode root) {
        // add lexical nodes
        if (root.getType().equals(RRGNodeType.LEX)) {
            if (lexNodes.get(root.getCategory()) == null) {
                Set<RRGNode> lexNodeswithCat = new HashSet<RRGNode>();
                lexNodeswithCat.add(root);
                lexNodes.put(root.getCategory(), lexNodeswithCat);
            } else {
                lexNodes.get(root.getCategory()).add(root);
            }
        } else if (root.getType().equals(RRGNodeType.SUBST)) { // add
                                                               // substitution
                                                               // nodes
            if (substNodes.get(root.getCategory()) == null) {
                Set<RRGNode> substNodeswithCat = new HashSet<RRGNode>();
                substNodeswithCat.add(root);
                substNodes.put(root.getCategory(), substNodeswithCat);
            } else {
                substNodes.get(root.getCategory()).add(root);
            }
        } else if (root.getType().equals(RRGNodeType.DDAUGHTER)) {
            if (this.ddaughter == null) {
                this.ddaughter = root;
            } else {
                System.err.println(
                        "Problem while processing tree: more than one d-daughter in"
                                + toString());
            }
        }

        for (Node daughter : root.getChildren()) {
            retrieveSpecialNodes((RRGNode) daughter);
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
    public Map<String, Set<RRGNode>> getLexNodes() {
        return lexNodes;
    }

    /**
     * 
     * @return a map with all the substitution nodes and their categories
     */
    public Map<String, Set<RRGNode>> getSubstNodes() {
        return substNodes;
    }

    /**
     * 
     * @return the ddaughter if there is one, or {@code null} if there is
     *         none.
     */
    public RRGNode getDdaughter() {
        return ddaughter;
    }

    /**
     * 
     * @return the root node of the tree
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Always keep in mind that this Id is only unique in the XML input grammar.
     * Later, it might reappear several times!
     * 
     * @return
     */
    public String getId() {
        return this.id;
    }

    /**
     * 
     * @return <code>true</code> if the root node of this tree is a
     *         <code>STAR</code> node
     *         made for sister
     *         adjunction, <code>false</code> otherwise
     */
    public boolean isSisAdjTree() {
        return ((RRGNode) root).getType().equals(RRGNodeType.STAR);
    }

    /**
     * prints the syntax tree from the root downwards
     */
    @Override
    public String toString() {
        String beiwerk = "ID: " + id + "\n";
        return beiwerk + RRGTreeTools.recursivelyPrintNode(root);
    }

}
