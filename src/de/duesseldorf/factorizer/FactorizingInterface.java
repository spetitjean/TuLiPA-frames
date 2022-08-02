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

    public List<EqClass> eqClasses = new ArrayList<>();

    private NameFactory nf = new NameFactory();

    public void factorize(Set<RRGTree> anchoredTrees) {
        for(RRGTree tree : anchoredTrees) {
            EqClass finClass = checkDaughters((RRGNode)tree.getRoot());
        }
        System.out.println("\n FACTORIZED TREES:" + this);
    }
    @Override
    public String toString() {
        String classes = "";
        int i = 0;
        for (EqClass eqClass : eqClasses) {
            classes += "\n Tree "+ i + eqClass.toString();
            i++;
        }
        return classes;
    }

    private EqClass checkDaughters(RRGNode root) {
        ArrayList<EqClass> daughtersEq = new ArrayList<EqClass>();
        for (Node child: root.getChildren()) {
            if(child.getChildren().size() > 0) {
                daughtersEq.add(checkDaughters((RRGNode) child));
            }
            else {daughtersEq.add(checkLeaveClasses((RRGNode)child));}
        }
        for (EqClass eqClass: eqClasses) {
            if(eqClass.belongs(root, daughtersEq)) {
                eqClass.add(root.getGornaddress(), new RRGTree(root, ""));
                return eqClass;
            }
        }
        EqClass newClass = new EqClass(daughtersEq, root.getCategory(), root.getType(), nf.getUniqueName());
        eqClasses.add(newClass);
        return newClass;
    }

    private EqClass checkLeaveClasses(RRGNode leave) {
        for (EqClass eqClass: getClassesByNumOfDaughters(0)) {
            if (eqClass.belongs(leave, new ArrayList<>())) {
                eqClass.add(leave.getGornaddress(), new RRGTree(leave, ""));
                return eqClass;
            }
        } EqClass newClass = new EqClass(new ArrayList<>(), leave.getCategory(), leave.getType(), nf.getUniqueName());
        newClass.add(leave.getGornaddress() , new RRGTree(leave,""));
        eqClasses.add(newClass);
        return newClass;
    }

    private ArrayList<EqClass> getClassesByNumOfDaughters(int numDaughters) {
        ArrayList<EqClass> possClasses = new ArrayList<EqClass>();
        for (EqClass eqClass: eqClasses) {
            if(numDaughters == eqClass.numDaughters){possClasses.add(eqClass);}
        }
        return possClasses;
    }
}
