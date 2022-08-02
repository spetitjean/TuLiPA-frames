package de.duesseldorf.factorizer;
/*
 *  File EqClass.java
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


import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGTree;

import java.util.ArrayList;
import java.util.Set;

public class EqClass {


    public ArrayList<EqClass> daughterEQClasses;

    public Set<RRGTree> factorizedTrees;

    public int numDaughters;

    public String cat;

    public RRGNodeType type;

    public String id;

    public EqClass(ArrayList<EqClass> daughters, String cat, RRGNodeType type, String id){
        this.id = id;
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
            if(cat.equals(node.getCategory())
                    && daughterEQClasses.isEmpty()
                    && checkType(node.getType())) {return true;}

        } else {
            if(cat.equals(node.getCategory())
                    && daughters.equals(daughterEQClasses)
                    && checkType(node.getType())) {return true;}
        }
        return false;
    }

    private boolean checkType(RRGNodeType type) {
        if(type == this.type) {return true;}
        return false;
    }


    @Override
    public String toString() {
        String out = "\n {Cat = "+ cat + " " + this.id + ", daughters = ";
        if(numDaughters == 0) {
            out += "No Daughters \n";
        }
        int i = 1;
        for(EqClass daughter : daughterEQClasses) {
            out += "\n" + i + ". Daughter = " + daughter.cat + " " + daughter.id;
            i++;
        }
        out += "}";
        return out;
    }
}
