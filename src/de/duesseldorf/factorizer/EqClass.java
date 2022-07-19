package de.duesseldorf.factorizer;
/*
 *  File FactorizingInterface.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     David Arps <david.arps@hhu.de>
 *     Simon Petitjean <petitjean@phil.hhu.de>
 *     Julia Block <julia.block@hhu.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *     David Arps, 2017
 *     Simon Petitjean, 2017
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

import java.util.ArrayList;
import java.util.Set;

public class EqClass {


    public ArrayList<EqClass> daughterEQClasses;

    public Set<RRGTree> factorizedTrees;

    public int numDaughters;

    public String cat;

    public RRGNode.RRGNodeType type;

    public EqClass(ArrayList<EqClass> daughters, String cat, RRGNode.RRGNodeType type){
        daughterEQClasses = daughters;
        this.cat = cat;
        if(daughters.isEmpty()) {
            this.numDaughters = 0;
        } else {
            this.numDaughters = daughters.size();
        }
        this.type = type;
    }


    public void add(RRGTree rootTree) {
        factorizedTrees.add(rootTree);
    }

    public boolean belongs(RRGNode node, ArrayList<EqClass> daughters) {
        if (daughters.isEmpty()) {
            if(cat.equals(node.getCategory()) && daughterEQClasses.isEmpty()) {return true;}

        } else {
            if(cat.equals(node.getCategory()) && daughters.equals(daughterEQClasses)) {return true;}
        }
        return false;
    }

    public String print() {
        String out = "\n {Cat = "+ cat + ", daughters = ";
        if(numDaughters == 0) {
            out += "No Daughters \n";
        }
        int i = 0;
        for(EqClass daughter : daughterEQClasses) {
            out += "\n" + i + ". Daughter = " + daughter.cat + "\n";
            i++;
        }
        out += "}";
        return out;
    }
}
