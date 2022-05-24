/*
 *  File FancyAttributeDisplayExtension.java
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
package de.tuebingen.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import de.tuebingen.gui.tree.view.*;

public class FancyAttributeDisplayExtension extends TreeViewExtension {
    public void paintOnTreePanel(TreeViewPanel panel, Graphics2D canvas) {
        XMLViewTree t = (XMLViewTree) panel.t;
        //print information on current tree
        for (int i = 0; i < t.getNodeLevels().size(); i++) {
            ArrayList<Integer> nodes = t.getNodeLevels().get(i);
            for (int j = 0; j < nodes.size(); j++) {
                //print attributes of node
                if (!t.collapsedAttributes.contains(nodes.get(j))) {
                    int attrYPos = t.treeNodes.get(nodes.get(j)).y;
                    int leftNormalBorder = t.treeNodes.get(nodes.get(j)).x;
                    int leftTopBorder = t.treeNodes.get(nodes.get(j)).x;
                    int leftBottomBorder = t.treeNodes.get(nodes.get(j)).x;

                    ArrayList<XMLViewTreeAttribute> feats = t.getAttrs(nodes.get(j));
                    ArrayList<XMLViewTreeAttribute> normalFeats = new ArrayList<XMLViewTreeAttribute>();
                    ArrayList<XMLViewTreeAttribute> topFeats = new ArrayList<XMLViewTreeAttribute>();
                    ArrayList<XMLViewTreeAttribute> botFeats = new ArrayList<XMLViewTreeAttribute>();
                    for (int k = 0; k < feats.size(); k++) {
                        XMLViewTreeAttribute attr = feats.get(k);
                        if (attr.name.startsWith("top:")) {
                            XMLViewTreeAttribute topFeat = new XMLViewTreeAttribute();
                            topFeat.name = attr.name.substring(4);
                            topFeat.value = attr.value;
                            topFeats.add(topFeat);

                            int leftFringe = t.treeNodes.get(nodes.get(j)).x - (attr.name.length() + attr.value.length() - 4) * 6;
                            if (leftFringe < leftTopBorder) {
                                leftTopBorder = leftFringe;
                            }
                        } else if (attr.name.startsWith("bot:")) {
                            XMLViewTreeAttribute botFeat = new XMLViewTreeAttribute();
                            botFeat.name = attr.name.substring(4);
                            botFeat.value = attr.value;
                            botFeats.add(botFeat);

                            int leftFringe = t.treeNodes.get(nodes.get(j)).x - (attr.name.length() + attr.value.length() - 4) * 6;
                            if (leftFringe < leftBottomBorder) {
                                leftBottomBorder = leftFringe;
                            }
                        } else {
                            if (attr.name.equals("type")) {
                                canvas.setColor(Color.BLACK);
                                int centerX = (int) (t.treeNodes.get(nodes.get(j)).x + 20 * panel.t.getZoomFactor());
                                int centerY = (int) (t.treeNodes.get(nodes.get(j)).y - panel.t.getFontSize() / 2);
                                if (attr.value.equals("anchor")) {
                                    //draw diamond next to node label
                                    canvas.drawLine(centerX - panel.t.getFontSize() / 3, centerY, centerX, centerY + panel.t.getFontSize() / 2);
                                    canvas.drawLine(centerX - panel.t.getFontSize() / 3, centerY, centerX, centerY - panel.t.getFontSize() / 2);
                                    canvas.drawLine(centerX + panel.t.getFontSize() / 3, centerY, centerX, centerY + panel.t.getFontSize() / 2);
                                    canvas.drawLine(centerX + panel.t.getFontSize() / 3, centerY, centerX, centerY - panel.t.getFontSize() / 2);
                                } else if (attr.value.equals("foot")) {
                                    //draw star next to node label
                                    canvas.drawLine(centerX - panel.t.getFontSize() / 2, centerY, centerX + panel.t.getFontSize() / 2, centerY);
                                    canvas.drawLine(centerX, centerY - panel.t.getFontSize() / 2, centerX, centerY + panel.t.getFontSize());
                                    canvas.drawLine(centerX - panel.t.getFontSize() / 3, centerY - panel.t.getFontSize() / 3, centerX + panel.t.getFontSize() / 3, centerY + panel.t.getFontSize() / 3);
                                    canvas.drawLine(centerX + panel.t.getFontSize() / 3, centerY - panel.t.getFontSize() / 3, centerX - panel.t.getFontSize() / 3, centerY + panel.t.getFontSize() / 3);
                                }
                            } else {
                                normalFeats.add(attr);
                                int leftFringe = t.treeNodes.get(nodes.get(j)).x - (attr.name.length() + attr.value.length()) * 6;
                                if (leftFringe < leftNormalBorder) {
                                    leftNormalBorder = leftFringe;
                                }
                            }
                        }
                    }
                    attrYPos += 2;
                    if (topFeats.size() > 0) {
                        canvas.setColor(Color.WHITE);
                        canvas.fillRect(leftTopBorder, attrYPos, (t.treeNodes.get(nodes.get(j)).x - leftTopBorder) * 2, 15 * topFeats.size());
                        canvas.setColor(Color.BLACK);
                        canvas.drawRect(leftTopBorder, attrYPos, (t.treeNodes.get(nodes.get(j)).x - leftTopBorder) * 2, 15 * topFeats.size());
                        for (int k = 0; k < topFeats.size(); k++) {
                            XMLViewTreeAttribute attr = topFeats.get(k);
                            attrYPos += 15;
                            if (attr.value.startsWith("#")) {
                                canvas.setColor(Color.RED);
                                String attrString = attr.name.toUpperCase() + ": " + attr.value.substring(1);
                                canvas.drawString(attrString, leftTopBorder, attrYPos - 2);
                            } else {
                                canvas.setColor(Color.BLACK);
                                String attrString = attr.name.toUpperCase() + ": " + attr.value;
                                canvas.drawString(attrString, leftTopBorder, attrYPos - 2);
                            }
                        }
                        attrYPos += 3;
                    }
                    if (botFeats.size() > 0) {
                        canvas.setColor(Color.WHITE);
                        canvas.fillRect(leftBottomBorder, attrYPos, (t.treeNodes.get(nodes.get(j)).x - leftBottomBorder) * 2, 15 * botFeats.size());
                        canvas.setColor(Color.BLACK);
                        canvas.drawRect(leftBottomBorder, attrYPos, (t.treeNodes.get(nodes.get(j)).x - leftBottomBorder) * 2, 15 * botFeats.size());
                        for (int k = 0; k < botFeats.size(); k++) {
                            XMLViewTreeAttribute attr = botFeats.get(k);
                            attrYPos += 15;
                            if (attr.value.startsWith("#")) {
                                canvas.setColor(Color.RED);
                                String attrString = attr.name.toUpperCase() + ": " + attr.value.substring(1);
                                canvas.drawString(attrString, leftBottomBorder, attrYPos - 2);
                            } else {
                                String attrString = attr.name.toUpperCase() + ": " + attr.value;
                                canvas.drawString(attrString, leftBottomBorder, attrYPos - 2);
                            }
                            canvas.setColor(Color.BLACK);
                        }
                        attrYPos += 3;
                    }
                    if (normalFeats.size() > 0) {
                        canvas.setColor(Color.WHITE);
                        canvas.fillRect(leftNormalBorder, attrYPos, (t.treeNodes.get(nodes.get(j)).x - leftNormalBorder) * 2, 15 * normalFeats.size());
                        canvas.setColor(Color.BLACK);
                        canvas.drawRect(leftNormalBorder, attrYPos, (t.treeNodes.get(nodes.get(j)).x - leftNormalBorder) * 2, 15 * normalFeats.size());
                        for (int k = 0; k < normalFeats.size(); k++) {
                            XMLViewTreeAttribute attr = normalFeats.get(k);
                            attrYPos += 15;
                            if (attr.value.startsWith("#")) {
                                canvas.setColor(Color.RED);
                                String attrString = attr.name.toUpperCase() + ": " + attr.value.substring(1);
                                canvas.drawString(attrString, leftNormalBorder, attrYPos - 2);
                            } else {
                                String attrString = attr.name.toUpperCase() + ": " + attr.value;
                                canvas.drawString(attrString, leftNormalBorder, attrYPos - 2);
                            }
                            canvas.setColor(Color.BLACK);
                        }
                        attrYPos += 5;
                    }

                }
            }
        }
    }
}
