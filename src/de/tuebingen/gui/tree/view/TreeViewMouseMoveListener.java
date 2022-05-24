/*
 *  File TreeViewMouseMoveListener.java
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

import java.awt.event.*;
import java.util.List;

public class TreeViewMouseMoveListener extends TreeViewMouseListener implements MouseMotionListener {
    int movedNodeID = -1;

    public TreeViewMouseMoveListener(TreeViewPanel viewPanel) {
        super(viewPanel);
    }

    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        TreeViewNode candidateNode = viewPanel.t.treeNodes.get(viewPanel.t.rootID);
        while (candidateNode.y < y && candidateNode.children.size() > 0) {
            List<Integer> children = candidateNode.children;
            candidateNode = viewPanel.t.treeNodes.get(children.get(0));
            for (int i : children) {
                if (viewPanel.t.treeNodes.get(i).x < x) {
                    candidateNode = viewPanel.t.treeNodes.get(i);
                }
            }
        }
        movedNodeID = candidateNode.id;
        viewPanel.repaint();
    }

    public void mouseReleased(MouseEvent e) {
        if (movedNodeID != -1) {
            TreeViewNode n = viewPanel.t.treeNodes.get(movedNodeID);
            n.x = e.getX();
            n.y = e.getY();
        }
        movedNodeID = -1;
        viewPanel.repaint();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        if (movedNodeID != -1) {
            TreeViewNode n = viewPanel.t.treeNodes.get(movedNodeID);
            n.x = e.getX();
            n.y = e.getY();
        }
        viewPanel.repaint();
    }
}
