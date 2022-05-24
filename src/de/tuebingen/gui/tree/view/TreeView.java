/*
 *  File TreeView.java
 *
 *  Authors:
 *     Johannes Dellert
 *
 *  Copyright:
 *     Johannes Dellert, 2009
 *
 *  Last modified:
 *     Do 16. Apr 09:55:36 CEST 2009
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
package de.tuebingen.gui.tree.view;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import de.tuebingen.gui.tree.model.TreeModel;
import de.tuebingen.gui.tree.model.TreeModelNode;

public class TreeView {
    // internal information
    protected ArrayList<ArrayList<Integer>> nodeLevels; // level 0: terminals!
    public HashMap<Integer, TreeViewNode> treeNodes;
    public int rootID;

    // associated model (can be null if view was generated directly)
    TreeModel model;

    // view options
    protected int treeNodesDistance = 50;
    protected int treeLevelHeight = 25;
    public int nodeShape;
    private int totalTreeWidth;
    private int totalTreeHeight;
    private int selectionRadius;

    // zoom status
    private double zoomFactor = 0.8;
    private int fontSize = 12;

    // view status
    protected HashSet<Integer> collapsedNodes;

    public static int BOX_SHAPE = 0;
    public static int OVAL_SHAPE = 1;

    public TreeView(TreeModel model) {
        this(model, 50, 25);
    }

    public TreeView(TreeModel model, int treeNodesDistance,
                    int treeLevelHeight) {
        rootID = -1;
        nodeShape = BOX_SHAPE;
        setTotalTreeWidth(0);
        setTotalTreeHeight(0);
        this.treeNodesDistance = treeNodesDistance;
        this.treeLevelHeight = treeLevelHeight;
        this.selectionRadius = 30;
        nodeLevels = new ArrayList<ArrayList<Integer>>();
        treeNodes = new HashMap<Integer, TreeViewNode>();
        collapsedNodes = new HashSet<Integer>();
        if (model != null) {
            this.model = model;
            createTreeStructure();
            calculateCoordinates();
        }
    }

    public int getTreeNodesDistance() {
        return treeNodesDistance;
    }

    public void setTreeNodesDistance(int treeNodesDistance) {
        this.treeNodesDistance = treeNodesDistance;
    }

    public int getTreeLevelHeight() {
        return treeLevelHeight;
    }

    public void setTreeLevelHeight(int treeLevelHeight) {
        this.treeLevelHeight = treeLevelHeight;
    }

    public int getSelectionRadius() {
        return selectionRadius;
    }

    public void setSelectionRadius(int selectionRadius) {
        this.selectionRadius = selectionRadius;
    }

    private void createTreeStructure() {
        if (model.root != -1) {
            TreeModelNode rootModel = model.nodes.get(model.root);
            TreeViewNode root = new TreeViewNode(rootModel, -1,
                    new ArrayList<Integer>(), 100, 50);
            root.edgeTag = rootModel.parentEdgeLabel;
            treeNodes.put(root.id, root);
            rootID = root.id;
            createSubtreeStructure(rootModel, root, model);
        }
    }

    private void createSubtreeStructure(TreeModelNode modelNode,
                                        TreeViewNode viewNode, TreeModel model) {
        for (int i = 0; i < modelNode.children.size(); i++) {
            TreeModelNode currentChild = model.nodes
                    .get(modelNode.children.get(i));
            TreeViewNode childNode = new TreeViewNode(currentChild, viewNode.id,
                    new ArrayList<Integer>(), viewNode.x,
                    viewNode.y + (int) (treeLevelHeight * zoomFactor));
            childNode.edgeTag = currentChild.parentEdgeLabel;
            viewNode.x += treeNodesDistance * zoomFactor;
            treeNodes.put(childNode.id, childNode);
            viewNode.children.add(childNode.id);
            createSubtreeStructure(currentChild, childNode, model);
        }
    }

    public void createNodeLayers() {
        nodeLevels = new ArrayList<ArrayList<Integer>>();
        if (model.usesTerminals) {
            getNodeLevels().add(model.terminals);
        }
        TreeViewNode root = treeNodes.get(rootID);
        ArrayList<Integer> rootLevel = new ArrayList<Integer>();
        rootLevel.add(rootID);
        getNodeLevels().add(rootLevel);
        ArrayList<Integer> children = root.children;
        while (true) {
            ArrayList<Integer> grandchildren = new ArrayList<Integer>();
            for (int i = 0; i < children.size(); i++) {
                for (int j = 0; j < treeNodes.get(children.get(i)).children
                        .size(); j++) {
                    int nodeID = treeNodes.get(children.get(i)).children.get(j);
                    if (model.usesTerminals) {
                        if (!model.terminals.contains(nodeID)) {
                            grandchildren.add(nodeID);
                        }
                    } else {
                        grandchildren.add(nodeID);
                    }
                }
            }
            getNodeLevels().add(children);
            children = grandchildren;
            if (grandchildren.size() == 0) {
                break;
            }
        }
    }

    public void calculateCoordinates() {
        createNodeLayers();
        setTotalTreeWidth(0);
        setTotalTreeHeight((int) ((getNodeLevels().size() + 1) * treeLevelHeight
                * zoomFactor));
        if (!model.usesTerminals) {
            // calculate (maximum) subtree width for each node bottom-up
            for (int i = getNodeLevels().size() - 1; i >= 0; i--) {
                ArrayList<Integer> nodes = getNodeLevels().get(i);
                for (int j = 0; j < nodes.size(); j++) {
                    TreeViewNode node = treeNodes.get(nodes.get(j));
                    if (node.children.size() > 0) {
                        node.subTreeWidth = collectWidths(node.children);
                    } else {
                        node.subTreeWidth = 1;
                    }
                }
            }
            treeNodes.get(rootID).x = treeNodes.get(rootID).subTreeWidth
                    * treeNodesDistance / 2;
            // no edges may cross, no nodes overlap
            for (int i = 0; i < getNodeLevels().size(); i++) {
                ArrayList<Integer> nodes = getNodeLevels().get(i);
                int xOffset = (int) (100 * zoomFactor);
                int parent = -1;
                for (int j = 0; j < nodes.size(); j++) {
                    int subtreeWidth = (int) (treeNodes
                            .get(nodes.get(j)).subTreeWidth * treeNodesDistance
                            * zoomFactor);
                    xOffset += subtreeWidth;
                    if (i > 0 && treeNodes.get(nodes.get(j))
                            .getParent() != parent) {
                        parent = treeNodes.get(nodes.get(j)).getParent();
                        xOffset = (int) (treeNodes.get(parent).x + treeNodes
                                .get(parent).subTreeWidth
                                * ((double) (treeNodes
                                .get(nodes.get(j)).subTreeWidth)
                                / treeNodes.get(parent).subTreeWidth
                                - 0.5)
                                * treeNodesDistance);
                    }
                    if (i > 0) {
                        treeNodes.get(nodes.get(j)).x = xOffset
                                - subtreeWidth / 2;
                    }
                }
                if (nodes.size() > 0
                        && treeNodes.get(nodes.get(nodes.size() - 1)).x
                        + (int) (treeNodesDistance
                        * zoomFactor) > getTotalTreeWidth()) {
                    setTotalTreeWidth(
                            treeNodes.get(nodes.get(nodes.size() - 1)).x
                                    + (int) (treeNodesDistance * zoomFactor));
                }
            }
        } else {
            ArrayList<Integer> terminals = getNodeLevels().get(0);
            int xpos = (int) (100 * zoomFactor);
            for (int t : terminals) {
                treeNodes.get(t).y = (int) (getNodeLevels().size()
                        * treeLevelHeight * zoomFactor);
                treeNodes.get(t).x = xpos;
                xpos += treeNodesDistance * zoomFactor;
            }
            setTotalTreeWidth(terminals.size()
                    * (int) ((treeNodesDistance + 2) * zoomFactor));
            for (int j = getNodeLevels().size() - 1; j > 0; j--) {
                for (int n : getNodeLevels().get(j)) {
                    int minX = Integer.MAX_VALUE;
                    int maxX = 0;
                    for (int c : treeNodes.get(n).children) {
                        int newX = treeNodes.get(c).x;
                        if (newX < minX)
                            minX = newX;
                        if (newX > maxX)
                            maxX = newX;
                    }
                    treeNodes.get(n).y = 50
                            + (int) ((j - 1) * treeLevelHeight * zoomFactor);
                    treeNodes.get(n).x = (minX + maxX) / 2;
                }
            }
            if (model.usesPreTerminals) {
                for (int t : terminals) {
                    treeNodes.get(
                            treeNodes.get(t).getParent()).y = treeNodes.get(t).y
                            - (int) (treeLevelHeight * zoomFactor);
                }
            }
        }
    }

    private int collectWidths(ArrayList<Integer> children) {
        int sum = 0;
        for (int i = 0; i < children.size(); i++) {
            sum += treeNodes.get(children.get(i)).subTreeWidth;
        }
        return sum;
    }

    public String showLevels() {
        String levelString = "";
        for (ArrayList<Integer> nodeLevel : getNodeLevels()) {
            levelString += nodeLevel + "\n";
        }
        return levelString;
    }

    public void increaseVerticalNodeDistance() {
        treeLevelHeight *= 1.25;
        calculateCoordinates();
    }

    public void decreaseVerticalNodeDistance() {
        treeLevelHeight *= 0.8;
        calculateCoordinates();
    }

    public void increaseHorizontalNodeDistance() {
        treeNodesDistance *= 1.25;
        calculateCoordinates();
    }

    public void decreaseHorizontalNodeDistance() {
        treeNodesDistance *= 0.8;
        calculateCoordinates();
    }

    public void setTotalTreeWidth(int totalTreeWidth) {
        this.totalTreeWidth = totalTreeWidth;
    }

    public int getTotalTreeWidth() {
        return totalTreeWidth;
    }

    public void setTotalTreeHeight(int totalTreeHeight) {
        this.totalTreeHeight = totalTreeHeight;
    }

    public int getTotalTreeHeight() {
        return totalTreeHeight;
    }

    public ArrayList<ArrayList<Integer>> getNodeLevels() {
        return nodeLevels;
    }

    public void collapseNode(int i) {
        collapsedNodes.add(i);
        calculateCoordinates();
    }

    public void expandNode(int i) {
        if (collapsedNodes.contains(i)) {
            collapsedNodes.remove(i);
        }
        calculateCoordinates();
    }

    public void toggleNode(int i) {
        if (collapsedNodes.contains(i)) {
            collapsedNodes.remove(i);
        } else {
            collapsedNodes.add(i);
        }
        calculateCoordinates();
    }

    public void collapseAllNodes() {
        for (int i = 0; i < treeNodes.size(); i++) {
            collapsedNodes.add(i);
        }
        calculateCoordinates();
    }

    public void expandAllNodes() {
        collapsedNodes.clear();
        calculateCoordinates();
    }

    public Color getNodeColor(int nodeID) {
        TreeViewNode n = treeNodes.get(nodeID);
        if (n == null)
            return null;
        return n.color;
    }

    public void zoomIn() {
        if (fontSize < 30) {
            zoomFactor *= ((fontSize + 1.0) / fontSize);
            fontSize++;
            calculateCoordinates();
        }
    }

    public void zoomOut() {
        if (fontSize > 4) {
            zoomFactor *= ((fontSize - 1.0) / fontSize);
            fontSize--;
            calculateCoordinates();
        }
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSizeAndZoomFactor(int fontSize) {
        this.fontSize = fontSize;
        this.zoomFactor = fontSize / 12.0;
        calculateCoordinates();
    }

    // danger: does not work with crossing edges!
    public int getNodeAtCoordinates(int x, int y) {
        int xDistance = (int) ((treeNodesDistance * zoomFactor) / 2);
        int yDistance = (int) ((treeLevelHeight * zoomFactor) / 2);
        TreeViewNode leftCandidateNode = treeNodes.get(rootID);
        TreeViewNode rightCandidateNode = treeNodes.get(rootID);
        while (leftCandidateNode.y + yDistance < y
                && rightCandidateNode.y + yDistance < y) {
            List<Integer> leftChildren = leftCandidateNode.children;
            List<Integer> rightChildren = rightCandidateNode.children;
            if (leftChildren.size() == 0 && rightChildren.size() == 0)
                break;
            if (leftChildren.size() > 0)
                leftCandidateNode = treeNodes.get(leftChildren.get(0));
            if (rightChildren.size() > 0)
                rightCandidateNode = treeNodes.get(rightChildren.get(0));
            for (int i = 0, j = 0; i < leftChildren.size()
                    || j < rightChildren.size(); i++, j++) {
                if (i < leftChildren.size()) {
                    int lChildID = leftChildren.get(i);
                    TreeViewNode lNode = treeNodes.get(lChildID);
                    if (lNode.x - xDistance <= x) {
                        leftCandidateNode = lNode;
                        if (i + 1 < leftChildren.size()) {
                            lChildID = leftChildren.get(i + 1);
                            lNode = treeNodes.get(lChildID);
                            if (lNode.x - xDistance >= x) {
                                rightCandidateNode = lNode;
                                break;
                            }
                        }
                    }
                }
                if (j < rightChildren.size()) {
                    int rChildID = rightChildren.get(j);
                    TreeViewNode rNode = treeNodes.get(rChildID);
                    if (rNode.x - xDistance <= x) {
                        rightCandidateNode = rNode;
                        if (j - 1 > 0) {
                            rChildID = rightChildren.get(j - 1);
                            rNode = treeNodes.get(rChildID);
                            if (rNode.x - xDistance <= x) {
                                leftCandidateNode = rNode;
                                break;
                            }
                        }
                    }
                }
            }
        }
        double leftDistance = Point.distance(x, y, leftCandidateNode.x,
                leftCandidateNode.y);
        double rightDistance = Point.distance(x, y, rightCandidateNode.x,
                rightCandidateNode.y);
        if (leftDistance > rightDistance) {
            if (rightDistance <= selectionRadius)
                return rightCandidateNode.id;
        } else {
            if (leftDistance <= selectionRadius)
                return leftCandidateNode.id;
        }
        return -1;
    }
}
