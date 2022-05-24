/*
 *  File XMLTreeListener.java
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

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import de.tuebingen.gui.tree.view.TreeViewMouseListener;
import de.tuebingen.gui.tree.view.TreeViewPanel;

public class XMLTreeListener extends TreeViewMouseListener implements ActionListener {
    JPopupMenu popupMenu;

    public XMLTreeListener(TreeViewPanel panel) {
        super(panel);
        popupMenu = new JPopupMenu();
        JMenuItem expandAllAttributesMenuItem = new JMenuItem("Expand all attributes");
        expandAllAttributesMenuItem.addActionListener(this);
        JMenuItem collapseAllAttributesMenuItem = new JMenuItem("Collapse all attributes");
        collapseAllAttributesMenuItem.addActionListener(this);
        JMenuItem expandAllNodesMenuItem = new JMenuItem("Expand all nodes");
        expandAllNodesMenuItem.addActionListener(this);
        JMenuItem collapseAllNodesMenuItem = new JMenuItem("Collapse all nodes");
        collapseAllNodesMenuItem.addActionListener(this);
        JMenuItem increaseVerticalNodeDistanceMenuItem = new JMenuItem("Increase vertical node distance");
        increaseVerticalNodeDistanceMenuItem.addActionListener(this);
        JMenuItem decreaseVerticalNodeDistanceMenuItem = new JMenuItem("Decrease vertical node distance");
        decreaseVerticalNodeDistanceMenuItem.addActionListener(this);
        JMenuItem increaseHorizontalNodeDistanceMenuItem = new JMenuItem("Increase horizontal node distance");
        increaseHorizontalNodeDistanceMenuItem.addActionListener(this);
        JMenuItem decreaseHorizontalNodeDistanceMenuItem = new JMenuItem("Decrease horizontal node distance");
        decreaseHorizontalNodeDistanceMenuItem.addActionListener(this);
        JMenuItem toggleEdgyLinesMenuItem = new JMenuItem("Toggle edgy lines");
        toggleEdgyLinesMenuItem.addActionListener(this);
        JMenuItem savePaneToFileMenuItem = new JMenuItem("Save tree display ...");
        savePaneToFileMenuItem.addActionListener(this);
        popupMenu.add(expandAllAttributesMenuItem);
        popupMenu.add(collapseAllAttributesMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(expandAllNodesMenuItem);
        popupMenu.add(collapseAllNodesMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(increaseVerticalNodeDistanceMenuItem);
        popupMenu.add(decreaseVerticalNodeDistanceMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(increaseHorizontalNodeDistanceMenuItem);
        popupMenu.add(decreaseHorizontalNodeDistanceMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(toggleEdgyLinesMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(savePaneToFileMenuItem);
    }

    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        int nodeID = viewPanel.t.getNodeAtCoordinates(x, y);

        if (nodeID != -1) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                viewPanel.t.toggleNode(nodeID);
            } else {
                ((XMLViewTree) viewPanel.t).toggleAttributes(nodeID);
            }
            viewPanel.repaint();
        } else {
            if (e.getButton() != MouseEvent.BUTTON1) {
                popupMenu.show(viewPanel, x, y);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("Expand all attributes")) {
            ((XMLViewTree) viewPanel.t).expandAllAttributes();
            viewPanel.repaint();
        } else if (s.equals("Collapse all attributes")) {
            ((XMLViewTree) viewPanel.t).collapseAllAttributes();
            viewPanel.repaint();
        } else if (s.equals("Expand all nodes")) {
            viewPanel.t.expandAllNodes();
            viewPanel.repaint();
        } else if (s.equals("Collapse all nodes")) {
            viewPanel.t.collapseAllNodes();
            viewPanel.repaint();
        } else if (s.equals("Increase vertical node distance")) {
            viewPanel.t.increaseVerticalNodeDistance();
            viewPanel.repaint();
        } else if (s.equals("Decrease vertical node distance")) {
            viewPanel.t.decreaseVerticalNodeDistance();
            viewPanel.repaint();
        } else if (s.equals("Increase horizontal node distance")) {
            viewPanel.t.increaseHorizontalNodeDistance();
            viewPanel.repaint();
        } else if (s.equals("Decrease horizontal node distance")) {
            viewPanel.t.decreaseHorizontalNodeDistance();
            viewPanel.repaint();
        } else if (s.equals("Toggle edgy lines")) {
            viewPanel.toggleEdgyLines();
            viewPanel.repaint();
        } else if (s.equals("Save tree display ...")) {
            JFileChooser chooser = new JFileChooser(new File("."));
            //FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
            //chooser.setFileFilter(filter);
            chooser.showSaveDialog(viewPanel);
            File outputFile = chooser.getSelectedFile();

            BufferedImage outputImage = new BufferedImage(viewPanel.getWidth(), viewPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics outputCanvas = outputImage.getGraphics();
            viewPanel.paint(outputCanvas);
            try {
                ImageIO.write(outputImage, "png", outputFile);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(viewPanel, ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
