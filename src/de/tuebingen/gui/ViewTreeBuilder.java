/*
 *  File ViewTreeBuilder.java
 *
 *  Authors:
 *     Johannes Dellert  <johannes.dellert@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Johannes Dellert, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:50:52 CEST 2007
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
package de.tuebingen.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.duesseldorf.frames.FsTools;
import de.tuebingen.derive.DerivedTree;
import de.tuebingen.derive.ElementaryTree;
import de.tuebingen.gui.tree.view.TreeViewNode;
import de.tuebingen.tag.Fs;
import de.tuebingen.tag.SemLit;
import de.tuebingen.tag.TagTree;

public class ViewTreeBuilder {
    /*
     * rebuilds XML model for derivation tree output and turns it into an
     * XMLViewTree for display
     */
    public static XMLViewTree makeViewableDerivationTree(Node derivTree,
            Map<String, TagTree> dict) throws Exception {
        Document D = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .newDocument();
        Node initialTree = D
                .importNode(derivTree.getLastChild().getFirstChild(), true);
        D.appendChild(initialTree);
        // rebuild feature structures (append them as attributes to tree nodes
        // above)
        NodeList nargNodes = D.getElementsByTagName("narg");
        while (nargNodes.getLength() > 0) {
            Node nargNode = nargNodes.item(0);
            ArrayList<Node> atts = extractFeatures(nargNode, D);
            System.out.println(
                    "Node List length in ViewTreeBUilder: " + atts.size());
            System.out.println("nargNode: " + nargNode.toString());
            for (Node att : atts) {
                System.out.println(att.toString());
                nargNode.getParentNode().getParentNode().getAttributes()
                        .setNamedItem(att);
            }
            nargNode.getParentNode().removeChild(nargNode);
        }
        // rebuild tree nodes
        NodeList treeNodes = D.getElementsByTagName("tree");
        ArrayList<Node> tNodes = new ArrayList<Node>();
        for (int l = 0; l < treeNodes.getLength(); l++) {
            tNodes.add(treeNodes.item(l));
        }
        for (int i = 0; i < tNodes.size(); i++) {
            Node treeNode = tNodes.get(i);
            String id = treeNode.getAttributes().getNamedItem("id")
                    .getNodeValue();
            // prevent display of disambiguation tags in derivation tree
            /*
             * if (id.indexOf("__") >= 0)
             * {
             * id = id.substring(0,id.indexOf("__"));
             * treeNode.getAttributes().getNamedItem("id").setNodeValue(id);
             * }
             */
            // ----------------------------
            // added for renaming trees accordingly
            treeNode.getAttributes().getNamedItem("id")
                    .setNodeValue(dict.get(id).getOriginalId());
            // ----------------------------
            if (treeNode.getParentNode().getParentNode() != null) {
                String opType = treeNode.getParentNode().getNodeName();
                String opNode = treeNode.getParentNode().getAttributes()
                        .getNamedItem("node").getNodeValue();
                Node op = treeNode.getOwnerDocument().createAttribute("op");
                op.setNodeValue(opType);
                treeNode.getAttributes().setNamedItem(op);
                Node opnode = treeNode.getOwnerDocument()
                        .createAttribute("op-node");
                opnode.setNodeValue(opNode);
                treeNode.getAttributes().setNamedItem(opnode);
                Node grandfather = treeNode.getParentNode().getParentNode();
                grandfather.removeChild(treeNode.getParentNode());
                grandfather.appendChild(treeNode);
            }

        }
        // draw anchor nodes into trees
        NodeList anchorNodes = D.getElementsByTagName("anchor");
        while (anchorNodes.getLength() > 0) {
            Node anchorNode = anchorNodes.item(0);
            String value = anchorNode.getAttributes().getNamedItem("lex")
                    .getNodeValue();
            Node attr = D.createAttribute("anchor");
            attr.setNodeValue(value);
            Node parent = anchorNode.getParentNode();
            parent.getAttributes().setNamedItem(attr);
            parent.removeChild(anchorNode);
        }
        // draw lexical nodes into trees
        NodeList lexicalNodes = D.getElementsByTagName("lexical");
        while (lexicalNodes.getLength() > 0) {
            Node lexicalNode = lexicalNodes.item(0);
            String value = lexicalNode.getAttributes().getNamedItem("lex")
                    .getNodeValue();
            Node attr = D.createAttribute("lexical");
            attr.setNodeValue(value);
            Node parent = lexicalNode.getParentNode();
            parent.getAttributes().setNamedItem(attr);
            parent.removeChild(lexicalNode);
        }

        return makeDerivationTree(initialTree);
    }

    private static XMLViewTree makeDerivationTree(Node node) {
        XMLViewTree viewTree = createViewTree(node);
        viewTree.description = node.getAttributes().getNamedItem("id")
                .getNodeValue();
        // change the tags to display a correct derivation tree
        for (TreeViewNode n : viewTree.treeNodes.values()) {
            if (n.tag.equals("tree")) {
                List<XMLViewTreeAttribute> nattr = viewTree.getAttrs(n.id);
                for (int i = 0; i < nattr.size(); i++) {
                    XMLViewTreeAttribute a = nattr.get(i);
                    if (a.name.equals("id")) {
                        if (n.tag.equals("tree")) {
                            n.tag = a.value;
                        } else {
                            n.tag = a.value + ": " + n.tag;
                        }
                        nattr.remove(a);
                        i--;
                    } else if (a.name.equals("op-node")) {
                        n.setEdgeTag(a.value);
                        nattr.remove(a);
                        i--;
                    } else if (a.name.equals("anchor")) {
                        if (n.tag.equals("tree")) {
                            n.tag = a.value;
                        } else {
			     n.tag = n.tag + ": " + a.value;
                        }
                        nattr.remove(a);
                        i--;
                    } else if (a.name.equals("op")) {
                        if (a.value.equals("subst")) {
                            n.setEdgeDir("down");
                        } else {
                            n.setEdgeDir("up");
                        }
                        nattr.remove(a);
                        i--;
                    }
                }
                if (nattr.size() > 0) {
                    viewTree.collapsedAttributes.add(n.id);
                }
            }
        }
        viewTree.createNodeLayers();
        viewTree.setTreeNodesDistance(250);
        viewTree.setTreeLevelHeight(100);
        viewTree.calculateCoordinates();
        return viewTree;
    }

    public static XMLViewTree createViewTree(Document dom) {
        return createViewTree(dom.getDocumentElement());
    }

    public static XMLViewTree createViewTree(Node xmlNode) {
        XMLViewTree tree = new XMLViewTree();

        TreeViewNode root = new TreeViewNode(tree.id++, -1,
                new ArrayList<Integer>(), retrieveTag(xmlNode), 100, 50);
        tree.treeNodes.put(0, root);
        tree.domNodes.put(0, xmlNode);
        tree.rootID = 0;
        handleAttributes(tree, xmlNode, 0);

        createSubtreeStructure(tree, xmlNode, root);
        return tree;
    }

    private static void createSubtreeStructure(XMLViewTree tree, Node xmlNode,
            TreeViewNode node) {
        for (int i = 0; i < xmlNode.getChildNodes().getLength(); i++) {
            Node currentChild = xmlNode.getChildNodes().item(i);
            if (contentNode(currentChild)) {
                TreeViewNode childNode = new TreeViewNode(tree.id++, node.id,
                        new ArrayList<Integer>(), retrieveTag(currentChild),
                        node.x, node.y + tree.getTreeLevelHeight());
                node.x += tree.getTreeNodesDistance();
                tree.treeNodes.put(childNode.id, childNode);
                tree.domNodes.put(childNode.id, currentChild);
                node.children.add(childNode.id);

                handleAttributes(tree, currentChild, childNode.id);

                createSubtreeStructure(tree, currentChild, childNode);
            }
        }
    }

    private static void handleAttributes(XMLViewTree tree, Node xmlNode,
            int nodeID) {
        if (xmlNode.getAttributes() != null) {
            for (int j = 0; j < xmlNode.getAttributes().getLength(); j++) {
                XMLViewTreeAttribute attr = new XMLViewTreeAttribute();
		attr.name = xmlNode.getAttributes().item(j).getNodeName();
		String val=xmlNode.getAttributes().item(j).getNodeValue();
		if(val.startsWith("_V_")){
		    attr.value=val.substring(3);
		    }
		else{
		    attr.value=val;
		}
		tree.addAttr(nodeID, attr);
            }
        }
    }

    private static boolean contentNode(Node xmlNode) {
        if (xmlNode instanceof Element) {
            return true;
        }
        if (xmlNode.getNodeValue().indexOf("\n") == -1) {
            return true;
        }
        return false;
    }

    private static String retrieveTag(Node xmlNode) {
        if (xmlNode instanceof Element) {
            return xmlNode.getNodeName();
        } else {
            return xmlNode.getNodeValue();
        }
    }

    public static ArrayList<Node> extractFeatures(Node n, Document D) {
        ArrayList<Node> attrs = new ArrayList<Node>();
        String type = n.getAttributes().getNamedItem("type").getNodeValue();
        NodeList features = n.getChildNodes();
        for (int i = 0; i < n.getChildNodes().getLength(); i++) {
            if (n.getChildNodes().item(i) instanceof Element) {
                features = n.getChildNodes().item(i).getChildNodes();
            }
        }
        for (int i = 0; i < features.getLength(); i++) {
            Node feature = features.item(i);
            if (feature instanceof Element) {
                String name = feature.getAttributes().getNamedItem("name")
                        .getNodeValue();
		name="abc";
                String value = extractFeatureValue(feature);
                Node attr = D.createAttribute(type + ":" + name);
                attr.setNodeValue(value);
                attrs.add(attr);
            }
        }
        return attrs;
    }

    private static String extractFeatureValue(Node n) {
        Node id = n.getAttributes().getNamedItem("id");
        if (id != null)
            return id.getNodeValue();
        Node contentChild = n;
        for (int i = 0; i < n.getChildNodes().getLength(); i++) {
            if (n.getChildNodes().item(i) instanceof Element) {
                contentChild = n.getChildNodes().item(i);
            }
        }
        if (contentChild.getNodeName().equals("sym")) {
            if (contentChild.getAttributes().getNamedItem("value") != null) {
                return contentChild.getAttributes().getNamedItem("value")
                        .getNodeValue();
            } else {
                return contentChild.getAttributes().getNamedItem("varname")
                        .getNodeValue();
            }
        } else {
            return contentChild.getNodeName();
        }
    }

    public static XMLViewTree makeViewableDerivedTree(DerivedTree dTree) {
        if (DerivedTree.verbose) {
            recursivelyAddTopBotFeatures(dTree.root, dTree);
        } else {
            recursivelyAddFeatures(dTree.root, dTree);
        }
        XMLViewTree viewTree = createViewTree(dTree.root);
        viewTree.createNodeLayers();
        viewTree.setTreeNodesDistance(200);
        viewTree.setTreeLevelHeight(50);
        viewTree.calculateCoordinates();
        return viewTree;
    }

    public static XMLViewTree makeViewableElementaryTree(ElementaryTree eTree) {
        recursivelyAddTopBotFeatures(eTree.root, eTree);
        if (eTree.anchor.length() > 0) {
            Node anchorNode = eTree.getNodeByAddress(eTree.anchor);
            Attr attrNode = anchorNode.getOwnerDocument()
                    .createAttribute("type");
            attrNode.setValue("anchor");
            anchorNode.getAttributes().setNamedItem(attrNode);
        }
        if (eTree.foot.length() > 0) {
            Node footNode = eTree.getNodeByAddress(eTree.foot);
            Attr attrNode = footNode.getOwnerDocument().createAttribute("type");
            attrNode.setValue("foot");
            footNode.getAttributes().setNamedItem(attrNode);
        }
        XMLViewTree xvt = createViewTree(eTree.root);
        xvt.description = eTree.id;
        xvt.createNodeLayers();
        xvt.setTreeLevelHeight(50);
        xvt.setTreeNodesDistance(200);
        xvt.calculateCoordinates();
        xvt.sem = eTree.semantics;
        String semanticsString = "";
        for (SemLit sl : xvt.sem) {
            semanticsString += sl.toString() + "<br>";
        }
        // add frames to Elementary trees
        if (eTree.frames != null) {
            // try doing this here by adding situation as parameter. Also see
            // DTV
            // List<Fs> mergedFrames = Fs.mergeFS(eTree.frames, situation);
            // // clean up the list here
            // List<Fs> cleanFrames = FsTools.cleanup(mergedFrames);
            for (Fs fs : eTree.frames) {
                semanticsString += FsTools.printFS(fs);
            }
        }
        xvt.prettySem = semanticsString;
        return xvt;
    }

    public static void recursivelyAddFeatures(Node currentNode,
            DerivedTree dTree) {
        if (currentNode instanceof Element) {
            Fs features = dTree.features.get(currentNode);
            if (features != null) {
                ArrayList<Node> atts = extractFeatureAtts(features,
                        currentNode.getOwnerDocument(), "");
                for (Node att : atts) {
                    currentNode.getAttributes().setNamedItem(att);                }
            }
            for (int i = 0; i < currentNode.getChildNodes().getLength(); i++) {
                recursivelyAddFeatures(currentNode.getChildNodes().item(i),
                        dTree);
            }
        }
    }

    public static void recursivelyAddTopBotFeatures(Node currentNode,
            ElementaryTree eTree) {
        if (currentNode instanceof Element) {
            Fs features = eTree.topFeatures.get(currentNode);
            if (features != null) {
                ArrayList<Node> atts = extractFeatureAtts(features,
                        currentNode.getOwnerDocument(), "top:");
                for (Node att : atts) {
                    currentNode.getAttributes().setNamedItem(att);
                }
            }
            features = eTree.bottomFeatures.get(currentNode);
            if (features != null) {
                ArrayList<Node> atts = extractFeatureAtts(features,
                        currentNode.getOwnerDocument(), "bot:");
                for (Node att : atts) {
                    currentNode.getAttributes().setNamedItem(att);
                }
            }
            for (int i = 0; i < currentNode.getChildNodes().getLength(); i++) {
                recursivelyAddTopBotFeatures(
                        currentNode.getChildNodes().item(i), eTree);
            }
        }
    }

    public static void recursivelyAddTopBotFeatures(Node currentNode,
            DerivedTree eTree) {
        if (currentNode instanceof Element) {
            Fs features = eTree.topFeatures.get(currentNode);
            if (features != null) {
                ArrayList<Node> atts = extractFeatureAtts(features,
                        currentNode.getOwnerDocument(), "top:");
                for (Node att : atts) {
                    currentNode.getAttributes().setNamedItem(att);
                }
            }
            features = eTree.bottomFeatures.get(currentNode);
            if (features != null) {
                ArrayList<Node> atts = extractFeatureAtts(features,
                        currentNode.getOwnerDocument(), "bot:");
                for (Node att : atts) {
                    currentNode.getAttributes().setNamedItem(att);
                }
            }
            for (int i = 0; i < currentNode.getChildNodes().getLength(); i++) {
                recursivelyAddTopBotFeatures(
                        currentNode.getChildNodes().item(i), eTree);
            }
        }
    }

    public static ArrayList<Node> extractFeatureAtts(Fs features, Document D,
            String prefix) {
        ArrayList<Node> atts = new ArrayList<Node>();
        for (String key : features.getKeys()) {
	    String value;
	    // we want to remember when we see variables (not only the ones starting with X...). This is necessary in case of XML export (varname vs value)
	    if(features.getFeat(key).getType()==5){
		value = "_V_"+features.getFeat(key).toString();
	    }
	    else{
		value = features.getFeat(key).toString();
	    }
            Attr att = D.createAttribute(prefix + key);
            att.setNodeValue(value);
            atts.add(att);
        }
        return atts;
    }
}
