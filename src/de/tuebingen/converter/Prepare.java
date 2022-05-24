/*
 *  File Prepare.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 11:11:06 CEST 2007
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
package de.tuebingen.converter;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import de.tuebingen.rcg.PredComplexLabel;
import de.tuebingen.rcg.PredLabel;

/**
 * Class which prepares the constraints for the distribution
 *
 * @author parmenti
 */
public class Prepare {

    private String currentTree;
    private Hashtable<String, Integer> node2int;
    private Hashtable<String, Integer> tree2int;
    private Hashtable<Integer, String> int2node;
    private Hashtable<Integer, String> int2tree;
    private int[][] constraints;
    private LinkedList<Object> solutions;
    private LinkedList<Hashtable<String, PredLabel>> decodedSols;

    public Prepare(String t, LinkedList<String> trees, LinkedList<String> nodes) {
        currentTree = t;
        tree2int = new Hashtable<String, Integer>();
        int2tree = new Hashtable<Integer, String>();
        for (int i = 0; i < trees.size(); i++) {
            tree2int.put(trees.get(i), i);
            int2tree.put(new Integer(i), trees.get(i));
        }
        node2int = new Hashtable<String, Integer>();
        int2node = new Hashtable<Integer, String>();
        for (int i = 0; i < nodes.size(); i++) {
            node2int.put(nodes.get(i), i);
            int2node.put(new Integer(i), nodes.get(i));
        }
        constraints = new int[trees.size()][nodes.size()];
        for (int i = 0; i < trees.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                constraints[i][j] = 0;
            }
        }
    }

    public void computeConstraints(Hashtable<Object, LinkedList<Object>> psi) {
        // we update the table of constraints according to psi
        Set<Object> keys = psi.keySet();
        Iterator<Object> i = keys.iterator();
        while (i.hasNext()) {
            String k = (String) i.next();
            //System.out.println("node "+k+" = "+node2int.get(k));
            int node = node2int.get(k);
            LinkedList<Object> trees = psi.get(k);
            for (int j = 0; j < trees.size(); j++) {
                int tree = tree2int.get(trees.get(j));
                constraints[tree][node] = 1;
                // this means the tree "tree" can attach to the node "node"
            }
        }
    }

    public void solveConstraints() {
        //solutions = DistributeLPA.solve(constraints);
    }

    public void decodeSolutions() {
        // decode all the lpa distributions as a list of hash of (nodeid, PredComplexLabel)
        decodedSols = new LinkedList<Hashtable<String, PredLabel>>();
        for (int i = 0; i < solutions.size(); i++) {
            int[][] aSol = (int[][]) solutions.get(i);
            decodedSols.add(decodeASol(aSol));
        }
    }

    public Hashtable<String, PredLabel> decodeASol(int[][] s) {
        // One solution is a hash (nodeid, PredComplexLabel)
        Hashtable<String, PredLabel> res = new Hashtable<String, PredLabel>();
        // for each nodeid (ie column!)
        for (int j = 0; j < s[0].length; j++) {
            // we create a new LPA and a PredComplexLabel
            LinkedList<Object> lpa = new LinkedList<Object>();
            PredComplexLabel lab = new PredComplexLabel(PredComplexLabel.ADJ, currentTree);
            lab.setNodeid(int2node.get(new Integer(j)));
            // for each treeid (ie row!)
            for (int i = 0; i < s.length; i++) {
                if (s[i][j] == 1) {
                    lpa.add(int2tree.get(new Integer(i)));
                }
            }
            lab.setLpa(lpa);
            res.put(lab.getNodeid(), lab);
        }
        return res;
    }

    public String getCurrentTree() {
        return currentTree;
    }

    public void setCurrentTree(String currentTree) {
        this.currentTree = currentTree;
    }

    public Hashtable<Integer, String> getInt2node() {
        return int2node;
    }

    public void setInt2node(Hashtable<Integer, String> int2node) {
        this.int2node = int2node;
    }

    public Hashtable<Integer, String> getInt2tree() {
        return int2tree;
    }

    public void setInt2tree(Hashtable<Integer, String> int2tree) {
        this.int2tree = int2tree;
    }

    public Hashtable<String, Integer> getNode2int() {
        return node2int;
    }

    public void setNode2int(Hashtable<String, Integer> node2int) {
        this.node2int = node2int;
    }

    public Hashtable<String, Integer> getTree2int() {
        return tree2int;
    }

    public void setTree2int(Hashtable<String, Integer> tree2int) {
        this.tree2int = tree2int;
    }

    public int[][] getConstraints() {
        return constraints;
    }

    public void setConstraints(int[][] constraints) {
        this.constraints = constraints;
    }

    public LinkedList<Object> getSolutions() {
        return solutions;
    }

    public void setSolutions(LinkedList<Object> solutions) {
        this.solutions = solutions;
    }

    public LinkedList<Hashtable<String, PredLabel>> getDecodedSols() {
        return decodedSols;
    }

    public void setDecodedSols(LinkedList<Hashtable<String, PredLabel>> decodedSols) {
        this.decodedSols = decodedSols;
    }

    public String toString() {
        String res = "Constraints matrix (trees X nodes):\n   ";
        for (int j = 0; j < constraints[0].length; j++) {
            res += j + " ";
        }
        res += "\n";
        for (int i = 0; i < constraints.length; i++) {
            res += i + " [";
            for (int j = 0; j < constraints[0].length; j++) {
                res += constraints[i][j] + " ";
            }
            res += "]\n";
        }
        if (decodedSols != null) {
            res += "Decoded solutions:\n  ";
            for (int i = 0; i < decodedSols.size(); i++) {
                res += "Solution n°" + (i + 1) + "\n   ";
                Hashtable<String, PredLabel> sol = decodedSols.get(i);
                Set<String> keys = sol.keySet();
                Iterator<String> j = keys.iterator();
                while (j.hasNext()) {
                    String k = (String) j.next();
                    res += "pred: " + sol.get(k).toString();
                }
            }
        }
        return res;
    }
}
