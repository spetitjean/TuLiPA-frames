/*
 *  File DependencyExtractor.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 11:08:37 CEST 2007
 *
 *  This file is part of the TuLiPA system
 *     http://www.sfb441.uni-tuebingen.de/emmy-noether-kallmeyer/tulipa
 *
 *  TuLiPA is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TuLiPA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.tuebingen.dependency;

import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tuebingen.tag.TagNode;
import de.tuebingen.tag.TagTree;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.util.Pair;
import de.tuebingen.expander.ParseTreeHandler;

public class DependencyExtractor {

    public static final int LEFT = 1;  // for "local" dependency (co-anc, etc) computation
    public static final int RIGHT = 2;  // idem

    private Map<String, List<Integer>> inputSentence; // needed for dependency on co-anchor and lex nodes
    private Document derivationTrees;
    private List<Map<Integer, Dependency>> dependences;
    private Map<String, TagTree> subgrammar;

    public DependencyExtractor(List<Word> tokens, Document d, Map<String, TagTree> sg, boolean needsAnchoring) {
        inputSentence = new Hashtable<String, List<Integer>>();
        derivationTrees = ParseTreeHandler.extractDerivationTrees(d);
        dependences = new LinkedList<Map<Integer, Dependency>>();
        subgrammar = sg;
        for (int i = 0; i < tokens.size(); i++) {
            String w = tokens.get(i).getWord();
            List<Integer> positions = inputSentence.get(w);
            if (positions == null) {
                positions = new LinkedList<Integer>();
                inputSentence.put(w, positions);
            }
            positions.add(tokens.get(i).getEnd());
        }
    }

    public void processAll() {
        try {
            NodeList startNodes = derivationTrees.getElementsByTagName("start");
            for (int i = 0; i < startNodes.getLength(); i++) {
                Node startNode = startNodes.item(i);
                Map<Integer, Dependency> dep = processDTree(startNode);
                if (dep.size() > 0) {
                    dependences.add(dep);
                }
            }
        } catch (Exception e) {
            System.err.println("Error while extracting dependencies: ");
            System.err.println(e.toString());
            StackTraceElement[] stack = e.getStackTrace();
            for (int i = 0; i < stack.length; i++) {
                System.err.println(stack[i]);
            }
        }
    }

    public Map<Integer, Dependency> processDTree(Node dTree) {
        Map<Integer, Dependency> dep = new HashMap<Integer, Dependency>();
        // Tuple to position mapping
        Map<String, Integer> tupleMap = new HashMap<String, Integer>();
        // here starts the hard part
        try {
            Document D = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Node initialTree = D.importNode(dTree.getLastChild().getFirstChild(), true);
            D.appendChild(initialTree);
            //rebuild tree nodes
            NodeList treeNodes = D.getElementsByTagName("tree");
            ArrayList<Node> tNodes = new ArrayList<Node>();
            for (int l = 0; l < treeNodes.getLength(); l++) {
                tNodes.add(treeNodes.item(l));
            }
            for (int i = 0; i < tNodes.size(); i++) {
                Node treeNode = tNodes.get(i);
                String treeid = treeNode.getAttributes().getNamedItem("id").getNodeValue();
                TagTree tree = subgrammar.get(treeid);
                int posi = this.getPosition(tree, tupleMap);
                // we process the "local" dependencies, ie co-anchors or lexical items
                this.getPositionsForTree(tree, posi, dep);

                if (treeNode.getParentNode().getParentNode() != null) {
                    // ie it is not the root node
                    // we retrieve the operation's Gorn address
                    String opNode = treeNode.getParentNode().getAttributes().getNamedItem("node").getNodeValue();
                    // we retrieve the parent
                    String parent = treeNode.getParentNode().getParentNode().getAttributes().getNamedItem("id").getNodeValue();
                    //System.err.println(treeid + " --> " + parent);
                    TagTree ptree = subgrammar.get(parent);
                    // we retrieve the node cat:
                    List<de.tuebingen.tree.Node> ln = new LinkedList<de.tuebingen.tree.Node>();
                    ptree.findNode(ptree.getRoot(), opNode, ln);
                    TagNode n = ln.size() > 0 ? (TagNode) ln.get(0) : null;
                    String cat = "";
                    if (n != null) {
                        cat = n.getCategory();
                    }
                    int parentPosi = this.getPosition(ptree, tupleMap);
                    if (parentPosi != posi && tree.getIsHead()) {
                        //System.err.println(" Dependency detected " + posi + " " + parentPosi);
                        dep.put(posi, new Dependency(parentPosi, cat));
                    }
                } else {
                    // otherwise
                    //System.err.println(" Dependency detected " + posi + " " + 0);
                    dep.put(posi, new Dependency(0, "ROOT"));
                }
            }
        } catch (ParserConfigurationException e) {
            System.err.println("Error while extracting dependencies: ");
            System.err.println(e.toString());
            StackTraceElement[] stack = e.getStackTrace();
            for (int i = 0; i < stack.length; i++) {
                System.err.println(stack[i]);
            }
        }
        //System.err.println(" ------ " );
        return dep;
    }

    public int getPosition(TagTree tree, Map<String, Integer> tupleMap) {
        // retrieve the position of tree
        String tuple = tree.getTupleId();
        int posi = 0;
        if (tree.getIsHead()) { // head tree
            posi = ((TagNode) tree.getLexAnc()).getWord().getEnd();
            // we update the tuple to position mapping
            if (!(tupleMap.containsKey(tuple))) {
                tupleMap.put(tuple, posi);
            }
        } else { // auxiliary tree
            if (tupleMap.containsKey(tuple)) {
                // we retrieve its position in the mapping
                posi = tupleMap.get(tuple);
            } else {
                System.err.println(" Argument tree with unknown head " + tuple);
                System.exit(1);
            }
        }
        return posi;
    }

    public void getPositionsForTree(TagTree tree, int position, Map<Integer, Dependency> dep) {
        Map<String, Pair> lexAd = new HashMap<String, Pair>();
        tree.findAllLex(tree.getRoot(), lexAd);

        Set<String> keys = lexAd.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String word = it.next();
            String wgorn = (String) lexAd.get(word).getKey();
            String cat = (String) lexAd.get(word).getValue();

            if (tree.getIsHead()) {
                // if we are processing a head (ie an anchored tree)
                String ancgorn = ((TagNode) tree.getLexAnc()).getAddress();
                if (wgorn.compareTo(ancgorn) < 0) {
                    int lexPos = this.getPositionForLex(word, position, LEFT);
                    if (lexPos != -1) {
                        dep.put(lexPos, new Dependency(position, cat));
                    }
                } else if (wgorn.compareTo(ancgorn) < 0) {
                    int lexPos = this.getPositionForLex(word, position, RIGHT);
                    if (lexPos != -1) {
                        dep.put(lexPos, new Dependency(position, cat));
                    }
                } // no final else -> skip the main anchor
            } else {
                // we are processing an argument tree
                if (tree.isLeftAdj()) {
                    int lexPos = this.getPositionForLex(word, position, LEFT);
                    if (lexPos != -1) {
                        dep.put(lexPos, new Dependency(position, cat));
                    }
                } else if (tree.isRightAdj()) {
                    int lexPos = this.getPositionForLex(word, position, RIGHT);
                    if (lexPos != -1) {
                        dep.put(lexPos, new Dependency(position, cat));
                    }
                }
                // no else, for wrapping adjunctions, we do not know
                // the position of lexical item
            }
        }
    }

    public int getPositionForLex(String word, int limit, int side) {
        // retrieves the position of a lexical item according to the position
        // of the anchor of its tree
        int res = -1;
        List<Integer> possiblePos = new LinkedList<Integer>();
        List<Integer> pos = inputSentence.get(word);
        switch (side) {
            case LEFT:
                for (int i = 0; i < pos.size() && pos.get(i) < limit; i++) {
                    possiblePos.add(pos.get(i));
                }
                break;
            case RIGHT:
                for (int i = pos.size(); i >= 0 && pos.get(i) > limit; i++) {
                    possiblePos.add(pos.get(i));
                }
                break;
            default://skip
        }
        // if there is no ambiguity about the dependency:
        if (possiblePos.size() == 1) {
            res = possiblePos.get(0);
        }
        return res;
    }

    public Map<String, List<Integer>> getInputSentence() {
        return inputSentence;
    }

    public void setInputSentence(Map<String, List<Integer>> inputSentence) {
        this.inputSentence = inputSentence;
    }

    public Document getDerivationTrees() {
        return derivationTrees;
    }

    public void setDerivationTrees(Document derivationTrees) {
        this.derivationTrees = derivationTrees;
    }

    public List<Map<Integer, Dependency>> getDependences() {
        return dependences;
    }

    public void setDependences(List<Map<Integer, Dependency>> dependences) {
        this.dependences = dependences;
    }

    public Map<String, TagTree> getSubgrammar() {
        return subgrammar;
    }

    public String toString() {
        String res = "";
        for (int u = 0; u < getDependences().size(); u++) {
            res += "Derivation n°" + (u + 1) + "\n\t";
            Map<Integer, Dependency> dep = getDependences().get(u);
            Set<Integer> keys = dep.keySet();
            Iterator<Integer> it = keys.iterator();
            while (it.hasNext()) {
                Integer id = it.next();
                Integer head = dep.get(id).getHead();
                res += "id " + id + " head " + head + "\n\t";
            }
            res += "\n";
        }
        return res;
    }

}
