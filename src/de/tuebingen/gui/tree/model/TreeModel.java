/*
 *  File TreeModel.java
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
package de.tuebingen.gui.tree.model;

import java.util.ArrayList;
import java.util.HashMap;

public class TreeModel {
    public HashMap<Integer, TreeModelNode> nodes;
    public int root;
    public int nextFreeID;

    public boolean usesTerminals;
    public boolean usesPreTerminals;
    public ArrayList<Integer> terminals;

    public TreeModel() {
        nodes = new HashMap<Integer, TreeModelNode>();
        root = -1;
        nextFreeID = 0;
        usesTerminals = false;
        usesPreTerminals = false;
        terminals = null;
    }

    public void useTerminals(boolean usesTerminals) {
        this.usesTerminals = usesTerminals;
        if (usesTerminals) {
            terminals = new ArrayList<Integer>();
        } else {
            terminals = null;
        }
    }

    public void usePreTerminals(boolean usesPreTerminals) {
        this.usesPreTerminals = usesPreTerminals;
        if (usesPreTerminals) {
            useTerminals(true);
        }
    }

    public void addTerminal(int termID) {
        int i = 0;
        while (i < terminals.size() && termID > terminals.get(i)) {
            i++;
        }
        terminals.add(i, termID);
    }

    public void addNode(TreeModelNode node) {
        nodes.put(node.id, node);
        nextFreeID++;
    }

    public String toString() {
        return nodeToString(root, 0);
    }

    private String nodeToString(int id, int offset) {
        TreeModelNode node = nodes.get(id);
        String root = "";
        for (int j = 0; j < offset; j++) {
            root += "  ";
        }
        root += node.content + "\n";
        for (int i : node.children) {
            root += nodeToString(i, offset + 1);
        }
        return root;
    }
}
