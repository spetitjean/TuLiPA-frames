package de.duesseldorf.rrg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.tag.Environment;
import de.tuebingen.tree.Node;

/**
 * File RRGTree.java
 * 
 * Authors:
 * David Arps <david.arps@hhu.de>
 * 
 * Copyright
 * David Arps, 2018
 * 
 * 
 * This file is part of the TuLiPA-frames system
 * https://github.com/spetitjean/TuLiPA-frames
 * 
 * 
 * TuLiPA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * TuLiPA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
public class RRGTree implements Comparable<RRGTree> {

    // representation of the syntactic tree
    protected Node root;
    private Map<String, Set<RRGNode>> lexNodes; // all lexical nodes
    private Map<String, Set<RRGNode>> substNodes; // all substitution nodes
    private RRGNode anchorNode; // all anchor nodes
    private List<RRGNode> ddaughters; // only one ddaughter is allowed!
    protected String id;

    private Environment env;
    /**
     * is initialized as empty string if no family is set through the method
     * setFamily, e.g. from the XMLRRGReader
     */
    private String family;

    public RRGTree(Node root, String id) {
        this.root = new RRGNode.Builder((RRGNode) root).build();
        this.id = id;
        this.setEnv(new Environment(0));
        this.family = "";

        retrieveSpecialNodes();
    }

    public RRGTree(RRGTree tree) {
        this.root = new RRGNode.Builder((RRGNode) tree.getRoot()).build();
        this.id = tree.id;
        this.setEnv(tree.getEnv());
        this.family = tree.getFamily();
        // System.out.println("TODO ENVIRONMENT IN RRGTREE");
        retrieveSpecialNodes();
    }

    /**
     * When creating a {@code RRGTree }object, store pointers to lex and subst
     * nodes and the ddaughter in order to access them later
     */
    public void retrieveSpecialNodes() {
        // inits
        this.lexNodes = new HashMap<String, Set<RRGNode>>();
        this.anchorNode = null;
        this.substNodes = new HashMap<String, Set<RRGNode>>();
        this.ddaughters = new LinkedList<>();

        retrieveSpecialNodes((RRGNode) root);

    }

    /**
     * recursive retrieval, use {@code retrieveSpecialNodes()} without
     * parameters to trigger retrieval
     * 
     * @param root
     */
    public void retrieveSpecialNodes(RRGNode root) {
        // add lexical nodes
        if (root.getType().equals(RRGNodeType.LEX)) {
            this.addLexNode(root);
        } else if (root.getType().equals(RRGNodeType.ANCHOR)) {
            anchorNode = root;
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
            // System.out.println("ddaughters length: " + ddaughters.size());
            this.ddaughters.add(root);
        }
        for (Node daughter : root.getChildren()) {
            retrieveSpecialNodes((RRGNode) daughter);
        }
    }

    /**
     * add a new lexical node @param node to the map of lex nodes
     */
    private void addLexNode(RRGNode node) {
        if (lexNodes.get(node.getCategory()) == null) {
            Set<RRGNode> lexNodeswithCat = new HashSet<RRGNode>();
            lexNodeswithCat.add(node);
            lexNodes.put(node.getCategory(), lexNodeswithCat);
        } else {
            lexNodes.get(node.getCategory()).add(node);
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
     * @return the ddaughters if there are some, or {@code null} if there is
     *         none.
     */
    public List<RRGNode> getDdaughters() {
        return ddaughters;
    }

    /**
     * 
     * @return the anchor node in the tree
     */
    public RRGNode getAnchorNode() {
        return anchorNode;
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

    public void setId(String id) {
        this.id = id;
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
     * hashCode does not use the special node maps: If they are different, the
     * root with its daughters are different too.
     */
    @Override
    public int hashCode() {
        return Objects.hash(root);
    }

    @Override
    public boolean equals(Object obj) {
        if (this != null && obj != null && obj instanceof RRGTree) {
            return this.hashCode() == obj.hashCode();
        }
        return false;
    }

    /**
     * ATTENTION! Really not a good implementation, but probably enough to order
     * trees in a set
     */
    @Override
    public int compareTo(RRGTree o) {
        int compIDs = id.compareTo(o.getId());
        if (compIDs != 0) {
            return compIDs;
        }
        return ((RRGNode) root).compareTo(((RRGNode) o.getRoot()));
    }

    /**
     * prints the syntax tree from the root downwards
     */
    @Override
    public String toString() {
        String beiwerk = "ID: " + id + "\n";
        return beiwerk + RRGTreeTools.recursivelyPrintNode(root);
    }

    public Environment getEnv() {
        return env;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getFamily() {
        if (this.family == null) {
            this.family = "";
        }
        return this.family;
    }

    public void addLexNodeToAnchor(RRGNode node) {
        anchorNode.addRightmostChild(node);
        this.addLexNode(node);
    }
}
