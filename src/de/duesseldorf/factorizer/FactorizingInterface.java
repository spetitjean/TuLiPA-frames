package de.duesseldorf.factorizer;
/*
 *  File FactorizingInterface.java
 *
 *  Authors:
 *     Julia Block <julia.block@hhu.de>
 *
 *  Copyright:
 *     Julia Block, 2022
 *
 * Last modified:
 *     2022
 *
 *  This file is part of the TuLiPA-frames system
 *     https://github.com/spetitjean/TuLiPA-frames
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


import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGTree;
import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.tree.Node;

import java.util.*;

public class FactorizingInterface {

    //All characteristics of the node are equal AND the daughters
    public List<EqClassBot> bottomEqClasses = new ArrayList<>();

    //All characteristics of the node are equal AND the daughters AND the left sisters
    public List<EqClassBot> topEqClasses = new ArrayList<>();

    private NameFactory nf = new NameFactory();

    public void factorize(Set<RRGTree> anchoredTrees) {
        for(RRGTree tree : anchoredTrees) {
            EqClassBot finClass = checkDaughters((RRGNode)tree.getRoot(), tree);
            finClass.setRoot(true);
            EqClassTop topClass = finClass.checkTopClasses(new ArrayList<>(), ((RRGNode) tree.getRoot()).getGornaddress(), tree);
            topClass.setId(nf.getUniqueName());
            topEqClasses.add(topClass);
        }
        //System.out.println("\n FACTORIZED TREES:" + this);
        System.out.println(topEqClasses);
    }
    @Override
    public String toString() {
        String classes = "";
        int i = 0;
        for (EqClassBot eqClassBot : bottomEqClasses) {
            classes += "\n Tree "+ i + eqClassBot.toString();
            i++;
        }
        return classes;
    }

    /**
     * Recursively go through daughters, start with leaves and create EQ classes or sort Nodes into existing EQ classes
     * @param root root node of the anchored tree
     * @param tree anchored tree
     * @return root EQ class of factorization
     */
    private EqClassBot checkDaughters(RRGNode root, RRGTree tree) {
        ArrayList<EqClassBot> daughtersEq = new ArrayList<EqClassBot>();
        EqClassBot rootClass = null;
        for (Node child: root.getChildren()) {
            EqClassBot childClass;
            if(child.getChildren().size() > 0) {
                childClass = checkDaughters((RRGNode) child, tree);
            }
            else {
                childClass = checkLeafClasses((RRGNode)child, tree);
            }

            // Create top eq class with left sisters
            EqClassTop topClass = childClass.checkTopClasses(new ArrayList(daughtersEq), ((RRGNode) child).getGornaddress(), tree);
            topClass.setId(nf.getUniqueName());
            daughtersEq.add(childClass);
            topEqClasses.add(topClass);
        }

        for (EqClassBot eqClassBot : bottomEqClasses) {
            if(eqClassBot.belongs(root, daughtersEq)) {
                eqClassBot.add(root.getGornaddress(), tree);
                return eqClassBot;
            }
        }
        rootClass = new EqClassBot(daughtersEq, root.getCategory(), root.getType(), nf.getUniqueName());
        bottomEqClasses.add(rootClass);
        return rootClass;
    }


    /**
     * @param leaf current found leaf
     * @param tree elementary tree of leaf
     * @return EqClass of leaf, new if it doesn't belong in any existing class
     */
    private EqClassBot checkLeafClasses(RRGNode leaf, RRGTree tree) {
        for (EqClassBot eqClassBot : getClassesByNumOfDaughters(0)) {
            if (eqClassBot.belongs(leaf, new ArrayList<>())) {
                eqClassBot.add(leaf.getGornaddress(), tree);
                return eqClassBot;
            }
        } EqClassBot newClass = new EqClassBot(new ArrayList<>(), leaf.getCategory(), leaf.getType(), nf.getUniqueName());
        newClass.add(leaf.getGornaddress() , tree);
        bottomEqClasses.add(newClass);
        return newClass;
    }

    /**
     *
     * @param numDaughters
     * @return List of Eq classes with wanted number of daughters
     */
    private ArrayList<EqClassBot> getClassesByNumOfDaughters(int numDaughters) {
        ArrayList<EqClassBot> possClasses = new ArrayList<EqClassBot>();
        for (EqClassBot eqClassBot : bottomEqClasses) {
            if(numDaughters == eqClassBot.numDaughters){possClasses.add(eqClassBot);}
        }
        return possClasses;
    }
}
