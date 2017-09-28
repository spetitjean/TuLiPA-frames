/*
 *  File XMLViewTree.java
 *
 *  Authors:
 *     Johannes Dellert  <johannes.dellert@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Johannes Dellert, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:53:00 CEST 2007
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.w3c.dom.Node;

import de.tuebingen.gui.tree.view.TreeView;
import de.tuebingen.gui.tree.view.TreeViewNode;
import de.tuebingen.tag.SemLit;

public class XMLViewTree extends TreeView {
    // external information
    String description = "derived tree";
    int id;
    // semantics (for elementary trees and steps)
    List<SemLit> sem;
    String prettySem;

    // added internal information
    HashMap<Integer, ArrayList<XMLViewTreeAttribute>> attrs;
    HashMap<Integer, Node> domNodes;

    // added display information
    HashSet<Integer> collapsedAttributes;

    public XMLViewTree() {
        super(null);
        id = 0;
        collapsedAttributes = new HashSet<Integer>();
        attrs = new HashMap<Integer, ArrayList<XMLViewTreeAttribute>>();
        domNodes = new HashMap<Integer, Node>();
    }

    public ArrayList<XMLViewTreeAttribute> getAttrs(int nodeID) {
        ArrayList<XMLViewTreeAttribute> attr = attrs.get(nodeID);
        if (attr == null) {
            attr = new ArrayList<XMLViewTreeAttribute>();
            attrs.put(nodeID, attr);
        }
        return attr;
    }

    public void addAttr(int nodeID, XMLViewTreeAttribute att) {
        ArrayList<XMLViewTreeAttribute> attr = attrs.get(nodeID);
        if (attr == null) {
            attr = new ArrayList<XMLViewTreeAttribute>();
            attrs.put(nodeID, attr);
        }
        attr.add(att);
    }

    public void createNodeLayers() {
        int level = 0;
        TreeViewNode root = treeNodes.get(0);
        nodeLevels = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> rootLevel = new ArrayList<Integer>();
        rootLevel.add(0);
        getNodeLevels().add(level, rootLevel);
        level++;
        if (!collapsedNodes.contains(0)) {
            ArrayList<Integer> children = root.children;
            while (true) {
                ArrayList<Integer> grandchildren = new ArrayList<Integer>();
                for (int i = 0; i < children.size(); i++) {
                    if (!collapsedNodes.contains(children.get(i))) {
                        for (int j = 0; j < treeNodes
                                .get(children.get(i)).children.size(); j++) {
                            grandchildren
                                    .add(treeNodes.get(children.get(i)).children
                                            .get(j));
                        }
                    }
                }
                getNodeLevels().add(level, children);
                children = grandchildren;
                level++;
                if (grandchildren.size() == 0) {
                    break;
                }
            }
        }
    }

    public void calculateCoordinates() {
        createNodeLayers();
        setTotalTreeWidth(0);
        setTotalTreeHeight(0);
        // calculate (maximum) subtree width for each node bottom-up
        for (int i = getNodeLevels().size() - 1; i >= 0; i--) {
            ArrayList<Integer> nodes = getNodeLevels().get(i);
            for (int j = 0; j < nodes.size(); j++) {
                TreeViewNode node = treeNodes.get(nodes.get(j));
                if (!collapsedNodes.contains(nodes.get(j))
                        && node.children.size() > 0) {
                    node.subTreeWidth = collectWidths(node.children);
                } else {
                    node.subTreeWidth = 1;
                }
            }
        }
        treeNodes.get(0).x = treeNodes.get(0).subTreeWidth * treeNodesDistance
                / 2;
        // no edges may cross, no nodes overlap
        for (int i = 0; i < getNodeLevels().size(); i++) {
            ArrayList<Integer> nodes = getNodeLevels().get(i);
            int xOffset = 200;
            int parent = -1;
            for (int j = 0; j < nodes.size(); j++) {
                int subtreeWidth = treeNodes.get(nodes.get(j)).subTreeWidth
                        * treeNodesDistance;
                xOffset += subtreeWidth;
                if (i > 0
                        && treeNodes.get(nodes.get(j)).getParent() != parent) {
                    parent = treeNodes.get(nodes.get(j)).getParent();
                    xOffset = (int) (treeNodes.get(parent).x
                            + treeNodes.get(parent).subTreeWidth
                                    * ((double) (treeNodes
                                            .get(nodes.get(j)).subTreeWidth)
                                            / treeNodes.get(parent).subTreeWidth
                                            - 0.5)
                                    * treeNodesDistance);
                }
                if (i > 0) {
                    treeNodes.get(nodes.get(j)).x = xOffset - subtreeWidth / 2;
                }
            }
            if (nodes.size() > 0 && treeNodes.get(nodes.get(nodes.size() - 1)).x
                    + treeNodesDistance > getTotalTreeWidth()) {
                setTotalTreeWidth(treeNodes.get(nodes.get(nodes.size() - 1)).x
                        + treeNodesDistance);
            }
        }
        // make vertical space for attributes
        for (int i = 1; i < treeNodes.size(); i++) {
            int attHeight = 0;
            if (!collapsedAttributes.contains(treeNodes.get(i).getParent())) {
                attHeight = getAttrs((treeNodes.get(i)).getParent()).size()
                        * 15;
            }
            treeNodes.get(i).y = treeNodes.get((treeNodes.get(i)).getParent()).y
                    + treeLevelHeight + attHeight;
            if (treeNodes.get(i).y + 200 + attHeight > getTotalTreeHeight()) {
                setTotalTreeHeight(treeNodes.get(i).y + 200 + attHeight);
            }
        }
    }

    public void collapseAttributes(int i) {
        collapsedAttributes.add(i);
        calculateCoordinates();
    }

    public void expandAttributes(int i) {
        if (collapsedAttributes.contains(i)) {
            collapsedAttributes.remove(i);
        }
        calculateCoordinates();
    }

    public void toggleAttributes(int i) {
        if (collapsedAttributes.contains(i)) {
            collapsedAttributes.remove(i);
        } else {
            collapsedAttributes.add(i);
        }
        calculateCoordinates();
    }

    public void collapseAllAttributes() {
        for (int i = 0; i < treeNodes.size(); i++) {
            if (getAttrs(i).size() > 0) {
                collapsedAttributes.add(i);
            }
        }
        calculateCoordinates();
    }

    public void expandAllAttributes() {
        collapsedAttributes.clear();
        calculateCoordinates();
    }

    private int collectWidths(ArrayList<Integer> children) {
        int sum = 0;
        for (int i = 0; i < children.size(); i++) {
            sum += treeNodes.get(children.get(i)).subTreeWidth;
        }
        return sum;
    }

    public String toString() {
        return description;
    }

    public HashMap<Integer, Node> getDomNodes() {
        return domNodes;
    }

    public Color getNodeColor(int nodeID) {
        boolean c = collapsedNodes.contains(nodeID);
        boolean a = collapsedAttributes.contains(nodeID);
        if (c) {
            if (a)
                return Color.green;
            return Color.yellow;
        }
        if (a)
            return Color.cyan;
        return Color.white;
    }
}
