/*
 *  File TreeViewPanel.java
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * A panel where a elementary tree, derivation tree or derived tree is
 * displayed.
 */
public class TreeViewPanel extends JPanel {
    // serialVersionUID to avoid warning
    private static final long serialVersionUID = 1L;

    public TreeView t;

    public boolean edgyLines;

    // public HashMap<String, Integer> eventGrid;

    TreeViewMouseListener mouseListener = null;

    // private boolean movableNodes;
    private boolean antialiasing;

    // tree view extensions can implement further tree rendering options
    public List<TreeViewExtension> viewExtensionsBeforeMainRendering;
    public List<TreeViewExtension> viewExtensionsAfterMainRendering;

    public TreeViewPanel() {
        t = null;
        // eventGrid = new HashMap<String, Integer>();
        edgyLines = false;
        // movableNodes = false;
        antialiasing = true;
        viewExtensionsBeforeMainRendering = new ArrayList<TreeViewExtension>();
        viewExtensionsAfterMainRendering = new ArrayList<TreeViewExtension>();
    }

    public void setMouseListener(TreeViewMouseListener mouseListener) {
        this.mouseListener = mouseListener;
        this.addMouseListener(mouseListener);
    }

    public void setMovableNodes(boolean movableNodes) {
        // this.movableNodes = movableNodes;
        if (movableNodes) {
            this.mouseListener = new TreeViewMouseMoveListener(this);
            this.addMouseListener(mouseListener);
            this.addMouseMotionListener(
                    (TreeViewMouseMoveListener) mouseListener);
        } else {
            this.removeMouseListener(mouseListener);
            this.removeMouseMotionListener(
                    (TreeViewMouseMoveListener) mouseListener);
            this.mouseListener = null;
        }
    }

    public void setAntialiasing(boolean anti) {
        this.antialiasing = anti;
    }

    public void paint(Graphics cnv) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            System.err.println("Sleep interrupted!");
        }
        Graphics2D canvas = (Graphics2D) cnv;
        if (antialiasing) {
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
        // determine font size
        int fontSize = t.getFontSize();
        Font font = new Font("Arial", Font.PLAIN, fontSize);
        canvas.setFont(font);
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        if (t != null) {
            clearCanvas(canvas);
            for (TreeViewExtension ext : viewExtensionsBeforeMainRendering) {
                ext.paintOnTreePanel(this, canvas);
            }
            printTreeNodes(canvas);
            printTreeEdges(canvas);
            for (TreeViewExtension ext : viewExtensionsAfterMainRendering) {
                ext.paintOnTreePanel(this, canvas);
            }
        }
    }

    public void clearCanvas(Graphics canvas) {
        // clear canvas
        Dimension newD = new Dimension(t.getTotalTreeWidth(),
                t.getTotalTreeHeight());
        this.setSize(newD);
        this.setMinimumSize(newD);
        this.setMaximumSize(newD);
        this.setPreferredSize(newD);
        canvas.setColor(new Color(220, 220, 220));
        canvas.fillRect(0, 0, 2000, 2000);
        canvas.fillRect(0, 0, this.getSize().width, this.getSize().height);
    }

    public void printBoxAroundNodeTag(Graphics canvas, int nodeID) {
        // print box around node tag
        int x = t.treeNodes.get(nodeID).x
                - (int) (t.treeNodes.get(nodeID).tag.length() * 6
                * t.getZoomFactor());
        int y = t.treeNodes.get(nodeID).y - 10;
        int width = (int) (t.treeNodes.get(nodeID).tag.length() * 12
                * t.getZoomFactor());
        Color color = t.getNodeColor(nodeID);
        if (color != null) {
            canvas.setColor(color);
            canvas.fillRect(x, y, width, t.getFontSize());
            canvas.setColor(Color.BLACK);
            canvas.drawRect(x, y, width, t.getFontSize());
        }
    }

    public void printEdgeArrow(Graphics canvas, int nodeID) {
        TreeViewNode node = t.treeNodes.get(nodeID);
        int x1 = t.treeNodes.get(node.getParent()).x;
        int y1 = t.treeNodes.get(node.getParent()).y + 2;
        int x2 = node.x;
        int y2 = node.y - 10;
        String edgeDir = node.edgeDir;
        if (edgeDir.equals("up")) {
            double slope = ((y1 - y2 + 0.1) / (x1 - x2 + 0.1));
            double lowerSlope = (slope - 1) / (1 + slope);
            double higherSlope = -(1 / lowerSlope);
            Polygon arrowhead = new Polygon();
            arrowhead.addPoint(x1, y1);
            if ((x2 <= x1 && slope < -1) || (x2 >= x1 && slope > -1))
                arrowhead.addPoint(x1
                                + (int) (10 / Math.sqrt(1 + lowerSlope * lowerSlope)),
                        y1 + (int) ((10
                                / Math.sqrt(1 + lowerSlope * lowerSlope))
                                * lowerSlope));
            if ((x2 <= x1 && slope > -1) || (x2 > x1 && slope < -1))
                arrowhead.addPoint(x1
                                - (int) (10 / Math.sqrt(1 + lowerSlope * lowerSlope)),
                        y1 - (int) ((10
                                / Math.sqrt(1 + lowerSlope * lowerSlope))
                                * lowerSlope));
            if ((x2 <= x1 && slope > 1) || (x2 > x1 && slope < 1))
                arrowhead.addPoint(x1
                                + (int) (10 / Math.sqrt(1 + higherSlope * higherSlope)),
                        y1 + (int) ((10
                                / Math.sqrt(1 + higherSlope * higherSlope))
                                * higherSlope));
            if ((x2 <= x1 && slope < 1) || (x2 >= x1 && slope > 1))
                arrowhead.addPoint(x1
                                - (int) (10 / Math.sqrt(1 + higherSlope * higherSlope)),
                        y1 - (int) ((10
                                / Math.sqrt(1 + higherSlope * higherSlope))
                                * higherSlope));
            canvas.fillPolygon(arrowhead);
        } else if (edgeDir.equals("down")) {
            double slope = ((y2 - y1 - 0.1) / (x2 - x1 + 0.1));
            double lowerSlope = (slope - 1) / (1 + slope);
            double higherSlope = -(1 / lowerSlope);
            Polygon arrowhead = new Polygon();
            arrowhead.addPoint(x2, y2);
            if ((x2 < x1 && slope < -1) || (x2 >= x1 && slope > -1))
                arrowhead.addPoint(x2
                                - (int) (10 / Math.sqrt(1 + lowerSlope * lowerSlope)),
                        y2 - (int) ((10
                                / Math.sqrt(1 + lowerSlope * lowerSlope))
                                * lowerSlope));
            if ((x2 < x1 && slope > -1) || (x2 > x1 && slope < -1))
                arrowhead.addPoint(x2
                                + (int) (10 / Math.sqrt(1 + lowerSlope * lowerSlope)),
                        y2 + (int) ((10
                                / Math.sqrt(1 + lowerSlope * lowerSlope))
                                * lowerSlope));
            if ((x2 < x1 && slope > 1) || (x2 > x1 && slope < 1))
                arrowhead.addPoint(x2
                                - (int) (10 / Math.sqrt(1 + higherSlope * higherSlope)),
                        y2 - (int) ((10
                                / Math.sqrt(1 + higherSlope * higherSlope))
                                * higherSlope));
            if ((x2 < x1 && slope < 1) || (x2 >= x1 && slope > 1))
                arrowhead.addPoint(x2
                                + (int) (10 / Math.sqrt(1 + higherSlope * higherSlope)),
                        y2 + (int) ((10
                                / Math.sqrt(1 + higherSlope * higherSlope))
                                * higherSlope));
            canvas.fillPolygon(arrowhead);
        }
    }

    public void printEdgeTag(Graphics canvas, int nodeID, boolean edgyLines) {
        TreeViewNode node = t.treeNodes.get(nodeID);
        if (node.edgeTag.length() > 0) {
            int x1 = t.treeNodes.get(node.getParent()).x;
            int y1 = t.treeNodes.get(node.getParent()).y + 2;
            int x2 = node.x;
            int y2 = node.y - 10;
            // print box around edge tag
            int y = (y1 + y2) / 2 - 10;
            int x = x2;
            if (!edgyLines) {
                x = (x1 + x2) / 2;
            }
            int width = (int) (node.edgeTag.length() * 6 * t.getZoomFactor());
            Color color = node.edgeTagColor;
            if (color == null) {
                canvas.setColor(Color.WHITE);
            } else {
                canvas.setColor(color);
            }
            canvas.fillRect(x - width, y, 2 * width, 12);
            canvas.setColor(Color.BLACK);
            canvas.drawRect(x - width, y, 2 * width, 12);
            canvas.drawString(node.edgeTag, x - width / 2, (y1 + y2) / 2);
        }
    }

    /**
     * prints the tag of a single node, e.g. the name of a phrase or the
     * terminaols
     *
     * @param canvas
     * @param nodeID
     */
    public void printNodeTag(Graphics canvas, int nodeID) {
        canvas.setColor(Color.BLACK);
        // print tag name of node
        int x = (int) (t.treeNodes.get(nodeID).x
                - t.treeNodes.get(nodeID).tag.length() * 4 * t.getZoomFactor());
        int y = t.treeNodes.get(nodeID).y;
        String tag = t.treeNodes.get(nodeID).tag;
        canvas.drawString(tag, x + 2, y);
    }

    public void printOvalAroundNodeTag(Graphics canvas, int nodeID) {
        // print oval around node tag
        int x = t.treeNodes.get(nodeID).x
                - (int) (t.treeNodes.get(nodeID).tag.length() * 8
                * t.getZoomFactor());
        int y = t.treeNodes.get(nodeID).y - 10;
        int width = (int) (t.treeNodes.get(nodeID).tag.length() * 16
                * t.getZoomFactor());
        Color color = t.getNodeColor(nodeID);
        if (color != null) {
            canvas.setColor(color);
            canvas.fillOval(x, y, width, t.getFontSize());
            canvas.setColor(Color.BLACK);
            canvas.drawOval(x, y, width, t.getFontSize());
        }
    }

    public void printTreeEdges(Graphics canvas) {
        // create lines and their tags
        canvas.setColor(Color.BLACK);
        for (int i = 0; i < t.getNodeLevels().size(); i++) {
            ArrayList<Integer> nodes = t.getNodeLevels().get(i);
            for (int j = 0; j < nodes.size(); j++) {
                if (t.treeNodes.get(nodes.get(j)).getParent() != -1) {
                    int x1 = t.treeNodes
                            .get(t.treeNodes.get(nodes.get(j)).getParent()).x;
                    int y1 = t.treeNodes
                            .get(t.treeNodes.get(nodes.get(j)).getParent()).y
                            + 2;
                    int x2 = t.treeNodes.get(nodes.get(j)).x;
                    int y2 = t.treeNodes.get(nodes.get(j)).y - 10;
                    if (edgyLines) {
                        drawLineAccordingToType(canvas,
                                t.treeNodes.get(nodes.get(j)).edgeType, x1, y1,
                                x2, y1);
                        drawLineAccordingToType(canvas,
                                t.treeNodes.get(nodes.get(j)).edgeType, x2, y1,
                                x2, y2);
                    } else {
                        drawLineAccordingToType(canvas,
                                t.treeNodes.get(nodes.get(j)).edgeType, x1, y1,
                                x2, y2);
                    }
                    printEdgeTag(canvas, nodes.get(j), edgyLines);
                    printEdgeArrow(canvas, nodes.get(j));
                }
            }
        }
    }

    /**
     * prints all the nodes in the tree to be displayed
     *
     * @param cnv
     */
    public void printTreeNodes(Graphics cnv) {
        // print nodes of the tree
        for (int i = 0; i < t.getNodeLevels().size(); i++) {
            ArrayList<Integer> nodes = t.getNodeLevels().get(i);
            for (int j = 0; j < nodes.size(); j++) {
                if (t.nodeShape == TreeView.BOX_SHAPE) {
                    printBoxAroundNodeTag(cnv, nodes.get(j));
                } else if (t.nodeShape == TreeView.OVAL_SHAPE) {
                    printOvalAroundNodeTag(cnv, nodes.get(j));
                }
                printNodeTag(cnv, nodes.get(j));
            }
        }
    }

    public void toggleEdgyLines() {
        if (edgyLines) {
            edgyLines = false;
        } else {
            edgyLines = true;
        }
    }

    public void displayTreeView(TreeView t) {
        this.t = t;
        repaint();
    }

    /**
     * prints a line that may be dottet (if type=="dotted")
     */
    public static void drawLineAccordingToType(Graphics g, String type, int x0,
                                               int y0, int x1, int y1) {
        if (type.equals("dotted")) {
            drawDottedLine(g, x0, y0, x1, y1, g.getColor(), 1, 1);
        } else {
            g.drawLine(x0, y0, x1, y1);
        }
    }

    public static void drawDottedLine(Graphics g, int x0, int y0, int x1,
                                      int y1, Color color, int dashLen, int spaceLen) {
        Color c = g.getColor();
        g.setColor(color);
        int dx = x1 - x0;
        int dy = y1 - y0;
        float t = 0.5f;

        g.setColor(color);
        g.drawLine(x0, y0, x0, y0);

        int dashCount = 0;
        int spaceCount = 0;
        boolean doPlot = dashLen > 1;

        if (Math.abs(dx) > Math.abs(dy)) { // slope < 1
            float m = (float) dy / (float) dx; // compute slope
            t += y0;
            dx = (dx < 0) ? -1 : 1;
            m *= dx;
            while (x0 != x1) {
                x0 += dx; // step to next x value
                t += m;
                if (doPlot) {
                    g.drawLine(x0, (int) t, x0, (int) t);
                    dashCount++;
                    if (dashCount >= dashLen) {
                        dashCount = 0;
                        spaceCount = 0;
                        doPlot = false;
                    }
                } else {
                    spaceCount++;
                    if (spaceCount >= spaceLen) {
                        spaceCount = 0;
                        dashCount = 0;
                        doPlot = true;
                    }
                }

            }
        } else if (dy != 0) { // slope >= 1
            float m = (float) dx / (float) dy; // compute slope
            t += x0;
            dy = (dy < 0) ? -1 : 1;
            m *= dy;
            while (y0 != y1) {
                y0 += dy; // step to next y value
                t += m;
                if (doPlot) {
                    g.drawLine((int) t, y0, (int) t, y0);
                    dashCount++;
                    if (dashCount >= dashLen) {
                        dashCount = 0;
                        spaceCount = 0;
                        doPlot = false;
                    }
                } else {
                    spaceCount++;
                    if (spaceCount >= spaceLen) {
                        spaceCount = 0;
                        dashCount = 0;
                        doPlot = true;
                    }
                }
            }
        }
        g.setColor(c);
    }
}
