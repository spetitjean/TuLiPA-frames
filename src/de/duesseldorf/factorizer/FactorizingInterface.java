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
    public Set<EqClassBot> bottomEqClasses = new HashSet<>();

    //All characteristics of the node are equal AND the daughters AND the left sisters
    public Set<EqClassTop> topEqClasses = new HashSet<>();

    private NameFactory nf = new NameFactory();



    public Set<EqClassBot> factorize(Set<RRGTree> anchoredTrees) {
        for(RRGTree tree : anchoredTrees) {
            EqClassBot finClass = checkDaughters((RRGNode)tree.getRoot(), tree);
            finClass.setRoot(true);
            EqClassTop topClass = finClass.checkTopClasses(new ArrayList<>(), ((RRGNode) tree.getRoot()).getGornaddress(), tree);
            topClass.setId(nf.getUniqueName());
            topEqClasses.add(topClass);
        }
        //System.out.println("\n FACTORIZED TREES:" + this);
        //System.out.println(topEqClasses);
        return bottomEqClasses;
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
        ArrayList<EqClassTop> topClasses = new ArrayList<EqClassTop>();
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
            topClasses.add(topClass);
        }
        //Add only new topClasses to general list
        topClasses.stream().filter(tc -> !(topEqClasses.contains(tc))).forEach(topClass -> topEqClasses.add(topClass));

        for (EqClassBot botClass : bottomEqClasses) {
            if(botClass.belongs(root, daughtersEq)) {
                botClass.add(root.getGornaddress(), tree);
                topClasses.stream().forEach(topClass -> topClass.addMother(botClass, false));
                topClasses.get(topClasses.size()-1).addMother(botClass, true);
                return botClass;
            }
        }
        rootClass = new EqClassBot(daughtersEq, root.getCategory(), root.getType(), nf.getUniqueName());
        rootClass.add(root.getGornaddress(), tree);
        bottomEqClasses.add(rootClass);
        EqClassBot finalRootClass = rootClass;
        topClasses.stream().forEach(topClass -> topClass.addMother(finalRootClass, false));
        topClasses.get(topClasses.size()-1).addMother(finalRootClass, true);
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
    private Set<EqClassBot> getClassesByNumOfDaughters(int numDaughters) {
        Set<EqClassBot> possClasses = new HashSet<>();
        for (EqClassBot eqClassBot : bottomEqClasses) {
            if(numDaughters == eqClassBot.numDaughters){possClasses.add(eqClassBot);}
        }
        return possClasses;
    }

    public ArrayList<EqClassBot> getLexClasses(String lex){
        ArrayList<EqClassBot> lexClasses = new ArrayList<>();

        for(EqClassBot leafClass : getClassesByNumOfDaughters(0)){
            if(leafClass.cat.equals(lex)){lexClasses.add(leafClass);}
        }
        return lexClasses;
    }
}
